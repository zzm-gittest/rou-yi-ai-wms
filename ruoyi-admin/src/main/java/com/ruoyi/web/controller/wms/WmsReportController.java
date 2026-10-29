package com.ruoyi.web.controller.wms;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.report.vo.*;
import com.ruoyi.wms.service.report.IWmsReportService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 报表Controller
 *
 * @author ruoyi
 */
@Api("报表中心")
@RestController
@RequestMapping("/wms/report")
@Validated
public class WmsReportController extends BaseController {

    private final IWmsReportService reportService;

    public WmsReportController(IWmsReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 获取报表分类列表
     */
    @ApiOperation("获取报表分类")
    @PreAuthorize("@ss.hasPermi('wms:report:query')")
    @GetMapping("/categories")
    public AjaxResult getCategories() {
        List<Map<String, Object>> categories = reportService.getReportCategories();
        return success(categories);
    }

    /**
     * 获取报表配置
     */
    @ApiOperation("获取报表配置")
    @PreAuthorize("@ss.hasPermi('wms:report:query')")
    @GetMapping("/config/{reportCode}")
    public AjaxResult getReportConfig(@PathVariable String reportCode) {
        Map<String, Object> config = reportService.getReportConfig(reportCode);
        return success(config);
    }

    /**
     * 通用报表查询
     */
    @ApiOperation("查询报表")
    @PreAuthorize("@ss.hasPermi('wms:report:query')")
    @PostMapping("/query")
    public AjaxResult queryReport(@RequestBody Map<String, Object> params) {
        String reportCode = (String) params.get("reportCode");
        params.remove("reportCode");
        Map<String, Object> result = reportService.queryReport(reportCode, params);
        return success(result);
    }

    /**
     * 获取今日销售数据
     */
    @ApiOperation("获取今日销售数据")
    @PreAuthorize("@ss.hasPermi('wms:report:query')")
    @GetMapping("/today-sales")
    public AjaxResult todaySales(@RequestParam(required = false) Long warehouseId) {
        return success(reportService.getTodaySales(warehouseId));
    }

    // ==================== 销售报表 ====================

    /**
     * 查询销售日报表
     */
    @ApiOperation("销售日报表")
    @PreAuthorize("@ss.hasPermi('wms:report:sales')")
    @PostMapping("/sales/daily")
    public TableDataInfo salesDailyReport(@RequestBody Map<String, Object> params) {
        LocalDate startDate = null;
        LocalDate endDate = null;
        List<Long> customerIds = null;
        List<Long> warehouseIds = null;
        String groupBy = "date";

        if (params.get("startDate") != null && !"".equals(params.get("startDate"))) {
            startDate = LocalDate.parse((String) params.get("startDate"));
        }
        if (params.get("endDate") != null && !"".equals(params.get("endDate"))) {
            endDate = LocalDate.parse((String) params.get("endDate"));
        }
        if (params.get("customerIds") != null) {
            customerIds = (List<Long>) params.get("customerIds");
        }
        if (params.get("warehouseIds") != null) {
            warehouseIds = (List<Long>) params.get("warehouseIds");
        }
        if (params.get("groupBy") != null && !"".equals(params.get("groupBy"))) {
            groupBy = (String) params.get("groupBy");
        }

        List<SalesDailyReportVO> list = reportService.querySalesDailyReport(startDate, endDate, customerIds, warehouseIds, groupBy);
        return getDataTable(list);
    }

    /**
     * 导出销售日报表
     */
    @ApiOperation("导出销售日报表")
    @PreAuthorize("@ss.hasPermi('wms:report:export')")
    @PostMapping("/sales/daily/export")
    public void exportSalesDaily(HttpServletResponse response,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<Long> customerIds,
            @RequestParam(required = false) List<Long> warehouseIds,
            @RequestParam(required = false) String groupBy) {
        if (groupBy == null || "".equals(groupBy)) {
            groupBy = "date";
        }
        LocalDate start = null;
        LocalDate end = null;
        if (startDate != null && !"".equals(startDate)) {
            start = LocalDate.parse(startDate);
        }
        if (endDate != null && !"".equals(endDate)) {
            end = LocalDate.parse(endDate);
        }

        List<SalesDailyReportVO> list = reportService.querySalesDailyReport(start, end, customerIds, warehouseIds, groupBy);
        ExcelUtil<SalesDailyReportVO> util = new ExcelUtil<>(SalesDailyReportVO.class);
        util.exportExcel(response, list, "销售日报表");
    }

    /**
     * 查询商品销售排行榜
     */
    @ApiOperation("商品销售排行榜")
    @PreAuthorize("@ss.hasPermi('wms:report:sales')")
    @GetMapping("/sales/goodsRank")
    public TableDataInfo goodsRankReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> warehouseIds,
            @RequestParam(required = false) String orderBy) {
        List<GoodsRankReportVO> list = reportService.queryGoodsRankReport(startDate, endDate, categoryIds, warehouseIds, orderBy);
        return getDataTable(list);
    }

