package com.ruoyi.wms.domain.vo.goodsSpec;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品规格响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("商品规格 Response VO")
@Data
public class WmsGoodsSpecRespVO {

    @ApiModelProperty(value = "规格ID")
    private Long id;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "规格名称")
    private String specName;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "规格值列表")
    private List<WmsGoodsSpecValueRespVO> values;
}
