package com.ruoyi.wms.domain.vo.importTask;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入任务报告 Response VO
 */
@ApiModel("导入任务报告 Response VO")
@Data
public class ImportTaskReportRespVO {

    @ApiModelProperty(value = "任务信息")
    private ImportTaskRespVO task;

    @ApiModelProperty(value = "错误列表")
    private List<ImportTaskErrorRespVO> errors = new ArrayList<>();

    @ApiModelProperty(value = "建议修复")
    private String suggestFix;

    @ApiModelProperty(value = "飞书单据号与WMS出库单号映射（销售出库导入）")
    private Map<String, String> billOutNoMap = new LinkedHashMap<>();
}
