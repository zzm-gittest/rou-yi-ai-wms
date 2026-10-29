package com.ruoyi.wms.domain.vo.salesReturn;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售退货响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("销售退货 Response VO")
@Data
public class WmsSalesReturnRespVO {

    @ApiModelProperty(value = "退货单ID")
    private Long id;

    @ApiModelProperty(value = "退货单号")
    private String returnNo;

    @ApiModelProperty(value = "销售出库ID")
    private Long outId;

    @ApiModelProperty(value = "出库单号")
    private String outNo;

    @ApiModelProperty(value = "来源类型（1直接入库 2出库单退货）")
    private Integer sourceType;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "仓库ID")
    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @ApiModelProperty(value = "退货日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate returnDate;

    @ApiModelProperty(value = "退货总额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "已退金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "退款状态（0未退 1部分 2已退）")
    private String paymentStatus;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "审核状态（0待审核 1已审核）")
    private String approveStatus;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "明细列表")
    private List<WmsSalesReturnItemRespVO> items;
}
