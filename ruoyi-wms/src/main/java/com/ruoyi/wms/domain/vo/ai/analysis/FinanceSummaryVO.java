package com.ruoyi.wms.domain.vo.ai.analysis;

import java.math.BigDecimal;

/**
 * 财务汇总数据 VO
 *
 * @author ruoyi
 */
public class FinanceSummaryVO {

    /** 应收总额 */
    private BigDecimal receivableTotal;
    /** 应付总额 */
    private BigDecimal payableTotal;
    /** 逾期应收笔数 */
    private int overdueReceivableCount;
    /** 逾期应收金额 */
    private BigDecimal overdueReceivableAmount;

    public FinanceSummaryVO() {
    }

    public BigDecimal getReceivableTotal() {
        return receivableTotal;
    }

    public void setReceivableTotal(BigDecimal receivableTotal) {
        this.receivableTotal = receivableTotal;
    }

    public BigDecimal getPayableTotal() {
        return payableTotal;
    }

    public void setPayableTotal(BigDecimal payableTotal) {
        this.payableTotal = payableTotal;
    }

    public int getOverdueReceivableCount() {
        return overdueReceivableCount;
    }

    public void setOverdueReceivableCount(int overdueReceivableCount) {
        this.overdueReceivableCount = overdueReceivableCount;
    }

    public BigDecimal getOverdueReceivableAmount() {
        return overdueReceivableAmount;
    }

    public void setOverdueReceivableAmount(BigDecimal overdueReceivableAmount) {
        this.overdueReceivableAmount = overdueReceivableAmount;
    }
}
