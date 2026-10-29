package com.ruoyi.wms.domain.vo.salesOut;

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
 * 销售出库保存Request VO
 *
 * @author ruoyi
 */
@ApiModel("销售出库保存Request VO")
@Data
public class WmsSalesOutSaveReqVO {

    // 注意：字段允许传入不代表可修改权限，具体改动约束以服务层兜底校验为准。

    @ApiModelProperty(value = "出库单ID")
    private Long id;

    @ApiModelProperty(value = "客户ID")
    @NotNull(message = "客户不能为空")
    private Long customerId;

    @ApiModelProperty(value = "仓库ID")
    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    @ApiModelProperty(value = "销售订单ID")
    private Long orderId;

    @ApiModelProperty(value = "来源类型（1直接出库 2订单出库）")
    private Integer sourceType;

    @ApiModelProperty(value = "来源单号（飞书单据号，导入时写入）")
    private String sourceBillNo;

    @ApiModelProperty(value = "出库日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "出库日期不能为空")
    private LocalDate outDate;

    @ApiModelProperty(value = "已收金额")
    private BigDecimal receivedAmount;

    @ApiModelProperty(value = "结算账户ID")
    private Long accountId;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "明细列表")
    @Valid
    private List<WmsSalesOutItemSaveReqVO> items;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Integer getSourceType() { return sourceType; }
    public void setSourceType(Integer sourceType) { this.sourceType = sourceType; }
    public LocalDate getOutDate() { return outDate; }
    public void setOutDate(LocalDate outDate) { this.outDate = outDate; }
    public BigDecimal getReceivedAmount() { return receivedAmount; }
    public void setReceivedAmount(BigDecimal receivedAmount) { this.receivedAmount = receivedAmount; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getHandler() { return handler; }
    public void setHandler(String handler) { this.handler = handler; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<WmsSalesOutItemSaveReqVO> getItems() { return items; }
    public void setItems(List<WmsSalesOutItemSaveReqVO> items) { this.items = items; }
}
