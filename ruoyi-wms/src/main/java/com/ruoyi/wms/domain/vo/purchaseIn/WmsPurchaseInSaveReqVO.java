package com.ruoyi.wms.domain.vo.purchaseIn;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 采购入库新增/修改 Request VO
 *
 * @author ruoyi
 */
@ApiModel("采购入库新增/修改 Request VO")
@Data
public class WmsPurchaseInSaveReqVO {

    @ApiModelProperty(value = "入库单ID")
    private Long id;

    @ApiModelProperty(value = "采购订单ID")
    private Long orderId;

    @ApiModelProperty(value = "来源类型（1直接入库 2采购订单入库）")
    private Integer sourceType;

    @ApiModelProperty(value = "供应商ID")
    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @ApiModelProperty(value = "仓库ID")
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @ApiModelProperty(value = "入库日期")
    private LocalDate inDate;

    @ApiModelProperty(value = "已付金额")
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @ApiModelProperty(value = "结算账户ID")
    private Long accountId;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    @Valid
    private List<WmsPurchaseInItemSaveReqVO> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public LocalDate getInDate() { return inDate; }
    public void setInDate(LocalDate inDate) { this.inDate = inDate; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<WmsPurchaseInItemSaveReqVO> getItems() { return items; }
    public void setItems(List<WmsPurchaseInItemSaveReqVO> items) { this.items = items; }
}
