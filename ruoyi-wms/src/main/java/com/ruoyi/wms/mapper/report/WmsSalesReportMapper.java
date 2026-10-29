package com.ruoyi.wms.mapper.report;

import com.ruoyi.wms.domain.report.vo.CustomerAnalysisReportVO;
import com.ruoyi.wms.domain.report.vo.GoodsRankReportVO;
import com.ruoyi.wms.domain.report.vo.SalesDailyReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售报表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsSalesReportMapper {

    BigDecimal getTodaySalesOut(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    BigDecimal getTodaySalesReturn(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    BigDecimal getTodaySalesOutCost(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    BigDecimal getTodaySalesReturnCost(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    BigDecimal getTodayPurchaseIn(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    BigDecimal getTodayPurchaseReturn(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    LocalDateTime getLastUpdateTime(@Param("tenantId") Long tenantId, @Param("warehouseId") Long warehouseId);

    /**
     * 查询销售日报表
     * @param groupBy 分组维度：date-按日期,customer-按客户,warehouse-按仓库,customer_warehouse-按客户和仓库
     */
    List<SalesDailyReportVO> querySalesDailyReport(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("customerIds") List<Long> customerIds,
                                                    @Param("warehouseIds") List<Long> warehouseIds,
                                                    @Param("groupBy") String groupBy);

    /**
     * 查询商品销售排行榜
     */
    List<GoodsRankReportVO> queryGoodsRankReport(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("categoryIds") List<Long> categoryIds,
                                                  @Param("warehouseIds") List<Long> warehouseIds,
                                                  @Param("orderBy") String orderBy);

    /**
     * 查询客户销售分析表
     */
    List<CustomerAnalysisReportVO> queryCustomerAnalysisReport(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate,
                                                                @Param("customerIds") List<Long> customerIds);
}
