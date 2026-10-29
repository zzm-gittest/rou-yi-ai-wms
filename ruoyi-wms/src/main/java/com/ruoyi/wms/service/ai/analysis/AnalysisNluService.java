package com.ruoyi.wms.service.ai.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.wms.domain.vo.ai.analysis.NluIntentResult;
import com.ruoyi.wms.service.ai.QwenLlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 经营分析意图识别服务
 * <p>
 * 调用 LLM 识别用户经营分析问题的意图和参数。
 * 支持上下文追问（继承上一轮时间范围和实体）。
 *
 * @author ruoyi
 */
@Service
public class AnalysisNluService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisNluService.class);
    private static final Pattern UNSUPPORTED_RELATIVE_PERIOD =
            Pattern.compile("最近[一二两三四五六七八九十0-9]+(天|周|月|年)");

    private static final String NLU_SYSTEM_PROMPT =
            "你是一个仓库管理系统(WMS)的经营分析意图识别引擎。\n\n" +
            "任务：从用户的提问中识别意图和参数。\n\n" +
            "可识别的意图：\n" +
            "- QUERY_SALES: 查询销售相关数据（销售额、出库情况等）\n" +
            "- QUERY_PURCHASE: 查询采购相关数据（采购额、入库情况等）\n" +
            "- QUERY_STOCK: 查询库存相关数据（某商品库存、仓库库存等）\n" +
            "- QUERY_FINANCE: 查询财务相关数据（应收、应付、逾期等）\n" +
            "- QUERY_PENDING: 查询待办事项（待审核、待入库、待出库等）\n" +
            "- QUERY_COMPARISON: 对比查询（同比、环比、与上期对比等）\n" +
            "- QUERY_CUSTOMER: 查询客户相关数据（某客户欠款、购买记录等）\n" +
            "- QUERY_SUPPLIER: 查询供应商相关数据\n" +
            "- QUERY_GOODS_RANK: 查询热销商品排行\n" +
            "- QUERY_CUSTOMER_RANK: 查询客户销售排名\n" +
            "- UNKNOWN: 无法识别\n\n" +
            "可提取的参数：\n" +
            "- period: today/yesterday/this_week/this_month/last_month\n" +
            "- entity: 涉及的实体名称（客户名/商品名/供应商名/仓库名）\n\n" +
            "上下文信息（如果有）会在用户消息后附加，用于理解追问。\n\n" +
            "严格按JSON格式返回，不要添加额外说明：\n" +
            "{\"intent\":\"QUERY_SALES\",\"period\":\"today\",\"entity\":\"\"}\n\n" +
            "如果无法确定时间范围，period 留空字符串。\n" +
            "如果没有涉及具体实体，entity 留空字符串。";

    private final QwenLlmService llmService;
    private final ObjectMapper objectMapper;

    public AnalysisNluService(QwenLlmService llmService, ObjectMapper objectMapper) {
        this.llmService = llmService;
        this.objectMapper = objectMapper;
    }

    /**
     * 解析用户意图
     *
     * @param message 用户输入
     * @param context 上下文（最近几轮的意图和参数）
     * @return 意图识别结果
     */
    public NluIntentResult parseIntent(String message, List<NluIntentResult> context) {
        NluIntentResult localResult = parseIntentLocally(message, context);
        if (!NluIntentResult.INTENT_UNKNOWN.equals(localResult.getIntent())) {
            return localResult;
        }

        NluIntentResult result = initResult(message);

        try {
            String userMessage = buildUserMessage(message, context);
            String llmResponse = llmService.chatCompletion(NLU_SYSTEM_PROMPT, userMessage);

            if (llmResponse == null || llmResponse.trim().isEmpty()) {
                log.warn("NLU LLM \u8FD4\u56DE\u7A7A\uFF0C\u7528\u6237\u8F93\u5165: {}", message);
                return result;
            }

            String json = extractJson(llmResponse);
            JsonNode node = objectMapper.readTree(json);

            if (node.has("intent")) {
                result.setIntent(node.get("intent").asText(NluIntentResult.INTENT_UNKNOWN));
            }
            if (node.has("period")) {
                result.setPeriod(node.get("period").asText(""));
            }
            if (node.has("entity")) {
                result.setEntity(node.get("entity").asText(""));
            }

            inheritContext(result, context);

            log.info("NLU \u8BC6\u522B\u7ED3\u679C: intent={}, period={}, entity={}, input={}",
                    result.getIntent(), result.getPeriod(), result.getEntity(), message);
        } catch (Exception e) {
            log.error("NLU \u89E3\u6790\u5931\u8D25: {}", message, e);
        }
        return result;
    }

    /**
     * 本地规则优先识别，命中则无需调用第三方LLM
     */
    public NluIntentResult parseIntentLocally(String message, List<NluIntentResult> context) {
        NluIntentResult result = initResult(message);
        String normalized = normalize(message);
        if (normalized.isEmpty()) {
            return result;
        }

        if (containsUnsupportedConstraint(normalized)) {
            return result;
        }

        result.setIntent(detectIntent(normalized));
        result.setPeriod(detectPeriod(normalized));

        if (NluIntentResult.INTENT_UNKNOWN.equals(result.getIntent())
                && result.getPeriod() != null && !result.getPeriod().isEmpty()
                && context != null && !context.isEmpty()) {
            NluIntentResult lastContext = context.get(context.size() - 1);
            if (lastContext.getIntent() != null
                    && !lastContext.getIntent().isEmpty()
                    && !NluIntentResult.INTENT_UNKNOWN.equals(lastContext.getIntent())) {
                result.setIntent(lastContext.getIntent());
            }
        }

        inheritContext(result, context);
        if (!NluIntentResult.INTENT_UNKNOWN.equals(result.getIntent())) {
            log.info("本地NLU识别结果: intent={}, period={}, entity={}, input={}",
                    result.getIntent(), result.getPeriod(), result.getEntity(), message);
        }
        return result;
    }

    public boolean canHandleLocally(String message, List<NluIntentResult> context) {
        return !NluIntentResult.INTENT_UNKNOWN.equals(parseIntentLocally(message, context).getIntent());
    }

    private NluIntentResult initResult(String message) {
        NluIntentResult result = new NluIntentResult();
        result.setRawInput(message);
        result.setIntent(NluIntentResult.INTENT_UNKNOWN);
        result.setPeriod("");
        result.setEntity("");
        return result;
    }

    private void inheritContext(NluIntentResult result, List<NluIntentResult> context) {
        if (context == null || context.isEmpty()) {
            return;
        }
        NluIntentResult lastContext = context.get(context.size() - 1);
        if ((result.getPeriod() == null || result.getPeriod().isEmpty())
                && lastContext.getPeriod() != null && !lastContext.getPeriod().isEmpty()) {
            result.setPeriod(lastContext.getPeriod());
        }
        if ((result.getEntity() == null || result.getEntity().isEmpty())
                && lastContext.getEntity() != null && !lastContext.getEntity().isEmpty()) {
            result.setEntity(lastContext.getEntity());
        }
    }

    private String normalize(String message) {
        if (message == null) {
            return "";
        }
        return message.toLowerCase(Locale.ROOT).replaceAll("\\s+", "").trim();
    }

    private boolean containsUnsupportedConstraint(String normalized) {
        if (UNSUPPORTED_RELATIVE_PERIOD.matcher(normalized).find()) {
            return true;
        }
        if (containsAny(normalized, "sku", "sku级", "sku粒度", "粒度", "维度")) {
            return true;
        }
        if (containsAny(normalized, "原因", "为什么", "怎么回事", "归因", "分析原因")) {
            return true;
        }
        if (containsAny(normalized, "华东仓", "华南仓", "华北仓", "仓客户", "客户a", "客户b")) {
            return true;
        }
        return false;
    }

    private String detectIntent(String normalized) {
        if (containsAny(normalized, "同比", "环比", "对比", "比较", "趋势", "波动")) {
            return NluIntentResult.INTENT_QUERY_COMPARISON;
        }
        if (containsAny(normalized, "客户销售排名", "客户销售排行", "客户排名", "客户排行", "客户榜单", "客户top")) {
            return NluIntentResult.INTENT_QUERY_CUSTOMER_RANK;
        }
        if (containsAny(normalized, "热销商品排行", "热销商品排名", "商品销售排行", "商品销售排名", "卖得最好", "热销排行", "卖得最快", "畅销商品", "商品榜单", "商品top")) {
            return NluIntentResult.INTENT_QUERY_GOODS_RANK;
        }
        if (containsAny(normalized, "待办", "待处理", "待审核", "待入库", "待出库", "积压订单", "还没出库", "未出库", "任务", "异常待处理")) {
            return NluIntentResult.INTENT_QUERY_PENDING;
        }
        if (containsAny(normalized, "库存预警", "断货", "缺货", "库存", "库存不足", "低库存", "补货")) {
            return NluIntentResult.INTENT_QUERY_STOCK;
        }
        if (containsAny(normalized, "逾期应收", "应收", "应付", "欠款", "财务", "逾期", "回款", "收款", "账款")) {
            return NluIntentResult.INTENT_QUERY_FINANCE;
        }
        if (containsAny(normalized, "采购", "入库", "进货", "买货", "补货情况")) {
            return NluIntentResult.INTENT_QUERY_PURCHASE;
        }
        if (containsAny(normalized, "销售", "出库", "毛利", "客单价", "销售额", "营收", "营业额", "卖货", "生意", "业绩")) {
            return NluIntentResult.INTENT_QUERY_SALES;
        }
        return NluIntentResult.INTENT_UNKNOWN;
    }

    private String detectPeriod(String normalized) {
        if (containsAny(normalized, "今日", "今天", "当日")) {
            return "today";
        }
        if (containsAny(normalized, "昨日", "昨天")) {
            return "yesterday";
        }
        if (containsAny(normalized, "本周", "这周", "本星期", "这星期", "周内")) {
            return "this_week";
        }
        if (containsAny(normalized, "本月", "这个月", "月内")) {
            return "this_month";
        }
        if (containsAny(normalized, "上月", "上个月")) {
            return "last_month";
        }
        return "";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建发给 LLM 的用户消息（含上下文）
     */
    private String buildUserMessage(String message, List<NluIntentResult> context) {
        StringBuilder sb = new StringBuilder();
        sb.append(message);

        if (context != null && !context.isEmpty()) {
            sb.append("\n\n[\u4E0A\u4E0B\u6587]\n");
            int start = Math.max(0, context.size() - 3);
            for (int i = start; i < context.size(); i++) {
                NluIntentResult c = context.get(i);
                sb.append("\u7B2C").append(i + 1).append("\u8F6E: \u610F\u56FE=")
                        .append(c.getIntent())
                        .append(", \u65F6\u95F4=").append(c.getPeriod())
                        .append(", \u5B9E\u4F53=").append(c.getEntity())
                        .append(", \u8F93\u5165=").append(c.getRawInput())
                        .append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * 从 LLM 输出中提取 JSON
     */
    private String extractJson(String text) {
        if (text == null) {
            return "{}";
        }
        String trimmed = text.trim();
        // 处理 markdown 代码块
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                trimmed = trimmed.substring(start, end).trim();
            }
        } else if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                trimmed = trimmed.substring(start, end).trim();
            }
        }
        // 尝试找到 JSON 对象
        int braceStart = trimmed.indexOf('{');
        int braceEnd = trimmed.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return trimmed.substring(braceStart, braceEnd + 1);
        }
        return "{}";
    }
}
