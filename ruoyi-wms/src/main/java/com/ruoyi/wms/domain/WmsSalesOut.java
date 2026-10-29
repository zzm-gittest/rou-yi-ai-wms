package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.ruoyi.common.core.domain.BaseDO;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售出库单表 wms_sales_out
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_sales_out")
@ApiModel(value = "WmsSalesOut", description = "销售出库单表")
public class WmsSalesOut extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "出库单号")
    private String outNo;

    @ApiModelProperty(value = "销售订单号")
    private String orderNo;

    @ApiModelProperty(value = "来源单号（飞书单据号等，导入防重）")
    @TableField("source_bill_no")
    private String sourceBillNo;

    @ApiModelProperty(value = "销售订单ID")
    private Long orderId;

    @ApiModelProperty(value = "来源类型（1直接出库 2订单出库）")
    private Integer sourceType;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "出库日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate outDate;

    @ApiModelProperty(value = "出库总额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "已收金额")
    private BigDecimal receivedAmount;

    @ApiModelProperty(value = "收款状态（0未收 1部分 2已收）")
    private String paymentStatus;

    @ApiModelProperty(value = "结算账户ID")
    private Long accountId;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "退货状态（0未退货 1部分退货 2全部退货）")
    private String returnStatus;

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

}
