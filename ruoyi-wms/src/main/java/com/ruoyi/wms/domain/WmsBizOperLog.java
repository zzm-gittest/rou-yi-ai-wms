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
 * WMS业务操作日志 wms_biz_oper_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_biz_oper_log")
@ApiModel(value = "WmsBizOperLog", description = "WMS业务操作日志")
public class WmsBizOperLog extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "业务模块")
    private String bizModule;

    @ApiModelProperty(value = "操作类型")
    private String bizType;

    @ApiModelProperty(value = "业务表名")
    private String bizTable;

    @ApiModelProperty(value = "业务数据ID")
    private Long bizId;

    @ApiModelProperty(value = "业务单号")
    private String bizNo;

    @ApiModelProperty(value = "操作人ID")
    private Long operUserId;

    @ApiModelProperty(value = "操作人用户名")
    private String operUserName;

    @ApiModelProperty(value = "操作IP")
    private String operIp;

    @ApiModelProperty(value = "请求URL")
    private String operUrl;

    @ApiModelProperty(value = "请求方法")
    private String operMethod;

    @ApiModelProperty(value = "请求参数")
    private String operParam;

    @ApiModelProperty(value = "返回结果")
    private String operResult;

    @ApiModelProperty(value = "操作状态(0成功,1失败)")
    private Integer status;

    @ApiModelProperty(value = "错误信息")
    private String errorMsg;

    @ApiModelProperty(value = "耗时(毫秒)")
    private Long costTime;
}
