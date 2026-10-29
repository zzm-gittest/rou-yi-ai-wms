package com.ruoyi.wms.domain.vo.accountsReceivable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 应收账款收款VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("应收账款收款")
public class WmsAccountsReceivableCollectReqVO {

    @ApiModelProperty(value = "主键ID", required = true)
    @NotNull(message = "ID不能为空")
    private Long id;

    @ApiModelProperty(value = "收款金额", required = true)
    @NotNull(message = "收款金额不能为空")
    private BigDecimal collectAmount;

    @ApiModelProperty(value = "结算账户ID", required = true)
    @NotNull(message = "结算账户不能为空")
    private Long accountId;

    @ApiModelProperty(value = "收款凭证")
    private String collectionProof;

    @ApiModelProperty(value = "收款备注")
    private String remark;
}
