package com.ruoyi.wms.domain.vo.ai.analysis;

/**
 * 待办事项汇总 VO
 *
 * @author ruoyi
 */
public class PendingTasksVO {

    /** 待审核采购订单数 */
    private int pendingPurchaseOrderCount;
    /** 待审核销售订单数 */
    private int pendingSalesOrderCount;
    /** 待入库数（已审核未入库的采购订单） */
    private int pendingPurchaseInCount;
    /** 待出库数（已审核未出库的销售订单） */
    private int pendingSalesOutCount;

    public PendingTasksVO() {
    }

    public int getPendingPurchaseOrderCount() {
        return pendingPurchaseOrderCount;
    }

    public void setPendingPurchaseOrderCount(int pendingPurchaseOrderCount) {
        this.pendingPurchaseOrderCount = pendingPurchaseOrderCount;
    }

    public int getPendingSalesOrderCount() {
        return pendingSalesOrderCount;
    }

    public void setPendingSalesOrderCount(int pendingSalesOrderCount) {
        this.pendingSalesOrderCount = pendingSalesOrderCount;
    }

    public int getPendingPurchaseInCount() {
        return pendingPurchaseInCount;
    }

    public void setPendingPurchaseInCount(int pendingPurchaseInCount) {
        this.pendingPurchaseInCount = pendingPurchaseInCount;
    }

    public int getPendingSalesOutCount() {
        return pendingSalesOutCount;
    }

    public void setPendingSalesOutCount(int pendingSalesOutCount) {
        this.pendingSalesOutCount = pendingSalesOutCount;
    }

    /**
     * 获取总待办数
     */
    public int getTotalCount() {
        return pendingPurchaseOrderCount + pendingSalesOrderCount
                + pendingPurchaseInCount + pendingSalesOutCount;
    }
}
