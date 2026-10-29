package com.ruoyi.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseDO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 收支流水表 wms_finance_log
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wms_finance_log")
@ApiModel(value = "WmsFinanceLog", description = "收支流水表")
public class WmsFinanceLog extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "凭证号")
    private String voucherNo;

    @ApiModelProperty(value = "单据类型（1收款 2付款 3费用）")
    private String billType;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据ID")
    private Long billId;

    @ApiModelProperty(value = "业务类型（1采购 2销售 3退货 4费用）")
    private String businessType;

    @ApiModelProperty(value = "原始单据类型")
    private String originalBillType;

    @ApiModelProperty(value = "往来单位ID")
    private Long partnerId;

    @ApiModelProperty(value = "往来单位类型（1供应商 2客户）")
    private String partnerType;

    @ApiModelProperty(value = "账户ID")
    private Long accountId;

    @ApiModelProperty(value = "交易前余额")
    private BigDecimal beforeBalance;

    @ApiModelProperty(value = "交易后余额")
    private BigDecimal afterBalance;

    @ApiModelProperty(value = "金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "方向（1收入 2支出）")
    private String direction;

    @ApiModelProperty(value = "支付方式（1现金 2银行 3支付宝 4微信）")
    private String paymentMethod;

    @ApiModelProperty(value = "单据日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate billDate;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "摘要说明")
    private String summary;

}
