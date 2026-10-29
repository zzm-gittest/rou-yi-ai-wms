package com.ruoyi.wms.service.ai.query;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.integration.BillDetailLineVO;
import com.ruoyi.wms.domain.vo.integration.BillDetailVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.service.WmsPurchaseInService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsPurchaseReturnService;
import com.ruoyi.wms.service.WmsSalesOrderService;
import com.ruoyi.wms.service.WmsSalesOutService;
import com.ruoyi.wms.service.WmsSalesReturnService;
import com.ruoyi.wms.service.WmsStockCheckService;
import com.ruoyi.wms.service.WmsStockTransferService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 单据详情（只读，供 IM 飞书卡片展示；逻辑对齐 PC 结果卡，独立包不修改 PC 代码）
 */
@Service
public class UnifiedBillDetailService {

    private final WmsPurchaseOrderService purchaseOrderService;
    private final WmsPurchaseInService purchaseInService;
    private final WmsPurchaseReturnService purchaseReturnService;
    private final WmsSalesOrderService salesOrderService;
    private final WmsSalesOutService salesOutService;
    private final WmsSalesReturnService salesReturnService;
    private final WmsStockCheckService stockCheckService;
    private final WmsStockTransferService stockTransferService;

    public UnifiedBillDetailService(WmsPurchaseOrderService purchaseOrderService,
                                    WmsPurchaseInService purchaseInService,
                                    WmsPurchaseReturnService purchaseReturnService,
                                    WmsSalesOrderService salesOrderService,
                                    WmsSalesOutService salesOutService,
                                    WmsSalesReturnService salesReturnService,
                                    WmsStockCheckService stockCheckService,
                                    WmsStockTransferService stockTransferService) {
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseInService = purchaseInService;
        this.purchaseReturnService = purchaseReturnService;
        this.salesOrderService = salesOrderService;
        this.salesOutService = salesOutService;
        this.salesReturnService = salesReturnService;
        this.stockCheckService = stockCheckService;
        this.stockTransferService = stockTransferService;
    }

    public BillDetailVO getDetail(String domainType, Long id) {
        if (StringUtils.isBlank(domainType) || id == null || id <= 0) {
            return null;
        }
        return switch (domainType) {
            case BillDomainCodes.PO -> fromPurchaseOrder(purchaseOrderService.get(id));
            case BillDomainCodes.PI -> fromPurchaseIn(purchaseInService.get(id));
            case BillDomainCodes.PR -> fromPurchaseReturn(purchaseReturnService.get(id));
            case BillDomainCodes.SO -> fromSalesOrder(salesOrderService.get(id));
            case BillDomainCodes.SOUT -> fromSalesOut(salesOutService.get(id));
            case BillDomainCodes.SR -> fromSalesReturn(salesReturnService.get(id));
            case BillDomainCodes.SC -> fromStockCheck(stockCheckService.get(id));
            case BillDomainCodes.ST -> fromStockTransfer(stockTransferService.get(id));
            default -> null;
        };
    }

    private BillDetailVO fromPurchaseOrder(WmsPurchaseOrderRespVO order) {
        if (order == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.PO, order.getId(), order.getOrderNo(),
            order.getSupplierName(), null, order.getOrderStatus(),
            order.getOrderDate() == null ? null : order.getOrderDate().toString(), order.getTotalAmount());
        List<WmsPurchaseOrderItemRespVO> items = order.getItems() == null ? List.of() : order.getItems();
        for (WmsPurchaseOrderItemRespVO item : items) {
            vo.getLines().add(line(item.getGoodsName(), item.getSkuName(), item.getUnitName(),
                item.getQuantity(), item.getPrice(), item.getAmount()));
        }
        return vo;
    }

    private BillDetailVO fromPurchaseIn(WmsPurchaseInRespVO resp) {
        if (resp == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.PI, resp.getId(), resp.getInNo(),
            resp.getSupplierName(), resp.getWarehouseName(), resp.getApproveStatus(),
            resp.getInDate() == null ? null : resp.getInDate().toString(), resp.getTotalAmount());
        if (resp.getItems() != null) {
            resp.getItems().forEach(item -> vo.getLines().add(line(item.getGoodsName(), item.getSkuName(),
                item.getUnitName(), item.getQuantity(), item.getPrice(), item.getAmount())));
        }
        return vo;
    }

    private BillDetailVO fromPurchaseReturn(WmsPurchaseReturnRespVO resp) {
        if (resp == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.PR, resp.getId(), resp.getReturnNo(),
            resp.getSupplierName(), resp.getWarehouseName(), resp.getApproveStatus(),
            resp.getReturnDate() == null ? null : resp.getReturnDate().toString(), resp.getTotalAmount());
        if (resp.getItems() != null) {
            resp.getItems().forEach(item -> vo.getLines().add(line(item.getGoodsName(), item.getSkuName(),
                item.getUnitName(), item.getQuantity(), item.getPrice(), item.getAmount())));
        }
        return vo;
    }

