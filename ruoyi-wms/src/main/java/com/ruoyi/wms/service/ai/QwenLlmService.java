package com.ruoyi.wms.service.ai;

import com.ruoyi.wms.ai.spi.llm.LlmChatRequest;
import com.ruoyi.wms.ai.spi.llm.LlmChatResponse;
import com.ruoyi.wms.ai.spi.llm.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通义千问大语言模型服务（保持原有 public API；底层委托给 {@link LlmClient} SPI）。
 *
 * <p>历史版本直接读取 yml 中的 qwen.api-key 调 DashScope；
 * 重构后变成 SPI 的薄包装：</p>
 * <ul>
 *   <li>SaaS 模式（默认）：{@code LlmClient} = {@code SaasYmlLlmClient}，行为完全等价于历史版本</li>
 *   <li>Gateway 模式（私有部署）：{@code LlmClient} = {@code AigwLlmClient}，
 *       通过 AIGW 网关代理调用模型，本地不再持有任何 API Key</li>
 * </ul>
 *
 * <p>历史的"语音解析专属 SYSTEM_PROMPT"与"chatCompletion 通用接口"全部保留，
 * 上层调用方（VoiceParseService / MobileGoodsParseHelper / AnalysisNluService 等）无需改动。</p>
 *
 * @author ruoyi
 */
@Service
public class QwenLlmService {

    private static final Logger log = LoggerFactory.getLogger(QwenLlmService.class);

    /**
     * 系统提示词 - 用于语音解析
     */
    private static final String SYSTEM_PROMPT = "你是一个仓库管理系统(WMS)的智能助手。你的任务是解析用户的语音输入，提取出结构化的业务数据。\n\n" +
            "支持的操作类型：\n" +
            "1. purchase（采购）- 采购订单、采购入库\n" +
            "2. sales（销售）- 销售订单、销售出库\n" +
            "3. stock（库存）- 库存盘点、库存查询\n" +
            "4. goods（商品）- 商品录入、商品查询\n\n" +
            "请严格按照JSON格式返回结果，不要添加任何额外的说明文字。";

    @Autowired
    private LlmClient llmClient;

    /**
     * 解析语音文本（保持原签名）
     *
     * @param text 语音识别的文本
     * @return 解析后的JSON字符串
     */
    public String parseVoiceText(String text) {
        try {
            log.info("[{}] 调用语音文本解析, 输入: {}", llmClient.mode(), text);
            LlmChatRequest req = LlmChatRequest.of(SYSTEM_PROMPT, buildPrompt(text))
                .setUsageScene("voice_parse");
            LlmChatResponse resp = llmClient.chat(req);
            String content = stripCodeFenceAndExtractJson(resp == null ? null : resp.getContent());
            if (content == null || content.isBlank()) {
                log.warn("[{}] LLM 返回空内容, 使用 fallback", llmClient.mode());
                return buildFallbackResponse(text);
            }
            return content;
        } catch (Exception e) {
            log.error("[{}] 调用 LLM 失败", llmClient.mode(), e);
            return buildFallbackResponse(text);
        }
    }

    /**
     * 通用 chat completion 调用（保持原签名）
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return LLM 返回的文本内容，失败返回 null
     */
    public String chatCompletion(String systemPrompt, String userMessage) {
        try {
            LlmChatRequest req = LlmChatRequest.of(systemPrompt, userMessage)
                .setMaxTokens(512)
                .setTemperature(0.1)
                .setUsageScene("chat_completion");
            LlmChatResponse resp = llmClient.chat(req);
            return stripCodeFenceAndExtractJson(resp == null ? null : resp.getContent());
        } catch (Exception e) {
            log.error("[{}] chatCompletion 异常", llmClient.mode(), e);
            return null;
        }
    }

    /* ===================== 历史兼容辅助方法（保持原逻辑） ===================== */

    private String buildPrompt(String text) {
        return "请解析以下语音输入，提取出结构化的业务数据：\n\n" +
                "用户输入：" + text + "\n\n" +
                "请以JSON格式返回，包含以下字段：\n" +
                "{\n" +
                "  \"operationType\": \"操作类型(purchase/sales/stock/goods)\",\n" +
                "  \"goods\": [\n" +
                "    {\n" +
                "      \"name\": \"商品名称\",\n" +
                "      \"barcode\": \"条码(如果有)\",\n" +
                "      \"spec\": \"规格(如果有)\",\n" +
                "      \"quantity\": 数量(数字),\n" +
                "      \"unit\": \"单位\",\n" +
                "      \"price\": 单价(数字,如果有),\n" +
                "      \"amount\": 金额(数字,如果有)\n" +
                "    }\n" +
                "  ],\n" +
                "  \"supplier\": {\"name\": \"供应商名称\"},\n" +
                "  \"customer\": {\"name\": \"客户名称\"},\n" +
                "  \"warehouse\": \"仓库名称\",\n" +
                "  \"remark\": \"备注信息或退货原因\"\n" +
                "}\n\n" +
                "注意：\n" +
                "1. 只返回JSON，不要有其他文字\n" +
                "2. 如果某个字段无法识别，不要包含该字段或设为null\n" +
                "3. 数量、价格等数字不要加引号\n" +
                "4. 商品名称要完整，不要省略\n" +
                "5. 提取客户名称时，注意识别以下格式：\n" +
                "   - \"出货给华联超市\" -> customer: 华联超市, goods: 不包含华联超市\n" +
                "   - \"卖给天迅科技\" -> customer: 天迅科技\n" +
                "   - \"销售给京东\" -> customer: 京东\n" +
                "6. 提取供应商名称时，注意识别以下格式：\n" +
                "   - \"向苹果科技采购\" -> supplier: 苹果科技, goods: 不包含苹果科技\n" +
                "   - \"从华为进货\" -> supplier: 华为\n" +
                "   - \"跟联想买\" -> supplier: 联想\n" +
                "7. 提取退货原因/备注时，注意识别以下格式：\n" +
                "   - \"原因是破损\" -> remark: 破损\n" +
                "   - \"因为质量问题\" -> remark: 质量问题\n" +
                "   - \"退货原因是客户拒收\" -> remark: 客户拒收\n" +
                "   - \"坏的\" -> remark: 商品损坏\n" +
                "   - \"破损\" -> remark: 破损";
    }

    /**
     * 历史行为：从可能包裹的 markdown 代码块中提取 JSON 对象部分
     * （SaasYmlLlmClient 已经做了一遍 stripCodeFence，这里二次兜底提取 JSON 起止）
     */
    private String stripCodeFenceAndExtractJson(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        int startIndex = trimmed.indexOf('{');
        int endIndex = trimmed.lastIndexOf('}');
        if (startIndex >= 0 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1).trim();
        }
        return trimmed;
    }

    /**
     * 历史 fallback 行为：LLM 调用失败时返回 error JSON，让上层显式感知
     */
    private String buildFallbackResponse(String text) {
        log.error("LLM调用失败，测试阶段禁用正则兜底，text: {}", text);
        return "{\"operationType\":\"error\",\"error\":\"LLM调用失败，测试阶段禁用正则兜底\"}";
    }
}
