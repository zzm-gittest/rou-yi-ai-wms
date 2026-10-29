package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审核日志表 wms_approve_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_approve_log")
@ApiModel(value = "WmsApproveLog", description = "审核日志表")
public class WmsApproveLog extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "单据类型")
    private String billType;

    @ApiModelProperty(value = "单据ID")
    private Long billId;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "动作类型")
    private String actionType;

    @ApiModelProperty(value = "审核模式")
    private String approveMode;

    @ApiModelProperty(value = "结果")
    private String result;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "操作人ID")
    private Long operatorId;

    @ApiModelProperty(value = "策略快照")
    private String policySnapshot;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;
}
