package com.ruoyi.wms.domain.vo.salesOrder;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售订单响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("销售订单 Response VO")
@Data
public class WmsSalesOrderRespVO {

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "订单日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;

    @ApiModelProperty(value = "交货日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;

    @ApiModelProperty(value = "订单总额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "应收金额")
    private BigDecimal receivableAmount;

    @ApiModelProperty(value = "已收金额")
    private BigDecimal receivedAmount;

    @ApiModelProperty(value = "收款状态（0未收 1部分 2已收）")
    private String paymentStatus;

    @ApiModelProperty(value = "订单状态（0待审核 1已审核 2部分发货 3全部发货 4已关闭）")
    private String orderStatus;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "明细列表")
    private List<WmsSalesOrderItemRespVO> items;
}
