package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.ArrayList;
import java.util.Map;

class PcPendingStepSupport {

    private static final String DOMAIN_PI = "PURCHASE_IN";

    private final PcMultiBillChatServiceImpl owner;

    PcPendingStepSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handlePendingStep(PcState state, String input) {
        switch (state.getStep()) {
            case "WAIT_DOMAIN_CHOICE":
                if (input.contains("订单")) {
                    state.setDomainType("PURCHASE_ORDER");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.continueCreateFlow(state);
                }
                if (input.contains("入库")) {
                    state.setDomainType(DOMAIN_PI);
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.continueCreateFlow(state);
                }
                return owner.conversationLifecycleSupport.buildReply(state, "请明确本次要创建“采购订单”还是“采购入库”。", owner.domainChoiceCard(), owner.conversationLifecycleSupport.quickActions("采购订单", "采购入库"));
            case "WAIT_SUPPLIER_INPUT":
            {
                Map<String, Object> semanticReply = owner.tryRestartSemanticFlow(state, input);
                if (semanticReply != null) {
                    return semanticReply;
                }
            }
            state.setSupplierInput(input);
            owner.resolveSupplierByInput(state, input);
            owner.conversationLifecycleSupport.saveState(state);
            if ("WAIT_SUPPLIER_CANDIDATE".equals(state.getStep())
                    || "WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())
                    || "WAIT_SUPPLIER_CREATE_CONFIRM".equals(state.getStep())) {
                return owner.buildSupplierFollowup(state);
            }
            return owner.resumeCurrentFlow(state);
            case "WAIT_SUPPLIER_CREATE_CONFIRM":
            {
                Map<String, Object> semanticReply = owner.tryRestartSemanticFlow(state, input);
                if (semanticReply != null) {
                    return semanticReply;
                }
            }
            return owner.handleSupplierCreateConfirm(state, input);
            case "WAIT_SUPPLIER_CANDIDATE":
            {
                Map<String, Object> semanticReply = owner.tryRestartSemanticFlow(state, input);
                if (semanticReply != null) {
                    return semanticReply;
                }
            }
            return owner.handleSupplierCandidateText(state, input);
            case "WAIT_SUPPLIER_CUSTOM_FORM":
            {
                Map<String, Object> semanticReply = owner.tryRestartSemanticFlow(state, input);
                if (semanticReply != null) {
                    return semanticReply;
                }
            }
            return owner.handleSupplierCustomText(state, input);
            case "WAIT_WAREHOUSE_INPUT":
                state.setWarehouseInput(input);
                owner.resolveWarehouseByInput(state, input);
                owner.conversationLifecycleSupport.saveState(state);
                if ("WAIT_WAREHOUSE_CANDIDATE".equals(state.getStep()) || "WAIT_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
                    return owner.buildWarehouseFollowup(state);
                }
                return owner.resumeCurrentFlow(state);
            case "WAIT_WAREHOUSE_CANDIDATE":
                return owner.handleWarehouseCandidateText(state, input);
            case "WAIT_WAREHOUSE_CUSTOM_FORM":
                return owner.handleWarehouseCustomText(state, input);
            case "WAIT_CUSTOMER_INPUT":
                state.customerInput = input;
                owner.resolveCustomerByInput(state, input, true);
                owner.conversationLifecycleSupport.saveState(state);
                if (!"IDLE".equals(state.step)) {
                    return owner.buildCustomerFollowup(state, true);
                }
                return owner.resumeCurrentFlow(state);
            case "WAIT_CUSTOMER_CANDIDATE":
                return owner.handleCustomerCandidateText(state, input, true);
            case "WAIT_CUSTOMER_CUSTOM_FORM":
                return owner.handleCustomerCustomText(state, input);
            case "WAIT_OUT_WAREHOUSE_INPUT":
                state.outWarehouseInput = input;
                owner.resolveWarehouseForSlot(state, input, "OUT");
                owner.conversationLifecycleSupport.saveState(state);
                if (!"IDLE".equals(state.step)) {
                    return owner.buildTransferWarehouseFollowup(state, "OUT");
                }
                return owner.resumeCurrentFlow(state);
            case "WAIT_IN_WAREHOUSE_INPUT":
                state.inWarehouseInput = input;
                owner.resolveWarehouseForSlot(state, input, "IN");
                owner.conversationLifecycleSupport.saveState(state);
                if (!"IDLE".equals(state.step)) {
                    return owner.buildTransferWarehouseFollowup(state, "IN");
                }
                return owner.resumeCurrentFlow(state);
            case "WAIT_OUT_WAREHOUSE_CANDIDATE":
                return owner.handleWarehouseCandidateText(state, input);
            case "WAIT_IN_WAREHOUSE_CANDIDATE":
                return owner.handleWarehouseCandidateText(state, input);
            case "WAIT_OUT_WAREHOUSE_CUSTOM_FORM":
            case "WAIT_IN_WAREHOUSE_CUSTOM_FORM":
                return owner.handleWarehouseCustomText(state, input);
            case "WAIT_ST_OUT_WAREHOUSE_SELECT":
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请在候选列表中选择调出仓库。",
                        owner.warehouseCandidateCard(state),
                        owner.conversationLifecycleSupport.quickActions("取消"));
            case "WAIT_SC_WAREHOUSE_SELECT":
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请在候选列表中选择盘点仓库。",
                        owner.warehouseCandidateCard(state),
                        owner.conversationLifecycleSupport.quickActions("取消"));
            case "WAIT_SC_GOODS_SELECT":
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请在商品列表中勾选要盘点的商品。",
                        null,
                        owner.conversationLifecycleSupport.quickActions("取消"));
            case "WAIT_ST_GOODS_SELECT":
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请在商品列表中选择要调拨的商品。",
                        null,
                        owner.conversationLifecycleSupport.quickActions("取消"));
            case "WAIT_LINK_ORDER_INPUT":
                if (DOMAIN_PI.equals(state.getDomainType()) && ("直接入库".equals(input) || "放弃关联".equals(input))) {
                    state.linkOrderNo = null;
                    state.linkOrderId = null;
                    state.linkOrderType = null;
                    state.linkOrderCandidates = new ArrayList<>();
                    state.linkOrderCandidatePage = 1;
                    state.setStep("IDLE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.continueCreateFlow(state);
                }
                if (DOMAIN_PI.equals(state.getDomainType()) && ("重新输入".equals(input) || "重新输入单号".equals(input))) {
                    state.setStep("WAIT_LINK_ORDER_INPUT");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.conversationLifecycleSupport.buildReply(state, "请重新输入采购订单单号，例如 PO202603190001。", owner.linkOrderTemplateCard(state), owner.conversationLifecycleSupport.quickActions("PO202603190001", "直接入库", "取消"));
                }
                // 采购退货关联模式：支持"直接退货"快捷切换
                if ("PURCHASE_RETURN".equals(state.getDomainType()) && ("直接退货".equals(input) || "放弃关联".equals(input))) {
                    state.purchaseInMode = "DIRECT";
                    state.linkOrderNo = null;
                    state.linkOrderId = null;
                    state.linkOrderType = null;
                    state.linkOrderCandidates = new ArrayList<>();
                    state.linkOrderCandidatePage = 1;
                    state.setStep("IDLE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.continueCreateFlow(state);
                }
                // 销售退货关联模式：支持"直接退货"快捷切换
                if ("SALES_RETURN".equals(state.getDomainType()) && ("直接退货".equals(input) || "放弃关联".equals(input))) {
                    state.salesReturnMode = "DIRECT";
                    state.linkOrderNo = null;
                    state.linkOrderId = null;
                    state.linkOrderType = null;
                    state.linkOrderCandidates = new ArrayList<>();
                    state.linkOrderCandidatePage = 1;
                    state.setStep("IDLE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.continueExtendedFlow(state);
                }
                state.linkOrderNo = owner.itemParseSupport.normalizeLinkOrderInput(input);
                state.linkOrderId = null;
                state.linkOrderType = null;
                state.linkOrderCandidates = new ArrayList<>();
                state.linkOrderCandidatePage = 1;
                owner.conversationLifecycleSupport.saveState(state);
                return DOMAIN_PI.equals(state.getDomainType()) ? owner.continueCreateFlow(state) : owner.continueExtendedFlow(state);
            case "WAIT_LINK_ORDER_CANDIDATE":
                return owner.handleLinkOrderCandidateText(state, input);
            case "WAIT_PI_MODE_CHOICE":
                return owner.handlePurchaseInModeChoice(state, input);
            case "WAIT_PR_MODE_CHOICE":
                return owner.handlePurchaseReturnModeChoice(state, input);
            case "WAIT_SOUT_MODE_CHOICE":
                return owner.handleSalesOutModeChoice(state, input);
            case "WAIT_SR_MODE_CHOICE":
                return owner.handleSalesReturnModeChoice(state, input);
            case "WAIT_ITEMS_INPUT":
                return owner.handleItemsText(state, input);
            case "WAIT_GOODS_CANDIDATE":
                return owner.handleGoodsCandidateText(state, input);
            case "WAIT_GOODS_BASE_FORM":
            case "WAIT_GOODS_CUSTOM_FORM":
                return owner.conversationLifecycleSupport.buildReply(state, "请填写商品基本信息后继续。", owner.goodsFlowSupport.goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            case "WAIT_GOODS_SPEC_FORM":
                return owner.conversationLifecycleSupport.buildReply(state, "请补充多规格信息，然后进入SKU确认。", owner.goodsFlowSupport.goodsSpecFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            case "WAIT_GOODS_SKU_FORM":
                return owner.conversationLifecycleSupport.buildReply(state, "请确认SKU列表后保存。", owner.goodsFlowSupport.goodsSkuFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            default:
                return null;
        }
    }
}

