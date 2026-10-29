package com.ruoyi.wms.domain.vo.salesReturn;

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
 * 销售退货保存Request VO
 *
 * @author ruoyi
 */
@ApiModel("销售退货保存Request VO")
@Data
public class WmsSalesReturnSaveReqVO {

    @ApiModelProperty(value = "退货单ID")
    private Long id;

    @ApiModelProperty(value = "销售出库ID")
    private Long outId;

    @ApiModelProperty(value = "来源类型（1直接入库 2出库单退货）")
    private Integer sourceType;

    @ApiModelProperty(value = "客户ID")
    @NotNull(message = "客户不能为空")
    private Long customerId;

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
    private List<WmsSalesReturnItemSaveReqVO> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOutId() { return outId; }
    public void setOutId(Long outId) { this.outId = outId; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<WmsSalesReturnItemSaveReqVO> getItems() { return items; }
    public void setItems(List<WmsSalesReturnItemSaveReqVO> items) { this.items = items; }
}
