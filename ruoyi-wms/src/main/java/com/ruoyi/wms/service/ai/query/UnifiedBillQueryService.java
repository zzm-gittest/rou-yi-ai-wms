package com.ruoyi.wms.service.ai.query;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.integration.BillQueryResultVO;
import com.ruoyi.wms.domain.vo.integration.BillQueryRowVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderPageReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutPageReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnPageReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckPageReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferPageReqVO;
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 跨 8 类单据统一查询（IM/开放接口专用，与 PC {@code PcQueryFlowSupport} 逻辑对齐但独立实现）
 */
@Service
public class UnifiedBillQueryService {

    private static final int PAGE_SIZE_PER_DOMAIN = 20;
    private static final int MAX_POOL = 50;

    private final WmsPurchaseOrderService purchaseOrderService;
    private final WmsPurchaseInService purchaseInService;
    private final WmsPurchaseReturnService purchaseReturnService;
    private final WmsSalesOrderService salesOrderService;
    private final WmsSalesOutService salesOutService;
    private final WmsSalesReturnService salesReturnService;
    private final WmsStockCheckService stockCheckService;
    private final WmsStockTransferService stockTransferService;

    public UnifiedBillQueryService(WmsPurchaseOrderService purchaseOrderService,
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

    public BillQueryResultVO queryByMessage(String message, Integer maxRows, Integer pageNo, Integer pageSize) {
        BillQueryIntent intent = BillQueryIntentParser.parse(message);
        List<BillRowInternal> pool = queryAllBills(intent);
        if (intent.isExactBillNoOnly() && !pool.isEmpty()) {
            pool = pool.stream().limit(1).collect(Collectors.toList());
        }
        int total = pool.size();
        boolean paged = !intent.isExactBillNoOnly()
            && pageNo != null && pageNo > 0 && pageSize != null && pageSize > 0;
        List<BillQueryRowVO> rows;
        BillQueryResultVO result = new BillQueryResultVO();
        result.setTemplates(UnifiedBillQueryTemplateProvider.defaultTemplates());
        result.setTotalMatched(total);

        if (paged) {
            int size = normalizePageSize(pageSize);
            int page = Math.max(1, pageNo);
            int totalPages = total == 0 ? 0 : (total + size - 1) / size;
            if (page > totalPages && totalPages > 0) {
                page = totalPages;
            }
            int from = (page - 1) * size;
            rows = pool.stream().skip(from).limit(size).map(this::toVo).collect(Collectors.toList());
            result.setPageNo(page);
            result.setPageSize(size);
            result.setTotalPages(totalPages);
            result.setReturned(rows.size());
            result.setRows(rows);
            if (total == 0) {
                result.setSummary(summarizeEmpty(intent));
            } else {
                result.setSummary("共 " + total + " 条，第 " + page + "/" + totalPages + " 页");
            }
        } else {
            int limit = intent.isExactBillNoOnly() ? 1 : normalizeLimit(maxRows);
            rows = pool.stream().limit(limit).map(this::toVo).collect(Collectors.toList());
            result.setReturned(rows.size());
            result.setRows(rows);
            if (total == 0) {
                result.setSummary(summarizeEmpty(intent));
            } else if (intent.isExactBillNoOnly()) {
                result.setSummary("已匹配单号 " + intent.getBillNo());
            } else if (total > rows.size()) {
                result.setSummary("共找到 " + total + " 条，以下展示前 " + rows.size() + " 条。");
            } else {
                result.setSummary("共找到 " + total + " 条单据。");
            }
        }
        return result;
    }

    private int normalizeLimit(Integer maxRows) {
        if (maxRows == null || maxRows <= 0) {
            return 5;
        }
        return Math.min(maxRows, 20);
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return 3;
        }
        return Math.min(pageSize, 10);
    }

    private String summarizeEmpty(BillQueryIntent intent) {
        if (intent.isExactBillNoOnly() && StringUtils.isNotBlank(intent.getBillNo())) {
            return "未找到单号 " + intent.getBillNo() + "，请提供完整订单号（如 CK202605210002）。";
        }
        return "未找到匹配的单据，可尝试换关键词或发「帮助」查看查询示例。";
    }

