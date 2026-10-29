package com.ruoyi.wms.domain.vo.purchaseIn;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购入库明细响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("采购入库明细 Response VO")
@Data
public class WmsPurchaseInItemRespVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "入库单ID")
    private Long inId;

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

    @ApiModelProperty(value = "订单数量（订单入库时有效）")
    private BigDecimal orderQuantity;

    @ApiModelProperty(value = "已入库数量（订单入库时有效）")
    private BigDecimal receivedQty;

    @ApiModelProperty(value = "已退货数量")
    private BigDecimal returnedQty;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "备注")
    private String remark;
}
