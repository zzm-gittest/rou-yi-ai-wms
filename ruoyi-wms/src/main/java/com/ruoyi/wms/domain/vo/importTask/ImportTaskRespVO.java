package com.ruoyi.wms.domain.vo.importTask;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导入任务 Response VO
 */
@ApiModel("导入任务 Response VO")
@Data
public class ImportTaskRespVO {

    @ApiModelProperty(value = "任务ID")
    private Long id;

    @ApiModelProperty(value = "任务编号")
    private String taskNo;

    @ApiModelProperty(value = "任务类型")
    private String taskType;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "状态文案")
    private String statusLabel;

    @ApiModelProperty(value = "文件地址")
    private String fileUrl;

    @ApiModelProperty(value = "总行数")
    private Integer totalRows;

    @ApiModelProperty(value = "成功行数")
    private Integer successRows;

    @ApiModelProperty(value = "失败行数")
    private Integer failRows;

    @ApiModelProperty(value = "策略JSON")
    private String strategyJson;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endedTime;
}
