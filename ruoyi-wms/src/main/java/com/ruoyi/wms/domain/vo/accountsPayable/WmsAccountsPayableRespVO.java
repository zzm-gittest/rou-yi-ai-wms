package com.ruoyi.wms.domain.vo.accountsPayable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应付账款响应VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("应付账款响应")
public class WmsAccountsPayableRespVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "供应商名称")
    private String supplierName;

    @ApiModelProperty(value = "客户ID（销售退货时使用）")
    private Long customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "单据类型（1采购订单 2采购入库 3销售退货）")
    private String billType;

    @ApiModelProperty(value = "单据类型名称")
    private String billTypeName;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据ID")
    private Long billId;

    @ApiModelProperty(value = "单据金额")
    private BigDecimal billAmount;

    @ApiModelProperty(value = "已付金额")
    private BigDecimal paidAmount;

    @ApiModelProperty(value = "应付金额")
    private BigDecimal apAmount;

    @ApiModelProperty(value = "状态（0未结清 1已结清）")
    private String apStatus;

    @ApiModelProperty(value = "状态名称")
    private String apStatusName;

    @ApiModelProperty(value = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @ApiModelProperty(value = "到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
