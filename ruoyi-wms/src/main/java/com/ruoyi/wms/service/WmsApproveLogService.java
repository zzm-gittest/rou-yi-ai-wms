package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.WmsApproveLog;
import com.ruoyi.wms.domain.vo.approveLog.WmsApproveLogPageReqVO;

/**
 * 审核日志 Service
 */
public interface WmsApproveLogService extends IService<WmsApproveLog> {

    /**
     * 分页查询审核日志
     */
    IPage<WmsApproveLog> page(WmsApproveLogPageReqVO reqVO);

    /**
     * 记录审核日志
     */
    void record(String billType,
                Long billId,
                String billNo,
                String actionType,
                String approveMode,
                String result,
                String errorMsg,
                String policySnapshot,
                String remark);
}
