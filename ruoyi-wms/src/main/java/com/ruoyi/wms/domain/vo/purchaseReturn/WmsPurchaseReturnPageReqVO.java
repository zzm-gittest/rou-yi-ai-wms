package com.ruoyi.wms.domain.vo.purchaseReturn;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 采购退货分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购退货分页查询 Request VO")
@Data
public class WmsPurchaseReturnPageReqVO {

    @ApiModelProperty(value = "退货单号")
    private String returnNo;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String approveStatus;

    @ApiModelProperty(value = "退款状态（0未退 1部分 2已退）")
    private String paymentStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