    private BillDetailVO fromSalesOrder(WmsSalesOrderRespVO resp) {
        if (resp == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.SO, resp.getId(), resp.getOrderNo(),
            resp.getCustomerName(), null, resp.getOrderStatus(),
            resp.getOrderDate() == null ? null : resp.getOrderDate().toString(), resp.getTotalAmount());
        if (resp.getItems() != null) {
            resp.getItems().forEach(item -> vo.getLines().add(line(item.getGoodsName(), item.getSkuName(),
                item.getUnitName(), item.getQuantity(), item.getPrice(), item.getAmount())));
        }
        return vo;
    }

    private BillDetailVO fromSalesOut(WmsSalesOutRespVO resp) {
        if (resp == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.SOUT, resp.getId(), resp.getOutNo(),
            resp.getCustomerName(), resp.getWarehouseName(), resp.getApproveStatus(),
            resp.getOutDate() == null ? null : resp.getOutDate().toString(), resp.getTotalAmount());
        if (resp.getItems() != null) {
            for (WmsSalesOutItemRespVO item : resp.getItems()) {
                vo.getLines().add(line(item.getGoodsName(), item.getSkuName(), item.getUnitName(),
                    item.getQuantity(), item.getPrice(), item.getAmount()));
            }
        }
        return vo;
    }

    private BillDetailVO fromSalesReturn(WmsSalesReturnRespVO resp) {
        if (resp == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.SR, resp.getId(), resp.getReturnNo(),
            resp.getCustomerName(), resp.getWarehouseName(), resp.getApproveStatus(),
            resp.getReturnDate() == null ? null : resp.getReturnDate().toString(), resp.getTotalAmount());
        if (resp.getItems() != null) {
            resp.getItems().forEach(item -> {
                BigDecimal amount = item.getAmount();
                if (amount == null && item.getPrice() != null && item.getQuantity() != null) {
                    amount = item.getPrice().multiply(item.getQuantity());
                }
                vo.getLines().add(line(item.getGoodsName(), item.getSkuName(), item.getUnitName(),
                    item.getQuantity(), item.getPrice(), amount));
            });
        }
        return vo;
    }

    private BillDetailVO fromStockCheck(WmsStockCheckRespVO resp) {
        if (resp == null) {
            return null;
        }
        BillDetailVO vo = header(BillDomainCodes.SC, resp.getId(), resp.getCheckNo(),
            resp.getWarehouseName(), resp.getWarehouseName(), resp.getCheckStatus(),
            resp.getCheckDate() == null ? null : resp.getCheckDate().toString(), null);
        if (resp.getItems() != null) {
            resp.getItems().forEach(item -> vo.getLines().add(line(item.getGoodsName(), item.getSkuName(),
                item.getUnitName(), item.getCheckQty(), item.getCostPrice(), item.getDiffAmount())));
        }
        return vo;
    }

    private BillDetailVO fromStockTransfer(WmsStockTransferRespVO resp) {
        if (resp == null) {
            return null;
        }
        String partner = StringUtils.defaultString(resp.getOutWarehouseName(), "-")
            + " → " + StringUtils.defaultString(resp.getInWarehouseName(), "-");
        BillDetailVO vo = header(BillDomainCodes.ST, resp.getId(), resp.getTransferNo(),
            partner, partner, resp.getTransferStatus(),
            resp.getTransferDate() == null ? null : resp.getTransferDate().toString(), null);
        if (resp.getItems() != null) {
            resp.getItems().forEach(item -> vo.getLines().add(line(item.getGoodsName(), item.getSkuName(),
                item.getUnitName(), item.getQuantity(), null, null)));
        }
        return vo;
    }

    private static BillDetailVO header(String domainType, Long id, String billNo, String partner, String warehouse,
                                       String status, String bizDate, BigDecimal totalAmount) {
        BillDetailVO vo = new BillDetailVO();
        vo.setDomainType(domainType);
        vo.setDomainTypeName(BillDomainCodes.toDisplayName(domainType));
        vo.setId(id);
        vo.setBillNo(billNo);
        vo.setPartnerName(partner);
        vo.setWarehouseName(warehouse);
        vo.setStatus(status);
        vo.setStatusLabel(BillDomainCodes.toStatusLabel(status));
        vo.setBizDate(bizDate);
        vo.setTotalAmount(totalAmount);
        return vo;
    }

    private static BillDetailLineVO line(String goods, String sku, String unit,
                                         BigDecimal qty, BigDecimal price, BigDecimal amount) {
        BillDetailLineVO line = new BillDetailLineVO();
        line.setGoodsName(goods);
        line.setSkuName(sku);
        line.setUnitName(unit);
        line.setQuantity(qty);
        line.setPrice(price);
        line.setAmount(amount);
        return line;
    }
}
