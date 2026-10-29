package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 导入任务
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_import_task")
@ApiModel(value = "WmsImportTask", description = "导入任务")
public class WmsImportTask extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "租户ID")
    @TableField("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "任务编号")
    @TableField("task_no")
    private String taskNo;

    @ApiModelProperty(value = "任务类型")
    @TableField("task_type")
    private String taskType;

    @ApiModelProperty(value = "状态：0待执行/1执行中/2完成/3部分失败/4失败")
    private Integer status;

    @ApiModelProperty(value = "文件地址")
    @TableField("file_url")
    private String fileUrl;

    @ApiModelProperty(value = "总行数")
    @TableField("total_rows")
    private Integer totalRows;

    @ApiModelProperty(value = "成功行数")
    @TableField("success_rows")
    private Integer successRows;

    @ApiModelProperty(value = "失败行数")
    @TableField("fail_rows")
    private Integer failRows;

    @ApiModelProperty(value = "策略JSON")
    @TableField("strategy_json")
    private String strategyJson;

    @ApiModelProperty(value = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("ended_time")
    private LocalDateTime endedTime;
}
