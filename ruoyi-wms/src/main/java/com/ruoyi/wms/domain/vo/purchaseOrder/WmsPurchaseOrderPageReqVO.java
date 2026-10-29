package com.ruoyi.wms.domain.vo.purchaseOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 采购订单分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购订单分页查询 Request VO")
@Data
public class WmsPurchaseOrderPageReqVO {

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "订单状态（0待审核 1已审核 2部分入库 3全部入库 4已关闭）")
    private String orderStatus;

    @ApiModelProperty(value = "付款状态（0未付 1部分 2已付）")
    private String paymentStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
