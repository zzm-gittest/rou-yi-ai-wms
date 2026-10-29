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
 * 供应商采购分析表VO
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "SupplierAnalysisReportVO", description = "供应商采购分析表")
public class SupplierAnalysisReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "供应商编码", sort = 1)
    @ApiModelProperty(value = "供应商编码")
    private String supplierCode;

    @Excel(name = "供应商名称", sort = 2)
    @ApiModelProperty(value = "供应商名称")
    private String supplierName;

    @Excel(name = "联系人", sort = 3)
    @ApiModelProperty(value = "联系人")
    private String contact;

    @Excel(name = "联系电话", sort = 4)
    @ApiModelProperty(value = "联系电话")
    private String phone;

    @Excel(name = "采购订单数", sort = 5)
    @ApiModelProperty(value = "采购订单数")
    private Integer orderCount;

    @Excel(name = "订单金额", sort = 6)
    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @Excel(name = "入库金额", sort = 7)
    @ApiModelProperty(value = "入库金额")
    private BigDecimal inAmount;

    @Excel(name = "退货金额", sort = 8)
    @ApiModelProperty(value = "退货金额")
    private BigDecimal returnAmount;

    @Excel(name = "净采购额", sort = 9)
    @ApiModelProperty(value = "净采购额")
    private BigDecimal netPurchase;

    @Excel(name = "应付金额", sort = 10)
    @ApiModelProperty(value = "应付金额")
    private BigDecimal apAmount;

    @Excel(name = "已付金额", sort = 11)
    @ApiModelProperty(value = "已付金额")
    private BigDecimal paidAmount;

    @Excel(name = "付款率(%)", sort = 12)
    @ApiModelProperty(value = "付款率(%)")
    private BigDecimal paymentRate;

    @Excel(name = "平均采购价", sort = 13)
    @ApiModelProperty(value = "平均采购价")
    private BigDecimal avgPrice;

    @Excel(name = "最后下单日期", sort = 14, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "最后下单日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastOrderDate;
}
