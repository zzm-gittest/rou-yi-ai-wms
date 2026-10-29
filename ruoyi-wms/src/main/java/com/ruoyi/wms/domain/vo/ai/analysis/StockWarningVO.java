package com.ruoyi.wms.domain.vo.ai.analysis;

import java.math.BigDecimal;

/**
 * 库存预警条目 VO
 *
 * @author ruoyi
 */
public class StockWarningVO {

    /** 商品ID */
    private Long goodsId;
    /** 商品名称 */
    private String goodsName;
    /** 规格 */
    private String spec;
    /** 当前库存数量 */
    private BigDecimal currentStock;
    /** 单位 */
    private String unitName;
    /** 日均销量 */
    private BigDecimal dailyAvgSales;
    /** 预计可售天数 */
    private int estimatedDays;
    /** 预警等级：RED=3天内断货, ORANGE=7天内断货 */
    private String warningLevel;

    public StockWarningVO() {
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public BigDecimal getDailyAvgSales() {
        return dailyAvgSales;
    }

    public void setDailyAvgSales(BigDecimal dailyAvgSales) {
        this.dailyAvgSales = dailyAvgSales;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    public void setEstimatedDays(int estimatedDays) {
        this.estimatedDays = estimatedDays;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }
}
