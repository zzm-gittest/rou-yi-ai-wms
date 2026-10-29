package com.ruoyi.wms.service.ai.query;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 统一查单解析结果（规则 NLU，非本地向量模型）
 */
public class BillQueryIntent {

    private final Set<String> domainTypes = new LinkedHashSet<>();
    private boolean pendingOnly;
    private int days;
    private String keyword;
    private String billNo;
    /** 按完整单号精准查询（禁用模糊 keyword、列表聚合展示） */
    private boolean exactBillNoOnly;

    public Set<String> getDomainTypes() {
        return domainTypes;
    }

    public boolean isPendingOnly() {
        return pendingOnly;
    }

    public void setPendingOnly(boolean pendingOnly) {
        this.pendingOnly = pendingOnly;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public boolean isExactBillNoOnly() {
        return exactBillNoOnly;
    }

    public void setExactBillNoOnly(boolean exactBillNoOnly) {
        this.exactBillNoOnly = exactBillNoOnly;
    }
}
