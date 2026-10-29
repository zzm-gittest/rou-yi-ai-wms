package com.ruoyi.wms.domain.vo.financeLog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 财务日志分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("财务日志分页查询 Request VO")
@Data
public class WmsFinanceLogPageReqVO {

    @ApiModelProperty(value = "凭证号")
    private String voucherNo;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据类型（1收款 2付款 3费用）")
    private String billType;

    @ApiModelProperty(value = "方向（1收入 2支出）")
    private String direction;

    @ApiModelProperty(value = "账户ID")
    private Long accountId;

    @ApiModelProperty(value = "开始日期")
    private LocalDate startDate;

    @ApiModelProperty(value = "结束日期")
    private LocalDate endDate;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
