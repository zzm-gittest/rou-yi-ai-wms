package com.ruoyi.wms.service.ai.pc.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class PendingGoodsDraft implements Serializable {
    private static final long serialVersionUID = 1L;

    private String goodsName;
    private String unitName;
    private Long categoryId;
    private String categoryName;
    private BigDecimal defaultQuantity;
    private BigDecimal defaultPrice;
    private List<SpecDraft> specs = new ArrayList<>();
    private List<SkuDraft> skuDrafts = new ArrayList<>();
}
