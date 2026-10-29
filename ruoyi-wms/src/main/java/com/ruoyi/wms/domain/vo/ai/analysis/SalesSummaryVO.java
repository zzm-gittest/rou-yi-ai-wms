package com.ruoyi.wms.domain.vo.ai.analysis;

import java.math.BigDecimal;

/**
 * 销售汇总数据 VO
 *
 * @author ruoyi
 */
public class SalesSummaryVO {

    /** 时间段标签 */
    private String periodLabel;
    /** 销售总额 */
    private BigDecimal salesAmount;
    /** 退货金额 */
    private BigDecimal returnAmount;
    /** 净销售额 */
    private BigDecimal netSalesAmount;
    /** 订单数 */
    private int orderCount;
    /** 客单价 */
    private BigDecimal avgOrderAmount;
    /** 毛利 */
    private BigDecimal grossProfit;

    public SalesSummaryVO() {
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount) {
        this.salesAmount = salesAmount;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public BigDecimal getNetSalesAmount() {
        return netSalesAmount;
    }

    public void setNetSalesAmount(BigDecimal netSalesAmount) {
        this.netSalesAmount = netSalesAmount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getAvgOrderAmount() {
        return avgOrderAmount;
    }

    public void setAvgOrderAmount(BigDecimal avgOrderAmount) {
        this.avgOrderAmount = avgOrderAmount;
    }

    public BigDecimal getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(BigDecimal grossProfit) {
        this.grossProfit = grossProfit;
    }
}
