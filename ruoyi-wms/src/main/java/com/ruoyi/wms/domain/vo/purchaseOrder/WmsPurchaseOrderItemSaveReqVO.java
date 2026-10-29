package com.ruoyi.wms.domain.vo.purchaseOrder;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 采购订单明细新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购订单明细新增/修改 Request VO")
@Data
public class WmsPurchaseOrderItemSaveReqVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "SKU ID（与商品名称二选一，优先使用skuId）")
    private Long skuId;

    @ApiModelProperty(value = "商品名称（skuId为空时必填，用于自动创建商品）")
    @com.fasterxml.jackson.annotation.JsonProperty("skuName")
    private String goodsName;

    @ApiModelProperty(value = "商品规格")
    @com.fasterxml.jackson.annotation.JsonProperty("specInfo")
    private String spec;

    @ApiModelProperty(value = "单位")
    @com.fasterxml.jackson.annotation.JsonProperty("unitName")
    private String unit;

    @ApiModelProperty(value = "数量")
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "折扣额")
    private BigDecimal discount = BigDecimal.ZERO;

    @ApiModelProperty(value = "备注")
    private String remark;
}
