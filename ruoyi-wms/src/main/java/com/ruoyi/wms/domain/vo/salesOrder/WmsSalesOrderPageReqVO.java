package com.ruoyi.wms.domain.vo.salesOrder;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 销售订单分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("销售订单分页查询 Request VO")
@Data
public class WmsSalesOrderPageReqVO {

    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "订单状态（0待审核 1已审核 2部分发货 3全部发货 4已关闭）")
    private String orderStatus;

    @ApiModelProperty(value = "收款状态（0未收 1部分 2已收）")
    private String paymentStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
