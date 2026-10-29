package com.ruoyi.wms.domain.vo.integration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("单据明细行")
public class BillDetailLineVO {

    @ApiModelProperty("商品名称")
    private String goodsName;

    @ApiModelProperty("SKU名称")
    private String skuName;

    @ApiModelProperty("单位")
    private String unitName;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @ApiModelProperty("单价")
    private BigDecimal price;

    @ApiModelProperty("金额")
    private BigDecimal amount;
}
