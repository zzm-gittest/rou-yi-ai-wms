package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.List;
import java.util.Map;

class PcSelectionFlowSupport {

    private static final String DOMAIN_PI = "PURCHASE_IN";

    private final PcMultiBillChatServiceImpl owner;

    PcSelectionFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handleSelection(PcState state, String input) {
        String payload = input.substring("__SELECT__".length());
        if (payload.startsWith("DOMAIN:")) {
            String value = payload.substring("DOMAIN:".length());
            state.setDomainType("PO".equalsIgnoreCase(value) ? "PO" : DOMAIN_PI);
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }

        if (payload.startsWith("SUPPLIER:")) {
            String value = payload.substring("SUPPLIER:".length());
            if ("OTHER".equals(value)) {
                state.setStep("WAIT_SUPPLIER_CUSTOM_FORM");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请在聊天框内补充供应商必填信息。",
                        owner.supplierFormCard(state),
                        owner.conversationLifecycleSupport.quickActions("取消"));
            }
            Long supplierId = Long.valueOf(value);
            WmsSupplierRespVO supplier = owner.findSupplierById(supplierId);
            if (supplier == null) {
                throw new ServiceException("供应商不存在");
            }
            state.setSupplierId(supplier.getId());
            state.setSupplierName(supplier.getSupplierName());
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }

        if (payload.startsWith("WAREHOUSE:")) {
            String value = payload.substring("WAREHOUSE:".length());
            if ("OTHER".equals(value)) {
                if ("WAIT_OUT_WAREHOUSE_CANDIDATE".equals(state.step)) {
                    state.step = "WAIT_OUT_WAREHOUSE_CUSTOM_FORM";
                } else if ("WAIT_IN_WAREHOUSE_CANDIDATE".equals(state.step)) {
                    state.step = "WAIT_IN_WAREHOUSE_CUSTOM_FORM";
                } else {
                    state.setStep("WAIT_WAREHOUSE_CUSTOM_FORM");
                }
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请在聊天框内补充仓库必填信息。",
                        owner.warehouseFormCard(state),
                        owner.conversationLifecycleSupport.quickActions("取消"));
            }
            Long warehouseId = Long.valueOf(value);
            WmsWarehouseRespVO warehouse = owner.findWarehouseById(warehouseId);
            if (warehouse == null) {
                throw new ServiceException("仓库不存在");
            }
            if ("WAIT_SC_WAREHOUSE_SELECT".equals(state.step)) {
                state.setWarehouseId(warehouse.getId());
                state.setWarehouseName(warehouse.getWarehouseName());
                state.setWarehouseInput(warehouse.getWarehouseName());
                state.setStep("IDLE");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.showStockCheckGoodsSelection(state);
            }
            if ("WAIT_ST_OUT_WAREHOUSE_SELECT".equals(state.step)) {
                owner.applyWarehouseToSlot(state, warehouse, "OUT");
                state.setStep("IDLE");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.showStockTransferGoodsSelection(state);
            }
            if ("WAIT_OUT_WAREHOUSE_CANDIDATE".equals(state.step)) {
                owner.applyWarehouseToSlot(state, warehouse, "OUT");
            } else if ("WAIT_IN_WAREHOUSE_CANDIDATE".equals(state.step)) {
                owner.applyWarehouseToSlot(state, warehouse, "IN");
            } else {
                state.setWarehouseId(warehouse.getId());
                state.setWarehouseName(warehouse.getWarehouseName());
            }
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }

        if (payload.startsWith("CUSTOMER:")) {
            String value = payload.substring("CUSTOMER:".length());
            if ("OTHER".equals(value)) {
                state.step = "WAIT_CUSTOMER_CUSTOM_FORM";
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "请在聊天框内补充客户必填信息。", owner.customerFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            }
            Long customerId = Long.valueOf(value);
            WmsCustomerRespVO customer = owner.findCustomerById(customerId);
            if (customer == null) {
                throw new ServiceException("客户不存在");
            }
            state.customerId = customer.getId();
            state.customerName = customer.getCustomerName();
            state.customerInput = customer.getCustomerName();
            state.step = "IDLE";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }

        if (payload.startsWith("LINK_ORDER:")) {
            Long linkId = Long.valueOf(payload.substring("LINK_ORDER:".length()));
            owner.applyLinkedSource(state, linkId);
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return DOMAIN_PI.equals(state.getDomainType()) ? owner.continueCreateFlow(state) : owner.continueExtendedFlow(state);
        }

        if (payload.startsWith("GOODS:")) {
            String value = payload.substring("GOODS:".length());
            if ("OTHER".equals(value)) {
                state.setStep("WAIT_GOODS_BASE_FORM");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "未找到合适的商品，已切换为聊天内新增商品。", owner.goodsFlowSupport.goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            }
            List<Long> skuIds = owner.parseSelectedSkuIds(value);
            if (skuIds.isEmpty()) {
                throw new ServiceException("商品不存在");
            }
            if ("WAIT_SC_GOODS_SELECT".equals(state.step)) {
                return owner.handleStockCheckGoodsSelect(state, skuIds);
            }
            if ("WAIT_ST_GOODS_SELECT".equals(state.step)) {
                return owner.handleStockTransferGoodsSelect(state, skuIds);
            }
            if (skuIds.size() == 1) {
                WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuIds.get(0));
                if (sku == null) {
                    throw new ServiceException("商品不存在");
                }
                owner.replacePendingGoodsBySku(state, sku);
            } else {
                owner.replacePendingGoodsBySelectedSkus(state, skuIds);
            }
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }

        return owner.conversationLifecycleSupport.buildReply(state, "未识别选择动作。", null, owner.conversationLifecycleSupport.quickActions("取消"));
    }
}

