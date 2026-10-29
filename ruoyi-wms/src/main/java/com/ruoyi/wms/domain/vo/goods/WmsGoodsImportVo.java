package com.ruoyi.wms.domain.vo.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品Excel导入VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("商品Excel导入VO")
public class WmsGoodsImportVo {

    @ApiModelProperty(value = "行号（用于错误提示）")
    private int rowNumber;

    @ApiModelProperty(value = "商品编码（留空自动生成）")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称", required = true)
    private String goodsName;

    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "产地")
    private String place;

    @ApiModelProperty(value = "状态（正常/停用）")
    private String status = "正常";

    @ApiModelProperty(value = "备注")
    private String remark;

    // SKU相关字段
    @ApiModelProperty(value = "商品条码")
    private String barcode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    // 规格信息 - 5组规格名/规格值
    @ApiModelProperty(value = "规格名1")
    private String specName1;

    @ApiModelProperty(value = "规格值1")
    private String specValue1;

    @ApiModelProperty(value = "规格名2")
    private String specName2;

    @ApiModelProperty(value = "规格值2")
    private String specValue2;

    @ApiModelProperty(value = "规格名3")
    private String specName3;

    @ApiModelProperty(value = "规格值3")
    private String specValue3;

    @ApiModelProperty(value = "规格名4")
    private String specName4;

    @ApiModelProperty(value = "规格值4")
    private String specValue4;

    @ApiModelProperty(value = "规格名5")
    private String specName5;

    @ApiModelProperty(value = "规格值5")
    private String specValue5;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "销售价")
    private BigDecimal salePrice;

    @ApiModelProperty(value = "最大库存")
    private Integer maxStock;

    @ApiModelProperty(value = "最小库存")
    private Integer minStock;

    /**
     * 判断是否有规格信息
     */
    public boolean hasSpec() {
        return isNotEmpty(specName1) || isNotEmpty(specName2) || isNotEmpty(specName3)
                || isNotEmpty(specName4) || isNotEmpty(specName5);
    }

    /**
     * 构建规格信息JSON字符串
     */
    public String buildSpecInfo() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (isNotEmpty(specName1) && isNotEmpty(specValue1)) {
            if (!first) sb.append(",");
            sb.append("\"").append(specName1).append("\":\"").append(specValue1).append("\"");
            first = false;
        }
        if (isNotEmpty(specName2) && isNotEmpty(specValue2)) {
            if (!first) sb.append(",");
            sb.append("\"").append(specName2).append("\":\"").append(specValue2).append("\"");
            first = false;
        }
        if (isNotEmpty(specName3) && isNotEmpty(specValue3)) {
            if (!first) sb.append(",");
            sb.append("\"").append(specName3).append("\":\"").append(specValue3).append("\"");
            first = false;
        }
        if (isNotEmpty(specName4) && isNotEmpty(specValue4)) {
            if (!first) sb.append(",");
            sb.append("\"").append(specName4).append("\":\"").append(specValue4).append("\"");
            first = false;
        }
        if (isNotEmpty(specName5) && isNotEmpty(specValue5)) {
            if (!first) sb.append(",");
            sb.append("\"").append(specName5).append("\":\"").append(specValue5).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * 构建SKU名称（规格值拼接）
     */
    public String buildSkuName() {
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(specValue1)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(specValue1);
        }
        if (isNotEmpty(specValue2)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(specValue2);
        }
        if (isNotEmpty(specValue3)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(specValue3);
        }
        if (isNotEmpty(specValue4)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(specValue4);
        }
        if (isNotEmpty(specValue5)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(specValue5);
        }
        return sb.toString();
    }

    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
