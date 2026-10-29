package com.ruoyi.wms.domain.vo.approvePolicy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 租户单据审核策略配置 Request VO
 */
@ApiModel("租户单据审核策略配置 Request VO")
@Data
public class WmsBillApprovePolicySaveReqVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "租户ID", required = true)
    @NotNull(message = "租户ID不能为空")
    private Long tenantId;

    @ApiModelProperty(value = "单据类型", required = true)
    @NotBlank(message = "单据类型不能为空")
    private String billType;

    @ApiModelProperty(value = "是否需要审核(1是,0否)", required = true)
    @NotNull(message = "是否需要审核不能为空")
    private Integer needApprove;

    @ApiModelProperty(value = "免审时是否自动打印小票(1是,0否)")
    private Integer autoPrint;

    @ApiModelProperty(value = "状态(0启用,1停用)", required = true)
    @NotBlank(message = "状态不能为空")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;
}
