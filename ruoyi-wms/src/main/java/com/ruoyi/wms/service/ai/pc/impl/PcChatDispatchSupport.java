package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.Map;

class PcChatDispatchSupport {

    private final PcMultiBillChatServiceImpl owner;

    PcChatDispatchSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> dispatch(PcState state, String input) {
        if ("取消".equals(input) || "重置".equals(input) || "退出".equals(input)) {
            state.resetFlow();
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, "已取消当前流程。", owner.cardBuilderSupport.actionListCard(),
                    owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元", "采购苹果笔记本50台", "查看刚创建的订单"));
        }

        if (input.startsWith("__ACTION__:")) {
            return owner.handleActionCommand(state, input.substring("__ACTION__:".length()));
        }

        if (input.startsWith("__ROUTE__")) {
            return owner.handleRouteCommand(state, input);
        }

        if (input.startsWith("__SELECT__")) {
            return owner.handleSelection(state, input);
        }

        if (input.startsWith("__FORM__")) {
            return owner.handleFormSubmit(state, input);
        }

        // 经营分析流程中的追问
        if (owner.isInAnalysisFlow(state)) {
            return owner.handleAnalysisChat(state, input);
        }

        if ("WAIT_CONFIRM_EXEC".equals(state.getStep())) {
            String confirmStageAction = owner.detectAction(input);
            if (owner.isCreateAction(confirmStageAction)) {
                return owner.handleSemanticCreateIntent(state, input, confirmStageAction);
            }
            if (owner.isConfirmInput(input)) {
                return owner.executeCreate(state);
            }
            if (owner.isCancelInput(input)) {
                state.resetFlow();
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "当前流程已取消，请重新输入业务信息。", owner.cardBuilderSupport.actionListCard(),
                        owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元", "采购苹果笔记本50台", "查看刚创建的订单"));
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.confirmHintText(), null, owner.confirmCancelQuickActions());
        }

        // 截图识别结果：流程进行中时优先交给当前补槽步骤消化
        // （如 WAIT_ITEMS_INPUT 直接填充商品明细），避免被误判为新的创建意图导致流程重置
        if (input != null && input.startsWith(com.ruoyi.wms.service.ai.VoiceParseService.IMAGE_RECOGNITION_PREFIX)
                && !"IDLE".equals(state.getStep()) && !"WAIT_CONFIRM_EXEC".equals(state.getStep())) {
            Map<String, Object> pendingResponse = owner.handlePendingStep(state, input);
            if (pendingResponse != null) {
                return pendingResponse;
            }
        }

        String action = owner.detectAction(input);
        if (!"IDLE".equals(state.getStep()) && owner.isCreateAction(action)) {
            return owner.handleSemanticCreateIntent(state, input, action);
        }

        // 查询指令需要高优先级，避免被当前补槽步骤吞掉
        // 经营分析意图
        if ("BUSINESS_ANALYSIS".equals(action)) {
            return owner.handleAnalysisEntry(state);
        }

        if ("QUERY_BILL_CENTER".equals(action)) {
            state.resetFlow();
            state.setAction("QUERY_BILL_CENTER");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.handleQueryBillCenter(state, input);
        }

        if ("QUERY_PO".equals(action)) {
            state.resetFlow();
            state.setAction("QUERY_PO");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.handleQueryPurchaseOrder(state, input);
        }

        Map<String, Object> pendingResponse = owner.handlePendingStep(state, input);
        if (pendingResponse != null) {
            return pendingResponse;
        }

        if ("AMBIGUOUS_RETURN".equals(action)) {
            if ("PURCHASE_RETURN".equals(state.getDomainType())) {
                return owner.handleSemanticCreateIntent(state, input, "CREATE_PR");
            }
            if ("SALES_RETURN".equals(state.getDomainType())) {
                return owner.handleSemanticCreateIntent(state, input, "CREATE_SR");
            }
            state.resetFlow();
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildAmbiguousReturnGuide(state);
        }

        if ("PURCHASE_INTENT".equals(action) || "CREATE_PO".equals(action) || "CREATE_PI".equals(action)
                || "CREATE_PR".equals(action) || "CREATE_SO".equals(action) || "CREATE_SOUT".equals(action)
                || "CREATE_SR".equals(action) || "CREATE_SC".equals(action) || "CREATE_ST".equals(action)) {
            return owner.handleSemanticCreateIntent(state, input, action);
        }

        String contextualCreateAction = owner.resolveContextualCreateAction(state, input, action);
        if (StringUtils.isNotBlank(contextualCreateAction)) {
            return owner.handleSemanticCreateIntent(state, input, contextualCreateAction);
        }

        return owner.conversationLifecycleSupport.buildReply(
                state,
                "未识别到有效单据信息。你可以直接这样说：\n"
                        + "1) 向XX公司采购XX商品50台，单价8000元\n"
                        + "2) 卖给XX客户XX商品10台，单价9000元\n"
                        + "3) 从A仓调XX商品到B仓\n"
                        + "4) 盘点A仓所有商品",
                owner.cardBuilderSupport.actionListCard(),
                owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元", "卖给客户A苹果笔记本10台，单价9000元", "从一号仓库调30台笔记本到二号仓库")
        );
    }
}

