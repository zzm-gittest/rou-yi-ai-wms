package com.ruoyi.wms.domain.vo.importTask;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 导入任务分页查询 Request VO
 */
@ApiModel("导入任务分页查询 Request VO")
@Data
public class ImportTaskPageReqVO {

    @ApiModelProperty(value = "任务编号")
    private String taskNo;

    @ApiModelProperty(value = "任务类型")
    private String taskType;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
