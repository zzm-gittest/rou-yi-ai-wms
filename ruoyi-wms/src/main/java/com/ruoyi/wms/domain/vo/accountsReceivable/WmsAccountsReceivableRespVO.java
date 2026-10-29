package com.ruoyi.wms.domain.vo.accountsReceivable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应收账款响应VO
 *
 * @author ruoyi
 */
@Data
@ApiModel("应收账款响应")
public class WmsAccountsReceivableRespVO {

    @ApiModelProperty(value = "主键ID")
    private Long id;

    @ApiModelProperty(value = "客户ID")
    private Long customerId;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "供应商ID（采购退货时使用）")
    private Long supplierId;

    @ApiModelProperty(value = "供应商名称")
    private String supplierName;

    @ApiModelProperty(value = "单据类型（1销售订单 2销售出库 3采购退货）")
    private String billType;

    @ApiModelProperty(value = "单据类型名称")
    private String billTypeName;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据ID")
    private Long billId;

    @ApiModelProperty(value = "单据金额")
    private BigDecimal billAmount;

    @ApiModelProperty(value = "已收金额")
    private BigDecimal receivedAmount;

    @ApiModelProperty(value = "应收金额")
    private BigDecimal arAmount;

    @ApiModelProperty(value = "状态（0未结清 1已结清）")
    private String arStatus;

    @ApiModelProperty(value = "状态名称")
    private String arStatusName;

    @ApiModelProperty(value = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @ApiModelProperty(value = "到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
