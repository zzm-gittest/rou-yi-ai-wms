package com.ruoyi.wms.domain.vo.importTask;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建导入任务 Request VO
 */
@ApiModel("创建导入任务 Request VO")
@Data
public class CreateImportTaskReqVO {

    @ApiModelProperty(value = "任务类型", required = true, example = "unit")
    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    @ApiModelProperty(value = "策略JSON")
    private String strategyJson;
}
