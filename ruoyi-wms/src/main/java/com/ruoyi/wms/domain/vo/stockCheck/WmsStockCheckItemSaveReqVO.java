package com.ruoyi.wms.domain.vo.stockCheck;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 库存盘点单明细新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存盘点单明细新增/修改 Request VO")
@Data
public class WmsStockCheckItemSaveReqVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "SKU ID")
    @NotNull(message = "SKU不能为空")
    private Long skuId;

    @ApiModelProperty(value = "账面数量")
    private BigDecimal beforeQty;

    @ApiModelProperty(value = "实际数量")
    @NotNull(message = "实际数量不能为空")
    private BigDecimal checkQty;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "备注")
    private String remark;
}
