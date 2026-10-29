package com.ruoyi.wms.domain.report.vo;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品销售排行榜VO
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "GoodsRankReportVO", description = "商品销售排行榜")
public class GoodsRankReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "排名", sort = 1)
    @ApiModelProperty(value = "排名")
    private Integer rankNo;

    @Excel(name = "商品编码", sort = 2)
    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @Excel(name = "商品名称", sort = 3)
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "规格", sort = 4)
    @ApiModelProperty(value = "规格")
    private String spec;

    @Excel(name = "单位", sort = 5)
    @ApiModelProperty(value = "单位")
    private String unit;

    @Excel(name = "分类名称", sort = 6)
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @Excel(name = "仓库名称", sort = 7)
    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @Excel(name = "销售数量", sort = 8)
    @ApiModelProperty(value = "销售数量")
    private BigDecimal saleQuantity;

    @Excel(name = "销售金额", sort = 9)
    @ApiModelProperty(value = "销售金额")
    private BigDecimal saleAmount;

    @Excel(name = "成本金额", sort = 10)
    @ApiModelProperty(value = "成本金额")
    private BigDecimal costAmount;

    @Excel(name = "利润金额", sort = 11)
    @ApiModelProperty(value = "利润金额")
    private BigDecimal profitAmount;

    @Excel(name = "利润率(%)", sort = 12)
    @ApiModelProperty(value = "利润率(%)")
    private BigDecimal profitRate;

    @ApiModelProperty(value = "排名变化")
    private Integer rankChange;
}
