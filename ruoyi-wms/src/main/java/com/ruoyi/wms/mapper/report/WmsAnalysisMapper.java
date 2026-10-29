package com.ruoyi.wms.mapper.report;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 经营分析数据 Mapper
 * <p>
 * 为 AI 经营分析功能提供专用查询方法。
 *
 * @author ruoyi
 */
@Mapper
public interface WmsAnalysisMapper {

    /**
     * 按时间段汇总销售出库金额
     */
    BigDecimal sumSalesAmount(@Param("tenantId") Long tenantId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("warehouseId") Long warehouseId);

    /**
     * 按时间段汇总销售退货金额
     */
    BigDecimal sumSalesReturnAmount(@Param("tenantId") Long tenantId,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("warehouseId") Long warehouseId);

    /**
     * 按时间段统计销售出库单数
     */
    int countSalesOutOrders(@Param("tenantId") Long tenantId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("warehouseId") Long warehouseId);

    /**
     * 按时间段汇总销售出库成本
     */
    BigDecimal sumSalesCost(@Param("tenantId") Long tenantId,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate,
                            @Param("warehouseId") Long warehouseId);

    /**
     * 按时间段汇总采购入库金额
     */
    BigDecimal sumPurchaseAmount(@Param("tenantId") Long tenantId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("warehouseId") Long warehouseId);

    /**
     * 按时间段汇总采购退货金额
     */
    BigDecimal sumPurchaseReturnAmount(@Param("tenantId") Long tenantId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("warehouseId") Long warehouseId);

    /**
     * 按时间段统计采购入库单数
     */
    int countPurchaseInOrders(@Param("tenantId") Long tenantId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("warehouseId") Long warehouseId);

    /**
     * 热销商品 TOP N
     */
    List<Map<String, Object>> selectTopSellingGoods(@Param("tenantId") Long tenantId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate,
                                                     @Param("limit") int limit);

    /**
     * 客户销售排名 TOP N
     */
    List<Map<String, Object>> selectCustomerSalesRanking(@Param("tenantId") Long tenantId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate,
                                                          @Param("limit") int limit);

    /**
     * 商品日均销量（近 N 天）
     */
    List<Map<String, Object>> selectDailyAvgSales(@Param("tenantId") Long tenantId,
                                                    @Param("days") int days);

    /**
     * 应收账款汇总
     */
    Map<String, Object> selectReceivableSummary(@Param("tenantId") Long tenantId);

    /**
     * 逾期应收列表
     */
    List<Map<String, Object>> selectOverdueReceivables(@Param("tenantId") Long tenantId,
                                                        @Param("overdueDays") int overdueDays);

    /**
     * 待审核采购订单数
     */
    int countPendingPurchaseOrders(@Param("tenantId") Long tenantId);

    /**
     * 待审核销售订单数
     */
    int countPendingSalesOrders(@Param("tenantId") Long tenantId);

    /**
     * 已审核但超 N 天未创建入库单的采购订单
     */
    List<Map<String, Object>> selectPendingPurchaseIn(@Param("tenantId") Long tenantId,
                                                       @Param("overdueDays") int overdueDays);

    /**
     * 已审核但超 N 天未创建出库单的销售订单
     */
    List<Map<String, Object>> selectPendingSalesOut(@Param("tenantId") Long tenantId,
                                                     @Param("overdueDays") int overdueDays);

    /**
     * 滞销商品（近 N 天零销售且库存金额 > minAmount）
     */
    List<Map<String, Object>> selectStaledGoods(@Param("tenantId") Long tenantId,
                                                 @Param("days") int days,
                                                 @Param("minAmount") BigDecimal minAmount);

    /**
     * 库存预警：当前库存 ÷ 日均销量 ≤ threshold 天
     */
    List<Map<String, Object>> selectStockShortage(@Param("tenantId") Long tenantId,
                                                    @Param("avgDays") int avgDays,
                                                    @Param("thresholdDays") int thresholdDays);
}
