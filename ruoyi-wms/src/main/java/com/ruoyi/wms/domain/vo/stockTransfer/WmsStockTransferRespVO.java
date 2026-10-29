package com.ruoyi.wms.domain.vo.stockTransfer;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存调拨单响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("库存调拨单 Response VO")
@Data
public class WmsStockTransferRespVO {

    @ApiModelProperty(value = "调拨单ID")
    private Long id;

    @ApiModelProperty(value = "调拨单号")
    private String transferNo;

    @ApiModelProperty(value = "调出仓库ID")
    private Long outWarehouseId;

    @ApiModelProperty(value = "调出仓库名称")
    private String outWarehouseName;

    @ApiModelProperty(value = "调入仓库ID")
    private Long inWarehouseId;

    @ApiModelProperty(value = "调入仓库名称")
    private String inWarehouseName;

    @ApiModelProperty(value = "调拨日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transferDate;

    @ApiModelProperty(value = "调拨状态（0待审核 1已审核）")
    private String transferStatus;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "明细列表")
    private List<WmsStockTransferItemRespVO> items;
}
