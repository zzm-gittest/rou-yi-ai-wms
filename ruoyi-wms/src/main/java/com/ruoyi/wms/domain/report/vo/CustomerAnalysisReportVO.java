package com.ruoyi.wms.domain.report.vo;

import com.ruoyi.common.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 客户销售分析表VO
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "CustomerAnalysisReportVO", description = "客户销售分析表")
public class CustomerAnalysisReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "客户编码", sort = 1)
    @ApiModelProperty(value = "客户编码")
    private String customerCode;

    @Excel(name = "客户名称", sort = 2)
    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @Excel(name = "联系人", sort = 3)
    @ApiModelProperty(value = "联系人")
    private String contact;

    @Excel(name = "联系电话", sort = 4)
    @ApiModelProperty(value = "联系电话")
    private String phone;

    @Excel(name = "订单数", sort = 5)
    @ApiModelProperty(value = "订单数")
    private Integer orderCount;

    @Excel(name = "订单金额", sort = 6)
    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @Excel(name = "出库金额", sort = 7)
    @ApiModelProperty(value = "出库金额")
    private BigDecimal outAmount;

    @Excel(name = "退货金额", sort = 8)
    @ApiModelProperty(value = "退货金额")
    private BigDecimal returnAmount;

    @Excel(name = "净销售额", sort = 9)
    @ApiModelProperty(value = "净销售额")
    private BigDecimal netSales;

    @Excel(name = "应收金额", sort = 10)
    @ApiModelProperty(value = "应收金额")
    private BigDecimal arAmount;

    @Excel(name = "已收金额", sort = 11)
    @ApiModelProperty(value = "已收金额")
    private BigDecimal receivedAmount;

    @Excel(name = "回款率(%)", sort = 12)
    @ApiModelProperty(value = "回款率(%)")
    private BigDecimal receiptRate;

    @Excel(name = "最后下单日期", sort = 13, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "最后下单日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastOrderDate;
}
