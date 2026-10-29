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
 * 租户单据审核策略表 wms_bill_approve_policy
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_bill_approve_policy")
@ApiModel(value = "WmsBillApprovePolicy", description = "租户单据审核策略表")
public class WmsBillApprovePolicy extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "单据类型")
    private String billType;

    @ApiModelProperty(value = "是否需要审核(1是,0否)")
    private Integer needApprove;

    @ApiModelProperty(value = "免审时是否自动打印小票(1是,0否)")
    private Integer autoPrint;

    @ApiModelProperty(value = "状态(0启用,1停用)")
    private String status;
}
