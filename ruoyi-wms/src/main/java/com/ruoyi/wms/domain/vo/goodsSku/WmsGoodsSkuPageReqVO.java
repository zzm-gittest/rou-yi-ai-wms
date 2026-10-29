package com.ruoyi.wms.domain.vo.goodsSku;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品SKU分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品SKU分页查询 Request VO")
@Data
public class WmsGoodsSkuPageReqVO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "商品编码（模糊搜索）")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称（模糊搜索）")
    private String goodsName;

    @ApiModelProperty(value = "关键词（综合搜索SKU名称、规格、商品名称、SKU编码、商品编码、条码）")
    private String keyword;
}
