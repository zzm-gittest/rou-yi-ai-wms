package com.ruoyi.wms.service.report.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.wms.domain.report.WmsReportConfig;
import com.ruoyi.wms.domain.report.vo.*;
import com.ruoyi.wms.mapper.report.*;
import com.ruoyi.wms.service.report.IWmsReportService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 报表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsReportServiceImpl implements IWmsReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final WmsReportConfigMapper reportConfigMapper;
    private final WmsSalesReportMapper salesReportMapper;
    private final WmsStockReportMapper stockReportMapper;
    private final WmsFinanceReportMapper financeReportMapper;

    public WmsReportServiceImpl(WmsReportConfigMapper reportConfigMapper,
                                 WmsSalesReportMapper salesReportMapper,
                                 WmsStockReportMapper stockReportMapper,
                                 WmsFinanceReportMapper financeReportMapper) {
        this.reportConfigMapper = reportConfigMapper;
        this.salesReportMapper = salesReportMapper;
        this.stockReportMapper = stockReportMapper;
        this.financeReportMapper = financeReportMapper;
    }

    @Override
    public List<Map<String, Object>> getReportCategories() {
        List<WmsReportConfig> configs = reportConfigMapper.selectList(
                new LambdaQueryWrapper<WmsReportConfig>()
                        .eq(WmsReportConfig::getStatus, "0")
                        .orderByAsc(WmsReportConfig::getReportType)
                        .orderByAsc(WmsReportConfig::getSortOrder)
        );

        Map<String, List<Map<String, Object>>> typeMap = new LinkedHashMap<>();
        typeMap.put("sales", new ArrayList<>());
        typeMap.put("purchase", new ArrayList<>());
        typeMap.put("stock", new ArrayList<>());
        typeMap.put("finance", new ArrayList<>());
        typeMap.put("comprehensive", new ArrayList<>());

        Map<String, String> typeNames = new HashMap<>();
        typeNames.put("sales", "销售报表");
        typeNames.put("purchase", "采购报表");
        typeNames.put("stock", "库存报表");
        typeNames.put("finance", "财务报表");
        typeNames.put("comprehensive", "综合报表");

        for (WmsReportConfig config : configs) {
            Map<String, Object> report = new HashMap<>();
            report.put("code", config.getReportCode());
            report.put("name", config.getReportName());
            typeMap.get(config.getReportType()).add(report);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : typeNames.entrySet()) {
            Map<String, Object> category = new HashMap<>();
            category.put("type", entry.getKey());
            category.put("typeName", entry.getValue());
            category.put("reports", typeMap.get(entry.getKey()));
            result.add(category);
        }

        return result;
    }

    @Override
    public Map<String, Object> getReportConfig(String reportCode) {
        WmsReportConfig config = reportConfigMapper.selectOne(
                new LambdaQueryWrapper<WmsReportConfig>()
                        .eq(WmsReportConfig::getReportCode, reportCode)
        );
        if (config == null) {
            return null;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reportCode", config.getReportCode());
        result.put("reportName", config.getReportName());
        result.put("reportType", config.getReportType());
        result.put("category", config.getCategory());
        // TODO: 解析 JSON 配置
        result.put("queryParams", config.getQueryParams());
        result.put("columnConfig", config.getColumnConfig());
        result.put("chartConfig", config.getChartConfig());

        return result;
    }

    @Override
    public Map<String, Object> queryReport(String reportCode, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        List<?> data = new ArrayList<>();

        switch (reportCode) {
            // 销售报表
            case "RPT_SALES_DAILY":
                data = querySalesDailyReport(
                        (LocalDate) params.get("startDate"),
                        (LocalDate) params.get("endDate"),
                        (List<Long>) params.get("customerIds"),
                        (List<Long>) params.get("warehouseIds"),
                        params.get("groupBy") != null ? (String) params.get("groupBy") : "date"
                );
                break;
            case "RPT_SALES_GOODS_RANK":
                data = queryGoodsRankReport(
                        (LocalDate) params.get("startDate"),
                        (LocalDate) params.get("endDate"),
                        (List<Long>) params.get("categoryIds"),
                        (List<Long>) params.get("warehouseIds"),
                        (String) params.get("orderBy")
                );
                break;
            case "RPT_SALES_CUSTOMER_ANALYSIS":
                data = queryCustomerAnalysisReport(
                        (LocalDate) params.get("startDate"),
                        (LocalDate) params.get("endDate"),
                        (List<Long>) params.get("customerIds")
                );
                break;

            // 库存报表
            case "RPT_STOCK_PSI":
                data = queryStockPsiReport(
                        (LocalDate) params.get("startDate"),
                        (LocalDate) params.get("endDate"),
                        (List<Long>) params.get("categoryIds"),
                        (List<Long>) params.get("warehouseIds"),
                        (String) params.get("goodsName")
                );
                break;

            // 财务报表
            case "RPT_FINANCE_AR_AGING":
                data = queryArAgingReport(
                        (LocalDate) params.get("statisticsDate"),
                        (List<Long>) params.get("customerIds")
                );
                break;
            case "RPT_FINANCE_SUPPLIER_ANALYSIS":
            case "RPT_PURCHASE_SUPPLIER_ANALYSIS":
                data = querySupplierAnalysisReport(
                        (LocalDate) params.get("startDate"),
                        (LocalDate) params.get("endDate"),
                        (List<Long>) params.get("supplierIds")
                );
                break;

            default:
                throw new IllegalArgumentException("未知的报表编码: " + reportCode);
        }

        result.put("data", data);
        result.put("total", data.size());
        return result;
    }

    @Override
    public TodaySalesVO getTodaySales(Long warehouseId) {
        Long tenantId = SecurityUtils.getTenantId();
        Long queryWarehouseId = (warehouseId != null && warehouseId > 0) ? warehouseId : null;

        BigDecimal salesAmount = Optional.ofNullable(salesReportMapper.getTodaySalesOut(tenantId, queryWarehouseId)).orElse(BigDecimal.ZERO);
        BigDecimal returnAmount = Optional.ofNullable(salesReportMapper.getTodaySalesReturn(tenantId, queryWarehouseId)).orElse(BigDecimal.ZERO);
        BigDecimal salesOutCost = Optional.ofNullable(salesReportMapper.getTodaySalesOutCost(tenantId, queryWarehouseId)).orElse(BigDecimal.ZERO);
        BigDecimal salesReturnCost = Optional.ofNullable(salesReportMapper.getTodaySalesReturnCost(tenantId, queryWarehouseId)).orElse(BigDecimal.ZERO);
        BigDecimal purchaseAmount = Optional.ofNullable(salesReportMapper.getTodayPurchaseIn(tenantId, queryWarehouseId)).orElse(BigDecimal.ZERO);
        BigDecimal purchaseReturnAmount = Optional.ofNullable(salesReportMapper.getTodayPurchaseReturn(tenantId, queryWarehouseId)).orElse(BigDecimal.ZERO);

        BigDecimal netSalesAmount = salesAmount.subtract(returnAmount);
        BigDecimal netSalesCost = salesOutCost.subtract(salesReturnCost);
        BigDecimal grossProfit = netSalesAmount.subtract(netSalesCost);
        LocalDateTime updateTime = salesReportMapper.getLastUpdateTime(tenantId, queryWarehouseId);

        TodaySalesVO vo = new TodaySalesVO();
        vo.setSalesAmount(salesAmount);
        vo.setReturnAmount(returnAmount);
        vo.setPurchaseAmount(purchaseAmount);
        vo.setPurchaseReturnAmount(purchaseReturnAmount);
        vo.setGrossProfit(grossProfit);
        vo.setUpdateTime(updateTime == null ? "-" : updateTime.format(DATE_TIME_FORMATTER));
        return vo;
    }

    // ==================== 销售报表 ====================

    @Override
    public List<SalesDailyReportVO> querySalesDailyReport(LocalDate startDate, LocalDate endDate,
                                                            List<Long> customerIds, List<Long> warehouseIds, String groupBy) {
        return salesReportMapper.querySalesDailyReport(startDate, endDate, customerIds, warehouseIds, groupBy);
    }

    @Override
    public List<GoodsRankReportVO> queryGoodsRankReport(LocalDate startDate, LocalDate endDate,
                                                         List<Long> categoryIds, List<Long> warehouseIds, String orderBy) {
        return salesReportMapper.queryGoodsRankReport(startDate, endDate, categoryIds, warehouseIds, orderBy);
    }

    @Override
    public List<CustomerAnalysisReportVO> queryCustomerAnalysisReport(LocalDate startDate, LocalDate endDate,
                                                                       List<Long> customerIds) {
        return salesReportMapper.queryCustomerAnalysisReport(startDate, endDate, customerIds);
    }

    // ==================== 库存报表 ====================

    @Override
    public List<StockPsiReportVO> queryStockPsiReport(LocalDate startDate, LocalDate endDate,
                                                       List<Long> categoryIds, List<Long> warehouseIds, String goodsName) {
        return stockReportMapper.queryStockPsiReport(startDate, endDate, categoryIds, warehouseIds, goodsName);
    }

    // ==================== 财务报表 ====================

    @Override
    public List<ArAgingReportVO> queryArAgingReport(LocalDate statisticsDate, List<Long> customerIds) {
        return financeReportMapper.queryArAgingReport(statisticsDate, customerIds);
    }

    @Override
    public List<SupplierAnalysisReportVO> querySupplierAnalysisReport(LocalDate startDate, LocalDate endDate,
                                                                       List<Long> supplierIds) {
        return financeReportMapper.querySupplierAnalysisReport(startDate, endDate, supplierIds);
    }
}
