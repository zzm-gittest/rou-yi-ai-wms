package com.ruoyi.wms.domain.vo.unit;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 计量单位响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("计量单位 Response VO")
@Data
public class WmsUnitRespVO {

    @ApiModelProperty(value = "单位ID")
    private Long id;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
