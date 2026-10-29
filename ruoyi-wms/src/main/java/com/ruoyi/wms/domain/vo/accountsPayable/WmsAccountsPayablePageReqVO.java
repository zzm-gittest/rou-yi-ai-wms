package com.ruoyi.wms.domain.vo.accountsPayable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 应付账款分页查询VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("应付账款分页查询")
public class WmsAccountsPayablePageReqVO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "供应商名称（模糊查询）")
    private String supplierName;

    @ApiModelProperty(value = "单据编号（模糊查询）")
    private String billNo;

    @ApiModelProperty(value = "单据类型（1采购订单 2采购入库 3采购退货）")
    private String billType;

    @ApiModelProperty(value = "状态（0未结清 1已结清）")
    private String apStatus;

    @ApiModelProperty(value = "付款状态（0未付款 1部分付款 2已付款）")
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
