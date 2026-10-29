package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.List;
import java.util.Map;

class PcFormFlowSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private final PcMultiBillChatServiceImpl owner;

    PcFormFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handleFormSubmit(PcState state, String input) {
        Map<String, Object> formData = owner.parseFormData(input);

        if ("WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())) {
            return owner.handleSupplierFormSubmit(state, formData);
        }

        if ("WAIT_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
            return owner.handleWarehouseFormSubmit(state, formData);
        }

        if ("WAIT_CUSTOMER_CUSTOM_FORM".equals(state.step)) {
            return owner.handleCustomerFormSubmit(state, formData);
        }

        if ("WAIT_ITEMS_INPUT".equals(state.getStep())) {
            String items = owner.str(formData.get("items"));
                List<ItemDraft> drafts = DOMAIN_SC.equals(state.getDomainType())
                    ? owner.itemParseSupport.parseNameOnlyItems(items)
                    : owner.itemParseSupport.parseItemsAllowPriceOptional(items);
            if (drafts.isEmpty()) {
                return owner.conversationLifecycleSupport.buildReply(state, "商品明细格式不正确。", owner.goodsTemplateCard(state.getDomainType()), owner.conversationLifecycleSupport.quickActions("取消"));
            }
            state.setItemDrafts(drafts);
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }

        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && DOMAIN_PO.equals(state.getDomainType()) && formData.get("items") instanceof List) {
            owner.applyDirectPurchasePreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.continueCreateFlow(state);
        }

        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && DOMAIN_PI.equals(state.getDomainType())
                && !owner.hasLinkedSource(state) && formData.get("items") instanceof List) {
            owner.applyPurchaseInPreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            if (owner.hasLinkedSource(state)) {
                return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("采购入库信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
            }
            return owner.continueCreateFlow(state);
        }

        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && "PURCHASE_RETURN".equals(state.getDomainType())
                && !owner.hasLinkedSource(state) && formData.get("items") instanceof List) {
            owner.applyPurchaseReturnPreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("采购退货信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        // 销售订单确认卡片表单提交（支持客户选择 + 单价编辑）
        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && "SALES_ORDER".equals(state.getDomainType())
                && formData.get("items") instanceof List) {
            owner.applySalesOrderPreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("销售订单信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        // 销售出库直接模式确认卡片表单提交（支持客户选择 + 仓库选择 + 单价编辑）
        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && DOMAIN_SOUT.equals(state.getDomainType())
                && !owner.hasLinkedSource(state) && formData.get("items") instanceof List) {
            owner.applySalesOutPreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("销售出库信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        // 销售退货直接模式确认卡片表单提交（支持客户选择 + 仓库选择 + 单价可选 + 备注）
        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && DOMAIN_SR.equals(state.getDomainType())
                && !owner.hasLinkedSource(state) && formData.get("items") instanceof List) {
            owner.applySalesReturnPreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("销售退货信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        // 库存调拨确认卡片表单提交（支持调入仓库选择 + 数量调整）
        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && DOMAIN_ST.equals(state.getDomainType())
                && formData.get("items") instanceof List) {
            owner.applyStockTransferPreviewForm(state, formData);
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("调拨单信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        if ("WAIT_CONFIRM_EXEC".equals(state.getStep()) && formData.get("items") instanceof List
                && (owner.hasLinkedSource(state) || DOMAIN_SC.equals(state.getDomainType()))) {
            if (DOMAIN_SC.equals(state.getDomainType())) {
                owner.applyStockCheckPreviewItems(state, formData.get("items"));
            } else {
                if (DOMAIN_PI.equals(state.getDomainType())) {
                    owner.applyLinkedPurchaseInWarehouseForm(state, formData);
                }
                if (DOMAIN_SOUT.equals(state.getDomainType())) {
                    owner.applyLinkedPurchaseInWarehouseForm(state, formData);
                }
                if (DOMAIN_SR.equals(state.getDomainType())) {
                    owner.applyLinkedPurchaseInWarehouseForm(state, formData);
                }
                owner.applyLinkedPreviewItems(state, formData.get("items"));
            }
            if (DOMAIN_PI.equals(state.getDomainType())) {
                state.setPreparedPiItems(owner.buildPurchaseInItemsFromLines(state.preparedLines));
            }
            owner.conversationLifecycleSupport.saveState(state);
            if ("CONFIRM_EXEC".equalsIgnoreCase(owner.str(formData.get("_submitAction")))) {
                return owner.executeCreate(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("单据信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        if ("WAIT_GOODS_BASE_FORM".equals(state.getStep()) || "WAIT_GOODS_CUSTOM_FORM".equals(state.getStep())) {
            return owner.handleGoodsBaseFormSubmit(state, formData);
        }

        if ("WAIT_GOODS_SPEC_FORM".equals(state.getStep())) {
            return owner.handleGoodsSpecFormSubmit(state, formData);
        }

        if ("WAIT_GOODS_SKU_FORM".equals(state.getStep())) {
            return owner.handleGoodsSkuFormSubmit(state, formData);
        }

        return owner.conversationLifecycleSupport.buildReply(state, "当前没有待提交的表单。", null, owner.conversationLifecycleSupport.quickActions("取消"));
    }
}
