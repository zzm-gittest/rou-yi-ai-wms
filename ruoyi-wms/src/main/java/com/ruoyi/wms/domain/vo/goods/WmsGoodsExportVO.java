package com.ruoyi.wms.domain.vo.goods;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品导出 VO（按SKU展开，参考多规格导入模板）
 *
 * @author ruoyi
 */
@ApiModel("商品导出 VO")
@Data
public class WmsGoodsExportVO {

    @Excel(name = "商品编码")
    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @Excel(name = "商品名称")
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "分类名称")
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @Excel(name = "单位名称")
    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @Excel(name = "品牌")
    @ApiModelProperty(value = "品牌")
    private String brand;

    @Excel(name = "产地")
    @ApiModelProperty(value = "产地")
    private String place;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    @ApiModelProperty(value = "状态")
    private String status;

    @Excel(name = "备注")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Excel(name = "商品条码")
    @ApiModelProperty(value = "商品条码")
    private String barcode;

    @Excel(name = "SKU名称")
    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @Excel(name = "SKU编码")
    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @Excel(name = "规格信息")
    @ApiModelProperty(value = "规格信息（如：颜色:黑色,容量:128G）")
    private String specInfo;

    @Excel(name = "成本价")
    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @Excel(name = "销售价")
    @ApiModelProperty(value = "销售价")
    private BigDecimal salePrice;

    @Excel(name = "最小库存")
    @ApiModelProperty(value = "最小库存")
    private Integer minStock;

    @Excel(name = "最大库存")
    @ApiModelProperty(value = "最大库存")
    private Integer maxStock;

    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
