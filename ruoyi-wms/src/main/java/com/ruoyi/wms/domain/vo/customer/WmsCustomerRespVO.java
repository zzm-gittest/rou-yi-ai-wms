package com.ruoyi.wms.domain.vo.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户响应 Response VO
 *
 * @author ruoyi
 */
@ApiModel("客户 Response VO")
@Data
public class WmsCustomerRespVO {

    @ApiModelProperty(value = "客户ID")
    private Long id;

    @ApiModelProperty(value = "客户编码")
    private String customerCode;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "联系人")
    private String contact;

    @ApiModelProperty(value = "联系电话")
    private String phone;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "地址")
    private String address;

    @ApiModelProperty(value = "客户类型（1批发 2零售）")
    private String customerType;

    @ApiModelProperty(value = "信用额度")
    private BigDecimal creditLimit;

    @ApiModelProperty(value = "当前欠款")
    private BigDecimal currentArrears;

    @ApiModelProperty(value = "结算方式（1现结 2账期）")
    private String settlementType;

    @ApiModelProperty(value = "账期天数")
    private Integer paymentDays;

    @ApiModelProperty(value = "状态（0正常 1停用）")
    private String status;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
