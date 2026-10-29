package com.ruoyi.wms.domain.vo.salesOut;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售出库明细响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("销售出库明细 Response VO")
@Data
public class WmsSalesOutItemRespVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "出库单ID")
    private Long outId;

    @ApiModelProperty(value = "SKU ID")
    private Long skuId;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "商品条码")
    private String barcode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "规格信息")
    private String specInfo;

    @ApiModelProperty(value = "单位ID")
    private Long unitId;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "数量")
    private BigDecimal quantity;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "已退货数量")
    private BigDecimal returnedQty;

    @ApiModelProperty(value = "订单数量")
    private BigDecimal orderQuantity;

    @ApiModelProperty(value = "已发货数量")
    private BigDecimal deliveredQty;

    @ApiModelProperty(value = "可发货数量")
    private BigDecimal availableQty;

    @ApiModelProperty(value = "备注")
    private String remark;
}
