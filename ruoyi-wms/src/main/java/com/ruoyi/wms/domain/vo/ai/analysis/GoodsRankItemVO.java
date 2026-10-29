package com.ruoyi.wms.domain.vo.ai.analysis;

import java.math.BigDecimal;

/**
 * 商品排行条目 VO（轻量版，用于 AI 分析排行展示）
 *
 * @author ruoyi
 */
public class GoodsRankItemVO {

    /** 排名 */
    private int rank;
    /** 商品名称 */
    private String goodsName;
    /** 规格 */
    private String spec;
    /** 销售金额 */
    private BigDecimal salesAmount;
    /** 销售数量 */
    private BigDecimal salesQuantity;
    /** 单位 */
    private String unitName;

    public GoodsRankItemVO() {
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public BigDecimal getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount) {
        this.salesAmount = salesAmount;
    }

    public BigDecimal getSalesQuantity() {
        return salesQuantity;
    }

    public void setSalesQuantity(BigDecimal salesQuantity) {
        this.salesQuantity = salesQuantity;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
