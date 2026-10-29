package com.ruoyi.wms.domain.vo.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 导入错误信息VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("导入错误信息VO")
public class ImportError {

    @ApiModelProperty(value = "行号")
    private int rowNumber;

    @ApiModelProperty(value = "错误信息")
    private String errorMessage;

    @ApiModelProperty(value = "该行原始数据（JSON）")
    private String rawData;
}
