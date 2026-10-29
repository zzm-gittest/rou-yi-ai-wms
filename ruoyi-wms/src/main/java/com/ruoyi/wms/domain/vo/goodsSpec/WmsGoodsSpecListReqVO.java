package com.ruoyi.wms.domain.vo.goodsSpec;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商品规格查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品规格查询 Request VO")
@Data
public class WmsGoodsSpecListReqVO {

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;
}
