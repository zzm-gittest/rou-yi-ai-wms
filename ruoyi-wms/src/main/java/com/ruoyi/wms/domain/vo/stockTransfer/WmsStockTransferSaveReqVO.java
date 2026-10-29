package com.ruoyi.wms.domain.vo.stockTransfer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存调拨单新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存调拨单新增/修改 Request VO")
@Data
public class WmsStockTransferSaveReqVO {

    @ApiModelProperty(value = "调拨单ID")
    private Long id;

    @ApiModelProperty(value = "调出仓库ID")
    @NotNull(message = "调出仓库不能为空")
    private Long outWarehouseId;

    @ApiModelProperty(value = "调入仓库ID")
    @NotNull(message = "调入仓库不能为空")
    private Long inWarehouseId;

    @ApiModelProperty(value = "调拨日期")
    private LocalDate transferDate;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    @Valid
    private List<WmsStockTransferItemSaveReqVO> items;
}
