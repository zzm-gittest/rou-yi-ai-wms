package com.ruoyi.wms.domain.vo.goods;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("商品 Response VO")
@Data
public class WmsGoodsRespVO {

    @ApiModelProperty(value = "商品ID")
    private Long id;

    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @ApiModelProperty(value = "单位ID")
    private Long unitId;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "产地")
    private String place;

    @ApiModelProperty(value = "商品主图")
    private String image;

    @ApiModelProperty(value = "商品图片集（JSON）")
    private String images;

    @ApiModelProperty(value = "商品详情")
    private String detail;

    @ApiModelProperty(value = "是否有规格（0无 1有）")
    private String hasSpec;

    @ApiModelProperty(value = "最小库存（SKU合计）")
    private Integer minStock;

    @ApiModelProperty(value = "最大库存（SKU合计）")
    private Integer maxStock;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "SKU列表")
    private List<com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO> skuList;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
