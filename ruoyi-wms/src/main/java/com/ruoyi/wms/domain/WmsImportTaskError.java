package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 导入任务错误明细
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_import_task_error")
@ApiModel(value = "WmsImportTaskError", description = "导入任务错误明细")
public class WmsImportTaskError extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "租户ID")
    @TableField("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "任务ID")
    @TableField("task_id")
    private Long taskId;

    @ApiModelProperty(value = "行号")
    @TableField("row_num")
    private Integer rowNum;

    @ApiModelProperty(value = "错误码")
    @TableField("error_code")
    private String errorCode;

    @ApiModelProperty(value = "错误信息")
    @TableField("error_msg")
    private String errorMsg;

    @ApiModelProperty(value = "原始数据JSON")
    @TableField("raw_data_json")
    private String rawDataJson;
}
