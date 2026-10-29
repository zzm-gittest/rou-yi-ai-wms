package com.ruoyi.wms.domain.vo.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 结算账户分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("结算账户分页查询 Request VO")
@Data
public class WmsAccountPageReqVO {

    @ApiModelProperty(value = "账户编码")
    private String accountCode;

    @ApiModelProperty(value = "账户名称")
    private String accountName;

    @ApiModelProperty(value = "账户类型（1现金 2银行 3支付宝 4微信）")
    private String accountType;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
