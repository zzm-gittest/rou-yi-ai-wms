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
import java.time.LocalDateTime;

/**
 * 报表订阅表 wms_report_subscription
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_report_subscription")
@ApiModel(value = "WmsReportSubscription", description = "报表订阅表")
public class WmsReportSubscription extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "订阅ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "报表ID")
    @TableField("report_id")
    private Long reportId;

    @ApiModelProperty(value = "订阅名称")
    private String subscriptionName;

    @ApiModelProperty(value = "订阅用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiModelProperty(value = "推送频率：daily-每日, weekly-每周, monthly-每月")
    private String frequency;

    @ApiModelProperty(value = "推送时间")
    private String pushTime;

    @ApiModelProperty(value = "推送配置(JSON)")
    private String pushConfig;

    @ApiModelProperty(value = "最后推送时间")
    @TableField("last_push_time")
    private LocalDateTime lastPushTime;

    @ApiModelProperty(value = "状态：0-启用,1-停用")
    private String status;

    @ApiModelProperty(value = "租户ID")
    @TableField("tenant_id")
    private Long tenantId;
}