    /**
     * 导出商品销售排行榜
     */
    @ApiOperation("导出商品销售排行榜")
    @PreAuthorize("@ss.hasPermi('wms:report:export')")
    @PostMapping("/sales/goodsRank/export")
    public void exportGoodsRank(HttpServletResponse response,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> warehouseIds,
            @RequestParam(required = false) String orderBy) {
        List<GoodsRankReportVO> list = reportService.queryGoodsRankReport(startDate, endDate, categoryIds, warehouseIds, orderBy);
        ExcelUtil<GoodsRankReportVO> util = new ExcelUtil<>(GoodsRankReportVO.class);
        util.exportExcel(response, list, "商品销售排行榜");
    }

    /**
     * 查询客户销售分析表
     */
    @ApiOperation("客户销售分析表")
    @PreAuthorize("@ss.hasPermi('wms:report:sales')")
    @GetMapping("/sales/customerAnalysis")
    public TableDataInfo customerAnalysisReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> customerIds) {
        List<CustomerAnalysisReportVO> list = reportService.queryCustomerAnalysisReport(startDate, endDate, customerIds);
        return getDataTable(list);
    }

    /**
     * 导出客户销售分析表
     */
    @ApiOperation("导出客户销售分析表")
    @PreAuthorize("@ss.hasPermi('wms:report:export')")
    @PostMapping("/sales/customerAnalysis/export")
    public void exportCustomerAnalysis(HttpServletResponse response,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> customerIds) {
        List<CustomerAnalysisReportVO> list = reportService.queryCustomerAnalysisReport(startDate, endDate, customerIds);
        ExcelUtil<CustomerAnalysisReportVO> util = new ExcelUtil<>(CustomerAnalysisReportVO.class);
        util.exportExcel(response, list, "客户销售分析表");
    }

    // ==================== 库存报表 ====================

    /**
     * 查询库存进销存报表
     */
    @ApiOperation("库存进销存报表")
    @PreAuthorize("@ss.hasPermi('wms:report:stock')")
    @GetMapping("/stock/psi")
    public TableDataInfo stockPsiReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> warehouseIds,
            @RequestParam(required = false) String goodsName) {
        List<StockPsiReportVO> list = reportService.queryStockPsiReport(startDate, endDate, categoryIds, warehouseIds, goodsName);
        return getDataTable(list);
    }

    /**
     * 导出库存进销存报表
     */
    @ApiOperation("导出库存进销存报表")
    @PreAuthorize("@ss.hasPermi('wms:report:export')")
    @PostMapping("/stock/psi/export")
    public void exportStockPsi(HttpServletResponse response,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> warehouseIds,
            @RequestParam(required = false) String goodsName) {
        List<StockPsiReportVO> list = reportService.queryStockPsiReport(startDate, endDate, categoryIds, warehouseIds, goodsName);
        ExcelUtil<StockPsiReportVO> util = new ExcelUtil<>(StockPsiReportVO.class);
        util.exportExcel(response, list, "库存进销存报表");
    }

    // ==================== 财务报表 ====================

    /**
     * 查询应收账款账龄分析表
     */
    @ApiOperation("应收账款账龄分析表")
    @PreAuthorize("@ss.hasPermi('wms:report:finance')")
    @GetMapping("/finance/arAging")
    public TableDataInfo arAgingReport(
            @RequestParam(required = false) LocalDate statisticsDate,
            @RequestParam(required = false) List<Long> customerIds) {
        if (statisticsDate == null) {
            statisticsDate = LocalDate.now();
        }
        List<ArAgingReportVO> list = reportService.queryArAgingReport(statisticsDate, customerIds);
        return getDataTable(list);
    }

    /**
     * 导出应收账款账龄分析表
     */
    @ApiOperation("导出应收账款账龄分析表")
    @PreAuthorize("@ss.hasPermi('wms:report:export')")
    @PostMapping("/finance/arAging/export")
    public void exportArAging(HttpServletResponse response,
            @RequestParam(required = false) LocalDate statisticsDate,
            @RequestParam(required = false) List<Long> customerIds) {
        if (statisticsDate == null) {
            statisticsDate = LocalDate.now();
        }
        List<ArAgingReportVO> list = reportService.queryArAgingReport(statisticsDate, customerIds);
        ExcelUtil<ArAgingReportVO> util = new ExcelUtil<>(ArAgingReportVO.class);
        util.exportExcel(response, list, "应收账款账龄分析表");
    }

    /**
     * 查询供应商采购分析表
     */
    @ApiOperation("供应商采购分析表")
    @PreAuthorize("@ss.hasPermi('wms:report:purchase')")
    @GetMapping("/purchase/supplierAnalysis")
    public TableDataInfo supplierAnalysisReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> supplierIds) {
        List<SupplierAnalysisReportVO> list = reportService.querySupplierAnalysisReport(startDate, endDate, supplierIds);
        return getDataTable(list);
    }

    /**
     * 导出供应商采购分析表
     */
    @ApiOperation("导出供应商采购分析表")
    @PreAuthorize("@ss.hasPermi('wms:report:export')")
    @PostMapping("/purchase/supplierAnalysis/export")
    public void exportSupplierAnalysis(HttpServletResponse response,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> supplierIds) {
        List<SupplierAnalysisReportVO> list = reportService.querySupplierAnalysisReport(startDate, endDate, supplierIds);
        ExcelUtil<SupplierAnalysisReportVO> util = new ExcelUtil<>(SupplierAnalysisReportVO.class);
        util.exportExcel(response, list, "供应商采购分析表");
    }
}
