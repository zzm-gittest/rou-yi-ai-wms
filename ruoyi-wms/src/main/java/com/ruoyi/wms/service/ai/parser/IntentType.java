package com.ruoyi.wms.service.ai.parser;

/**
 * 意图类型枚举
 *
 * @author ruoyi
 */
public enum IntentType {
    /**
     * 采购订单
     */
    PURCHASE_ORDER("create_purchase_in", "采购入库"),

    /**
     * 销售订单
     */
    SALES_ORDER("create_sales_out", "销售出库"),

    /**
     * 商品查询
     */
    QUERY_GOODS("query_goods", "商品查询"),

    /**
     * 库存查询
     */
    QUERY_STOCK("query_stock", "库存查询"),

    /**
     * 未知意图
     */
    UNKNOWN("unknown", "未知");

    private final String code;
    private final String description;

    IntentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取意图类型
     */
    public static IntentType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (IntentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
