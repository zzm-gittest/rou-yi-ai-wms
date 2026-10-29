package com.ruoyi.wms.domain.vo.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 费用响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("费用 Response VO")
@Data
public class WmsExpenseRespVO {

    @ApiModelProperty(value = "费用ID")
    private Long id;

    @ApiModelProperty(value = "费用单号")
    private String expenseNo;

    @ApiModelProperty(value = "费用类型ID")
    private Long expenseTypeId;

    @ApiModelProperty(value = "费用类型名称")
    private String expenseTypeName;

    @ApiModelProperty(value = "费用日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expenseDate;

    @ApiModelProperty(value = "费用金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "付款账户ID")
    private Long accountId;

    @ApiModelProperty(value = "账户名称")
    private String accountName;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String approveStatus;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
