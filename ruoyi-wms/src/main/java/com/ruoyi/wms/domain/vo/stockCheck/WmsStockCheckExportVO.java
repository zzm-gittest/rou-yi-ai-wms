package com.ruoyi.wms.domain.vo.stockCheck;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存盘点导出 VO（按明细行展开）
 *
 * @author ruoyi
 */
@ApiModel("库存盘点导出 VO")
@Data
public class WmsStockCheckExportVO {

    @Excel(name = "盘点单号")
    @ApiModelProperty(value = "盘点单号")
    private String checkNo;

    @Excel(name = "盘点日期", dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "盘点日期")
    private LocalDate checkDate;

    @Excel(name = "仓库名称")
    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @Excel(name = "盘点类型", readConverterExp = "FULL=全盘,SAMPLE=抽盘")
    @ApiModelProperty(value = "盘点类型")
    private String checkType;

    @Excel(name = "商品名称")
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "SKU名称")
    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @Excel(name = "SKU编码")
    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @Excel(name = "账面数量")
    @ApiModelProperty(value = "账面数量")
    private BigDecimal beforeQty;

    @Excel(name = "实盘数量")
    @ApiModelProperty(value = "实盘数量")
    private BigDecimal checkQty;

    @Excel(name = "盈亏数量")
    @ApiModelProperty(value = "盈亏数量")
    private BigDecimal diffQty;

    @Excel(name = "盘点状态", readConverterExp = "0=待审核,1=已审核")
    @ApiModelProperty(value = "盘点状态")
    private String checkStatus;

    @Excel(name = "经办人")
    @ApiModelProperty(value = "经办人")
    private String handler;

    @Excel(name = "备注")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
