package com.ruoyi.wms.domain.vo.stock;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 库存分页查询 Request VO
 *
 * @author ruoyi
 */
@ApiModel("库存分页查询 Request VO")
@Data
public class WmsStockPageReqVO {

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "SKU编码")
    private String skuCode;

    @ApiModelProperty(value = "仅显示预警库存（库存小于10）")
    private Boolean lowStock;

    @ApiModelProperty(value = "关键词（综合搜索商品名称、SKU名称、规格）")
    private String keyword;

    @ApiModelProperty(value = "页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "每页条数")
    private Integer pageSize = 10;
}
