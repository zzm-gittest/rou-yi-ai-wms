package com.ruoyi.wms.domain.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 报表配置表 wms_report_config
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_report_config")
@ApiModel(value = "WmsReportConfig", description = "报表配置表")
public class WmsReportConfig extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报表ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "报表编码")
    @TableField("report_code")
    private String reportCode;

    @ApiModelProperty(value = "报表名称")
    private String reportName;

    @ApiModelProperty(value = "报表类型：sales-销售, purchase-采购, stock-库存, finance-财务, comprehensive-综合")
    private String reportType;

    @ApiModelProperty(value = "报表分类")
    private String category;

    @ApiModelProperty(value = "数据源SQL/接口")
    private String dataSource;

    @ApiModelProperty(value = "查询参数配置(JSON)")
    private String queryParams;

    @ApiModelProperty(value = "列配置(JSON)")
    private String columnConfig;

    @ApiModelProperty(value = "图表配置(JSON)")
    private String chartConfig;

    @ApiModelProperty(value = "导出配置(JSON)")
    private String exportConfig;

    @ApiModelProperty(value = "状态：0-正常,1-停用")
    private String status;

    @ApiModelProperty(value = "是否系统报表：0-自定义,1-系统")
    private String isSystem;

    @ApiModelProperty(value = "排序")
    private Integer sortOrder;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "租户ID")
    @TableField("tenant_id")
    private Long tenantId;
}
