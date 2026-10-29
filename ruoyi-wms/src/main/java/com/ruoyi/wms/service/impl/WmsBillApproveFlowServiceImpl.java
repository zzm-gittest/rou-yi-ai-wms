package com.ruoyi.wms.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsBillApprovePolicy;
import com.ruoyi.wms.service.WmsApproveLogService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import com.ruoyi.wms.service.WmsBillApprovePolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 审核流程编排 Service 实现
 */
@Service
public class WmsBillApproveFlowServiceImpl implements WmsBillApproveFlowService {

    private static final ThreadLocal<String> APPROVE_MODE_HOLDER = new ThreadLocal<>();

    @Autowired
    private WmsBillApprovePolicyService policyService;

    @Autowired
    private WmsApproveLogService approveLogService;

    @Override
    public boolean needApprove(String billType) {
        Long tenantId = safeTenantId();
        return policyService.needApprove(tenantId, billType);
    }

    @Override
    public boolean isAutoPrint(String billType) {
        Long tenantId = safeTenantId();
        return policyService.isAutoPrint(tenantId, billType);
    }

    @Override
    public void autoApproveAfterCreate(String billType, Long billId, String billNo, ApproveAction approveAction) {
        if (needApprove(billType)) {
            return;
        }
        runWithMode(WmsApproveConstants.APPROVE_MODE_AUTO_POLICY, approveAction);
    }

    @Override
    public String currentApproveMode() {
        String mode = APPROVE_MODE_HOLDER.get();
        if (StringUtils.isNotEmpty(mode)) {
            return mode;
        }
        return WmsApproveConstants.APPROVE_MODE_MANUAL;
    }

    @Override
    public void recordApproveSuccess(String billType, Long billId, String billNo) {
        approveLogService.record(
                billType,
                billId,
                billNo,
                WmsApproveConstants.ACTION_APPROVE,
                currentApproveMode(),
                WmsApproveConstants.RESULT_SUCCESS,
                null,
                buildPolicySnapshot(billType),
                "Approve success"
        );
    }

    @Override
    public void recordApproveFail(String billType, Long billId, String billNo, Throwable throwable) {
        String error = throwable == null ? null : StringUtils.substring(throwable.getMessage(), 0, 1000);
        approveLogService.record(
                billType,
                billId,
                billNo,
                WmsApproveConstants.ACTION_APPROVE,
                currentApproveMode(),
                WmsApproveConstants.RESULT_FAIL,
                error,
                buildPolicySnapshot(billType),
                "Approve failed"
        );
    }

    private void runWithMode(String mode, ApproveAction approveAction) {
        String previousMode = APPROVE_MODE_HOLDER.get();
        APPROVE_MODE_HOLDER.set(mode);
        try {
            approveAction.run();
        } finally {
            if (previousMode == null) {
                APPROVE_MODE_HOLDER.remove();
            } else {
                APPROVE_MODE_HOLDER.set(previousMode);
            }
        }
    }

    private String buildPolicySnapshot(String billType) {
        Long tenantId = safeTenantId();
        boolean needApprove = policyService.needApprove(tenantId, billType);
        boolean autoPrint = policyService.isAutoPrint(tenantId, billType);
        WmsBillApprovePolicy policy = policyService.findEnabledPolicy(tenantId, billType);
        if (policy == null) {
            return "{\"source\":\"fallback\",\"needApprove\":" + (needApprove ? 1 : 0) + ",\"autoPrint\":" + (autoPrint ? 1 : 0) + "}";
        }
        return "{\"source\":\"policy\",\"policyId\":" + policy.getId()
                + ",\"needApprove\":" + (needApprove ? 1 : 0)
                + ",\"autoPrint\":" + (autoPrint ? 1 : 0)
                + ",\"status\":\"" + policy.getStatus() + "\"}";
    }

    private Long safeTenantId() {
        try {
            return SecurityUtils.getTenantId();
        } catch (ServiceException e) {
            return null;
        }
    }
}
