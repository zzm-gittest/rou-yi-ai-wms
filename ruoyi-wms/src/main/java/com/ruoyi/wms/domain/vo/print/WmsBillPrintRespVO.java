package com.ruoyi.wms.domain.vo.print;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ApiModel("单据打印响应 VO")
@Data
public class WmsBillPrintRespVO {

    @ApiModelProperty(value = "单据标题")
    private String title;

    @ApiModelProperty(value = "公司名称")
    private String companyName;

    @ApiModelProperty(value = "公司地址")
    private String companyAddress;

    @ApiModelProperty(value = "公司电话")
    private String companyPhone;

    @ApiModelProperty(value = "公司Logo路径")
    private String logoPath;

    @ApiModelProperty(value = "打印字体缩放倍数")
    private java.math.BigDecimal printFontScale;

    @ApiModelProperty(value = "打印纸张大小（A4/SLIP_241X279/SLIP_241X140）")
    private String printPageSize;

    @ApiModelProperty(value = "单据编号")
    private String billNo;

    @ApiModelProperty(value = "单据日期")
    private String billDate;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;

    @ApiModelProperty(value = "往来单位")
    private String counterpartName;

    @ApiModelProperty(value = "经办人")
    private String handler;

    @ApiModelProperty(value = "审核人")
    private String approveBy;

    @ApiModelProperty(value = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approveTime;

    @ApiModelProperty(value = "合计金额")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "打印时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime printTime;

    @ApiModelProperty(value = "明细列表")
    private List<WmsBillPrintItemRespVO> items;
}