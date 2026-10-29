package com.ruoyi.wms.domain.vo.stock;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存导出 VO
 *
 * @author ruoyi
 */
@ApiModel("库存导出 VO")
@Data
public class WmsStockExportVO {

    @Excel(name = "仓库")
    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @Excel(name = "商品编码")
    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @Excel(name = "商品名称")
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "SKU编码")
    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @Excel(name = "SKU名称")
    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @Excel(name = "规格")
    @ApiModelProperty(value = "规格信息")
    private String specInfo;

    @Excel(name = "单位")
    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @Excel(name = "库存数量")
    @ApiModelProperty(value = "库存数量")
    private BigDecimal quantity;

    @Excel(name = "成本价")
    @ApiModelProperty(value = "成本价（移动加权平均）")
    private BigDecimal costPrice;

    @Excel(name = "库存金额")
    @ApiModelProperty(value = "库存金额")
    private BigDecimal amount;

    @Excel(name = "更新时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private String updateTime;
}
