package com.ruoyi.wms.domain.vo.ai.analysis;

/**
 * NLU 意图识别结果 VO
 *
 * @author ruoyi
 */
public class NluIntentResult {

    /** 意图类型 */
    private String intent;
    /** 时间段 */
    private String period;
    /** 实体名称（客户名/商品名/供应商名/仓库名） */
    private String entity;
    /** 原始用户输入 */
    private String rawInput;

    public NluIntentResult() {
    }

    public NluIntentResult(String intent, String period, String entity) {
        this.intent = intent;
        this.period = period;
        this.entity = entity;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getRawInput() {
        return rawInput;
    }

    public void setRawInput(String rawInput) {
        this.rawInput = rawInput;
    }

    /**
     * 意图枚举常量
     */
    public static final String INTENT_QUERY_SALES = "QUERY_SALES";
    public static final String INTENT_QUERY_PURCHASE = "QUERY_PURCHASE";
    public static final String INTENT_QUERY_STOCK = "QUERY_STOCK";
    public static final String INTENT_QUERY_FINANCE = "QUERY_FINANCE";
    public static final String INTENT_QUERY_PENDING = "QUERY_PENDING";
    public static final String INTENT_QUERY_COMPARISON = "QUERY_COMPARISON";
    public static final String INTENT_QUERY_CUSTOMER = "QUERY_CUSTOMER";
    public static final String INTENT_QUERY_SUPPLIER = "QUERY_SUPPLIER";
    public static final String INTENT_QUERY_GOODS_RANK = "QUERY_GOODS_RANK";
    public static final String INTENT_QUERY_CUSTOMER_RANK = "QUERY_CUSTOMER_RANK";
    public static final String INTENT_UNKNOWN = "UNKNOWN";
}
