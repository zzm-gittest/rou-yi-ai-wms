package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnItemSaveReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckItemSaveReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferItemSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PcPreviewItemSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";

    private final PcMultiBillChatServiceImpl owner;

    PcPreviewItemSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> buildDirectPurchasePreviewCard(PcState state, List<LineDraft> lines) {
        List<Map<String, Object>> previewRows = buildDirectPurchasePreviewRows(lines);
        Map<String, Object> card = new HashMap<>();
        card.put("type", "DIFF_PREVIEW");
        card.put("title", DOMAIN_PO.equals(state.getDomainType()) ? "采购订单创建预览"
                : (DOMAIN_PR.equals(state.getDomainType()) ? "采购退货创建预览" : "采购入库确认信息"));
        card.put("domainType", state.getDomainType());
        card.put("supplier", state.getSupplierName());
        card.put("supplierId", state.getSupplierId());
        if (StringUtils.isNotBlank(state.getWarehouseName())) {
            card.put("warehouse", state.getWarehouseName());
        }
        card.put("items", previewRows);
        if (DOMAIN_PO.equals(state.getDomainType())) {
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("allowSupplierEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("supplierId", state.getSupplierId());
            formModel.put("items", previewRows.stream().map(HashMap::new).collect(Collectors.toList()));
            card.put("formModel", formModel);
        } else if (DOMAIN_PI.equals(state.getDomainType())) {
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("allowSupplierEdit", true);
            card.put("allowWarehouseEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("supplierId", state.getSupplierId());
            formModel.put("warehouseId", state.getWarehouseId());
            formModel.put("items", previewRows.stream().map(HashMap::new).collect(Collectors.toList()));
            card.put("formModel", formModel);
        } else if (DOMAIN_PR.equals(state.getDomainType())) {
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("priceRequired", false);
            card.put("allowSupplierEdit", true);
            card.put("allowWarehouseEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("supplierId", state.getSupplierId());
            formModel.put("warehouseId", state.getWarehouseId());
            formModel.put("items", previewRows.stream().map(HashMap::new).collect(Collectors.toList()));
            card.put("formModel", formModel);
        }
        return card;
    }

    Map<String, Object> buildExtendedPreviewCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "DIFF_PREVIEW");
        card.put("domainType", state.getDomainType());
        card.put("title", DOMAIN_PI.equals(state.getDomainType())
            ? "采购入库确认信息"
            : owner.cardResultSupport.domainLabel(state.getDomainType()) + "创建预览");
        card.put("linkedMode", owner.hasLinkedSource(state));
        if (StringUtils.isNotBlank(state.getSupplierName())) {
            card.put("supplier", state.getSupplierName());
        }
        if (StringUtils.isNotBlank(state.customerName)) {
            card.put("customer", state.customerName);
        }
        if (StringUtils.isNotBlank(state.getWarehouseName())) {
            card.put("warehouse", state.getWarehouseName());
        }
        if (StringUtils.isNotBlank(state.outWarehouseName)) {
            card.put("outWarehouse", state.outWarehouseName);
        }
        if (StringUtils.isNotBlank(state.inWarehouseName)) {
            card.put("inWarehouse", state.inWarehouseName);
        }
        if (StringUtils.isNotBlank(state.checkType)) {
            card.put("checkType", state.checkType);
        }
        if (StringUtils.isNotBlank(state.reason)) {
            card.put("remark", state.reason);
        }
        if (StringUtils.isNotBlank(state.linkOrderNo)) {
            card.put("linkOrderNo", state.linkOrderNo);
        }
        if (StringUtils.isNotBlank(state.linkOrderType)) {
            card.put("linkOrderType", state.linkOrderType);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> formItems = new ArrayList<>();
        if (state.preparedLines != null) {
            for (LineDraft line : state.preparedLines) {
                Map<String, Object> row = buildLineRow(line);
                items.add(row);
                formItems.add(new HashMap<>(row));
            }
        }
        card.put("items", items);
        if (owner.hasLinkedSource(state) || DOMAIN_SC.equals(state.getDomainType())) {
            card.put("editable", true);
            Map<String, Object> formModel = new HashMap<>();
            if (DOMAIN_PI.equals(state.getDomainType())) {
                card.put("allowWarehouseEdit", true);
                formModel.put("warehouseId", state.getWarehouseId());
            }
            if ("SALES_OUT".equals(state.getDomainType())) {
                // 关联出库：允许编辑仓库和单价，客户只读
                card.put("allowWarehouseEdit", true);
                card.put("allowPriceEdit", true);
                formModel.put("warehouseId", state.getWarehouseId());
            }
            if ("SALES_RETURN".equals(state.getDomainType())) {
                // 关联退货：允许编辑仓库和备注，客户只读
                card.put("allowWarehouseEdit", true);
                card.put("allowRemarkEdit", true);
                formModel.put("warehouseId", state.getWarehouseId());
                formModel.put("remark", state.reason);
            }
            formModel.put("items", formItems);
            card.put("formModel", formModel);
        } else if (DOMAIN_PR.equals(state.getDomainType())) {
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("priceRequired", false);
            card.put("allowSupplierEdit", true);
            card.put("allowWarehouseEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("supplierId", state.getSupplierId());
            formModel.put("warehouseId", state.getWarehouseId());
            formModel.put("items", formItems);
            card.put("formModel", formModel);
        } else if ("SALES_ORDER".equals(state.getDomainType())) {
            // 销售订单：支持在确认卡片上编辑单价和选择客户
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("priceRequired", true);
            card.put("allowCustomerEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("customerId", state.customerId);
            formModel.put("items", formItems);
            card.put("formModel", formModel);
        } else if ("SALES_OUT".equals(state.getDomainType())) {
            // 销售出库 直接模式：允许编辑客户、仓库、单价
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("priceRequired", false);
            card.put("allowCustomerEdit", true);
            card.put("allowWarehouseEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("customerId", state.customerId);
            formModel.put("warehouseId", state.getWarehouseId());
            formModel.put("items", formItems);
            card.put("formModel", formModel);
        } else if ("SALES_RETURN".equals(state.getDomainType()) && !owner.hasLinkedSource(state)) {
            // 销售退货 直接模式：允许编辑客户、仓库、单价（可选）、备注
            card.put("editable", true);
            card.put("allowPriceEdit", true);
            card.put("priceRequired", false);
            card.put("allowCustomerEdit", true);
            card.put("allowWarehouseEdit", true);
            card.put("allowRemarkEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("customerId", state.customerId);
            formModel.put("warehouseId", state.getWarehouseId());
            formModel.put("remark", state.reason);
            formModel.put("items", formItems);
            card.put("formModel", formModel);
        } else if ("STOCK_TRANSFER".equals(state.getDomainType())) {
            // 库存调拨：允许编辑调入仓库和调拨数量
            card.put("editable", true);
            card.put("allowInWarehouseEdit", true);
            Map<String, Object> formModel = new HashMap<>();
            formModel.put("outWarehouseId", state.outWarehouseId);
            formModel.put("inWarehouseId", state.inWarehouseId);
            formModel.put("items", formItems);
            card.put("formModel", formModel);
        }
        return card;
    }

    List<WmsPurchaseOrderItemSaveReqVO> buildPurchaseOrderItemsFromLines(List<LineDraft> lines) {
        List<WmsPurchaseOrderItemSaveReqVO> items = new ArrayList<>();
        if (lines == null) {
            return items;
        }
        for (LineDraft line : lines) {
            if (line == null) {
                continue;
            }
            WmsPurchaseOrderItemSaveReqVO item = new WmsPurchaseOrderItemSaveReqVO();
            item.setSkuId(line.getSkuId());
            item.setUnit(line.getUnitName());
            item.setQuantity(line.getQuantity());
            item.setPrice(line.getPrice());
            item.setDiscount(BigDecimal.ZERO);
            items.add(item);
        }
        return items;
    }

    List<WmsPurchaseReturnItemSaveReqVO> buildPurchaseReturnItems(PcState state) {
        List<WmsPurchaseReturnItemSaveReqVO> items = new ArrayList<>();
        for (LineDraft line : state.preparedLines) {
            WmsPurchaseReturnItemSaveReqVO item = new WmsPurchaseReturnItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            item.setPrice(line.price);
            items.add(item);
        }
        return items;
    }

    List<WmsPurchaseReturnItemSaveReqVO> buildPurchaseReturnItemsFromLines(List<LineDraft> lines) {
        List<WmsPurchaseReturnItemSaveReqVO> items = new ArrayList<>();
        if (lines == null) {
            return items;
        }
        for (LineDraft line : lines) {
            if (line == null) {
                continue;
            }
            WmsPurchaseReturnItemSaveReqVO item = new WmsPurchaseReturnItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            item.setPrice(line.price);
            items.add(item);
        }
        return items;
    }

    List<WmsPurchaseInItemSaveReqVO> buildPurchaseInItemsFromLines(List<LineDraft> lines) {
        List<WmsPurchaseInItemSaveReqVO> items = new ArrayList<>();
        if (lines == null) {
            return items;
        }
        for (LineDraft line : lines) {
            WmsPurchaseInItemSaveReqVO item = new WmsPurchaseInItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            item.setPrice(line.price);
            item.setRemark(line.remark);
            items.add(item);
        }
        return items;
    }

    List<WmsSalesOrderItemSaveReqVO> buildSalesOrderItems(PcState state) {
        List<WmsSalesOrderItemSaveReqVO> items = new ArrayList<>();
        for (LineDraft line : state.preparedLines) {
            WmsSalesOrderItemSaveReqVO item = new WmsSalesOrderItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            item.setPrice(line.price);
            item.setDiscount(BigDecimal.ZERO);
            items.add(item);
        }
        return items;
    }

    List<WmsSalesOutItemSaveReqVO> buildSalesOutItems(PcState state) {
        List<WmsSalesOutItemSaveReqVO> items = new ArrayList<>();
        for (LineDraft line : state.preparedLines) {
            WmsSalesOutItemSaveReqVO item = new WmsSalesOutItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            item.setPrice(line.price);
            items.add(item);
        }
        return items;
    }

    List<WmsSalesReturnItemSaveReqVO> buildSalesReturnItems(PcState state) {
        List<WmsSalesReturnItemSaveReqVO> items = new ArrayList<>();
        for (LineDraft line : state.preparedLines) {
            WmsSalesReturnItemSaveReqVO item = new WmsSalesReturnItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            item.setPrice(line.price);
            items.add(item);
        }
        return items;
    }

    List<WmsStockCheckItemSaveReqVO> buildStockCheckItems(PcState state) {
        List<WmsStockCheckItemSaveReqVO> items = new ArrayList<>();
        for (LineDraft line : state.preparedLines) {
            WmsStockCheckItemSaveReqVO item = new WmsStockCheckItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setBeforeQty(line.beforeQty);
            item.setCheckQty(line.quantity);
            item.setCostPrice(line.price);
            items.add(item);
        }
        return items;
    }

    List<WmsStockTransferItemSaveReqVO> buildStockTransferItems(PcState state) {
        List<WmsStockTransferItemSaveReqVO> items = new ArrayList<>();
        for (LineDraft line : state.preparedLines) {
            WmsStockTransferItemSaveReqVO item = new WmsStockTransferItemSaveReqVO();
            item.setSkuId(line.skuId);
            item.setQuantity(line.quantity);
            items.add(item);
        }
        return items;
    }

    private List<Map<String, Object>> buildDirectPurchasePreviewRows(List<LineDraft> lines) {
        List<Map<String, Object>> previewRows = new ArrayList<>();
        if (lines == null) {
            return previewRows;
        }
        for (LineDraft line : lines) {
            if (line == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("skuId", line.getSkuId());
            row.put("goods", line.getGoodsName());
            row.put("sku", line.getSkuName());
            row.put("unit", line.getUnitName());
            row.put("quantity", line.getQuantity());
            row.put("price", line.getPrice());
            row.put("amount", line.getPrice() == null || line.getQuantity() == null ? null : line.getQuantity().multiply(line.getPrice()));
            previewRows.add(row);
        }
        return previewRows;
    }

    private Map<String, Object> buildLineRow(LineDraft line) {
        Map<String, Object> row = new HashMap<>();
        row.put("sourceLineId", line.sourceLineId);
        row.put("skuId", line.skuId);
        row.put("goods", line.goodsName);
        row.put("sku", line.skuName);
        row.put("unit", line.unitName);
        row.put("orderQuantity", line.orderQuantity);
        row.put("receivedQty", line.receivedQty);
        row.put("quantity", line.quantity);
        row.put("price", line.price);
        row.put("beforeQty", line.beforeQty);
        row.put("diffQty", line.beforeQty == null || line.quantity == null ? null : line.quantity.subtract(line.beforeQty));
        row.put("maxQty", line.maxQty);
        row.put("amount", line.price == null || line.quantity == null ? null : line.price.multiply(line.quantity));
        return row;
    }
}
