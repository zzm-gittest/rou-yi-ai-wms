package com.ruoyi.wms.domain.vo.goodsCategory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.xss.Xss;

/**
 * 商品分类新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("商品分类新增/修改 Request VO")
@Data
public class WmsGoodsCategorySaveReqVO {

    @ApiModelProperty(value = "分类ID")
    private Long id;

    @ApiModelProperty(value = "父分类ID")
    private Long parentId = 0L;

    @ApiModelProperty(value = "分类名称", required = true)
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    @Xss(message = "分类名称不允许包含脚本标签")
    private String categoryName;

    @ApiModelProperty(value = "显示顺序")
    private Integer orderNum = 0;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status = "0";

    @ApiModelProperty(value = "分类图片")
    private String image;

    @ApiModelProperty(value = "备注")
    @Size(max = 200, message = "备注不能超过200个字符")
    private String remark;
}
