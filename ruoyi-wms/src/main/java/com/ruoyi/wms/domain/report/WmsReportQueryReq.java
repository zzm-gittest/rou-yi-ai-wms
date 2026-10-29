package com.ruoyi.wms.domain.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 报表查询请求
 *
 * @author ruoyi
 */
@Data
@ApiModel(value = "WmsReportQueryReq", description = "报表查询请求")
public class WmsReportQueryReq implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报表编码")
    private String reportCode;

    @ApiModelProperty(value = "查询参数")
    private Map<String, Object> params;

    @ApiModelProperty(value = "分页页码")
    private Integer pageNo = 1;

    @ApiModelProperty(value = "分页大小")
    private Integer pageSize = 20;

    @ApiModelProperty(value = "导出类型：excel, pdf")
    private String exportType;
}
