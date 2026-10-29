package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.wms.domain.vo.ai.analysis.*;
import com.ruoyi.wms.service.ai.analysis.*;
import com.ruoyi.wms.service.ai.pc.vo.PcState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PC端AI助手 - 经营分析模块
 * <p>
 * 复用移动端已有的分析服务（DailyBriefingService / AnalysisNluService / AnalysisNlgService / BusinessAnalysisDataService），
 * 适配 PC 端的卡片式响应格式。
 */
class PcBusinessAnalysisSupport {

    private static final Logger log = LoggerFactory.getLogger(PcBusinessAnalysisSupport.class);

    private static final String STEP_ANALYSIS_QA = "ANALYSIS_QA";
    private static final int MAX_CONTEXT_ROUNDS = 3;
    private static final String BRIEFING_TITLE = "📊 今日经营简报\n\n";
    private static final String ANOMALY_TITLE = "\n\n⚠️ 异常提醒：";
    private static final String UNKNOWN_INTENT_REPLY = "我不是很明白你说的话，你可以点击下方的提示词操作。";
    private static final String FOLLOW_UP_TITLE = "\n\n你可以接着问我任何经营相关的问题。";

    private final PcMultiBillChatServiceImpl owner;
    private final DailyBriefingService briefingService;
    private final AnalysisNluService nluService;
    private final AnalysisNlgService nlgService;
    private final BusinessAnalysisDataService dataService;

    PcBusinessAnalysisSupport(PcMultiBillChatServiceImpl owner,
                              DailyBriefingService briefingService,
                              AnalysisNluService nluService,
                              AnalysisNlgService nlgService,
                              BusinessAnalysisDataService dataService) {
        this.owner = owner;
        this.briefingService = briefingService;
        this.nluService = nluService;
        this.nlgService = nlgService;
        this.dataService = dataService;
    }

