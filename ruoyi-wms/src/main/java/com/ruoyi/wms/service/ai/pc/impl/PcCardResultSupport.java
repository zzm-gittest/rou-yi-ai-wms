package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnItemRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckItemRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferItemRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PcCardResultSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    String domainLabel(String domainType) {
        if (DOMAIN_PR.equals(domainType)) {
            return "采购退货";
        }
        if (DOMAIN_SO.equals(domainType)) {
            return "销售订单";
        }
        if (DOMAIN_SOUT.equals(domainType)) {
            return "销售出库";
        }
        if (DOMAIN_SR.equals(domainType)) {
            return "销售退货";
        }
        if (DOMAIN_SC.equals(domainType)) {
            return "库存盘点";
        }
        if (DOMAIN_ST.equals(domainType)) {
            return "库存调拨";
        }
        if (DOMAIN_PI.equals(domainType)) {
            return "采购入库";
        }
        return "采购订单";
    }

    Map<String, Object> buildSimpleResultCard(String domainType, Long id, String billNo, String partner) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "EXEC_RESULT");
        card.put("domainType", domainType);
        card.put("id", id);
        card.put("billNo", billNo);
        card.put("partner", partner);
        card.put("orderStatus", "0");
        return card;
    }

    Map<String, Object> buildPurchaseInResultCard(WmsPurchaseInRespVO resp, Long id, String supplierName) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_PI, id, resp == null ? null : resp.getInNo(), supplierName);
        if (resp == null) {
            return card;
        }
        card.put("supplierName", StringUtils.defaultIfBlank(resp.getSupplierName(), supplierName));
        card.put("partner", StringUtils.defaultIfBlank(resp.getSupplierName(), supplierName));
        card.put("warehouse", resp.getWarehouseName());
        card.put("orderDate", resp.getInDate());
        card.put("orderStatus", resp.getApproveStatus());
        card.put("totalAmount", resp.getTotalAmount());
        card.put("sourceNo", resp.getOrderNo());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsPurchaseInItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsPurchaseInItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            row.put("price", item.getPrice());
            row.put("amount", item.getAmount() == null && item.getPrice() != null && item.getQuantity() != null ? item.getPrice().multiply(item.getQuantity()) : item.getAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }

    Map<String, Object> buildPurchaseReturnResultCard(WmsPurchaseReturnRespVO resp, Long id, String supplierName) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_PR, id, resp == null ? null : resp.getReturnNo(), supplierName);
        if (resp == null) {
            return card;
        }
        card.put("supplierName", StringUtils.defaultIfBlank(resp.getSupplierName(), supplierName));
        card.put("partner", StringUtils.defaultIfBlank(resp.getSupplierName(), supplierName));
        card.put("warehouse", resp.getWarehouseName());
        card.put("orderDate", resp.getReturnDate());
        card.put("orderStatus", resp.getApproveStatus());
        card.put("totalAmount", resp.getTotalAmount());
        card.put("sourceNo", resp.getInNo());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsPurchaseReturnItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsPurchaseReturnItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            row.put("price", item.getPrice());
            row.put("amount", item.getAmount() == null && item.getPrice() != null && item.getQuantity() != null ? item.getPrice().multiply(item.getQuantity()) : item.getAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }

    Map<String, Object> buildSalesOrderResultCard(WmsSalesOrderRespVO resp, Long id, String customerName) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_SO, id, resp == null ? null : resp.getOrderNo(), customerName);
        if (resp == null) {
            return card;
        }
        card.put("customerName", StringUtils.defaultIfBlank(resp.getCustomerName(), customerName));
        card.put("partner", StringUtils.defaultIfBlank(resp.getCustomerName(), customerName));
        card.put("orderNo", resp.getOrderNo());
        card.put("orderDate", resp.getOrderDate());
        card.put("orderStatus", resp.getOrderStatus());
        card.put("totalAmount", resp.getTotalAmount());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsSalesOrderItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsSalesOrderItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            row.put("price", item.getPrice());
            row.put("amount", item.getAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }

    Map<String, Object> buildSalesOutResultCard(WmsSalesOutRespVO resp, Long id, String customerName) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_SOUT, id, resp == null ? null : resp.getOutNo(), customerName);
        if (resp == null) {
            return card;
        }
        card.put("customerName", StringUtils.defaultIfBlank(resp.getCustomerName(), customerName));
        card.put("partner", StringUtils.defaultIfBlank(resp.getCustomerName(), customerName));
        card.put("warehouse", resp.getWarehouseName());
        card.put("billNo", resp.getOutNo());
        card.put("orderDate", resp.getOutDate());
        card.put("orderStatus", resp.getApproveStatus());
        card.put("totalAmount", resp.getTotalAmount());
        card.put("sourceNo", resp.getOrderNo());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsSalesOutItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsSalesOutItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            row.put("price", item.getPrice());
            row.put("amount", item.getAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }

    Map<String, Object> buildSalesReturnResultCard(WmsSalesReturnRespVO resp, Long id, String customerName) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_SR, id, resp == null ? null : resp.getReturnNo(), customerName);
        if (resp == null) {
            return card;
        }
        card.put("customerName", StringUtils.defaultIfBlank(resp.getCustomerName(), customerName));
        card.put("partner", StringUtils.defaultIfBlank(resp.getCustomerName(), customerName));
        card.put("warehouse", resp.getWarehouseName());
        card.put("billNo", resp.getReturnNo());
        card.put("orderDate", resp.getReturnDate());
        card.put("orderStatus", resp.getApproveStatus());
        card.put("totalAmount", resp.getTotalAmount());
        card.put("sourceNo", resp.getOutNo());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsSalesReturnItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsSalesReturnItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            row.put("price", item.getPrice());
            row.put("amount", item.getAmount() == null && item.getPrice() != null && item.getQuantity() != null ? item.getPrice().multiply(item.getQuantity()) : item.getAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }

    Map<String, Object> buildStockCheckResultCard(WmsStockCheckRespVO resp, Long id, String warehouseName) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_SC, id, resp == null ? null : resp.getCheckNo(), warehouseName);
        if (resp == null) {
            return card;
        }
        card.put("warehouse", StringUtils.defaultIfBlank(resp.getWarehouseName(), warehouseName));
        card.put("partner", StringUtils.defaultIfBlank(resp.getWarehouseName(), warehouseName));
        card.put("billNo", resp.getCheckNo());
        card.put("orderDate", resp.getCheckDate());
        card.put("orderStatus", resp.getCheckStatus());
        card.put("checkType", resp.getCheckType());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsStockCheckItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsStockCheckItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("beforeQty", item.getBeforeQty());
            row.put("quantity", item.getCheckQty());
            row.put("diffQty", item.getDiffQty());
            row.put("price", item.getCostPrice());
            row.put("amount", item.getDiffAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }

    Map<String, Object> buildStockTransferResultCard(WmsStockTransferRespVO resp,
                                                      Long id,
                                                      String transferWarehousePair,
                                                      List<LineDraft> previewLines) {
        Map<String, Object> card = buildSimpleResultCard(DOMAIN_ST, id, resp == null ? null : resp.getTransferNo(), transferWarehousePair);
        if (resp == null) {
            card.put("items", buildLineFallbackItems(previewLines, false, false));
            return card;
        }
        String partner = transferWarehousePair;
        if (StringUtils.isBlank(partner)) {
            partner = StringUtils.defaultString(resp.getOutWarehouseName(), "-") + " -> " + StringUtils.defaultString(resp.getInWarehouseName(), "-");
        }
        card.put("partner", partner);
        card.put("warehouse", partner);
        card.put("billNo", resp.getTransferNo());
        card.put("orderDate", resp.getTransferDate());
        card.put("orderStatus", resp.getTransferStatus());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsStockTransferItemRespVO> respItems = resp.getItems() == null ? new ArrayList<>() : resp.getItems();
        for (WmsStockTransferItemRespVO item : respItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            items.add(row);
        }
        if (items.isEmpty()) {
            items = buildLineFallbackItems(previewLines, false, false);
        }
        card.put("items", items);
        return card;
    }

    List<Map<String, Object>> buildLineFallbackItems(List<LineDraft> lines, boolean includeBeforeQty) {
        return buildLineFallbackItems(lines, includeBeforeQty, true);
    }

    List<Map<String, Object>> buildLineFallbackItems(List<LineDraft> lines, boolean includeBeforeQty, boolean includePriceAndAmount) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (lines == null) {
            return items;
        }
        for (LineDraft line : lines) {
            if (line == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("goods", line.goodsName);
            row.put("sku", line.skuName);
            row.put("unit", line.unitName);
            row.put("quantity", line.quantity);
            if (includePriceAndAmount) {
                row.put("price", line.price);
                row.put("amount", line.price == null || line.quantity == null ? null : line.price.multiply(line.quantity));
            }
            if (includeBeforeQty) {
                row.put("beforeQty", line.beforeQty);
            }
            items.add(row);
        }
        return items;
    }

    Map<String, Object> buildOrderResultCard(WmsPurchaseOrderRespVO order, Long id) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "EXEC_RESULT");
        card.put("domainType", DOMAIN_PO);
        card.put("id", id);
        if (order == null) {
            return card;
        }
        card.put("orderNo", order.getOrderNo());
        card.put("supplierName", order.getSupplierName());
        card.put("orderDate", order.getOrderDate());
        card.put("orderStatus", order.getOrderStatus());
        card.put("totalAmount", order.getTotalAmount());
        List<Map<String, Object>> items = new ArrayList<>();
        List<WmsPurchaseOrderItemRespVO> orderItems = order.getItems() == null ? new ArrayList<WmsPurchaseOrderItemRespVO>() : order.getItems();
        for (WmsPurchaseOrderItemRespVO item : orderItems) {
            Map<String, Object> row = new HashMap<>();
            row.put("goods", item.getGoodsName());
            row.put("sku", item.getSkuName());
            row.put("unit", item.getUnitName());
            row.put("quantity", item.getQuantity());
            row.put("price", item.getPrice());
            row.put("amount", item.getAmount());
            items.add(row);
        }
        card.put("items", items);
        return card;
    }
}
