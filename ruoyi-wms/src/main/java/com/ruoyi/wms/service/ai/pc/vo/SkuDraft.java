package com.ruoyi.wms.service.ai.pc.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SkuDraft implements Serializable {
    private static final long serialVersionUID = 1L;

    private String skuName;
    private String specInfo;
    private String unitName;
    private BigDecimal quantity;
    private BigDecimal price;
}
