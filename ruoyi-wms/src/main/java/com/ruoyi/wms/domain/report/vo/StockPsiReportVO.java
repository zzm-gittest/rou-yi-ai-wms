package com.ruoyi.wms.domain.report.vo;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存进销存报表VO
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "StockPsiReportVO", description = "库存进销存报表")
public class StockPsiReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "仓库名称", sort = 1)
    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @Excel(name = "分类名称", sort = 2)
    @ApiModelProperty(value = "分类名称")
    private String categoryName;

    @Excel(name = "商品编码", sort = 3)
    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @Excel(name = "商品名称", sort = 4)
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "规格", sort = 5)
    @ApiModelProperty(value = "规格")
    private String spec;

    @Excel(name = "单位", sort = 6)
    @ApiModelProperty(value = "单位")
    private String unit;

    @Excel(name = "期初数量", sort = 7)
    @ApiModelProperty(value = "期初数量")
    private BigDecimal beginQty;

    @Excel(name = "期初金额", sort = 8)
    @ApiModelProperty(value = "期初金额")
    private BigDecimal beginAmount;

    @Excel(name = "入库数量", sort = 9)
    @ApiModelProperty(value = "入库数量")
    private BigDecimal inQty;

    @Excel(name = "入库金额", sort = 10)
    @ApiModelProperty(value = "入库金额")
    private BigDecimal inAmount;

    @Excel(name = "出库数量", sort = 11)
    @ApiModelProperty(value = "出库数量")
    private BigDecimal outQty;

    @Excel(name = "出库金额", sort = 12)
    @ApiModelProperty(value = "出库金额")
    private BigDecimal outAmount;

    @Excel(name = "结存数量", sort = 13)
    @ApiModelProperty(value = "结存数量")
    private BigDecimal endQty;

    @Excel(name = "结存金额", sort = 14)
    @ApiModelProperty(value = "结存金额")
    private BigDecimal endAmount;
}
