package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.Map;

class PcActionRoutingSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private final PcMultiBillChatServiceImpl owner;

    PcActionRoutingSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handleActionCommand(PcState state, String command) {
        if (command != null && command.startsWith("__ACTION__:")) {
            command = command.substring("__ACTION__:".length());
        }
        if ("CANCEL_SLOT".equals(command)) {
            return cancelInlineForm(state);
        }
        if ("QUERY_PO".equals(command)) {
            state.setAction("QUERY_PO");
            return owner.handleQueryPurchaseOrder(state, "查看刚创建的订单");
        }
        if ("QUERY_BILL_CENTER".equals(command)) {
            state.setAction("QUERY_BILL_CENTER");
            return owner.handleQueryBillCenterEntry(state);
        }
        if ("BUSINESS_ANALYSIS".equals(command)) {
            return owner.handleAnalysisEntry(state);
        }
        if (command.startsWith("BILL_DETAIL:")) {
            String payload = command.substring("BILL_DETAIL:".length());
            int colon = payload.indexOf(':');
            if (colon > 0) {
                String domainType = normalizeQueryDomainType(payload.substring(0, colon));
                try {
                    Long id = Long.parseLong(payload.substring(colon + 1));
                    if (StringUtils.isBlank(domainType)) {
                        return owner.conversationLifecycleSupport.buildReply(state, "不支持该单据类型的详情查看。", owner.cardBuilderSupport.actionListCard(), owner.conversationLifecycleSupport.quickActions());
                    }
                    return owner.handleBillDetailById(state, domainType, id);
                } catch (NumberFormatException ignored) {
                }
            }
            return owner.conversationLifecycleSupport.buildReply(state, "单据详情参数无效。", owner.cardBuilderSupport.actionListCard(), owner.conversationLifecycleSupport.quickActions());
        }
        if ("UNMATCHED_GOODS_CREATE_ONE".equals(command)) {
            return owner.goodsFlowSupport.startSingleUnmatchedGoodsCreate(state);
        }
        if ("BATCH_CREATE_UNMATCHED_GOODS".equals(command)) {
            return owner.goodsFlowSupport.batchCreateUnmatchedGoods(state);
        }
        String domainType = domainByAction(command);
        if (StringUtils.isBlank(domainType)) {
            return owner.conversationLifecycleSupport.buildReply(state, "未识别操作类型。", owner.cardBuilderSupport.actionListCard(), owner.conversationLifecycleSupport.quickActions());
        }
        state.resetFlow();
        state.setAction("CREATE");
        state.setDomainType(domainType);
        if (DOMAIN_PI.equals(domainType) || DOMAIN_PR.equals(domainType)) {
            state.lastIntentText = DOMAIN_PI.equals(domainType) ? "采购入库" : "采购退货";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueCreateFlow(state);
        }
        if (DOMAIN_SOUT.equals(domainType)) {
            state.lastIntentText = "销售出库";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueExtendedFlow(state);
        }
        if (DOMAIN_SR.equals(domainType)) {
            state.lastIntentText = "销售退货";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueExtendedFlow(state);
        }
        if (DOMAIN_SC.equals(domainType)) {
            state.lastIntentText = "库存盘点";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.startStockCheckGuidedFlow(state);
        }
        if (DOMAIN_ST.equals(domainType)) {
            state.lastIntentText = "库存调拨";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.startStockTransferGuidedFlow(state);
        }
        owner.conversationLifecycleSupport.saveState(state);
        return owner.buildDomainStartGuide(state);
    }

    private String normalizeQueryDomainType(String domainType) {
        if (StringUtils.isBlank(domainType)) {
            return null;
        }
        String value = domainType.trim();
        if ("PO".equalsIgnoreCase(value) || "PURCHASE_ORDER".equalsIgnoreCase(value) || "采购订单".equals(value)) return "PO";
        if ("PI".equalsIgnoreCase(value) || "PURCHASE_IN".equalsIgnoreCase(value) || "采购入库".equals(value)) return "PI";
        if ("PR".equalsIgnoreCase(value) || "PURCHASE_RETURN".equalsIgnoreCase(value) || "采购退货".equals(value)) return "PR";
        if ("SO".equalsIgnoreCase(value) || "SALES_ORDER".equalsIgnoreCase(value) || "销售订单".equals(value)) return "SO";
        if ("SOUT".equalsIgnoreCase(value) || "SALES_OUT".equalsIgnoreCase(value) || "销售出库".equals(value)) return "SOUT";
        if ("SR".equalsIgnoreCase(value) || "SALES_RETURN".equalsIgnoreCase(value) || "销售退货".equals(value)) return "SR";
        if ("SC".equalsIgnoreCase(value) || "STOCK_CHECK".equalsIgnoreCase(value) || "库存盘点".equals(value)) return "SC";
        if ("ST".equalsIgnoreCase(value) || "STOCK_TRANSFER".equalsIgnoreCase(value) || "库存调拨".equals(value)) return "ST";
        return null;
    }

    Map<String, Object> handleRouteCommand(PcState state, String input) {
        String payload = input.substring("__ROUTE__".length());
        int separatorIndex = payload.indexOf("::");
        String domainType = separatorIndex >= 0 ? payload.substring(0, separatorIndex) : payload;
        String text = separatorIndex >= 0 ? payload.substring(separatorIndex + 2) : "";
        String action = actionByDomain(domainType);
        if (StringUtils.isBlank(action)) {
            return owner.conversationLifecycleSupport.buildReply(state, "未识别模板路由。", owner.cardBuilderSupport.actionListCard(), owner.conversationLifecycleSupport.quickActions());
        }
        if (StringUtils.isBlank(text)) {
            state.resetFlow();
            state.setAction("CREATE");
            state.setDomainType(domainType);
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildDomainStartGuide(state);
        }
        return owner.handleSemanticCreateIntent(state, text, action);
    }

    Map<String, Object> cancelInlineForm(PcState state) {
        if ("WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())) {
            state.setStep("WAIT_SUPPLIER_INPUT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildSupplierFollowup(state);
        }
        if ("WAIT_CUSTOMER_CUSTOM_FORM".equals(state.step)) {
            state.step = "WAIT_CUSTOMER_INPUT";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildCustomerFollowup(state, true);
        }
        if ("WAIT_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
            state.setStep("WAIT_WAREHOUSE_INPUT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildWarehouseFollowup(state);
        }
        if ("WAIT_OUT_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
            state.setStep("WAIT_OUT_WAREHOUSE_INPUT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildTransferWarehouseFollowup(state, "OUT");
        }
        if ("WAIT_IN_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
            state.setStep("WAIT_IN_WAREHOUSE_INPUT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildTransferWarehouseFollowup(state, "IN");
        }
        return owner.conversationLifecycleSupport.buildReply(state, "已返回当前流程。", owner.cardBuilderSupport.startTemplateCard(state.getDomainType()), owner.cardBuilderSupport.domainStartQuickActions(state.getDomainType()));
    }

    Map<String, Object> tryRestartSemanticFlow(PcState state, String input) {
        String action = owner.detectAction(input);
        if (!isCreateAction(action)) {
            return null;
        }
        state.resetFlow();
        return owner.handleSemanticCreateIntent(state, input, action);
    }

    boolean isCreateAction(String action) {
        return "PURCHASE_INTENT".equals(action)
                || "CREATE_PO".equals(action)
                || "CREATE_PI".equals(action)
                || "CREATE_PR".equals(action)
                || "CREATE_SO".equals(action)
                || "CREATE_SOUT".equals(action)
                || "CREATE_SR".equals(action)
                || "CREATE_SC".equals(action)
                || "CREATE_ST".equals(action);
    }

    String domainByAction(String action) {
        if ("CREATE_PO".equals(action) || "PURCHASE_ORDER".equals(action)) {
            return DOMAIN_PO;
        }
        if ("CREATE_PI".equals(action) || "PURCHASE_IN".equals(action)) {
            return DOMAIN_PI;
        }
        if ("CREATE_PR".equals(action) || "PURCHASE_RETURN".equals(action)) {
            return DOMAIN_PR;
        }
        if ("CREATE_SO".equals(action) || "SALES_ORDER".equals(action)) {
            return DOMAIN_SO;
        }
        if ("CREATE_SOUT".equals(action) || "SALES_OUT".equals(action)) {
            return DOMAIN_SOUT;
        }
        if ("CREATE_SR".equals(action) || "SALES_RETURN".equals(action)) {
            return DOMAIN_SR;
        }
        if ("CREATE_SC".equals(action) || "STOCK_CHECK".equals(action)) {
            return DOMAIN_SC;
        }
        if ("CREATE_ST".equals(action) || "STOCK_TRANSFER".equals(action)) {
            return DOMAIN_ST;
        }
        return null;
    }

    String actionByDomain(String domainType) {
        if (DOMAIN_PO.equals(domainType)) {
            return "CREATE_PO";
        }
        if (DOMAIN_PI.equals(domainType)) {
            return "CREATE_PI";
        }
        if (DOMAIN_PR.equals(domainType)) {
            return "CREATE_PR";
        }
        if (DOMAIN_SO.equals(domainType)) {
            return "CREATE_SO";
        }
        if (DOMAIN_SOUT.equals(domainType)) {
            return "CREATE_SOUT";
        }
        if (DOMAIN_SR.equals(domainType)) {
            return "CREATE_SR";
        }
        if (DOMAIN_SC.equals(domainType)) {
            return "CREATE_SC";
        }
        if (DOMAIN_ST.equals(domainType)) {
            return "CREATE_ST";
        }
        return null;
    }
}

