package com.ruoyi.wms.domain.vo.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.xss.Xss;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;

import java.util.List;

/**
 * 商品新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品新增/修改 Request VO")
@Data
public class WmsGoodsSaveReqVO {

    @ApiModelProperty(value = "商品ID")
    private Long id;

    @ApiModelProperty(value = "商品编码（留空自动生成）")
    @Size(max = 50, message = "商品编码不能超过50个字符")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称", required = true)
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100个字符")
    @Xss(message = "商品名称不允许包含脚本标签")
    private String goodsName;

    @ApiModelProperty(value = "分类ID", required = true)
    @NotBlank(message = "分类不能为空")
    private String categoryId;

    @ApiModelProperty(value = "单位ID")
    private Long unitId;

    @ApiModelProperty(value = "品牌")
    @Size(max = 50, message = "品牌不能超过50个字符")
    @Xss(message = "品牌不允许包含脚本标签")
    private String brand;

    @ApiModelProperty(value = "产地")
    @Size(max = 50, message = "产地不能超过50个字符")
    @Xss(message = "产地不允许包含脚本标签")
    private String place;

    @ApiModelProperty(value = "商品主图")
    private String image;

    @ApiModelProperty(value = "商品图片集（JSON）")
    private String images;

    @ApiModelProperty(value = "商品详情")
    private String detail;

    @ApiModelProperty(value = "是否有规格（0无 1有）")
    private String hasSpec = "0";

    @ApiModelProperty(value = "最小库存（SKU合计）")
    private Integer minStock = 0;

    @ApiModelProperty(value = "最大库存（SKU合计）")
    private Integer maxStock = 999999;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status = "0";

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "SKU明细列表")
    private List<WmsGoodsSkuSaveReqVO> skuList;

    @ApiModelProperty(value = "是否同步SKU明细（仅一体化页面传true）")
    private Boolean syncSkuList;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getGoodsCode() { return goodsCode; }
    public void setGoodsCode(String goodsCode) { this.goodsCode = goodsCode; }
    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getHasSpec() { return hasSpec; }
    public void setHasSpec(String hasSpec) { this.hasSpec = hasSpec; }
    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }
    public Integer getMaxStock() { return maxStock; }
    public void setMaxStock(Integer maxStock) { this.maxStock = maxStock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<WmsGoodsSkuSaveReqVO> getSkuList() { return skuList; }
    public void setSkuList(List<WmsGoodsSkuSaveReqVO> skuList) { this.skuList = skuList; }
    public Boolean getSyncSkuList() { return syncSkuList; }
    public void setSyncSkuList(Boolean syncSkuList) { this.syncSkuList = syncSkuList; }
}
