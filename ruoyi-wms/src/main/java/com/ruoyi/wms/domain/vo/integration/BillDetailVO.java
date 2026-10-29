package com.ruoyi.wms.domain.vo.integration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("单据详情（IM 只读展示）")
public class BillDetailVO {

    private String domainType;
    private String domainTypeName;
    private Long id;
    private String billNo;
    private String partnerName;
    private String warehouseName;
    private String status;
    private String statusLabel;
    private String bizDate;
    private BigDecimal totalAmount;
    private List<BillDetailLineVO> lines = new ArrayList<>();
}
