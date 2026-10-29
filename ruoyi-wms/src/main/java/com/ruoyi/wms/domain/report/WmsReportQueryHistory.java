package com.ruoyi.wms.domain.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 报表查询历史表 wms_report_query_history
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_report_query_history")
@ApiModel(value = "WmsReportQueryHistory", description = "报表查询历史表")
public class WmsReportQueryHistory extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "记录ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "报表ID")
    @TableField("report_id")
    private Long reportId;

    @ApiModelProperty(value = "报表编码")
    @TableField("report_code")
    private String reportCode;

    @ApiModelProperty(value = "查询用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "查询用户名")
    @TableField("user_name")
    private String userName;

    @ApiModelProperty(value = "查询参数(JSON)")
    private String queryParams;

    @ApiModelProperty(value = "结果数量")
    @TableField("result_count")
    private Integer resultCount;

    @ApiModelProperty(value = "查询耗时(ms)")
    @TableField("query_time")
    private Integer queryTime;

    @ApiModelProperty(value = "租户ID")
    @TableField("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "备注")
    private String remark;
}
