package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsApproveLog;
import com.ruoyi.wms.domain.vo.approveLog.WmsApproveLogPageReqVO;
import com.ruoyi.wms.mapper.WmsApproveLogMapper;
import com.ruoyi.wms.service.WmsApproveLogService;
import org.springframework.stereotype.Service;

/**
 * 审核日志 Service 实现
 */
@Service
public class WmsApproveLogServiceImpl extends ServiceImpl<WmsApproveLogMapper, WmsApproveLog>
        implements WmsApproveLogService {

    @Override
    public IPage<WmsApproveLog> page(WmsApproveLogPageReqVO reqVO) {
        Page<WmsApproveLog> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        LambdaQueryWrapper<WmsApproveLog> queryWrapper = new LambdaQueryWrapper<WmsApproveLog>()
                .eq(reqVO.getTenantId() != null, WmsApproveLog::getTenantId, reqVO.getTenantId())
                .like(StringUtils.isNotEmpty(reqVO.getBillType()), WmsApproveLog::getBillType, reqVO.getBillType())
                .like(StringUtils.isNotEmpty(reqVO.getBillNo()), WmsApproveLog::getBillNo, reqVO.getBillNo())
                .eq(StringUtils.isNotEmpty(reqVO.getActionType()), WmsApproveLog::getActionType, reqVO.getActionType())
                .eq(StringUtils.isNotEmpty(reqVO.getApproveMode()), WmsApproveLog::getApproveMode, reqVO.getApproveMode())
                .eq(StringUtils.isNotEmpty(reqVO.getResult()), WmsApproveLog::getResult, reqVO.getResult())
                .like(StringUtils.isNotEmpty(reqVO.getOperator()), WmsApproveLog::getOperator, reqVO.getOperator())
                .orderByDesc(WmsApproveLog::getCreateTime)
                .orderByDesc(WmsApproveLog::getId);
        return page(page, queryWrapper);
    }

    @Override
    public void record(String billType,
                       Long billId,
                       String billNo,
                       String actionType,
                       String approveMode,
                       String result,
                       String errorMsg,
                       String policySnapshot,
                       String remark) {
        WmsApproveLog log = new WmsApproveLog();
        log.setTenantId(safeTenantId());
        log.setBillType(billType);
        log.setBillId(billId);
        log.setBillNo(billNo);
        log.setActionType(actionType);
        log.setApproveMode(approveMode);
        log.setResult(result);
        log.setOperator(safeUsername());
        log.setOperatorId(safeUserId());
        log.setErrorMsg(errorMsg);
        log.setPolicySnapshot(policySnapshot);
        log.setRemark(remark);
        save(log);
    }

    private Long safeTenantId() {
        try {
            return SecurityUtils.getTenantId();
        } catch (ServiceException e) {
            return null;
        }
    }

    private Long safeUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (ServiceException e) {
            return null;
        }
    }

    private String safeUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (ServiceException e) {
            return "system";
        }
    }
}
