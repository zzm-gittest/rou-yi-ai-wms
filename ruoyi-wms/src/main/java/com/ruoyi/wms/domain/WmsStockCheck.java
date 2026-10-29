package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.ruoyi.common.core.domain.BaseDO;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存盘点单表 wms_stock_check
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_stock_check")
@ApiModel(value = "WmsStockCheck", description = "库存盘点单表")
public class WmsStockCheck extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "盘点单号")
    private String checkNo;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "盘点日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkDate;

    @ApiModelProperty(value = "盘点类型（FULL全盘 SAMPLE抽盘）")
    private String checkType;

    @ApiModelProperty(value = "盘点状态（0待审核 1已审核）")
    private String checkStatus;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "备注")
    private String remark;

}
