package com.ruoyi.wms.service.ai.query;

/**
 * 统一查单 · 单据域编码（与 PC 助手 QUERY_BILL_CENTER 一致，供 IM/开放接口复用）
 */
public final class BillDomainCodes {

    private BillDomainCodes() {
    }

    public static final String PO = "PO";
    public static final String PI = "PI";
    public static final String PR = "PR";
    public static final String SO = "SO";
    public static final String SOUT = "SOUT";
    public static final String SR = "SR";
    public static final String SC = "SC";
    public static final String ST = "ST";

    public static String toDisplayName(String domainType) {
        if (PO.equals(domainType)) {
            return "采购订单";
        }
        if (PI.equals(domainType)) {
            return "采购入库";
        }
        if (PR.equals(domainType)) {
            return "采购退货";
        }
        if (SO.equals(domainType)) {
            return "销售订单";
        }
        if (SOUT.equals(domainType)) {
            return "销售出库";
        }
        if (SR.equals(domainType)) {
            return "销售退货";
        }
        if (SC.equals(domainType)) {
            return "库存盘点";
        }
        if (ST.equals(domainType)) {
            return "库存调拨";
        }
        return domainType;
    }

    public static String toStatusLabel(String status) {
        if ("0".equals(status)) {
            return "待审核";
        }
        if ("1".equals(status)) {
            return "已审核";
        }
        if ("2".equals(status)) {
            return "执行中";
        }
        if ("3".equals(status)) {
            return "已完成";
        }
        return status == null || status.isEmpty() ? "-" : status;
    }
}
