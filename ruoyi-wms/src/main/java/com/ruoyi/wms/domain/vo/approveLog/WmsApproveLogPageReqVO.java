package com.ruoyi.wms.domain.vo.approveLog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核日志分页查询 Request VO
 */
@ApiModel("审核日志分页查询 Request VO")
@Data
public class WmsApproveLogPageReqVO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "单据类型")
    private String billType;

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
}
