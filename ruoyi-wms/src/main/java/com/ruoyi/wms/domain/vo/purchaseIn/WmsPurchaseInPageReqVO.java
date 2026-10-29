package com.ruoyi.wms.domain.vo.purchaseIn;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 采购入库分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购入库分页查询 Request VO")
@Data
public class WmsPurchaseInPageReqVO {

    @ApiModelProperty(value = "入库单号")
    private String inNo;

    @ApiModelProperty(value = "供应商ID")
    private Long supplierId;

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
