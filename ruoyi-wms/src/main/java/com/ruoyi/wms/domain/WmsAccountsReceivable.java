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
 * 应收账款表 wms_accounts_receivable
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_accounts_receivable")
@ApiModel(value = "WmsAccountsReceivable", description = "应收账款表")
public class WmsAccountsReceivable extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "供应商ID（采购退货时使用）")
    private Long supplierId;

    @ApiModelProperty(value = "单据类型（1销售订单 2销售出库 3采购退货）")
    private String billType;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据ID")
    private Long billId;

    @ApiModelProperty(value = "单据金额")
    private BigDecimal billAmount;

    @ApiModelProperty(value = "已收金额")
    private BigDecimal receivedAmount;

    @ApiModelProperty(value = "应收金额")
    private BigDecimal arAmount;

    @ApiModelProperty(value = "状态（0未结清 1已结清）")
    private String arStatus;

    @ApiModelProperty(value = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @ApiModelProperty(value = "到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @ApiModelProperty(value = "收款凭证")
    private String collectionProof;

    @ApiModelProperty(value = "备注")
    private String remark;

}