    private List<BillRowInternal> queryAllBills(BillQueryIntent intent) {
        String exactBillNo = intent.isExactBillNoOnly() ? intent.getBillNo() : null;
        List<BillRowInternal> rows = new ArrayList<>();
        if (intent.getDomainTypes().contains(BillDomainCodes.PO)) {
            WmsPurchaseOrderPageReqVO req = new WmsPurchaseOrderPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setOrderNo(exactBillNo);
            }
            IPage<WmsPurchaseOrderRespVO> page = purchaseOrderService.page(req);
            for (WmsPurchaseOrderRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.PO, vo.getId(), vo.getOrderNo(), vo.getSupplierName(), null,
                    vo.getOrderStatus(), vo.getOrderDate() == null ? null : vo.getOrderDate().toString(),
                    vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.PI)) {
            WmsPurchaseInPageReqVO req = new WmsPurchaseInPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setInNo(exactBillNo);
            }
            IPage<WmsPurchaseInRespVO> page = purchaseInService.page(req);
            for (WmsPurchaseInRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.PI, vo.getId(), vo.getInNo(), vo.getSupplierName(), vo.getWarehouseName(),
                    vo.getApproveStatus(), vo.getInDate() == null ? null : vo.getInDate().toString(),
                    vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.PR)) {
            WmsPurchaseReturnPageReqVO req = new WmsPurchaseReturnPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setReturnNo(exactBillNo);
            }
            IPage<WmsPurchaseReturnRespVO> page = purchaseReturnService.page(req);
            for (WmsPurchaseReturnRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.PR, vo.getId(), vo.getReturnNo(), vo.getSupplierName(), vo.getWarehouseName(),
                    vo.getApproveStatus(), vo.getReturnDate() == null ? null : vo.getReturnDate().toString(),
                    vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.SO)) {
            WmsSalesOrderPageReqVO req = new WmsSalesOrderPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setOrderNo(exactBillNo);
            }
            IPage<WmsSalesOrderRespVO> page = salesOrderService.page(req);
            for (WmsSalesOrderRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.SO, vo.getId(), vo.getOrderNo(), vo.getCustomerName(), null,
                    vo.getOrderStatus(), vo.getOrderDate() == null ? null : vo.getOrderDate().toString(),
                    vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.SOUT)) {
            WmsSalesOutPageReqVO req = new WmsSalesOutPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setOutNo(exactBillNo);
            }
            IPage<WmsSalesOutRespVO> page = salesOutService.page(req);
            for (WmsSalesOutRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.SOUT, vo.getId(), vo.getOutNo(), vo.getCustomerName(), vo.getWarehouseName(),
                    vo.getApproveStatus(), vo.getOutDate() == null ? null : vo.getOutDate().toString(),
                    vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.SR)) {
            WmsSalesReturnPageReqVO req = new WmsSalesReturnPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setReturnNo(exactBillNo);
            }
            IPage<WmsSalesReturnRespVO> page = salesReturnService.page(req);
            for (WmsSalesReturnRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.SR, vo.getId(), vo.getReturnNo(), vo.getCustomerName(), vo.getWarehouseName(),
                    vo.getApproveStatus(), vo.getReturnDate() == null ? null : vo.getReturnDate().toString(),
                    vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.SC)) {
            WmsStockCheckPageReqVO req = new WmsStockCheckPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setCheckNo(exactBillNo);
            }
            IPage<WmsStockCheckRespVO> page = stockCheckService.page(req);
            for (WmsStockCheckRespVO vo : page.getRecords()) {
                rows.add(buildRow(BillDomainCodes.SC, vo.getId(), vo.getCheckNo(), vo.getWarehouseName(), vo.getWarehouseName(),
                    vo.getCheckStatus(), vo.getCheckDate() == null ? null : vo.getCheckDate().toString(),
                    null, vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.getDomainTypes().contains(BillDomainCodes.ST)) {
            WmsStockTransferPageReqVO req = new WmsStockTransferPageReqVO();
            req.setPageNo(1);
            req.setPageSize(PAGE_SIZE_PER_DOMAIN);
            if (exactBillNo != null) {
                req.setTransferNo(exactBillNo);
            }
            IPage<WmsStockTransferRespVO> page = stockTransferService.page(req);
            for (WmsStockTransferRespVO vo : page.getRecords()) {
                String warehouse = StringUtils.defaultString(vo.getOutWarehouseName(), "-")
                    + " -> " + StringUtils.defaultString(vo.getInWarehouseName(), "-");
                rows.add(buildRow(BillDomainCodes.ST, vo.getId(), vo.getTransferNo(), warehouse, warehouse,
                    vo.getTransferStatus(), vo.getTransferDate() == null ? null : vo.getTransferDate().toString(),
                    null, vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }

        return rows.stream()
            .filter(row -> filterByStatus(row, intent.isPendingOnly()))
            .filter(row -> filterByBillNo(row, intent.getBillNo()))
            .filter(row -> filterByKeyword(row, intent.getKeyword()))
            .filter(row -> filterByDate(row, intent.getDays()))
            .sorted(Comparator.comparing(row -> row.sortTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(MAX_POOL)
            .collect(Collectors.toList());
    }

    private BillRowInternal buildRow(String domainType, Long id, String billNo, String partnerName, String warehouseName,
                                   String status, String bizDate, Object totalAmount, String sortTime) {
        BillRowInternal row = new BillRowInternal();
        row.domainType = domainType;
        row.id = id;
        row.billNo = billNo;
        row.partnerName = partnerName;
        row.warehouseName = warehouseName;
        row.status = status;
        row.bizDate = bizDate;
        if (totalAmount instanceof java.math.BigDecimal bd) {
            row.totalAmount = bd;
        }
        row.sortTime = StringUtils.defaultIfBlank(sortTime, StringUtils.defaultString(bizDate));
        return row;
    }

    private BillQueryRowVO toVo(BillRowInternal row) {
        BillQueryRowVO vo = new BillQueryRowVO();
        vo.setDomainType(row.domainType);
        vo.setDomainTypeName(BillDomainCodes.toDisplayName(row.domainType));
        vo.setId(row.id);
        vo.setBillNo(row.billNo);
        vo.setPartnerName(row.partnerName);
        vo.setWarehouseName(row.warehouseName);
        vo.setStatus(row.status);
        vo.setStatusLabel(BillDomainCodes.toStatusLabel(row.status));
        vo.setBizDate(row.bizDate);
        vo.setTotalAmount(row.totalAmount);
        return vo;
    }

    private boolean filterByStatus(BillRowInternal row, boolean pendingOnly) {
        if (!pendingOnly) {
            return true;
        }
        return "0".equals(row.status);
    }

    private boolean filterByKeyword(BillRowInternal row, String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return true;
        }
        String key = keyword.toLowerCase();
        return containsIgnoreCase(row.billNo, key)
            || containsIgnoreCase(row.partnerName, key)
            || containsIgnoreCase(row.warehouseName, key);
    }

    private boolean filterByBillNo(BillRowInternal row, String billNo) {
        if (StringUtils.isBlank(billNo)) {
            return true;
        }
        if (row.billNo == null) {
            return false;
        }
        return row.billNo.trim().equalsIgnoreCase(billNo.trim());
    }

    private boolean filterByDate(BillRowInternal row, int days) {
        if (days <= 0 || StringUtils.isBlank(row.bizDate)) {
            return days <= 0;
        }
        try {
            LocalDate date = LocalDate.parse(row.bizDate);
            long diff = ChronoUnit.DAYS.between(date, LocalDate.now());
            return diff >= 0 && diff < days;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean containsIgnoreCase(String text, String key) {
        return text != null && text.toLowerCase().contains(key);
    }

    private static final class BillRowInternal {
        private String domainType;
        private Long id;
        private String billNo;
        private String partnerName;
        private String warehouseName;
        private String status;
        private String bizDate;
        private java.math.BigDecimal totalAmount;
        private String sortTime;
    }
}
