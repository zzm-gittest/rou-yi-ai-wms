package com.ruoyi.wms.domain.vo.expenseType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 费用类型保存 Request VO
 *
 * @author ruoyi
 */
@ApiModel("费用类型保存 Request VO")
@Data
public class WmsExpenseTypeSaveReqVO {

    @ApiModelProperty(value = "类型ID")
    private Long id;

    @ApiModelProperty(value = "类型名称")
    @NotBlank(message = "类型名称不能为空")
    private String typeName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;
}
