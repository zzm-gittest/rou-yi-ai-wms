package com.ruoyi.wms.service.ai.pc.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
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
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 查询与最近单据回看流程支持。
 */
class PcQueryFlowSupport {

    private static final Pattern BILL_NO_PATTERN = Pattern.compile("(?:单号|单据号|订单号)\\s*[:：]?\\s*([A-Za-z0-9_-]+)");

    private static final String DOMAIN_PO = "PO";
    private static final String DOMAIN_PI = "PI";
    private static final String DOMAIN_PR = "PR";
    private static final String DOMAIN_SO = "SO";
    private static final String DOMAIN_SOUT = "SOUT";
    private static final String DOMAIN_SR = "SR";
    private static final String DOMAIN_SC = "SC";
    private static final String DOMAIN_ST = "ST";

    private final PcMultiBillChatServiceImpl owner;

    PcQueryFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handleQueryBillCenterEntry(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", "统一查询模板");
        card.put("templates", List.of(
                "查询待审核单据",
                "查询单号 PO20260324001",
                "查询销售出库单据",
                "查询近7天库存盘点单",
                "查询全部"
        ));
        return owner.conversationLifecycleSupport.buildReply(
                state,
                "请选择一个查询模板，复制到输入框后可继续修改再发送。",
                card,
                owner.conversationLifecycleSupport.quickActions());
    }

