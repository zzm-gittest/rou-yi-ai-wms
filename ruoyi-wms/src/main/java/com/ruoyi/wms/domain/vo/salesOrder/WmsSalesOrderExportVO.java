package com.ruoyi.wms.domain.vo.salesOrder;

import com.ruoyi.common.annotation.Excel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单导出 VO（按明细行展开）
 *
 * @author ruoyi
 */
@ApiModel("销售订单导出 VO")
@Data
public class WmsSalesOrderExportVO {

    @Excel(name = "订单编号")
    @ApiModelProperty(value = "订单编号")
    private String orderNo;

    @Excel(name = "订单日期", dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "订单日期")
    private LocalDate orderDate;

    @Excel(name = "交货日期", dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "交货日期")
    private LocalDate deliveryDate;

    @Excel(name = "客户名称")
    @ApiModelProperty(value = "客户名称")
    private String customerName;

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

    @Excel(name = "订单总额")
    @ApiModelProperty(value = "订单总额")
    private BigDecimal totalAmount;

    @Excel(name = "优惠金额")
    @ApiModelProperty(value = "优惠金额")
    private BigDecimal discountAmount;

    @Excel(name = "收款状态", readConverterExp = "0=未收,1=部分,2=已收")
    @ApiModelProperty(value = "收款状态")
    private String paymentStatus;

    @Excel(name = "订单状态", readConverterExp = "0=待审核,1=已审核,2=部分发货,3=全部发货,4=已关闭")
    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

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
