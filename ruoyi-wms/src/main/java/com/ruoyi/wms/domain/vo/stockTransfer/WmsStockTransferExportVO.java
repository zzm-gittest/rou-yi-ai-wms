package com.ruoyi.wms.domain.vo.stockTransfer;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存调拨导出 VO（按明细行展开）
 *
 * @author ruoyi
 */
@ApiModel("库存调拨导出 VO")
@Data
public class WmsStockTransferExportVO {

    @Excel(name = "调拨单号")
    @ApiModelProperty(value = "调拨单号")
    private String transferNo;

    @Excel(name = "调拨日期", dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "调拨日期")
    private LocalDate transferDate;

    @Excel(name = "调出仓库")
    @ApiModelProperty(value = "调出仓库")
    private String outWarehouseName;

    @Excel(name = "调入仓库")
    @ApiModelProperty(value = "调入仓库")
    private String inWarehouseName;

    @Excel(name = "商品名称")
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "SKU名称")
    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @Excel(name = "SKU编码")
    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @Excel(name = "数量")
    @ApiModelProperty(value = "数量")
    private BigDecimal quantity;

    @Excel(name = "成本价")
    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @Excel(name = "调拨状态", readConverterExp = "0=待审核,1=已审核")
    @ApiModelProperty(value = "调拨状态")
    private String transferStatus;

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
