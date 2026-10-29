package com.ruoyi.wms.domain.vo.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算账户响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("结算账户 Response VO")
@Data
public class WmsAccountRespVO {

    @ApiModelProperty(value = "账户ID")
    private Long id;

    @ApiModelProperty(value = "账户编码")
    private String accountCode;

    @ApiModelProperty(value = "账户名称")
    private String accountName;

    @ApiModelProperty(value = "账户类型（1现金 2银行 3支付宝 4微信）")
    private String accountType;

    @ApiModelProperty(value = "账户类型名称")
    private String accountTypeName;

    @ApiModelProperty(value = "账户余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "开户银行")
    private String bankName;

    @ApiModelProperty(value = "银行账号")
    private String accountNo;

    @ApiModelProperty(value = "持卡人")
    private String holder;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;
}
