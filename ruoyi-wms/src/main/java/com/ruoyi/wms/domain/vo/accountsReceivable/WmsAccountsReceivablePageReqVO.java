package com.ruoyi.wms.domain.vo.accountsReceivable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 应收账款分页查询VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("应收账款分页查询")
public class WmsAccountsReceivablePageReqVO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "客户名称（模糊查询）")
    private String customerName;

    @ApiModelProperty(value = "单据编号（模糊查询）")
    private String billNo;

    @ApiModelProperty(value = "单据类型（1销售订单 2销售出库 3销售退货）")
    private String billType;

    @ApiModelProperty(value = "状态（0未结清 1已结清）")
    private String arStatus;

    @ApiModelProperty(value = "付款状态（0未收款 1部分收款 2已收款）")
    private Integer paymentStatus;

    @ApiModelProperty(value = "单据日期开始")
    private LocalDate billDateStart;

    @ApiModelProperty(value = "单据日期结束")
    private LocalDate billDateEnd;

    @ApiModelProperty(value = "到期日期开始")
    private LocalDate dueDateStart;

    @ApiModelProperty(value = "到期日期结束")
    private LocalDate dueDateEnd;
}
