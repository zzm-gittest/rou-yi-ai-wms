package com.ruoyi.wms.domain.vo.salesOut;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 销售出库分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("销售出库分页查询 Request VO")
@Data
public class WmsSalesOutPageReqVO {

    @ApiModelProperty(value = "出库单号")
    private String outNo;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String approveStatus;

    @ApiModelProperty(value = "付款状态（0未付 1部分 2已付）")
    private String paymentStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
