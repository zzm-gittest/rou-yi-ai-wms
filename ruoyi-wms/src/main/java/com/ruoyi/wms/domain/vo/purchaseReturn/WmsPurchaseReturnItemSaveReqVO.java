package com.ruoyi.wms.domain.vo.purchaseReturn;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 采购退货明细保存Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购退货明细保存Request VO")
@Data
public class WmsPurchaseReturnItemSaveReqVO {

    @ApiModelProperty(value = "明细ID")
    private Long id;

    @ApiModelProperty(value = "SKU ID")
    @NotNull(message = "SKU不能为空")
    private Long skuId;

    @ApiModelProperty(value = "数量")
    @NotNull(message = "数量不能为空")
    private BigDecimal quantity;

    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @ApiModelProperty(value = "备注")
    private String remark;
}
