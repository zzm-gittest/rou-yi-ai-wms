package com.ruoyi.wms.domain.vo.stockLog;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 库存变动日志分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存变动日志分页查询 Request VO")
@Data
public class WmsStockLogPageReqVO {

    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "页码（兼容旧参数）", example = "1")
    private Integer pageNum;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "SKU ID")
    private Long skuId;

    @ApiModelProperty(value = "商品ID")
    private Long goodsId;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "单据类型（1采购入库 2销售出库 3采购退货 4销售退货 5盘点 6调拨）")
    private String billType;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "变动类型（1增加 2减少）")
    private String changeType;
}
