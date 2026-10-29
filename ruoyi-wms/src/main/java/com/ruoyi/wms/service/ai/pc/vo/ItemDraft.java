package com.ruoyi.wms.service.ai.pc.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ItemDraft implements Serializable {
    private static final long serialVersionUID = 1L;

    // 保持与原逻辑兼容：当前主流程大量直接字段访问。
    public String name;
    public String unit;
    public BigDecimal quantity;
    public BigDecimal price;
    public Long forceSkuId;
}
