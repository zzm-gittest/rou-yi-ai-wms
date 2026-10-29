package com.ruoyi.wms.service;

/**
 * 审核流程编排 Service
 */
public interface WmsBillApproveFlowService {

    @FunctionalInterface
    interface ApproveAction {
        void run();
    }

    /**
     * 当前流程是否需要审核
     */
    boolean needApprove(String billType);

    /**
     * 免审时是否自动打印小票
     */
    boolean isAutoPrint(String billType);

    /**
     * 创建后按策略触发自动审核
     */
    void autoApproveAfterCreate(String billType, Long billId, String billNo, ApproveAction approveAction);

    /**
     * 获取当前审核模式
     */
    String currentApproveMode();

    /**
     * 审核成功日志
     */
    void recordApproveSuccess(String billType, Long billId, String billNo);

    /**
     * 审核失败日志
     */
    void recordApproveFail(String billType, Long billId, String billNo, Throwable throwable);
}
