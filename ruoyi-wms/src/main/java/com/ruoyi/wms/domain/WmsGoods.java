package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.ruoyi.common.core.domain.BaseDO;
import java.io.Serializable;

/**
 * 商品表(SPU) wms_goods
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_goods")
@ApiModel(value = "WmsGoods", description = "商品表(SPU)")
public class WmsGoods extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "商品编码")
    private String goodsCode;

    @ApiModelProperty(value = "商品名称")
    private String goodsName;

    @ApiModelProperty(value = "分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "单位ID")
    private Long unitId;

    @ApiModelProperty(value = "品牌")
    private String brand;

    @ApiModelProperty(value = "产地")
    private String place;

    @ApiModelProperty(value = "商品主图")
    private String image;

    @ApiModelProperty(value = "商品图片集（JSON）")
    private String images;

    @ApiModelProperty(value = "商品详情")
    private String detail;

    @ApiModelProperty(value = "是否有规格（0无 1有）")
    private String hasSpec;

    @ApiModelProperty(value = "最小库存（SKU合计）")
    private Integer minStock;

    @ApiModelProperty(value = "最大库存（SKU合计）")
    private Integer maxStock;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

}
