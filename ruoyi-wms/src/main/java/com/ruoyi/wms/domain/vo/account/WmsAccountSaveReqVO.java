package com.ruoyi.wms.domain.vo.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 结算账户保存 Request VO
 *
 * @author ruoyi
 */
@ApiModel("结算账户保存 Request VO")
@Data
public class WmsAccountSaveReqVO {

    @ApiModelProperty(value = "账户ID")
    private Long id;

    @ApiModelProperty(value = "账户编码")
    private String accountCode;

    @ApiModelProperty(value = "账户名称")
    @NotBlank(message = "账户名称不能为空")
    private String accountName;

    @ApiModelProperty(value = "账户类型（1现金 2银行 3支付宝 4微信）")
    @NotBlank(message = "账户类型不能为空")
    private String accountType;

    @ApiModelProperty(value = "初始余额")
    private BigDecimal initBalance;

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
