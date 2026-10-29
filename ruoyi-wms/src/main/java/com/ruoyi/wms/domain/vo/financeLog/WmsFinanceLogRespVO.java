package com.ruoyi.wms.domain.vo.financeLog;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务日志响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("财务日志 Response VO")
@Data
public class WmsFinanceLogRespVO {

    @ApiModelProperty(value = "日志ID")
    private Long id;

    @ApiModelProperty(value = "凭证号")
    private String voucherNo;

    @ApiModelProperty(value = "单据类型（1收款 2付款 3费用）")
    private String billType;

    @ApiModelProperty(value = "单据类型名称")
    private String billTypeName;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "原始单据类型")
    private String originalBillType;

    @ApiModelProperty(value = "原始单据类型名称")
    private String originalBillTypeName;

    @ApiModelProperty(value = "业务类型（1采购 2销售 3退货 4费用）")
    private String businessType;

    @ApiModelProperty(value = "业务类型名称")
    private String businessTypeName;

    @ApiModelProperty(value = "往来单位ID")
    private Long partnerId;

    @ApiModelProperty(value = "往来单位名称")
    private String partnerName;

    @ApiModelProperty(value = "往来单位类型（1供应商 2客户）")
    private String partnerType;

    @ApiModelProperty(value = "账户ID")
    private Long accountId;

    @ApiModelProperty(value = "账户名称")
    private String accountName;

    @ApiModelProperty(value = "交易前余额")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "交易后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "方向（1收入 2支出）")
    private String direction;

    @ApiModelProperty(value = "方向名称")
    private String directionName;

    @ApiModelProperty(value = "支付方式（1现金 2银行 3支付宝 4微信）")
    private String paymentMethod;

    @ApiModelProperty(value = "支付方式名称")
    private String paymentMethodName;

    @ApiModelProperty(value = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "摘要说明")
    private String summary;
}
