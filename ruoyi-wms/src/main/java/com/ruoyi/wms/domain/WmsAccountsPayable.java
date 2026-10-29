package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应付账款表 wms_accounts_payable
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_accounts_payable")
@ApiModel(value = "WmsAccountsPayable", description = "应付账款表")
public class WmsAccountsPayable extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "客户ID（销售退货时使用）")
    private Long customerId;

    @ApiModelProperty(value = "单据类型（1采购订单 2采购入库 3销售退货）")
    private String billType;

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

    @ApiModelProperty(value = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @ApiModelProperty(value = "到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @ApiModelProperty(value = "付款凭证")
    private String paymentProof;

    @ApiModelProperty(value = "备注")
    private String remark;

}
