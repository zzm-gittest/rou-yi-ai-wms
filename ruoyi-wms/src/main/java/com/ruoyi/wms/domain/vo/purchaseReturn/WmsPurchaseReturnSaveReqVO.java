package com.ruoyi.wms.domain.vo.purchaseReturn;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购退货保存Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购退货保存Request VO")
@Data
public class WmsPurchaseReturnSaveReqVO {

    @ApiModelProperty(value = "退货单ID")
    private Long id;

    @ApiModelProperty(value = "来源类型（1直接退货 2入库单退货）")
    private Integer sourceType;

    @ApiModelProperty(value = "采购入库ID")
    private Long inId;

    @ApiModelProperty(value = "供应商ID")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @ApiModelProperty(value = "仓库ID")
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @ApiModelProperty(value = "退货日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "退货日期不能为空")
    private LocalDate returnDate;

    @ApiModelProperty(value = "已退金额")
    private BigDecimal refundAmount;

    @ApiModelProperty(value = "结算账户ID")
    private Long accountId;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    @Valid
    private List<WmsPurchaseReturnItemSaveReqVO> items;
}
