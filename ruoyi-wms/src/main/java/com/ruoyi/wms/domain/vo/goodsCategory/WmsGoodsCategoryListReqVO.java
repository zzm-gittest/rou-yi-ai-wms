package com.ruoyi.wms.domain.vo.goodsCategory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品分类查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品分类查询 Request VO")
@Data
public class WmsGoodsCategoryListReqVO {

    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;
}
