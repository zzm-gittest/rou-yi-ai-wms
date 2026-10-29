package com.ruoyi.wms.domain.vo.stockTransfer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 库存调拨单分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存调拨单分页查询 Request VO")
@Data
public class WmsStockTransferPageReqVO {

    @ApiModelProperty(value = "调拨单号")
    private String transferNo;

    @ApiModelProperty(value = "调出仓库ID")
    private Long outWarehouseId;

    @ApiModelProperty(value = "调入仓库ID")
    private Long inWarehouseId;

    @ApiModelProperty(value = "调拨状态（0待审核 1已审核）")
    private String transferStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
