package com.ruoyi.wms.domain.vo.stockTransfer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 库存调拨单明细新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存调拨单明细新增/修改 Request VO")
@Data
public class WmsStockTransferItemSaveReqVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "SKU ID")
    @NotNull(message = "SKU不能为空")
    private Long skuId;

    @ApiModelProperty(value = "数量")
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @ApiModelProperty(value = "备注")
    private String remark;
}
