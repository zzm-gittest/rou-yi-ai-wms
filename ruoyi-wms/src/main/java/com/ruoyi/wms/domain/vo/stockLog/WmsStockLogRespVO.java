package com.ruoyi.wms.domain.vo.stockLog;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存变动日志 Response VO
 *
 * @author ruoyi
 */
@ApiModel("库存变动日志 Response VO")
@Data
public class WmsStockLogRespVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "SKU名称")
    private String skuName;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "单据类型名称")
    private String billTypeName;

    @ApiModelProperty(value = "单据类型（1采购入库 2销售出库 3采购退货 4销售退货 5盘点 6调拨）")
    private String billType;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据ID")
    private Long billId;

    @ApiModelProperty(value = "变动类型名称")
    private String changeTypeName;

    @ApiModelProperty(value = "变动类型（1增加 2减少）")
    private String changeType;

    @ApiModelProperty(value = "变动数量")
    private BigDecimal quantity;

    @ApiModelProperty(value = "变动前数量")
    private BigDecimal beforeQty;

    @ApiModelProperty(value = "变动后数量")
    private BigDecimal afterQty;

    @ApiModelProperty(value = "成本价")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "创建者")
    private String createBy;

    @ApiModelProperty(value = "备注")
    private String remark;
}
