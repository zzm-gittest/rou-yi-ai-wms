package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.WmsBillApprovePolicy;
import com.ruoyi.wms.domain.vo.approvePolicy.WmsBillApprovePolicyPageReqVO;
import com.ruoyi.wms.domain.vo.approvePolicy.WmsBillApprovePolicySaveReqVO;

/**
 * 租户单据审核策略 Service
 */
public interface WmsBillApprovePolicyService extends IService<WmsBillApprovePolicy> {

    /**
     * 分页查询策略
     */
    IPage<WmsBillApprovePolicy> page(WmsBillApprovePolicyPageReqVO reqVO);

    /**
     * 保存策略配置（存在则更新，不存在则新增）
     */
    Long saveOrUpdatePolicy(WmsBillApprovePolicySaveReqVO reqVO);

    /**
     * 查询启用中的策略
     */
    WmsBillApprovePolicy findEnabledPolicy(Long tenantId, String billType);

    /**
     * 判断当前单据是否需要审核
     */
    boolean needApprove(Long tenantId, String billType);

    /**
     * 判断免审时是否自动打印小票
     */
    boolean isAutoPrint(Long tenantId, String billType);
}
