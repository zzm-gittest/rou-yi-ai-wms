package com.ruoyi.wms.domain.vo.ai.analysis;

import java.math.BigDecimal;

/**
 * 采购汇总数据 VO
 *
 * @author ruoyi
 */
public class PurchaseSummaryVO {

    /** 时间段标签 */
    private String periodLabel;
    /** 采购入库总额 */
    private BigDecimal purchaseAmount;
    /** 采购退货金额 */
    private BigDecimal returnAmount;
    /** 净采购额 */
    private BigDecimal netPurchaseAmount;
    /** 入库单数 */
    private int inCount;

    public PurchaseSummaryVO() {
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(BigDecimal purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public BigDecimal getNetPurchaseAmount() {
        return netPurchaseAmount;
    }

    public void setNetPurchaseAmount(BigDecimal netPurchaseAmount) {
        this.netPurchaseAmount = netPurchaseAmount;
    }

    public int getInCount() {
        return inCount;
    }

    public void setInCount(int inCount) {
        this.inCount = inCount;
    }
}
