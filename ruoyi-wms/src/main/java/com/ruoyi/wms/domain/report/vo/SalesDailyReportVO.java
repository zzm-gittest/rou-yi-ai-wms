package com.ruoyi.wms.domain.report.vo;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售日报表VO
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "SalesDailyReportVO", description = "销售日报表")
public class SalesDailyReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报表日期")
    @Excel(name = "报表日期", sort = 1)
    private String reportDate;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "客户名称")
    @Excel(name = "客户名称", sort = 2)
    private String customerName;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    @Excel(name = "仓库名称", sort = 3)
    private String warehouseName;

    @ApiModelProperty(value = "销售订单数")
    @Excel(name = "订单数", sort = 4, cellType = Excel.ColumnType.NUMERIC)
    private Integer orderCount;

    @ApiModelProperty(value = "订单金额")
    @Excel(name = "订单金额", sort = 5, cellType = Excel.ColumnType.NUMERIC, scale = 2)
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "出库单数")
    @Excel(name = "出库单数", sort = 6, cellType = Excel.ColumnType.NUMERIC)
    private Integer outCount;

    @ApiModelProperty(value = "出库金额")
    @Excel(name = "出库金额", sort = 7, cellType = Excel.ColumnType.NUMERIC, scale = 2)
    private BigDecimal outAmount;

    @ApiModelProperty(value = "退货单数")
    @Excel(name = "退货单数", sort = 8, cellType = Excel.ColumnType.NUMERIC)
    private Integer returnCount;

    @ApiModelProperty(value = "退货金额")
    @Excel(name = "退货金额", sort = 9, cellType = Excel.ColumnType.NUMERIC, scale = 2)
    private BigDecimal returnAmount;

    @ApiModelProperty(value = "净销售额")
    @Excel(name = "净销售额", sort = 10, cellType = Excel.ColumnType.NUMERIC, scale = 2)
    private BigDecimal netSales;

    @ApiModelProperty(value = "毛利")
    @Excel(name = "毛利", sort = 11, cellType = Excel.ColumnType.NUMERIC, scale = 2)
    private BigDecimal grossProfit;

    @ApiModelProperty(value = "毛利率(%)")
    @Excel(name = "毛利率(%)", sort = 12, cellType = Excel.ColumnType.NUMERIC, scale = 2, suffix = "%")
    private BigDecimal grossProfitRate;
}
