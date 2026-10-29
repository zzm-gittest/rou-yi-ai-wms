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
 * 销售退货单表 wms_sales_return
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_sales_return")
@ApiModel(value = "WmsSalesReturn", description = "销售退货单表")
public class WmsSalesReturn extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "退货单号")
    private String returnNo;

    @ApiModelProperty(value = "销售出库ID")
    private Long outId;

    @ApiModelProperty(value = "来源类型（1直接入库 2出库单退货）")
    private Integer sourceType;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "退货日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate;

    @ApiModelProperty(value = "退货总额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "已退金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款状态（0未退 1部分 2已退）")
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

    @ApiModelProperty(value = "备注")
    private String remark;

}
