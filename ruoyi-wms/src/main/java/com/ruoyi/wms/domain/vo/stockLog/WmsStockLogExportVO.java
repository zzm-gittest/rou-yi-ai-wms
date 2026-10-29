package com.ruoyi.wms.domain.vo.stockLog;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存变动日志导出 VO
 *
 * @author ruoyi
 */
@ApiModel("库存变动日志导出 VO")
@Data
public class WmsStockLogExportVO {

    @Excel(name = "仓库")
    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @Excel(name = "商品名称")
    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @Excel(name = "SKU编码")
    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @Excel(name = "SKU名称")
    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @Excel(name = "单据类型")
    @ApiModelProperty(value = "单据类型名称")
    private String billTypeName;

    @Excel(name = "单据编号")
    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @Excel(name = "变动类型")
    @ApiModelProperty(value = "变动类型名称")
    private String changeTypeName;

    @Excel(name = "变动数量")
    @ApiModelProperty(value = "变动数量")
    private BigDecimal quantity;

    @Excel(name = "变动前")
    @ApiModelProperty(value = "变动前数量")
    private BigDecimal beforeQty;

    @Excel(name = "变动后")
    @ApiModelProperty(value = "变动后数量")
    private BigDecimal afterQty;

    @Excel(name = "操作人")
    @ApiModelProperty(value = "创建者")
    private String createBy;

    @Excel(name = "操作时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @Excel(name = "备注")
    @ApiModelProperty(value = "备注")
    private String remark;
}
