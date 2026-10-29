package com.ruoyi.wms.domain.vo.goodsSpec;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品规格值响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("商品规格值 Response VO")
@Data
public class WmsGoodsSpecValueRespVO {

    @ApiModelProperty(value = "规格值ID")
    private Long id;

    @ApiModelProperty(value = "规格ID")
    private Long specId;

    @ApiModelProperty(value = "规格值")
    private String specValue;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
