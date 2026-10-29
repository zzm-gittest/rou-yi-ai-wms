package com.ruoyi.wms.service.ai.analysis;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.domain.vo.ai.analysis.*;
import com.ruoyi.wms.mapper.report.WmsAnalysisMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 经营分析数据查询服务
 * <p>
 * 负责从数据库获取各类经营数据，返回结构化 VO。
 * PC 端与移动端 100% 复用。
 *
 * @author ruoyi
 */
@Service
public class BusinessAnalysisDataService {

    private static final Logger log = LoggerFactory.getLogger(BusinessAnalysisDataService.class);

    private final WmsAnalysisMapper analysisMapper;

    public BusinessAnalysisDataService(WmsAnalysisMapper analysisMapper) {
        this.analysisMapper = analysisMapper;
    }

    /**
     * 获取销售汇总
     */
    public SalesSummaryVO getSalesSummary(AnalysisPeriod period, Long warehouseId) {
        Long tenantId = SecurityUtils.getTenantId();
        SalesSummaryVO vo = new SalesSummaryVO();
        vo.setPeriodLabel(period.getLabel());

        BigDecimal salesAmount = safe(analysisMapper.sumSalesAmount(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId));
        BigDecimal returnAmount = safe(analysisMapper.sumSalesReturnAmount(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId));
        BigDecimal salesCost = safe(analysisMapper.sumSalesCost(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId));
        int orderCount = analysisMapper.countSalesOutOrders(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId);

        BigDecimal netSales = salesAmount.subtract(returnAmount);
        BigDecimal grossProfit = netSales.subtract(salesCost);
        BigDecimal avgOrder = orderCount > 0
                ? netSales.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        vo.setSalesAmount(salesAmount);
        vo.setReturnAmount(returnAmount);
        vo.setNetSalesAmount(netSales);
        vo.setOrderCount(orderCount);
        vo.setAvgOrderAmount(avgOrder);
        vo.setGrossProfit(grossProfit);
        return vo;
    }

    /**
     * 获取采购汇总
     */
    public PurchaseSummaryVO getPurchaseSummary(AnalysisPeriod period, Long warehouseId) {
        Long tenantId = SecurityUtils.getTenantId();
        PurchaseSummaryVO vo = new PurchaseSummaryVO();
        vo.setPeriodLabel(period.getLabel());

        BigDecimal purchaseAmount = safe(analysisMapper.sumPurchaseAmount(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId));
        BigDecimal returnAmount = safe(analysisMapper.sumPurchaseReturnAmount(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId));
        int inCount = analysisMapper.countPurchaseInOrders(
                tenantId, period.getStartDate(), period.getEndDate(), warehouseId);

        vo.setPurchaseAmount(purchaseAmount);
        vo.setReturnAmount(returnAmount);
        vo.setNetPurchaseAmount(purchaseAmount.subtract(returnAmount));
        vo.setInCount(inCount);
        return vo;
    }

    /**
     * 获取财务汇总
     */
    public FinanceSummaryVO getFinanceSummary() {
        Long tenantId = SecurityUtils.getTenantId();
        FinanceSummaryVO vo = new FinanceSummaryVO();

        Map<String, Object> summary = analysisMapper.selectReceivableSummary(tenantId);
        if (summary != null) {
            vo.setReceivableTotal(toBigDecimal(summary.get("receivableTotal")));
            vo.setOverdueReceivableAmount(toBigDecimal(summary.get("overdueAmount")));
            vo.setOverdueReceivableCount(toInt(summary.get("overdueCount")));
        } else {
            vo.setReceivableTotal(BigDecimal.ZERO);
            vo.setOverdueReceivableAmount(BigDecimal.ZERO);
            vo.setOverdueReceivableCount(0);
        }
        // 暂不区分应付
        vo.setPayableTotal(BigDecimal.ZERO);
        return vo;
    }

    /**
     * 获取待办事项
     */
    public PendingTasksVO getPendingTasks() {
        Long tenantId = SecurityUtils.getTenantId();
        PendingTasksVO vo = new PendingTasksVO();
        vo.setPendingPurchaseOrderCount(analysisMapper.countPendingPurchaseOrders(tenantId));
        vo.setPendingSalesOrderCount(analysisMapper.countPendingSalesOrders(tenantId));

        List<Map<String, Object>> pendingIn = analysisMapper.selectPendingPurchaseIn(tenantId, 0);
        vo.setPendingPurchaseInCount(pendingIn != null ? pendingIn.size() : 0);

        List<Map<String, Object>> pendingOut = analysisMapper.selectPendingSalesOut(tenantId, 0);
        vo.setPendingSalesOutCount(pendingOut != null ? pendingOut.size() : 0);
        return vo;
    }

    /**
     * 获取热销商品 TOP N
     */
    public List<GoodsRankItemVO> getTopSellingGoods(AnalysisPeriod period, int limit) {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectTopSellingGoods(
                tenantId, period.getStartDate(), period.getEndDate(), limit);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<GoodsRankItemVO> list = new ArrayList<>(rows.size());
        int rank = 1;
        for (Map<String, Object> row : rows) {
            GoodsRankItemVO item = new GoodsRankItemVO();
            item.setRank(rank++);
            item.setGoodsName(toStr(row.get("goodsName")));
            item.setSpec(toStr(row.get("spec")));
            item.setUnitName(toStr(row.get("unitName")));
            item.setSalesAmount(toBigDecimal(row.get("salesAmount")));
            item.setSalesQuantity(toBigDecimal(row.get("salesQuantity")));
            list.add(item);
        }
        return list;
    }

    /**
     * 获取客户销售排名 TOP N
     */
    public List<CustomerRankItemVO> getCustomerSalesRanking(AnalysisPeriod period, int limit) {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectCustomerSalesRanking(
                tenantId, period.getStartDate(), period.getEndDate(), limit);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<CustomerRankItemVO> list = new ArrayList<>(rows.size());
        int rank = 1;
        for (Map<String, Object> row : rows) {
            CustomerRankItemVO item = new CustomerRankItemVO();
            item.setRank(rank++);
            item.setCustomerName(toStr(row.get("customerName")));
            item.setSalesAmount(toBigDecimal(row.get("salesAmount")));
            item.setOrderCount(toInt(row.get("orderCount")));
            list.add(item);
        }
        return list;
    }

    /**
     * 获取库存预警列表
     */
    public List<StockWarningVO> getStockWarnings(Long warehouseId) {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectStockShortage(tenantId, 30, 7);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<StockWarningVO> list = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            StockWarningVO item = new StockWarningVO();
            item.setGoodsName(toStr(row.get("goodsName")));
            item.setSpec(toStr(row.get("spec")));
            item.setUnitName(toStr(row.get("unitName")));
            item.setCurrentStock(toBigDecimal(row.get("currentStock")));
            item.setDailyAvgSales(toBigDecimal(row.get("dailyAvgQty")));
            int days = toInt(row.get("estimatedDays"));
            item.setEstimatedDays(days);
            item.setWarningLevel(days <= 3 ? "RED" : "ORANGE");
            list.add(item);
        }
        return list;
    }

    // ========== 工具方法 ==========

    private BigDecimal safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) {
            return BigDecimal.ZERO;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        try {
            return new BigDecimal(val.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int toInt(Object val) {
        if (val == null) {
            return 0;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String toStr(Object val) {
        return val != null ? val.toString() : "";
    }
}
