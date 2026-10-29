package com.ruoyi.wms.domain.vo.ai.analysis;

import java.math.BigDecimal;

/**
 * 客户排行条目 VO
 *
 * @author ruoyi
 */
public class CustomerRankItemVO {

    /** 排名 */
    private int rank;
    /** 客户名称 */
    private String customerName;
    /** 销售金额 */
    private BigDecimal salesAmount;
    /** 订单数 */
    private int orderCount;

    public CustomerRankItemVO() {
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount) {
        this.salesAmount = salesAmount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }
}
