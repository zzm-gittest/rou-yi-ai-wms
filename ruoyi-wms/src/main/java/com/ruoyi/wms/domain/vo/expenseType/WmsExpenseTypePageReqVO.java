package com.ruoyi.wms.domain.vo.expenseType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 费用类型分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("费用类型分页查询 Request VO")
@Data
public class WmsExpenseTypePageReqVO {

    @ApiModelProperty(value = "类型名称")
    private String typeName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
