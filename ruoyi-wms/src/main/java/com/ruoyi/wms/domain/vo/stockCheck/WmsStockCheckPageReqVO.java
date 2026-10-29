package com.ruoyi.wms.domain.vo.stockCheck;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 库存盘点单分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存盘点单分页查询 Request VO")
@Data
public class WmsStockCheckPageReqVO {

    @ApiModelProperty(value = "盘点单号")
    private String checkNo;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "盘点类型（FULL全盘 SAMPLE抽盘）")
    private String checkType;

    @ApiModelProperty(value = "盘点状态（0待审核 1已审核）")
    private String checkStatus;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
