package com.ruoyi.wms.domain.vo.approvePolicy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 租户单据审核策略分页查询 Request VO
 */
@ApiModel("租户单据审核策略分页查询 Request VO")
@Data
public class WmsBillApprovePolicyPageReqVO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "单据类型")
    private String billType;

    @ApiModelProperty(value = "是否需要审核(1是,0否)")
    private Integer needApprove;

    @ApiModelProperty(value = "状态(0启用,1停用)")
    private String status;
}
