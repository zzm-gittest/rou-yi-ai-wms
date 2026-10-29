package com.ruoyi.wms.service.ai.pc.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class LineDraft implements Serializable {
    private static final long serialVersionUID = 1L;

    // 保持与原逻辑兼容：当前主流程大量直接字段访问。
    public Long sourceLineId;
    public Long skuId;
    public String goodsName;
    public String skuName;
    public String unitName;
    public BigDecimal orderQuantity;
    public BigDecimal receivedQty;
    public BigDecimal quantity;
    public BigDecimal price;
    public BigDecimal costPrice;
    public BigDecimal beforeQty;
    public BigDecimal maxQty;
    public String remark;
}
