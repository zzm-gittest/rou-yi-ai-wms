package com.ruoyi.wms.domain.vo.purchaseOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购订单新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购订单新增/修改 Request VO")
@Data
public class WmsPurchaseOrderSaveReqVO {

    @ApiModelProperty(value = "订单ID")
    private Long id;

    @ApiModelProperty(value = "供应商ID")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @ApiModelProperty(value = "订单日期")
    private LocalDate orderDate;

    @ApiModelProperty(value = "交货日期")
    private LocalDate deliveryDate;

    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "已付金额")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "结算账户ID")
    private Long accountId;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    @Valid
    private List<WmsPurchaseOrderItemSaveReqVO> items;
}
