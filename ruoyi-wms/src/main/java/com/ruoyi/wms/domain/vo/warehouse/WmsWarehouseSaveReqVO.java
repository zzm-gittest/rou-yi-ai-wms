package com.ruoyi.wms.domain.vo.warehouse;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 仓库新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("仓库新增/修改 Request VO")
@Data
public class WmsWarehouseSaveReqVO {

    @ApiModelProperty(value = "仓库ID")
    private Long id;

    @ApiModelProperty(value = "仓库编码")
    private String warehouseCode;

    @ApiModelProperty(value = "仓库名称", required = true)
    @NotBlank(message = "仓库名称不能为空")
    private String warehouseName;

    @ApiModelProperty(value = "联系人")
    private String contact;

    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "仓库地址")
    private String address;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status = "0";

    @ApiModelProperty(value = "显示顺序", required = true)
    @NotNull(message = "显示顺序不能为空")
    private Integer sort = 0;

    @ApiModelProperty(value = "备注")
    private String remark;
}
