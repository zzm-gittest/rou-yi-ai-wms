package com.ruoyi.wms.domain.vo.stockCheck;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 库存盘点单新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存盘点单新增/修改 Request VO")
@Data
public class WmsStockCheckSaveReqVO {

    @ApiModelProperty(value = "盘点单ID")
    private Long id;

    @ApiModelProperty(value = "仓库ID")
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @ApiModelProperty(value = "盘点日期")
    private LocalDate checkDate;

    @ApiModelProperty(value = "盘点类型（FULL全盘 SAMPLE抽盘）")
    private String checkType;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    @Valid
    private List<WmsStockCheckItemSaveReqVO> items;
}
