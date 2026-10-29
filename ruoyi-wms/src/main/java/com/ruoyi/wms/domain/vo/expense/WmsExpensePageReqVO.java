package com.ruoyi.wms.domain.vo.expense;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 费用分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("费用分页查询 Request VO")
@Data
public class WmsExpensePageReqVO {

    @ApiModelProperty(value = "费用单号")
    private String expenseNo;

    @ApiModelProperty(value = "费用类型ID")
    private Long expenseTypeId;

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String approveStatus;

    @ApiModelProperty(value = "开始日期")
    private LocalDate startDate;

    @ApiModelProperty(value = "结束日期")
    private LocalDate endDate;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
