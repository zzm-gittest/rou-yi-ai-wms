package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PcPurchaseBillCreateProcessor {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";

    private final PcMultiBillChatServiceImpl owner;

    PcPurchaseBillCreateProcessor(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> execute(PcState state) {
        if (DOMAIN_PO.equals(state.getDomainType())) {
            return executeCreatePurchaseOrder(state);
        }
        if (DOMAIN_PI.equals(state.getDomainType())) {
            return executeCreatePurchaseIn(state);
        }
        if (DOMAIN_PR.equals(state.getDomainType())) {
            return executeCreatePurchaseReturn(state);
        }
        throw new ServiceException("未识别采购域单据类型");
    }

    private Map<String, Object> executeCreatePurchaseOrder(PcState state) {
        if (state.getSupplierId() == null || state.getPreparedPoItems() == null || state.getPreparedPoItems().isEmpty()) {
            return owner.continueCreateFlow(state);
        }
        boolean invalidDirectPoItem = state.getPreparedPoItems().stream().anyMatch(item -> item.getQuantity() == null
                || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0
                || item.getPrice() == null
                || item.getPrice().compareTo(BigDecimal.ZERO) <= 0);
        if (invalidDirectPoItem) {
            return owner.continueCreateFlow(state);
        }
        WmsPurchaseOrderSaveReqVO req = new WmsPurchaseOrderSaveReqVO();
        req.setSupplierId(state.getSupplierId());
        req.setOrderDate(LocalDate.now());
        req.setDeliveryDate(LocalDate.now().plusDays(3));
        req.setRemark(StringUtils.isBlank(state.getReason()) ? null : state.getReason().trim());
        req.setItems(state.getPreparedPoItems());
        Long id = owner.purchaseOrderService.create(req);
        WmsPurchaseOrderRespVO order = owner.purchaseOrderService.get(id);

        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(order == null ? null : order.getOrderNo());
        state.setLastCreatedDomainType(DOMAIN_PO);

        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                order == null ? "采购订单创建成功，ID=" + id : "采购订单创建成功！单号：" + order.getOrderNo(),
                owner.cardResultSupport.buildOrderResultCard(order, id),
                owner.conversationLifecycleSupport.quickActions("查看刚创建的订单", "再来一单采购"));
    }

    private Map<String, Object> executeCreatePurchaseIn(PcState state) {
        String supplierName = state.getSupplierName();
        if (state.getSupplierId() == null || state.getWarehouseId() == null) {
            return owner.continueCreateFlow(state);
        }
        List<WmsPurchaseInItemSaveReqVO> createItems = owner.hasLinkedSource(state)
            ? owner.buildPurchaseInItemsFromLines(state.getPreparedLines())
                : state.getPreparedPiItems();
        if (createItems == null || createItems.isEmpty()) {
            return owner.continueCreateFlow(state);
        }
        if (!owner.hasLinkedSource(state)) {
            boolean invalidDirectPiItem = createItems.stream().anyMatch(item -> item.getQuantity() == null
                    || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0
                    || item.getPrice() == null
                    || item.getPrice().compareTo(BigDecimal.ZERO) <= 0);
            if (invalidDirectPiItem) {
                return owner.continueCreateFlow(state);
            }
        }
        WmsPurchaseInSaveReqVO req = new WmsPurchaseInSaveReqVO();
        req.setSourceType(owner.hasLinkedSource(state) ? 2 : 1);
        req.setOrderId(DOMAIN_PO.equals(state.getLinkOrderType()) ? state.getLinkOrderId() : null);
        req.setSupplierId(state.getSupplierId());
        req.setWarehouseId(state.getWarehouseId());
        req.setInDate(LocalDate.now());
        req.setItems(createItems);
        Long id = owner.purchaseInService.create(req);
        WmsPurchaseInRespVO purchaseIn = owner.purchaseInService.get(id);

        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(purchaseIn == null ? null : purchaseIn.getInNo());
        state.setLastCreatedDomainType(DOMAIN_PI);

        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                purchaseIn == null ? "采购入库创建成功，ID=" + id : "采购入库创建成功，单号：" + purchaseIn.getInNo(),
                owner.cardResultSupport.buildPurchaseInResultCard(purchaseIn, id, supplierName),
                owner.conversationLifecycleSupport.quickActions("查看刚创建的订单", "再来一单采购入库"));
    }

    private Map<String, Object> executeCreatePurchaseReturn(PcState state) {
        if (state.getSupplierId() == null) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "采购退货必须选择供应商，请在卡片中选择供应商后点击确认。",
                    owner.buildExtendedPreviewCard(state),
                    owner.confirmCancelQuickActions());
        }
        String supplierName = state.getSupplierName();
        List<LineDraft> previewLines = state.getPreparedLines() == null
            ? new ArrayList<>() : new ArrayList<>(state.getPreparedLines());
        owner.log().info("=== 创建采购退货 previewLines: {}, preparedLines: {}", previewLines.size(), state.getPreparedLines() == null ? 0 : state.getPreparedLines().size());
        owner.log().info("=== 创建采购退货 state.reason: {}", state.getReason());
        WmsPurchaseReturnSaveReqVO req = new WmsPurchaseReturnSaveReqVO();
        req.setSourceType(owner.hasLinkedSource(state) ? 2 : 1);
        req.setInId(DOMAIN_PI.equals(state.getLinkOrderType()) ? state.getLinkOrderId() : null);
        req.setSupplierId(state.getSupplierId());
        req.setWarehouseId(state.getWarehouseId());
        req.setReturnDate(LocalDate.now());
        String remark = StringUtils.defaultIfBlank(state.getReason(), "PC-AI助手语义建单");
        owner.log().info("=== 创建采购退货 最终 remark: {}", remark);
        req.setRemark(remark);
        List<WmsPurchaseReturnItemSaveReqVO> returnItems = owner.buildPurchaseReturnItemsFromLines(state.getPreparedLines());
        if ((returnItems == null || returnItems.isEmpty())
                && state.getPreparedPiItems() != null && !state.getPreparedPiItems().isEmpty()) {
            returnItems = new ArrayList<>();
            for (WmsPurchaseInItemSaveReqVO item : state.getPreparedPiItems()) {
                WmsPurchaseReturnItemSaveReqVO returnItem = new WmsPurchaseReturnItemSaveReqVO();
                returnItem.setSkuId(item.getSkuId());
                returnItem.setQuantity(item.getQuantity());
                returnItem.setPrice(item.getPrice());
                returnItems.add(returnItem);
            }
        }
        owner.log().info("=== 创建采购退货 returnItems: {}", returnItems.size());
        req.setItems(returnItems);
        Long id = owner.purchaseReturnService.create(req);
        WmsPurchaseReturnRespVO resp = owner.purchaseReturnService.get(id);
        owner.log().info("=== 创建采购退货 resp items: {}", resp.getItems() == null ? 0 : resp.getItems().size());
        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(resp == null ? null : resp.getReturnNo());
        state.setLastCreatedDomainType(DOMAIN_PR);
        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        Map<String, Object> card = owner.cardResultSupport.buildPurchaseReturnResultCard(resp, id, supplierName);
        List<?> items = (List<?>) card.get("items");
        if (items == null || items.isEmpty()) {
            owner.log().info("=== 创建采购退货 fallback 使用 previewLines: {}", previewLines.size());
            card.put("items", owner.cardResultSupport.buildLineFallbackItems(previewLines, false));
        }
        return owner.conversationLifecycleSupport.buildReply(state,
                resp == null ? "采购退货创建成功，ID=" + id : "采购退货创建成功，单号：" + resp.getReturnNo(),
                card,
                owner.conversationLifecycleSupport.quickActions("再来一单采购退货", "查看刚创建的订单"));
    }
}
