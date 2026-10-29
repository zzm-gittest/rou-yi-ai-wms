package com.ruoyi.wms.service.ai.analysis;

import com.ruoyi.wms.domain.vo.ai.analysis.*;
import com.ruoyi.wms.service.ai.QwenLlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

/**
 * 经营分析摘要生成服务
 * <p>
 * 将结构化数据转为自然语言文本。
 * 调用 LLM 时严禁编造数据，必须基于传入的结构化数据。
 *
 * @author ruoyi
 */
@Service
public class AnalysisNlgService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisNlgService.class);
    private static final DecimalFormat AMOUNT_FMT = new DecimalFormat("#,##0.00");

    private static final String BRIEFING_SYSTEM_PROMPT =
            "你是一个仓库管理系统的AI助手，负责将经营数据生成简洁的每日简报摘要。\n\n" +
            "要求：\n" +
            "1. 按分析维度列出编号列表，格式如下：\n" +
            "   1.销售情况：今日销售额 ¥xxx；\n" +
            "   2.采购情况：今日采购入库 ¥xxx；\n" +
            "   3.库存/财务/待办：（冒号后换行）\n" +
            "   应收总额 ¥xxx；（独占一行）\n" +
            "   待审核采购 X，待审核销售 X，待入库 X，待出库 X。（独占一行）\n" +
            "   第1、2条用中文分号；结尾，第3条内部按上述换行格式\n" +
            "2. 金额前加 \u00A5 符号，数字和金额符号之间不要有空格，金额数字与前后文字之间保留一个空格\n" +
            "3. 严禁使用任何 Markdown 格式（如 ** 加粗、# 标题等），输出纯文本\n" +
            "4. 语言简洁口语化，像对老板汇报\n" +
            "5. 如有异常数据要重点提醒\n" +
            "6. 严禁自行编造任何数据，只能使用我提供的数据\n" +
            "7. 控制在 200 字以内\n" +
            "8. 不要使用表格格式";

    private static final String ANSWER_SYSTEM_PROMPT =
            "你是一个仓库管理系统的AI助手，负责根据查询结果回答用户的经营问题。\n\n" +
            "要求：\n" +
            "1. 先直接回答问题，再给出分析\n" +
            "2. 按分析维度分点列出，格式：1.xxx； 2.xxx； 3.xxx。每条之间用中文分号；最后一条用句号。\n" +
            "3. 金额前加 \u00A5 符号，数字和金额符号之间不要有空格，金额数字与前后文字之间保留一个空格\n" +
            "4. 严禁使用任何 Markdown 格式（如 ** 加粗、# 标题等），输出纯文本\n" +
            "5. 如有异常或建议，单独另起一行，用 \u201C\n\uD83D\uDCA1 \u201D 开头给出\n" +
            "6. 严禁编造数据，只能使用我提供的数据\n" +
            "7. 控制在 150 字以内\n" +
            "8. 对于销售/采购概况数据，保持每个指标独占一行的格式，严禁合并到同一行\n" +
            "9. 对于排名类数据，保持列表格式，每个排名项独占一行展示";

    private static final String ANSWER_ANALYSIS_PROMPT =
            "你是一个仓库管理系统的AI助手。根据查询结果只输出简短分析和建议，不要重复展示数据本身。\n\n" +
            "要求：\n" +
            "1. 按分析维度分点列出，格式：1.xxx；2.xxx；3.xxx。\n" +
            "2. 如有建议，用 \u201C\n\uD83D\uDCA1 \u201D 开头另起一行\n" +
            "3. 严禁编造数据，严禁使用 Markdown 格式\n" +
            "4. 控制在 80 字以内";

    private final QwenLlmService llmService;

    public AnalysisNlgService(QwenLlmService llmService) {
        this.llmService = llmService;
    }

    /**
     * 生成简报摘要
     */
    public String generateBriefingSummary(BriefingDataVO data) {
        String structuredData = buildBriefingData(data);
        String response = llmService.chatCompletion(BRIEFING_SYSTEM_PROMPT, structuredData);
        if (response != null && !response.trim().isEmpty()) {
            return response.trim();
        }
        // LLM 失败时降级为模板生成
        return buildFallbackBriefingBody(data);
    }

    /**
     * 本地模板简报，不调用第三方LLM
     */
    public String generateBriefingSummaryWithoutLlm(BriefingDataVO data) {
        return buildFallbackBriefingBody(data);
    }

    /**
     * 根据意图和数据生成自然语言回复
     */
    public String generateAnswer(String intent, Object data) {
        String structuredData = buildAnswerData(intent, data);
        String response = llmService.chatCompletion(ANSWER_ANALYSIS_PROMPT,
                "\u7528\u6237\u95EE\u9898\u610F\u56FE: " + intent + "\n\n\u67E5\u8BE2\u7ED3\u679C:\n" + structuredData);
        if (response != null && !response.trim().isEmpty()) {
            return structuredData + "\n" + response.trim();
        }
        // 降级
        return structuredData;
    }

    /**
     * 本地模板回复，不调用第三方LLM
     */
    public String generateAnswerWithoutLlm(String intent, Object data) {
        return buildAnswerData(intent, data);
    }

    // ========== 数据序列化 ==========

    private String buildBriefingData(BriefingDataVO data) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u4ECA\u65E5\u7ECF\u8425\u6570\u636E\u6982\u89C8\uFF1A\n\n");

        if (data.getSalesSummary() != null) {
            SalesSummaryVO s = data.getSalesSummary();
            sb.append("\u3010\u9500\u552E\u3011")
                    .append(s.getPeriodLabel()).append("\u9500\u552E\u989D \u00A5").append(fmt(s.getSalesAmount()))
                    .append("\uFF0C\u9000\u8D27 \u00A5").append(fmt(s.getReturnAmount()))
                    .append("\uFF0C\u51C0\u9500\u552E \u00A5").append(fmt(s.getNetSalesAmount()))
                    .append("\uFF0C\u8BA2\u5355 ").append(s.getOrderCount()).append(" \u7B14")
                    .append("\uFF0C\u6BDB\u5229 \u00A5").append(fmt(s.getGrossProfit()))
                    .append("\n");
        }

        if (data.getPurchaseSummary() != null) {
            PurchaseSummaryVO p = data.getPurchaseSummary();
            sb.append("\u3010\u91C7\u8D2D\u3011")
                    .append(p.getPeriodLabel()).append("\u91C7\u8D2D\u5165\u5E93 \u00A5").append(fmt(p.getPurchaseAmount()))
                    .append("\uFF0C\u9000\u8D27 \u00A5").append(fmt(p.getReturnAmount()))
                    .append("\uFF0C\u5165\u5E93\u5355 ").append(p.getInCount()).append(" \u7B14")
                    .append("\n");
        }

        if (data.getStockWarnings() != null && !data.getStockWarnings().isEmpty()) {
            sb.append("\u3010\u5E93\u5B58\u9884\u8B66\u3011").append(data.getStockWarnings().size())
                    .append(" \u6B3E\u5546\u54C1\u5E93\u5B58\u4E0D\u8DB3 7 \u5929\n");
        } else {
            sb.append("\u3010\u5E93\u5B58\u3011\u65E0\u9884\u8B66\n");
        }

        if (data.getFinanceSummary() != null) {
            FinanceSummaryVO f = data.getFinanceSummary();
            sb.append("\u3010\u8D22\u52A1\u3011\u5E94\u6536\u603B\u989D \u00A5").append(fmt(f.getReceivableTotal()));
            if (f.getOverdueReceivableCount() > 0) {
                sb.append("\uFF0C\u903E\u671F ").append(f.getOverdueReceivableCount())
                        .append(" \u7B14 \u00A5").append(fmt(f.getOverdueReceivableAmount()));
            }
            sb.append("\n");
        }

        if (data.getPendingTasks() != null) {
            PendingTasksVO t = data.getPendingTasks();
            sb.append("\u3010\u5F85\u529E\u3011")
                    .append("\u5F85\u5BA1\u6838\u91C7\u8D2D ").append(t.getPendingPurchaseOrderCount())
                    .append("\uFF0C\u5F85\u5BA1\u6838\u9500\u552E ").append(t.getPendingSalesOrderCount())
                    .append("\uFF0C\u5F85\u5165\u5E93 ").append(t.getPendingPurchaseInCount())
                    .append("\uFF0C\u5F85\u51FA\u5E93 ").append(t.getPendingSalesOutCount())
                    .append("\n");
        }

        if (data.getTopSellingGoods() != null && !data.getTopSellingGoods().isEmpty()) {
            sb.append("\u3010\u70ED\u9500 TOP3\u3011\n");
            int count = Math.min(3, data.getTopSellingGoods().size());
            for (int i = 0; i < count; i++) {
                GoodsRankItemVO g = data.getTopSellingGoods().get(i);
                sb.append("  ").append(i + 1).append(". ").append(g.getGoodsName())
                        .append(" \u00A5").append(fmt(g.getSalesAmount())).append("\n");
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String buildAnswerData(String intent, Object data) {
        if (data instanceof SalesSummaryVO) {
            SalesSummaryVO s = (SalesSummaryVO) data;
            return s.getPeriodLabel() + "\u9500\u552E\u989D \u00A5" + fmt(s.getSalesAmount()) + "\n"
                    + "\u9000\u8D27 \u00A5" + fmt(s.getReturnAmount()) + "\n"
                    + "\u51C0\u9500\u552E \u00A5" + fmt(s.getNetSalesAmount()) + "\n"
                    + "\u5BA2\u5355\u4EF7 \u00A5" + fmt(s.getAvgOrderAmount()) + "\n"
                    + "\u6BDB\u5229 \u00A5" + fmt(s.getGrossProfit()) + "\n"
                    + "\u8BA2\u5355\u7B14\u6570 " + s.getOrderCount() + " \u7B14";
        }
        if (data instanceof PurchaseSummaryVO) {
            PurchaseSummaryVO p = (PurchaseSummaryVO) data;
            return p.getPeriodLabel() + "\u91C7\u8D2D\u5165\u5E93 \u00A5" + fmt(p.getPurchaseAmount())
                    + "\uFF0C\u9000\u8D27 \u00A5" + fmt(p.getReturnAmount())
                    + "\uFF0C\u51C0\u91C7\u8D2D \u00A5" + fmt(p.getNetPurchaseAmount())
                    + "\uFF0C\u5165\u5E93\u5355 " + p.getInCount() + " \u7B14";
        }
        if (data instanceof FinanceSummaryVO) {
            FinanceSummaryVO f = (FinanceSummaryVO) data;
            return "\u5E94\u6536\u603B\u989D \u00A5" + fmt(f.getReceivableTotal())
                    + "\uFF0C\u903E\u671F " + f.getOverdueReceivableCount()
                    + " \u7B14 \u00A5" + fmt(f.getOverdueReceivableAmount());
        }
        if (data instanceof PendingTasksVO) {
            PendingTasksVO t = (PendingTasksVO) data;
            return "\u5F85\u5BA1\u6838\u91C7\u8D2D " + t.getPendingPurchaseOrderCount()
                    + "\uFF0C\u5F85\u5BA1\u6838\u9500\u552E " + t.getPendingSalesOrderCount()
                    + "\uFF0C\u5F85\u5165\u5E93 " + t.getPendingPurchaseInCount()
                    + "\uFF0C\u5F85\u51FA\u5E93 " + t.getPendingSalesOutCount();
        }
        if (data instanceof List) {
            List<?> list = (List<?>) data;
            if (list.isEmpty()) {
                return "\u6682\u65E0\u6570\u636E";
            }
            Object first = list.get(0);
            if (first instanceof GoodsRankItemVO) {
                return buildGoodsRankText((List<GoodsRankItemVO>) list);
            }
            if (first instanceof CustomerRankItemVO) {
                return buildCustomerRankText((List<CustomerRankItemVO>) list);
            }
            if (first instanceof StockWarningVO) {
                return buildStockWarningText((List<StockWarningVO>) list);
            }
        }
        return data != null ? data.toString() : "\u6682\u65E0\u6570\u636E";
    }

    private String buildGoodsRankText(List<GoodsRankItemVO> list) {
        StringBuilder sb = new StringBuilder("\u70ED\u9500\u5546\u54C1\u6392\u884C\uFF1A\n");
        for (GoodsRankItemVO item : list) {
            sb.append(getRankEmoji(item.getRank())).append(" ")
                    .append(item.getGoodsName());
            if (item.getSpec() != null && !item.getSpec().isEmpty()) {
                sb.append("(").append(item.getSpec()).append(")");
            }
            sb.append(" \u00A5").append(fmt(item.getSalesAmount()))
                    .append("\n");
        }
        return sb.toString();
    }

    private String buildCustomerRankText(List<CustomerRankItemVO> list) {
        StringBuilder sb = new StringBuilder("\u5BA2\u6237\u9500\u552E\u6392\u540D\uFF1A\n");
        for (CustomerRankItemVO item : list) {
            sb.append(getRankEmoji(item.getRank())).append(" ")
                    .append(item.getCustomerName())
                    .append(" \u00A5").append(fmt(item.getSalesAmount()))
                    .append(" (").append(item.getOrderCount()).append("\u5355)");
            if (item.getRank() <= 3) {
                sb.append("\n");
            } else {
                sb.append("\uFF1B");
            }
        }
        return sb.toString();
    }

    private String buildStockWarningText(List<StockWarningVO> list) {
        StringBuilder sb = new StringBuilder("\u5E93\u5B58\u9884\u8B66\u5546\u54C1\u5171 ")
                .append(list.size()).append(" \u6B3E\uFF1A\n");
        for (int i = 0; i < list.size(); i++) {
            StockWarningVO w = list.get(i);
            sb.append(i + 1).append(". ").append(w.getGoodsName());
            if (w.getSpec() != null && !w.getSpec().isEmpty()) {
                sb.append("(").append(w.getSpec()).append(")");
            }
            sb.append(" \u5269\u4F59 ").append(w.getCurrentStock()).append(w.getUnitName())
                    .append("\uFF0C\u9884\u8BA1\u53EF\u552E ").append(w.getEstimatedDays()).append(" \u5929")
                    .append("\n");
        }
        return sb.toString();
    }

    /**
     * LLM 不可用时的降级简报
     */
        private String buildFallbackBriefingBody(BriefingDataVO data) {
        StringBuilder sb = new StringBuilder();

        if (data.getSalesSummary() != null) {
            SalesSummaryVO s = data.getSalesSummary();
            sb.append("销售：净销售额 ¥").append(fmt(s.getNetSalesAmount()))
                .append("，共 ").append(s.getOrderCount()).append(" 笔订单，毛利 ¥")
                .append(fmt(s.getGrossProfit())).append("。\n");
        }
        if (data.getPurchaseSummary() != null) {
            PurchaseSummaryVO p = data.getPurchaseSummary();
            sb.append("采购：入库 ¥").append(fmt(p.getPurchaseAmount()))
                .append("，共 ").append(p.getInCount()).append(" 单。\n");
        }
        if (data.getPendingTasks() != null && data.getPendingTasks().getTotalCount() > 0) {
            sb.append("待办：当前共有 ").append(data.getPendingTasks().getTotalCount())
                .append(" 项待处理。\n");
        }
        if (data.getStockWarnings() != null && !data.getStockWarnings().isEmpty()) {
            sb.append("库存预警：").append(data.getStockWarnings().size())
                .append(" 款商品库存不足。\n");
        }
        return sb.toString().trim();
    }

    private String fmt(BigDecimal val) {
        if (val == null) {
            return "0.00";
        }
        return AMOUNT_FMT.format(val);
    }

    private String getRankEmoji(int rank) {
        switch (rank) {
            case 1: return "\uD83E\uDD47";
            case 2: return "\uD83E\uDD48";
            case 3: return "\uD83E\uDD49";
            default: return rank + ".";
        }
    }
}
