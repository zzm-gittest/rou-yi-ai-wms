package com.ruoyi.wms.domain.vo.goodsSpec;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 商品规格值新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品规格值新增/修改 Request VO")
@Data
public class WmsGoodsSpecValueSaveReqVO {

    @ApiModelProperty(value = "规格值ID")
    private Long id;

    @ApiModelProperty(value = "规格ID")
    private Long specId;

    @ApiModelProperty(value = "规格值", required = true)
    @NotBlank(message = "规格值不能为空")
    private String specValue;

    @ApiModelProperty(value = "排序")
    private Integer sort = 0;
}
