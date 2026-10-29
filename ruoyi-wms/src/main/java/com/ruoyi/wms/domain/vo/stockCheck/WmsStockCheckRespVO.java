package com.ruoyi.wms.domain.vo.stockCheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存盘点单响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("库存盘点单 Response VO")
@Data
public class WmsStockCheckRespVO {

    @ApiModelProperty(value = "盘点单ID")
    private Long id;

    @ApiModelProperty(value = "盘点单号")
    private String checkNo;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

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

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "明细列表")
    private List<WmsStockCheckItemRespVO> items;
}
