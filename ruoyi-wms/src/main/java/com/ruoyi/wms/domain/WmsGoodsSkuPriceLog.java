package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU价格日志表 wms_goods_sku_price_log
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_goods_sku_price_log")
@ApiModel(value = "WmsGoodsSkuPriceLog", description = "商品SKU价格日志表")
public class WmsGoodsSkuPriceLog extends BaseDO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "SKU ID")
    private Long skuId;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "订单类型：PURCHASE_ORDER/SALES_ORDER")
    private String orderType;

    @ApiModelProperty(value = "订单ID")
    private Long orderId;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "订单明细ID")
    private Long orderItemId;

    @ApiModelProperty(value = "价格类型：PURCHASE/SALE")
    private String priceType;

    @ApiModelProperty(value = "变更前价格")
    private BigDecimal oldPrice;

    @ApiModelProperty(value = "变更后价格")
    private BigDecimal newPrice;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "操作时间")
    private LocalDateTime operateTime;
}
