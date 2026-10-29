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
 * 采购入库单表 wms_purchase_in
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_purchase_in")
@ApiModel(value = "WmsPurchaseIn", description = "采购入库单表")
public class WmsPurchaseIn extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "入库单号")
    private String inNo;

    @ApiModelProperty(value = "采购订单ID")
    private Long orderId;

    @ApiModelProperty(value = "来源类型（1直接入库 2采购订单入库）")
    private Integer sourceType;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "入库日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate inDate;

    @ApiModelProperty(value = "入库总额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "已付金额")
    private BigDecimal paidAmount;

    @ApiModelProperty(value = "付款状态（0未付 1部分 2已付）")
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

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String status;

    @ApiModelProperty(value = "退货状态（0未退货 1部分退货 2全部退货）")
    private String returnStatus;

    @ApiModelProperty(value = "备注")
    private String remark;

    // ========== OCR识别相关字段 ==========

    @ApiModelProperty(value = "OCR识别单据号")
    private String ocrOrderNo;

    @ApiModelProperty(value = "OCR识别图片URL")
    private String ocrImageUrl;

}
