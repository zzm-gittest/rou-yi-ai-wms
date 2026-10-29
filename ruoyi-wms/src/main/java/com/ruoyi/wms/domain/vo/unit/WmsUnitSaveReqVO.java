package com.ruoyi.wms.domain.vo.unit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 计量单位新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("计量单位新增/修改 Request VO")
@Data
public class WmsUnitSaveReqVO {

    @ApiModelProperty(value = "单位ID")
    private Long id;

    @ApiModelProperty(value = "单位名称", required = true)
    @NotBlank(message = "单位名称不能为空")
    private String unitName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status = "0";

    @ApiModelProperty(value = "备注")
    private String remark;
}
