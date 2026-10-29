package com.ruoyi.wms.mapper.report;

import com.ruoyi.wms.domain.report.vo.ArAgingReportVO;
import com.ruoyi.wms.domain.report.vo.SupplierAnalysisReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务报表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsFinanceReportMapper {

    /**
     * 查询应收账款账龄分析表
     */
    List<ArAgingReportVO> queryArAgingReport(@Param("statisticsDate") LocalDate statisticsDate,
                                             @Param("customerIds") List<Long> customerIds);

    /**
     * 查询供应商采购分析表
     */
    List<SupplierAnalysisReportVO> querySupplierAnalysisReport(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate,
                                                                @Param("supplierIds") List<Long> supplierIds);
}
