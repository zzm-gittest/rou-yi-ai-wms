package com.ruoyi.wms.domain.vo.goodsSpec;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 商品规格新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品规格新增/修改 Request VO")
@Data
public class WmsGoodsSpecSaveReqVO {

    @ApiModelProperty(value = "规格ID")
    private Long id;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @ApiModelProperty(value = "规格名称", required = true)
    @NotBlank(message = "规格名称不能为空")
    private String specName;

    @ApiModelProperty(value = "排序")
    private Integer sort = 0;

    @ApiModelProperty(value = "规格值列表")
    @Valid
    private List<WmsGoodsSpecValueSaveReqVO> values;
}
