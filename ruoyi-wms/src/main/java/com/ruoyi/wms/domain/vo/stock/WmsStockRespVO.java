package com.ruoyi.wms.domain.vo.stock;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("库存 Response VO")
@Data
public class WmsStockRespVO {

    @ApiModelProperty(value = "库存ID")
    private Long id;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @ApiModelProperty(value = "SKU ID")
    private Long skuId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "规格信息")
    private String specInfo;

    @ApiModelProperty(value = "库存数量")
    private BigDecimal quantity;

    @ApiModelProperty(value = "成本价（移动加权平均）")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "采购价")
    private BigDecimal purchasePrice;

    @ApiModelProperty(value = "销售价")
    private BigDecimal salePrice;

    @ApiModelProperty(value = "批发价")
    private BigDecimal wholesalePrice;

    @ApiModelProperty(value = "库存金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
