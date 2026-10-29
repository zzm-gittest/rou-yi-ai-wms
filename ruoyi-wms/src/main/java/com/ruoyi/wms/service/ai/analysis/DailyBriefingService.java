package com.ruoyi.wms.service.ai.analysis;

import com.ruoyi.wms.domain.vo.ai.analysis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 经营简报生成服务
 * <p>
 * 汇总各模块数据，调用 LLM 生成自然语言摘要，
 * 并根据数据特征动态生成引导提问。
 *
 * @author ruoyi
 */
@Service
public class DailyBriefingService {

    private static final Logger log = LoggerFactory.getLogger(DailyBriefingService.class);

    private final BusinessAnalysisDataService dataService;
    private final AnomalyDetectionService anomalyService;
    private final AnalysisNlgService nlgService;

    public DailyBriefingService(BusinessAnalysisDataService dataService,
                                AnomalyDetectionService anomalyService,
                                AnalysisNlgService nlgService) {
        this.dataService = dataService;
        this.anomalyService = anomalyService;
        this.nlgService = nlgService;
    }

    /**
     * 生成完整简报（简报文本 + 异常列表 + 引导提问）
     *
     * @param warehouseId 仓库ID（可选）
     * @return 包含三部分数据的 Map
     */
    public Map<String, Object> generateBriefing(Long warehouseId) {
        return generateBriefing(warehouseId, true);
    }

    /**
     * 生成本地模板简报（不调用第三方LLM）
     */
    public Map<String, Object> generateBriefingWithoutLlm(Long warehouseId) {
        return generateBriefing(warehouseId, false);
    }

    private Map<String, Object> generateBriefing(Long warehouseId, boolean useLlm) {
        Map<String, Object> result = new HashMap<>();

        // 1. 汇总数据
        BriefingDataVO briefingData = collectBriefingData(warehouseId);

        // 2. 生成简报摘要
        String briefingText = useLlm
                ? nlgService.generateBriefingSummary(briefingData)
                : nlgService.generateBriefingSummaryWithoutLlm(briefingData);
        result.put("briefingText", briefingText);
        result.put("briefingData", briefingData);

        // 3. 异常扫描
        List<AnomalyVO> anomalies = anomalyService.scanAll();
        result.put("anomalies", anomalies);

        // 4. 动态引导提问
        List<Map<String, String>> guideQuestions = buildGuideQuestions(briefingData, anomalies);
        result.put("guideQuestions", guideQuestions);

        return result;
    }

    /**
     * 收集简报所需的五个板块数据
     */
    private BriefingDataVO collectBriefingData(Long warehouseId) {
        BriefingDataVO data = new BriefingDataVO();
        try {
            data.setSalesSummary(dataService.getSalesSummary(AnalysisPeriod.TODAY, warehouseId));
        } catch (Exception e) {
            log.error("\u83B7\u53D6\u9500\u552E\u6C47\u603B\u5931\u8D25", e);
        }
        try {
            data.setPurchaseSummary(dataService.getPurchaseSummary(AnalysisPeriod.TODAY, warehouseId));
        } catch (Exception e) {
            log.error("\u83B7\u53D6\u91C7\u8D2D\u6C47\u603B\u5931\u8D25", e);
        }
        try {
            data.setStockWarnings(dataService.getStockWarnings(warehouseId));
        } catch (Exception e) {
            log.error("\u83B7\u53D6\u5E93\u5B58\u9884\u8B66\u5931\u8D25", e);
        }
        try {
            data.setFinanceSummary(dataService.getFinanceSummary());
        } catch (Exception e) {
            log.error("\u83B7\u53D6\u8D22\u52A1\u6C47\u603B\u5931\u8D25", e);
        }
        try {
            data.setPendingTasks(dataService.getPendingTasks());
        } catch (Exception e) {
            log.error("\u83B7\u53D6\u5F85\u529E\u4E8B\u9879\u5931\u8D25", e);
        }
        try {
            data.setTopSellingGoods(dataService.getTopSellingGoods(AnalysisPeriod.TODAY, 5));
        } catch (Exception e) {
            log.error("\u83B7\u53D6\u70ED\u9500\u5546\u54C1\u5931\u8D25", e);
        }
        return data;
    }

    /**
     * 根据简报数据和异常列表，动态生成引导提问
     */
    private List<Map<String, String>> buildGuideQuestions(BriefingDataVO data, List<AnomalyVO> anomalies) {
        List<Map<String, String>> questions = new ArrayList<>();

        // 基础提问（始终显示）
        questions.add(buildQuestion("\u672C\u5468\u9500\u552E\u60C5\u51B5\u600E\u4E48\u6837\uFF1F",
                "\u672C\u5468\u9500\u552E\u60C5\u51B5\u600E\u4E48\u6837\uFF1F", "question"));

        // 根据数据特征动态追加
        if (data.getSalesSummary() != null && data.getSalesSummary().getOrderCount() > 0) {
            questions.add(buildQuestion("\u4ECA\u65E5\u54EA\u4E2A\u5546\u54C1\u5356\u5F97\u6700\u597D\uFF1F",
                    "\u4ECA\u65E5\u70ED\u9500\u5546\u54C1\u6392\u884C", "question"));
        }

        if (data.getStockWarnings() != null && !data.getStockWarnings().isEmpty()) {
            questions.add(buildQuestion("\u54EA\u4E9B\u5546\u54C1\u5FEB\u65AD\u8D27\u4E86\uFF1F",
                    "\u5E93\u5B58\u5373\u5C06\u65AD\u8D27\u7684\u5546\u54C1\u6709\u54EA\u4E9B\uFF1F", "question"));
        }

        if (data.getFinanceSummary() != null && data.getFinanceSummary().getOverdueReceivableCount() > 0) {
            questions.add(buildQuestion("\u903E\u671F\u5E94\u6536\u660E\u7EC6",
                    "\u6709\u54EA\u4E9B\u5BA2\u6237\u7684\u5E94\u6536\u6B3E\u903E\u671F\u4E86\uFF1F", "question"));
        }

        if (data.getPendingTasks() != null && data.getPendingTasks().getTotalCount() > 0) {
            questions.add(buildQuestion("\u67E5\u770B\u5F85\u529E\u4E8B\u9879",
                    "\u5F53\u524D\u6709\u54EA\u4E9B\u5F85\u5904\u7406\u7684\u4E8B\u9879\uFF1F", "question"));
        }

        // 异常相关引导
        for (AnomalyVO anomaly : anomalies) {
            if ("PENDING_SALES_OUT".equals(anomaly.getType())) {
                questions.add(buildQuestion("\u67E5\u770B\u79EF\u538B\u8BA2\u5355",
                        "\u54EA\u4E9B\u9500\u552E\u8BA2\u5355\u8FD8\u6CA1\u6709\u51FA\u5E93\uFF1F", "question"));
                break;
            }
        }

        // 限制最多 5 条
        if (questions.size() > 5) {
            questions = questions.subList(0, 5);
        }
        return questions;
    }

    private Map<String, String> buildQuestion(String label, String value, String type) {
        Map<String, String> q = new HashMap<>();
        q.put("label", label);
        q.put("value", value);
        q.put("type", type);
        return q;
    }
}
