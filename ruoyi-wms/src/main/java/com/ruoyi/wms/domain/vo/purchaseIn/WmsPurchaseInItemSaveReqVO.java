package com.ruoyi.wms.domain.vo.purchaseIn;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 采购入库明细新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购入库明细新增/修改 Request VO")
@Data
public class WmsPurchaseInItemSaveReqVO {

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
