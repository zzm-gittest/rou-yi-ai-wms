package com.ruoyi.wms.domain.vo.importTask;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 导入任务错误明细 Response VO
 */
@ApiModel("导入任务错误明细 Response VO")
@Data
public class ImportTaskErrorRespVO {

    @ApiModelProperty(value = "行号")
    private Integer rowNum;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "原始数据")
    private String rawDataJson;
}
