package com.ruoyi.wms.domain.vo.goods;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 导入结果VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("导入结果VO")
public class ImportResult {

    @ApiModelProperty(value = "总行数（不含表头）")
    private int total;

    @ApiModelProperty(value = "成功数量")
    private int successCount;

    @ApiModelProperty(value = "失败数量")
    private int failCount;

    @ApiModelProperty(value = "错误信息列表")
    private List<ImportError> errors;

    /**
     * 销售出库导入：飞书单据号 → WMS 出库单号（如 CK202605210002）
     */
    @ApiModelProperty(value = "单据号与WMS出库单号映射")
    private Map<String, String> billOutNoMap = new LinkedHashMap<>();
}