    Map<String, Object> handleQueryBillCenter(PcState state, String input) {
        if (containsRecentOrderQuery(input)) {
            if (state.getLastCreatedOrderId() == null) {
                return owner.conversationLifecycleSupport.buildReply(state,
                        "当前会话还没有成功创建过单据。",
                        null,
                        owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元", "卖给客户A苹果笔记本10台，单价9000元"));
            }
            return buildRecentOrderReply(state);
        }

        QueryIntent intent = parseQueryIntent(input);
        List<Map<String, Object>> rows = queryAllBills(intent);

        Map<String, Object> card = new HashMap<>();
        card.put("type", "TABLE");
        card.put("title", "单据查询结果");
        card.put("rows", rows);
        card.put("total", rows.size());
        return owner.conversationLifecycleSupport.buildReply(
                state,
                "共找到 " + rows.size() + " 条单据。",
                card,
            owner.conversationLifecycleSupport.quickActions(
                "查询待审核单据",
                "查询单号 PO20260324001",
                "查询销售出库单据",
                "查询近7天库存盘点单",
                "查询全部"));
    }

    Map<String, Object> handleQueryPurchaseOrder(PcState state, String input) {
        return handleQueryBillCenter(state, input);
    }

    Map<String, Object> buildRecentOrderReply(PcState state) {
        if (DOMAIN_PO.equals(state.lastCreatedDomainType)) {
            WmsPurchaseOrderRespVO order = owner.purchaseOrderService.get(state.getLastCreatedOrderId());
            if (order == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "最近创建的采购订单未查询到，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions("再来一单采购"));
            }
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的采购订单详情。", owner.cardResultSupport.buildOrderResultCard(order, order.getId()), owner.conversationLifecycleSupport.quickActions("再来一单采购"));
        }
        if (DOMAIN_PI.equals(state.lastCreatedDomainType)) {
            WmsPurchaseInRespVO resp = owner.purchaseInService.get(state.getLastCreatedOrderId());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的采购入库单。", owner.cardResultSupport.buildPurchaseInResultCard(resp, state.getLastCreatedOrderId(), resp == null ? null : resp.getSupplierName()), owner.conversationLifecycleSupport.quickActions("再来一单采购入库"));
        }
        if (DOMAIN_PR.equals(state.lastCreatedDomainType)) {
            WmsPurchaseReturnRespVO resp = owner.purchaseReturnService.get(state.getLastCreatedOrderId());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的采购退货单。", owner.cardResultSupport.buildPurchaseReturnResultCard(resp, state.getLastCreatedOrderId(), resp == null ? null : resp.getSupplierName()), owner.conversationLifecycleSupport.quickActions("再来一单采购退货"));
        }
        if (DOMAIN_SO.equals(state.lastCreatedDomainType)) {
            WmsSalesOrderRespVO resp = owner.salesOrderService.get(state.getLastCreatedOrderId());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的销售订单。", owner.cardResultSupport.buildSalesOrderResultCard(resp, state.getLastCreatedOrderId(), resp == null ? null : resp.getCustomerName()), owner.conversationLifecycleSupport.quickActions("再来一单销售订单"));
        }
        if (DOMAIN_SOUT.equals(state.lastCreatedDomainType)) {
            WmsSalesOutRespVO resp = owner.salesOutService.get(state.getLastCreatedOrderId());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的销售出库单。", owner.cardResultSupport.buildSalesOutResultCard(resp, state.getLastCreatedOrderId(), resp == null ? null : resp.getCustomerName()), owner.conversationLifecycleSupport.quickActions("再来一单销售出库"));
        }
        if (DOMAIN_SR.equals(state.lastCreatedDomainType)) {
            WmsSalesReturnRespVO resp = owner.salesReturnService.get(state.getLastCreatedOrderId());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的销售退货单。", owner.cardResultSupport.buildSalesReturnResultCard(resp, state.getLastCreatedOrderId(), resp == null ? null : resp.getCustomerName()), owner.conversationLifecycleSupport.quickActions("再来一单销售退货"));
        }
        if (DOMAIN_SC.equals(state.lastCreatedDomainType)) {
            WmsStockCheckRespVO resp = owner.stockCheckService.get(state.getLastCreatedOrderId());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的库存盘点单。", owner.cardResultSupport.buildStockCheckResultCard(resp, state.getLastCreatedOrderId(), resp == null ? null : resp.getWarehouseName()), owner.conversationLifecycleSupport.quickActions("再来一单库存盘点"));
        }
        if (DOMAIN_ST.equals(state.lastCreatedDomainType)) {
            WmsStockTransferRespVO resp = owner.stockTransferService.get(state.getLastCreatedOrderId());
            String partner = resp == null ? null : (resp.getOutWarehouseName() + " -> " + resp.getInWarehouseName());
            return owner.conversationLifecycleSupport.buildReply(state, "这是刚刚创建的库存调拨单。", owner.cardResultSupport.buildStockTransferResultCard(resp, state.getLastCreatedOrderId(), partner, null), owner.conversationLifecycleSupport.quickActions("再来一单库存调拨"));
        }
        return owner.conversationLifecycleSupport.buildReply(state, "最近创建的单据类型暂不支持回看。", null, owner.conversationLifecycleSupport.quickActions("查看刚创建的订单"));
    }

    private boolean containsRecentOrderQuery(String input) {
        return input != null && (input.contains("刚创建") || input.contains("刚才的单") || input.contains("刚刚的单") || input.contains("最近创建的订单"));
    }

    private List<Map<String, Object>> queryAllBills(QueryIntent intent) {
        List<Map<String, Object>> rows = new ArrayList<>();

        if (intent.domains.contains("PO")) {
            WmsPurchaseOrderPageReqVO req = new WmsPurchaseOrderPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsPurchaseOrderRespVO> page = owner.purchaseOrderService.page(req);
            for (WmsPurchaseOrderRespVO vo : page.getRecords()) {
                rows.add(buildRow("PO", vo.getId(), vo.getOrderNo(), vo.getSupplierName(), null, vo.getOrderStatus(), vo.getOrderDate() == null ? null : vo.getOrderDate().toString(), vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("PI")) {
            WmsPurchaseInPageReqVO req = new WmsPurchaseInPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsPurchaseInRespVO> page = owner.purchaseInService.page(req);
            for (WmsPurchaseInRespVO vo : page.getRecords()) {
                rows.add(buildRow("PI", vo.getId(), vo.getInNo(), vo.getSupplierName(), vo.getWarehouseName(), vo.getApproveStatus(), vo.getInDate() == null ? null : vo.getInDate().toString(), vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("PR")) {
            WmsPurchaseReturnPageReqVO req = new WmsPurchaseReturnPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsPurchaseReturnRespVO> page = owner.purchaseReturnService.page(req);
            for (WmsPurchaseReturnRespVO vo : page.getRecords()) {
                rows.add(buildRow("PR", vo.getId(), vo.getReturnNo(), vo.getSupplierName(), vo.getWarehouseName(), vo.getApproveStatus(), vo.getReturnDate() == null ? null : vo.getReturnDate().toString(), vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("SO")) {
            WmsSalesOrderPageReqVO req = new WmsSalesOrderPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsSalesOrderRespVO> page = owner.salesOrderService.page(req);
            for (WmsSalesOrderRespVO vo : page.getRecords()) {
                rows.add(buildRow("SO", vo.getId(), vo.getOrderNo(), vo.getCustomerName(), null, vo.getOrderStatus(), vo.getOrderDate() == null ? null : vo.getOrderDate().toString(), vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("SOUT")) {
            WmsSalesOutPageReqVO req = new WmsSalesOutPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsSalesOutRespVO> page = owner.salesOutService.page(req);
            for (WmsSalesOutRespVO vo : page.getRecords()) {
                rows.add(buildRow("SOUT", vo.getId(), vo.getOutNo(), vo.getCustomerName(), vo.getWarehouseName(), vo.getApproveStatus(), vo.getOutDate() == null ? null : vo.getOutDate().toString(), vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("SR")) {
            WmsSalesReturnPageReqVO req = new WmsSalesReturnPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsSalesReturnRespVO> page = owner.salesReturnService.page(req);
            for (WmsSalesReturnRespVO vo : page.getRecords()) {
                rows.add(buildRow("SR", vo.getId(), vo.getReturnNo(), vo.getCustomerName(), vo.getWarehouseName(), vo.getApproveStatus(), vo.getReturnDate() == null ? null : vo.getReturnDate().toString(), vo.getTotalAmount(), vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("SC")) {
            WmsStockCheckPageReqVO req = new WmsStockCheckPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsStockCheckRespVO> page = owner.stockCheckService.page(req);
            for (WmsStockCheckRespVO vo : page.getRecords()) {
                rows.add(buildRow("SC", vo.getId(), vo.getCheckNo(), vo.getWarehouseName(), vo.getWarehouseName(), vo.getCheckStatus(), vo.getCheckDate() == null ? null : vo.getCheckDate().toString(), null, vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }
        if (intent.domains.contains("ST")) {
            WmsStockTransferPageReqVO req = new WmsStockTransferPageReqVO();
            req.setPageNo(1);
            req.setPageSize(20);
            IPage<WmsStockTransferRespVO> page = owner.stockTransferService.page(req);
            for (WmsStockTransferRespVO vo : page.getRecords()) {
                String warehouse = StringUtils.defaultString(vo.getOutWarehouseName(), "-") + " -> " + StringUtils.defaultString(vo.getInWarehouseName(), "-");
                rows.add(buildRow("ST", vo.getId(), vo.getTransferNo(), warehouse, warehouse, vo.getTransferStatus(), vo.getTransferDate() == null ? null : vo.getTransferDate().toString(), null, vo.getCreateTime() == null ? null : vo.getCreateTime().toString()));
            }
        }

        List<Map<String, Object>> filtered = rows.stream()
                .filter(row -> filterByStatus(row, intent.pendingOnly))
            .filter(row -> filterByBillNo(row, intent.billNo))
                .filter(row -> filterByKeyword(row, intent.keyword))
                .filter(row -> filterByDate(row, intent.days))
                .sorted(Comparator.comparing((Map<String, Object> row) -> String.valueOf(row.get("sortTime")), Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(50)
                .collect(Collectors.toList());
        filtered.forEach(row -> row.remove("sortTime"));
        return filtered;
    }

    private QueryIntent parseQueryIntent(String input) {
        String text = StringUtils.defaultString(input);
        QueryIntent intent = new QueryIntent();
        if (text.contains("采购订单")) intent.domains.add("PO");
        if (text.contains("采购入库")) intent.domains.add("PI");
        if (text.contains("采购退货")) intent.domains.add("PR");
        if (text.contains("销售订单")) intent.domains.add("SO");
        if (text.contains("销售出库")) intent.domains.add("SOUT");
        if (text.contains("销售退货")) intent.domains.add("SR");
        if (text.contains("库存盘点") || text.contains("盘点")) intent.domains.add("SC");
        if (text.contains("库存调拨") || text.contains("调拨")) intent.domains.add("ST");
        if (intent.domains.isEmpty()) {
            intent.domains.add("PO");
            intent.domains.add("PI");
            intent.domains.add("PR");
            intent.domains.add("SO");
            intent.domains.add("SOUT");
            intent.domains.add("SR");
            intent.domains.add("SC");
            intent.domains.add("ST");
        }
        intent.pendingOnly = text.contains("待审核") || text.contains("待审批");
        intent.days = text.contains("今天") ? 1 : (text.contains("近7天") ? 7 : 0);
        Matcher matcher = BILL_NO_PATTERN.matcher(text);
        if (matcher.find()) {
            intent.billNo = matcher.group(1);
        }
        String keyword = text
                .replace("查询", "")
                .replace("查", "")
                .replace("全部", "")
                .replace("单据", "")
                .replace("单号", "")
                .replace("单据号", "")
                .replace("订单号", "")
                .replace("相关", "")
                .replace("最近7天", "")
                .replace("近7天", "")
                .replace("今天", "")
                .replace("待审核", "")
                .replace("采购订单", "")
                .replace("采购入库", "")
                .replace("采购退货", "")
                .replace("销售订单", "")
                .replace("销售出库", "")
                .replace("销售退货", "")
                .replace("库存盘点", "")
                .replace("盘点", "")
                .replace("库存调拨", "")
                .replace("调拨", "")
                .trim();
        intent.keyword = keyword.length() >= 2 ? keyword : null;
        return intent;
    }

    private Map<String, Object> buildRow(String domainType, Long id, String billNo, String partnerName, String warehouseName,
                                         String status, String bizDate, Object totalAmount, String sortTime) {
        Map<String, Object> row = new HashMap<>();
        row.put("domainType", domainType);
        row.put("domainTypeName", toDomainTypeName(domainType));
        row.put("id", id);
        row.put("orderNo", billNo);
        row.put("billNo", billNo);
        row.put("supplierName", partnerName);
        row.put("partnerName", partnerName);
        row.put("warehouseName", warehouseName);
        row.put("orderStatus", status);
        row.put("status", status);
        row.put("statusLabel", toStatusLabel(status));
        row.put("orderDate", bizDate);
        row.put("bizDate", bizDate);
        row.put("totalAmount", totalAmount);
        row.put("sortTime", StringUtils.defaultIfBlank(sortTime, StringUtils.defaultString(bizDate)));
        return row;
    }

    private boolean filterByStatus(Map<String, Object> row, boolean pendingOnly) {
        if (!pendingOnly) return true;
        return "0".equals(String.valueOf(row.get("status")));
    }

    private boolean filterByKeyword(Map<String, Object> row, String keyword) {
        if (StringUtils.isBlank(keyword)) return true;
        String key = keyword.toLowerCase();
        String billNo = StringUtils.defaultString((String) row.get("billNo")).toLowerCase();
        String partner = StringUtils.defaultString((String) row.get("partnerName")).toLowerCase();
        String warehouse = StringUtils.defaultString((String) row.get("warehouseName")).toLowerCase();
        return billNo.contains(key) || partner.contains(key) || warehouse.contains(key);
    }

    private boolean filterByBillNo(Map<String, Object> row, String billNo) {
        if (StringUtils.isBlank(billNo)) return true;
        String rowBillNo = StringUtils.defaultString((String) row.get("billNo")).toLowerCase();
        return rowBillNo.contains(billNo.toLowerCase());
    }

    private boolean filterByDate(Map<String, Object> row, int days) {
        if (days <= 0) return true;
        String bizDate = (String) row.get("bizDate");
        if (StringUtils.isBlank(bizDate)) return false;
        try {
            LocalDate date = LocalDate.parse(bizDate);
            long diff = ChronoUnit.DAYS.between(date, LocalDate.now());
            return diff >= 0 && diff < days;
        } catch (Exception e) {
            return false;
        }
    }

    private static class QueryIntent {
        private final LinkedHashSet<String> domains = new LinkedHashSet<>();
        private boolean pendingOnly;
        private int days;
        private String keyword;
        private String billNo;
    }

    private String toDomainTypeName(String domainType) {
        if ("PO".equals(domainType)) return "采购订单";
        if ("PI".equals(domainType)) return "采购入库";
        if ("PR".equals(domainType)) return "采购退货";
        if ("SO".equals(domainType)) return "销售订单";
        if ("SOUT".equals(domainType)) return "销售出库";
        if ("SR".equals(domainType)) return "销售退货";
        if ("SC".equals(domainType)) return "库存盘点";
        if ("ST".equals(domainType)) return "库存调拨";
        return domainType;
    }

    private String toStatusLabel(String status) {
        if ("0".equals(status)) return "待审核";
        if ("1".equals(status)) return "已审核";
        if ("2".equals(status)) return "执行中";
        if ("3".equals(status)) return "已完成";
        return StringUtils.defaultIfBlank(status, "-");
    }

    Map<String, Object> handleBillDetailById(PcState state, String domainType, Long id) {
        if ("PO".equals(domainType)) {
            WmsPurchaseOrderRespVO order = owner.purchaseOrderService.get(id);
            if (order == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该采购订单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "采购订单详情如下。", owner.cardResultSupport.buildOrderResultCard(order, order.getId()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("PI".equals(domainType)) {
            WmsPurchaseInRespVO resp = owner.purchaseInService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该采购入库单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "采购入库单详情如下。", owner.cardResultSupport.buildPurchaseInResultCard(resp, id, resp.getSupplierName()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("PR".equals(domainType)) {
            WmsPurchaseReturnRespVO resp = owner.purchaseReturnService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该采购退货单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "采购退货单详情如下。", owner.cardResultSupport.buildPurchaseReturnResultCard(resp, id, resp.getSupplierName()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("SO".equals(domainType)) {
            WmsSalesOrderRespVO resp = owner.salesOrderService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该销售订单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "销售订单详情如下。", owner.cardResultSupport.buildSalesOrderResultCard(resp, id, resp.getCustomerName()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("SOUT".equals(domainType)) {
            WmsSalesOutRespVO resp = owner.salesOutService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该销售出库单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "销售出库单详情如下。", owner.cardResultSupport.buildSalesOutResultCard(resp, id, resp.getCustomerName()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("SR".equals(domainType)) {
            WmsSalesReturnRespVO resp = owner.salesReturnService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该销售退货单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "销售退货单详情如下。", owner.cardResultSupport.buildSalesReturnResultCard(resp, id, resp.getCustomerName()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("SC".equals(domainType)) {
            WmsStockCheckRespVO resp = owner.stockCheckService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该库存盘点单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            return owner.conversationLifecycleSupport.buildReply(state, "库存盘点单详情如下。", owner.cardResultSupport.buildStockCheckResultCard(resp, id, resp.getWarehouseName()), owner.conversationLifecycleSupport.quickActions());
        }
        if ("ST".equals(domainType)) {
            WmsStockTransferRespVO resp = owner.stockTransferService.get(id);
            if (resp == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "未找到该库存调拨单，可能已被删除。", null, owner.conversationLifecycleSupport.quickActions());
            }
            String partner = resp.getOutWarehouseName() + " -> " + resp.getInWarehouseName();
            return owner.conversationLifecycleSupport.buildReply(state, "库存调拨单详情如下。", owner.cardResultSupport.buildStockTransferResultCard(resp, id, partner, null), owner.conversationLifecycleSupport.quickActions());
        }
        return owner.conversationLifecycleSupport.buildReply(state, "暂不支持该单据类型的详情查看。", null, owner.conversationLifecycleSupport.quickActions());
    }

    private Map<String, Object> toOrderRow(WmsPurchaseOrderRespVO vo) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", vo.getId());
        row.put("orderNo", vo.getOrderNo());
        row.put("supplierName", vo.getSupplierName());
        row.put("orderStatus", vo.getOrderStatus());
        row.put("orderDate", vo.getOrderDate());
        row.put("totalAmount", vo.getTotalAmount());
        return row;
    }
}
