package com.ruoyi.wms.domain.report.vo;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 应收账款账龄分析表VO
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "ArAgingReportVO", description = "应收账款账龄分析表")
public class ArAgingReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "客户编码", sort = 1)
    @ApiModelProperty(value = "客户编码")
    private String customerCode;

    @Excel(name = "客户名称", sort = 2)
    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @Excel(name = "应收总额", sort = 3)
    @ApiModelProperty(value = "应收总额")
    private BigDecimal totalAr;

    @Excel(name = "30天内", sort = 4)
    @ApiModelProperty(value = "30天内")
    private BigDecimal within30;

    @Excel(name = "30-60天", sort = 5)
    @ApiModelProperty(value = "30-60天")
    private BigDecimal days30_60;

    @Excel(name = "60-90天", sort = 6)
    @ApiModelProperty(value = "60-90天")
    private BigDecimal days60_90;

    @Excel(name = "90-180天", sort = 7)
    @ApiModelProperty(value = "90-180天")
    private BigDecimal days90_180;

    @Excel(name = "180天以上", sort = 8)
    @ApiModelProperty(value = "180天以上")
    private BigDecimal over180;

    @Excel(name = "逾期金额", sort = 9)
    @ApiModelProperty(value = "逾期金额")
    private BigDecimal overdueAmount;

    @Excel(name = "逾期率(%)", sort = 10)
    @ApiModelProperty(value = "逾期率(%)")
    private BigDecimal overdueRate;
}
