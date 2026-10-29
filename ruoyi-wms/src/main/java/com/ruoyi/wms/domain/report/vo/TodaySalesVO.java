package com.ruoyi.wms.domain.report.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 今日销售数据 VO
 */
@ApiModel("今日销售数据")
@Data
public class TodaySalesVO {

    @ApiModelProperty("销售出库金额")
    private BigDecimal salesAmount;

    @ApiModelProperty("销售退货金额")
    private BigDecimal returnAmount;

    @ApiModelProperty("采购入库金额")
    private BigDecimal purchaseAmount;

    @ApiModelProperty("采购退货金额")
    private BigDecimal purchaseReturnAmount;

    @ApiModelProperty("毛利")
    private BigDecimal grossProfit;

    @ApiModelProperty("更新时间")
    private String updateTime;
}
