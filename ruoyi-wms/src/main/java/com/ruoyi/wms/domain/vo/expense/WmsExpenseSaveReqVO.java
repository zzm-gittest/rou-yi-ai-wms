package com.ruoyi.wms.domain.vo.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 费用保存 Request VO
 *
 * @author ruoyi
 */
@ApiModel("费用保存 Request VO")
@Data
public class WmsExpenseSaveReqVO {

    @ApiModelProperty(value = "费用ID")
    private Long id;

    @ApiModelProperty(value = "费用类型ID")
    @NotNull(message = "费用类型不能为空")
    private Long expenseTypeId;

    @ApiModelProperty(value = "费用日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "费用日期不能为空")
    private LocalDate expenseDate;

    @ApiModelProperty(value = "费用金额")
    @NotNull(message = "费用金额不能为空")
    private BigDecimal amount;

    @ApiModelProperty(value = "付款账户ID")
    @NotNull(message = "付款账户不能为空")
    private Long accountId;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;
}
