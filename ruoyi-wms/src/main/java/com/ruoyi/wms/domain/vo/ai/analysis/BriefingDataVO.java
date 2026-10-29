package com.ruoyi.wms.domain.vo.ai.analysis;

import java.util.List;

/**
 * 经营简报数据 VO
 * <p>
 * 聚合销售、采购、库存、财务、待办 五个板块数据，
 * 作为 LLM 生成摘要的输入。
 *
 * @author ruoyi
 */
public class BriefingDataVO {

    /** 销售汇总 */
    private SalesSummaryVO salesSummary;
    /** 采购汇总 */
    private PurchaseSummaryVO purchaseSummary;
    /** 库存预警列表 */
    private List<StockWarningVO> stockWarnings;
    /** 财务汇总 */
    private FinanceSummaryVO financeSummary;
    /** 待办汇总 */
    private PendingTasksVO pendingTasks;
    /** 热销商品 TOP5 */
    private List<GoodsRankItemVO> topSellingGoods;

    public BriefingDataVO() {
    }

    public SalesSummaryVO getSalesSummary() {
        return salesSummary;
    }

    public void setSalesSummary(SalesSummaryVO salesSummary) {
        this.salesSummary = salesSummary;
    }

    public PurchaseSummaryVO getPurchaseSummary() {
        return purchaseSummary;
    }

    public void setPurchaseSummary(PurchaseSummaryVO purchaseSummary) {
        this.purchaseSummary = purchaseSummary;
    }

    public List<StockWarningVO> getStockWarnings() {
        return stockWarnings;
    }

    public void setStockWarnings(List<StockWarningVO> stockWarnings) {
        this.stockWarnings = stockWarnings;
    }

    public FinanceSummaryVO getFinanceSummary() {
        return financeSummary;
    }

    public void setFinanceSummary(FinanceSummaryVO financeSummary) {
        this.financeSummary = financeSummary;
    }

    public PendingTasksVO getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(PendingTasksVO pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public List<GoodsRankItemVO> getTopSellingGoods() {
        return topSellingGoods;
    }

    public void setTopSellingGoods(List<GoodsRankItemVO> topSellingGoods) {
        this.topSellingGoods = topSellingGoods;
    }
}
