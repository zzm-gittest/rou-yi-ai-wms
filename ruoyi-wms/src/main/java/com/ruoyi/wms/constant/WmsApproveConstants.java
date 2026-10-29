package com.ruoyi.wms.constant;

/**
 * 审核相关常量
 */
public final class WmsApproveConstants {

    private WmsApproveConstants() {
    }

    public static final String BILL_TYPE_PURCHASE_ORDER = "PURCHASE_ORDER";
    public static final String BILL_TYPE_PURCHASE_IN = "PURCHASE_IN";
    public static final String BILL_TYPE_PURCHASE_RETURN = "PURCHASE_RETURN";
    public static final String BILL_TYPE_SALES_ORDER = "SALES_ORDER";
    public static final String BILL_TYPE_SALES_OUT = "SALES_OUT";
    public static final String BILL_TYPE_SALES_RETURN = "SALES_RETURN";
    public static final String BILL_TYPE_STOCK_CHECK = "STOCK_CHECK";
    public static final String BILL_TYPE_STOCK_TRANSFER = "STOCK_TRANSFER";

    public static final String ACTION_APPROVE = "APPROVE";
    public static final String APPROVE_MODE_MANUAL = "MANUAL";
    public static final String APPROVE_MODE_AUTO_POLICY = "AUTO_POLICY";
    public static final String RESULT_SUCCESS = "SUCCESS";
    public static final String RESULT_FAIL = "FAIL";
}
