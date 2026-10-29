package com.ruoyi.wms.service.report;

import com.ruoyi.wms.domain.report.vo.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表 Service接口
 *
 * @author ruoyi
 */
public interface IWmsReportService {

    /**
     * 获取报表分类列表
     */
    List<Map<String, Object>> getReportCategories();

    /**
     * 根据报表编码获取报表配置
     *
     * @param reportCode 报表编码
     * @return 报表配置
     */
    Map<String, Object> getReportConfig(String reportCode);

    /**
     * 通用报表查询
     *
     * @param reportCode 报表编码
     * @param params 查询参数
     * @return 查询结果
     */
    Map<String, Object> queryReport(String reportCode, Map<String, Object> params);

    /**
     * 获取今日销售数据
     *
     * @param warehouseId 仓库ID（可选，0或null表示全部仓库）
     * @return 今日销售数据
     */
    TodaySalesVO getTodaySales(Long warehouseId);

    // ==================== 销售报表 ====================

    /**
     * 查询销售日报表
     * @param groupBy 分组维度：date-按日期,customer-按客户,warehouse-按仓库,customer_warehouse-按客户和仓库
     */
    List<SalesDailyReportVO> querySalesDailyReport(LocalDate startDate, LocalDate endDate,
                                                     List<Long> customerIds, List<Long> warehouseIds, String groupBy);

    /**
     * 查询商品销售排行榜
     */
    List<GoodsRankReportVO> queryGoodsRankReport(LocalDate startDate, LocalDate endDate,
                                                    List<Long> categoryIds, List<Long> warehouseIds, String orderBy);

    /**
     * 查询客户销售分析表
     */
    List<CustomerAnalysisReportVO> queryCustomerAnalysisReport(LocalDate startDate, LocalDate endDate,
                                                                 List<Long> customerIds);

    // ==================== 库存报表 ====================

    /**
     * 查询库存进销存报表
     */
    List<StockPsiReportVO> queryStockPsiReport(LocalDate startDate, LocalDate endDate,
                                                List<Long> categoryIds, List<Long> warehouseIds, String goodsName);

    // ==================== 财务报表 ====================

    /**
     * 查询应收账款账龄分析表
     */
    List<ArAgingReportVO> queryArAgingReport(LocalDate statisticsDate, List<Long> customerIds);

    /**
     * 查询供应商采购分析表
     */
    List<SupplierAnalysisReportVO> querySupplierAnalysisReport(LocalDate startDate, LocalDate endDate,
                                                                List<Long> supplierIds);
}
