package com.ruoyi.wms.domain.vo.expenseType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 费用类型响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("费用类型 Response VO")
@Data
public class WmsExpenseTypeRespVO {

    @ApiModelProperty(value = "类型ID")
    private Long id;

    @ApiModelProperty(value = "类型名称")
    private String typeName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;
}
