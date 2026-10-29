package com.ruoyi.wms.domain.vo.integration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("统一查单单行")
public class BillQueryRowVO {

    @ApiModelProperty("域编码 PO/SO/SOUT...")
    private String domainType;

    @ApiModelProperty("单据类型名称")
    private String domainTypeName;

    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("单号")
    private String billNo;

    @ApiModelProperty("往来方")
    private String partnerName;

    @ApiModelProperty("仓库")
    private String warehouseName;

    @ApiModelProperty("状态码")
    private String status;

    @ApiModelProperty("状态文案")
    private String statusLabel;

    @ApiModelProperty("业务日期")
    private String bizDate;

    @ApiModelProperty("金额")
    private BigDecimal totalAmount;
}
