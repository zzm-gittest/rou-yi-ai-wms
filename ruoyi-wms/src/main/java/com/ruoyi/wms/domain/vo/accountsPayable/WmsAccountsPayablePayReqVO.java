package com.ruoyi.wms.domain.vo.accountsPayable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 应付账款付款VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("应付账款付款")
public class WmsAccountsPayablePayReqVO {

    @ApiModelProperty(value = "主键ID", required = true)
    @NotNull(message = "ID不能为空")
    private Long id;

    @ApiModelProperty(value = "付款金额", required = true)
    @NotNull(message = "付款金额不能为空")
    private BigDecimal payAmount;

    @ApiModelProperty(value = "结算账户ID", required = true)
    @NotNull(message = "结算账户不能为空")
    private Long accountId;

    @ApiModelProperty(value = "付款凭证")
    private String paymentProof;

    @ApiModelProperty(value = "付款备注")
    private String remark;
}
