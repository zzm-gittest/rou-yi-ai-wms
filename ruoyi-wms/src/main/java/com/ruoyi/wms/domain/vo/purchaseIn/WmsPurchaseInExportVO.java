package com.ruoyi.wms.domain.vo.purchaseIn;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购入库导出 VO（按明细行展开，参考导入模板字段）
 *
 * @author ruoyi
 */
@ApiModel("采购入库导出 VO")
@Data
public class WmsPurchaseInExportVO {

    @Excel(name = "入库单号")
    @ApiModelProperty(value = "入库单号")
    private String inNo;

    @Excel(name = "采购订单号")
    @ApiModelProperty(value = "采购订单号")
    private String orderNo;

    @Excel(name = "来源类型", readConverterExp = "1=直接入库,2=采购订单入库")
    @ApiModelProperty(value = "来源类型")
    private Integer sourceType;

    @Excel(name = "入库日期", dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "入库日期")
    private LocalDate inDate;

    @Excel(name = "供应商名称")
    @ApiModelProperty(value = "供应商名称")
    private String supplierName;

    @Excel(name = "仓库名称")
    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

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

    @Excel(name = "单价")
    @ApiModelProperty(value = "单价")
    private BigDecimal price;

    @Excel(name = "金额")
    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @Excel(name = "入库总额")
    @ApiModelProperty(value = "入库总额")
    private BigDecimal totalAmount;

    @Excel(name = "付款状态", readConverterExp = "0=未付,1=部分,2=已付")
    @ApiModelProperty(value = "付款状态")
    private String paymentStatus;

    @Excel(name = "审核状态", readConverterExp = "0=待审核,1=已审核")
    @ApiModelProperty(value = "审核状态")
    private String approveStatus;

    @Excel(name = "备注")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Excel(name = "创建时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;
}
