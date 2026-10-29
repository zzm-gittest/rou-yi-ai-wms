package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.ruoyi.common.core.domain.BaseDO;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU表 wms_goods_sku
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_goods_sku")
@ApiModel(value = "WmsGoodsSku", description = "商品SKU表")
public class WmsGoodsSku extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "条形码")
    private String barcode;

    @ApiModelProperty(value = "单位ID")
    private Long unitId;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty("规格信息（JSON：{\"颜色\":\"红色\",\"尺码\":\"L\"}）")
    private String specInfo;

    @ApiModelProperty(value = "规格值ID组合（逗号分隔：100,103）")
    private String specIds;

    @ApiModelProperty(value = "采购价")
    private BigDecimal purchasePrice;

    @ApiModelProperty(value = "销售价")
    private BigDecimal salePrice;

    @ApiModelProperty(value = "批发价")
    private BigDecimal wholesalePrice;

    @ApiModelProperty(value = "成本价（移动加权平均）")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "最小库存")
    private Integer minStock;

    @ApiModelProperty(value = "最大库存")
    private Integer maxStock;

    @ApiModelProperty(value = "SKU图片")
    private String image;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

}
