package com.ruoyi.wms.service.ai.query;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.ai.analysis.*;
import com.ruoyi.wms.domain.vo.integration.IntegrationAnalysisAnswerVO;
import com.ruoyi.wms.service.ai.analysis.AnalysisNlgService;
import com.ruoyi.wms.service.ai.analysis.AnalysisNluService;
import com.ruoyi.wms.service.ai.analysis.BusinessAnalysisDataService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 开放接口 · 经营分析（复用移动端 NLU + 数据 + 本地 NLG，不调用 LLM）
 */
@Service
public class IntegrationBusinessAnalysisFacade {

    private static final int RANK_LIMIT = 10;
    private static final int STOCK_WARNING_LIMIT = 15;

    private final AnalysisNluService nluService;
    private final BusinessAnalysisDataService dataService;
    private final AnalysisNlgService nlgService;

    public IntegrationBusinessAnalysisFacade(AnalysisNluService nluService,
                                             BusinessAnalysisDataService dataService,
                                             AnalysisNlgService nlgService) {
        this.nluService = nluService;
        this.dataService = dataService;
        this.nlgService = nlgService;
    }

    public IntegrationAnalysisAnswerVO answer(String message) {
        IntegrationAnalysisAnswerVO vo = new IntegrationAnalysisAnswerVO();
        if (StringUtils.isBlank(message)) {
            vo.setTitle("经营分析");
            vo.setBody("请输入要查询的内容，例如：今日销售概况、热销商品排行、库存预警。");
            return vo;
        }
        String trimmed = message.trim();
        NluIntentResult intentResult = nluService.parseIntentLocally(trimmed, Collections.emptyList());
        String intent = intentResult.getIntent();
        AnalysisPeriod period = AnalysisPeriod.fromCode(
            StringUtils.isNotBlank(intentResult.getPeriod()) ? intentResult.getPeriod() : defaultPeriod(trimmed));

        vo.setIntent(intent);
        vo.setPeriod(period.getCode());

        if (NluIntentResult.INTENT_UNKNOWN.equals(intent)) {
            vo.setTitle("经营分析");
            vo.setBody("暂未理解该问题。可尝试：今日销售概况、本周销售情况、热销商品排行、库存预警。");
            return vo;
        }

        Object queryData = loadData(intent, period);
        String body = nlgService.generateAnswerWithoutLlm(intent, queryData);
        vo.setTitle(resolveTitle(intent, period, trimmed));
        vo.setBody(body);
        return vo;
    }

    private Object loadData(String intent, AnalysisPeriod period) {
        return switch (intent) {
            case NluIntentResult.INTENT_QUERY_SALES, NluIntentResult.INTENT_QUERY_COMPARISON ->
                dataService.getSalesSummary(period, null);
            case NluIntentResult.INTENT_QUERY_PURCHASE -> dataService.getPurchaseSummary(period, null);
            case NluIntentResult.INTENT_QUERY_STOCK -> {
                List<StockWarningVO> warnings = dataService.getStockWarnings(null);
                if (warnings == null || warnings.isEmpty()) {
                    yield warnings;
                }
                yield warnings.size() <= STOCK_WARNING_LIMIT
                    ? warnings : warnings.subList(0, STOCK_WARNING_LIMIT);
            }
            case NluIntentResult.INTENT_QUERY_FINANCE -> dataService.getFinanceSummary();
            case NluIntentResult.INTENT_QUERY_PENDING -> dataService.getPendingTasks();
            case NluIntentResult.INTENT_QUERY_GOODS_RANK ->
                dataService.getTopSellingGoods(period, RANK_LIMIT);
            case NluIntentResult.INTENT_QUERY_CUSTOMER_RANK ->
                dataService.getCustomerSalesRanking(period, RANK_LIMIT);
            default -> null;
        };
    }

    private static String defaultPeriod(String message) {
        String n = message.replaceAll("\\s+", "");
        if (n.contains("本周") || n.contains("这周")) {
            return "this_week";
        }
        if (n.contains("本月")) {
            return "this_month";
        }
        if (n.contains("昨日") || n.contains("昨天")) {
            return "yesterday";
        }
        return "today";
    }

    private static String resolveTitle(String intent, AnalysisPeriod period, String rawMessage) {
        if (rawMessage.contains("库存预警") || NluIntentResult.INTENT_QUERY_STOCK.equals(intent)) {
            return "库存预警";
        }
        if (rawMessage.contains("热销") || NluIntentResult.INTENT_QUERY_GOODS_RANK.equals(intent)) {
            return "热销商品排行 · " + period.getLabel();
        }
        if (rawMessage.contains("客户") && rawMessage.contains("排名")) {
            return "客户销售排名 · " + period.getLabel();
        }
        if (rawMessage.contains("待办") || NluIntentResult.INTENT_QUERY_PENDING.equals(intent)) {
            return "待办事项";
        }
        if (NluIntentResult.INTENT_QUERY_PURCHASE.equals(intent)) {
            return "采购概况 · " + period.getLabel();
        }
        if (rawMessage.contains("本周销售")) {
            return "本周销售情况";
        }
        if (rawMessage.contains("今日销售")) {
            return "今日销售概况";
        }
        if (NluIntentResult.INTENT_QUERY_SALES.equals(intent)
            || NluIntentResult.INTENT_QUERY_COMPARISON.equals(intent)) {
            return "销售概况 · " + period.getLabel();
        }
        return "经营分析 · " + period.getLabel();
    }
}