    /**
     * 进入经营分析：生成简报 + 异常 + 引导提问
     */
    Map<String, Object> handleAnalysisEntry(PcState state) {
        state.resetFlow();
        state.setAction("BUSINESS_ANALYSIS");
        state.setStep(STEP_ANALYSIS_QA);
        state.analysisContextHistory = new ArrayList<>();

        try {
            Map<String, Object> briefing = briefingService.generateBriefingWithoutLlm(null);
            String briefingText = (String) briefing.get("briefingText");
            @SuppressWarnings("unchecked")
            List<AnomalyVO> anomalies = (List<AnomalyVO>) briefing.get("anomalies");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> guideQuestions = (List<Map<String, String>>) briefing.get("guideQuestions");

            // 构建简报文本
            StringBuilder reply = new StringBuilder();
            reply.append(BRIEFING_TITLE);
            reply.append(briefingText);

            if (anomalies != null && !anomalies.isEmpty()) {
                reply.append(ANOMALY_TITLE);
                for (AnomalyVO anomaly : anomalies) {
                    reply.append("\n").append(getSeverityIcon(anomaly.getSeverity()))
                            .append(" ").append(anomaly.getTitle())
                            .append("：").append(anomaly.getDescription());
                }
            }

            reply.append(FOLLOW_UP_TITLE);

            // 构建经营简报卡片
            Map<String, Object> card = buildBriefingCard(briefing, anomalies, guideQuestions);

            // 构建快捷操作
            List<String> quickActions = new ArrayList<>();
            if (guideQuestions != null) {
                for (Map<String, String> q : guideQuestions) {
                    quickActions.add(q.get("value"));
                }
            }
            if (quickActions.isEmpty()) {
                quickActions.add("今日销售情况怎么样？");
                quickActions.add("热销商品排行");
                quickActions.add("当前有哪些待处理的事项？");
            }

            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, reply.toString(), card, quickActions);
        } catch (Exception e) {
            log.error("经营简报生成失败", e);
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state,
                    "抱歉，经营简报生成失败，请稍后重试。",
                    null, Collections.emptyList());
        }
    }

    /**
     * 处理经营分析的后续追问
     */
    Map<String, Object> handleAnalysisChat(PcState state, String input) {
        try {
            List<NluIntentResult> contextHistory = state.analysisContextHistory;
            if (contextHistory == null) {
                contextHistory = new ArrayList<>();
            }

            NluIntentResult intentResult = nluService.parseIntentLocally(input, contextHistory);

            // 更新上下文
            if (!NluIntentResult.INTENT_UNKNOWN.equals(intentResult.getIntent())) {
                contextHistory.add(intentResult);
                if (contextHistory.size() > MAX_CONTEXT_ROUNDS) {
                    contextHistory = new ArrayList<>(
                            contextHistory.subList(contextHistory.size() - MAX_CONTEXT_ROUNDS, contextHistory.size()));
                }
            }
            state.analysisContextHistory = contextHistory;

            // 根据意图分发查询
            return dispatchByIntent(state, intentResult);
        } catch (Exception e) {
            log.error("经营分析问答处理失败", e);
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state,
                    "抱歉，处理您的问题时出现错误，请尝试换个方式提问。",
                    null, buildDefaultQuickActions());
        }
    }

    boolean willUseLlm(PcState state, String input) {
        return false;
    }

    /**
     * 判断当前是否处于经营分析流程中
     */
    boolean isInAnalysisFlow(PcState state) {
        return STEP_ANALYSIS_QA.equals(state.getStep()) && "BUSINESS_ANALYSIS".equals(state.getAction());
    }

    // ────────────── 私有方法 ──────────────

    private Map<String, Object> dispatchByIntent(PcState state, NluIntentResult intentResult) {
        String intent = intentResult.getIntent();
        AnalysisPeriod period = AnalysisPeriod.fromCode(
                intentResult.getPeriod() != null && !intentResult.getPeriod().isEmpty()
                        ? intentResult.getPeriod() : "today");

        Object queryData;
        String cardType = "ANALYSIS_ANSWER";

        switch (intent) {
            case NluIntentResult.INTENT_QUERY_SALES:
            case NluIntentResult.INTENT_QUERY_COMPARISON:
                queryData = dataService.getSalesSummary(period, null);
                break;
            case NluIntentResult.INTENT_QUERY_PURCHASE:
                queryData = dataService.getPurchaseSummary(period, null);
                break;
            case NluIntentResult.INTENT_QUERY_STOCK:
                queryData = dataService.getStockWarnings(null);
                break;
            case NluIntentResult.INTENT_QUERY_FINANCE:
                queryData = dataService.getFinanceSummary();
                break;
            case NluIntentResult.INTENT_QUERY_PENDING:
                queryData = dataService.getPendingTasks();
                break;
            case NluIntentResult.INTENT_QUERY_GOODS_RANK:
                queryData = dataService.getTopSellingGoods(period, 10);
                cardType = "ANALYSIS_RANKING";
                break;
            case NluIntentResult.INTENT_QUERY_CUSTOMER_RANK:
                queryData = dataService.getCustomerSalesRanking(period, 10);
                cardType = "ANALYSIS_RANKING";
                break;
            case NluIntentResult.INTENT_UNKNOWN:
            default:
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        UNKNOWN_INTENT_REPLY,
                        null, buildDefaultQuickActions());
        }

        String nlgText = nlgService.generateAnswerWithoutLlm(intent, queryData);

        // 构建回复卡片
        Map<String, Object> card = new HashMap<>();
        card.put("type", cardType);
        card.put("intent", intent);
        card.put("period", period.getLabel());

        if ("ANALYSIS_RANKING".equals(cardType) && queryData instanceof List) {
            card.put("ranking", queryData);
            card.put("rankingType", NluIntentResult.INTENT_QUERY_GOODS_RANK.equals(intent) ? "goods" : "customer");
        } else {
            card.put("data", queryData);
        }

        // 追问引导
        List<String> quickActions = buildFollowUpQuickActions(intent, period);

        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state, nlgText, card, quickActions);
    }

    private Map<String, Object> buildBriefingCard(Map<String, Object> briefing,
                                                   List<AnomalyVO> anomalies,
                                                   List<Map<String, String>> guideQuestions) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "ANALYSIS_BRIEFING");

        BriefingDataVO briefingData = (BriefingDataVO) briefing.get("briefingData");
        if (briefingData != null) {
            Map<String, Object> summary = new HashMap<>();
            if (briefingData.getSalesSummary() != null) {
                summary.put("sales", briefingData.getSalesSummary());
            }
            if (briefingData.getPurchaseSummary() != null) {
                summary.put("purchase", briefingData.getPurchaseSummary());
            }
            if (briefingData.getPendingTasks() != null) {
                summary.put("pending", briefingData.getPendingTasks());
            }
            if (briefingData.getFinanceSummary() != null) {
                summary.put("finance", briefingData.getFinanceSummary());
            }
            card.put("summary", summary);

            if (briefingData.getTopSellingGoods() != null && !briefingData.getTopSellingGoods().isEmpty()) {
                card.put("topGoods", briefingData.getTopSellingGoods());
            }
        }

        if (anomalies != null && !anomalies.isEmpty()) {
            List<Map<String, Object>> anomalyList = anomalies.stream().map(a -> {
                Map<String, Object> m = new HashMap<>();
                m.put("type", a.getType());
                m.put("severity", a.getSeverity());
                m.put("title", a.getTitle());
                m.put("description", a.getDescription());
                return m;
            }).collect(Collectors.toList());
            card.put("anomalies", anomalyList);
        }

        if (guideQuestions != null) {
            card.put("guideQuestions", guideQuestions);
        }

        return card;
    }

    private List<String> buildFollowUpQuickActions(String intent, AnalysisPeriod period) {
        List<String> actions = new ArrayList<>();
        switch (intent) {
            case NluIntentResult.INTENT_QUERY_SALES:
                actions.add("热销商品排行");
                actions.add("客户销售排名");
                if (period == AnalysisPeriod.TODAY) {
                    actions.add("本周销售情况怎么样？");
                }
                break;
            case NluIntentResult.INTENT_QUERY_PURCHASE:
                actions.add("今日销售情况");
                actions.add("待办事项");
                break;
            case NluIntentResult.INTENT_QUERY_GOODS_RANK:
                actions.add("客户销售排名");
                actions.add("哪些商品快断货了？");
                break;
            case NluIntentResult.INTENT_QUERY_CUSTOMER_RANK:
                actions.add("热销商品排行");
                actions.add("库存预警");
                break;
            case NluIntentResult.INTENT_QUERY_FINANCE:
                actions.add("逾期应收明细");
                actions.add("今日销售情况");
                break;
            case NluIntentResult.INTENT_QUERY_STOCK:
                actions.add("今日采购情况");
                actions.add("热销商品排行");
                break;
            default:
                actions.addAll(buildDefaultQuickActions());
                break;
        }
        return actions;
    }

    private List<String> buildDefaultQuickActions() {
        List<String> actions = new ArrayList<>();
        actions.add("今日销售情况怎么样？");
        actions.add("热销商品排行");
        actions.add("当前有哪些待处理的事项？");
        return actions;
    }

    private String getSeverityIcon(String severity) {
        if (severity == null) return "⚪";
        switch (severity) {
            case "RED":    return "🔴";
            case "ORANGE": return "🟠";
            case "YELLOW": return "🟡";
            case "BLUE":   return "🔵";
            default:       return "⚪";
        }
    }
}
