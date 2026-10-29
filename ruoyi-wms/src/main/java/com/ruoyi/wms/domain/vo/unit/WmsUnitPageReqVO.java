package com.ruoyi.wms.domain.vo.unit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 计量单位分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("计量单位分页查询 Request VO")
@Data
public class WmsUnitPageReqVO {

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
