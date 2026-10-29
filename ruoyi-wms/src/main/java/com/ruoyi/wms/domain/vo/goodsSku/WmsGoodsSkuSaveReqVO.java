package com.ruoyi.wms.domain.vo.goodsSku;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.xss.Xss;
import java.math.BigDecimal;

/**
 * 商品SKU新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品SKU新增/修改 Request VO")
@Data
public class WmsGoodsSkuSaveReqVO {

    @ApiModelProperty(value = "SKU ID")
    private Long id;

    @ApiModelProperty(value = "商品ID", required = true)
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    @ApiModelProperty(value = "SKU编码")
    @Size(max = 50, message = "SKU编码不能超过50个字符")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    @Size(max = 100, message = "SKU名称不能超过100个字符")
    @Xss(message = "SKU名称不允许包含脚本标签")
    private String skuName;

    @ApiModelProperty(value = "条形码")
    @Size(max = 50, message = "条形码不能超过50个字符")
    private String barcode;

    @ApiModelProperty(value = "单位ID")
    private Long unitId;

    @ApiModelProperty(value = "规格信息")
    private String specInfo;

    @ApiModelProperty(value = "规格值ID组合")
    private String specIds;

    @ApiModelProperty(value = "采购价")
    private BigDecimal purchasePrice;

    @ApiModelProperty(value = "销售价")
    private BigDecimal salePrice;

    @ApiModelProperty(value = "批发价")
    private BigDecimal wholesalePrice;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "最小库存")
    private Integer minStock;

    @ApiModelProperty(value = "最大库存")
    private Integer maxStock;

    @ApiModelProperty(value = "SKU图片")
    private String image;

    @ApiModelProperty(value = "状态")
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGoodsId() { return goodsId; }
    public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getSpecInfo() { return specInfo; }
    public void setSpecInfo(String specInfo) { this.specInfo = specInfo; }
    public String getSpecIds() { return specIds; }
    public void setSpecIds(String specIds) { this.specIds = specIds; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public BigDecimal getWholesalePrice() { return wholesalePrice; }
    public void setWholesalePrice(BigDecimal wholesalePrice) { this.wholesalePrice = wholesalePrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }
    public Integer getMaxStock() { return maxStock; }
    public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
