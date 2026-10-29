package com.ruoyi.wms.domain.vo.stockCheck;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存盘点单明细响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("库存盘点单明细 Response VO")
@Data
public class WmsStockCheckItemRespVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "盘点单ID")
    private Long checkId;

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

    @ApiModelProperty(value = "账面数量")
    private BigDecimal beforeQty;

    @ApiModelProperty(value = "实际数量")
    private BigDecimal checkQty;

    @ApiModelProperty(value = "差异数量")
    private BigDecimal diffQty;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "差异金额")
    private BigDecimal diffAmount;

    @ApiModelProperty(value = "备注")
    private String remark;
}
