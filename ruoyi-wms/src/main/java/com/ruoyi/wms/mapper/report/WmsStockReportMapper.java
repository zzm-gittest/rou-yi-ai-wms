package com.ruoyi.wms.mapper.report;

import com.ruoyi.wms.domain.report.vo.StockPsiReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 库存报表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsStockReportMapper {

    /**
     * 查询库存进销存报表
     */
    List<StockPsiReportVO> queryStockPsiReport(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("categoryIds") List<Long> categoryIds,
                                               @Param("warehouseIds") List<Long> warehouseIds,
                                               @Param("goodsName") String goodsName);
}
