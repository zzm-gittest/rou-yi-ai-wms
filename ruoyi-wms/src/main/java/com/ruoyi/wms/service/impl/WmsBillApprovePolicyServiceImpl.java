package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsBillApprovePolicy;
import com.ruoyi.wms.domain.vo.approvePolicy.WmsBillApprovePolicyPageReqVO;
import com.ruoyi.wms.domain.vo.approvePolicy.WmsBillApprovePolicySaveReqVO;
import com.ruoyi.wms.mapper.WmsBillApprovePolicyMapper;
import com.ruoyi.wms.service.WmsBillApprovePolicyService;
import org.springframework.stereotype.Service;

/**
 * 租户单据审核策略 Service 实现
 */
@Service
public class WmsBillApprovePolicyServiceImpl extends ServiceImpl<WmsBillApprovePolicyMapper, WmsBillApprovePolicy>
        implements WmsBillApprovePolicyService {

    @Override
    public IPage<WmsBillApprovePolicy> page(WmsBillApprovePolicyPageReqVO reqVO) {
        Page<WmsBillApprovePolicy> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        LambdaQueryWrapper<WmsBillApprovePolicy> queryWrapper = new LambdaQueryWrapper<WmsBillApprovePolicy>()
                .eq(reqVO.getTenantId() != null, WmsBillApprovePolicy::getTenantId, reqVO.getTenantId())
                .like(StringUtils.isNotEmpty(reqVO.getBillType()), WmsBillApprovePolicy::getBillType, reqVO.getBillType())
                .eq(reqVO.getNeedApprove() != null, WmsBillApprovePolicy::getNeedApprove, reqVO.getNeedApprove())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsBillApprovePolicy::getStatus, reqVO.getStatus())
                .orderByAsc(WmsBillApprovePolicy::getTenantId)
                .orderByAsc(WmsBillApprovePolicy::getBillType);
        return page(page, queryWrapper);
    }

    @Override
    public Long saveOrUpdatePolicy(WmsBillApprovePolicySaveReqVO reqVO) {
        String billType = reqVO.getBillType().trim().toUpperCase();
        if (reqVO.getNeedApprove() != 0 && reqVO.getNeedApprove() != 1) {
            throw new ServiceException("是否需要审核仅支持 0 或 1");
        }
        if (!"0".equals(reqVO.getStatus()) && !"1".equals(reqVO.getStatus())) {
            throw new ServiceException("状态仅支持 0 或 1");
        }

        WmsBillApprovePolicy policy;
        if (reqVO.getId() != null) {
            policy = getById(reqVO.getId());
            if (policy == null) {
                throw new ServiceException("策略不存在");
            }
        } else {
            policy = getOne(new LambdaQueryWrapper<WmsBillApprovePolicy>()
                    .eq(WmsBillApprovePolicy::getTenantId, reqVO.getTenantId())
                    .eq(WmsBillApprovePolicy::getBillType, billType)
                    .last("LIMIT 1"));
            if (policy == null) {
                policy = new WmsBillApprovePolicy();
                policy.setTenantId(reqVO.getTenantId());
                policy.setBillType(billType);
                policy.setCreateBy(SecurityUtils.getUsername());
            }
        }

        WmsBillApprovePolicy duplicated = getOne(new LambdaQueryWrapper<WmsBillApprovePolicy>()
                .eq(WmsBillApprovePolicy::getTenantId, reqVO.getTenantId())
                .eq(WmsBillApprovePolicy::getBillType, billType)
                .ne(policy.getId() != null, WmsBillApprovePolicy::getId, policy.getId())
                .last("LIMIT 1"));
        if (duplicated != null) {
            throw new ServiceException("该租户单据类型策略已存在");
        }

        policy.setTenantId(reqVO.getTenantId());
        policy.setBillType(billType);
        policy.setNeedApprove(reqVO.getNeedApprove());
        policy.setAutoPrint(reqVO.getAutoPrint() == null ? 0 : reqVO.getAutoPrint());
        policy.setStatus(reqVO.getStatus());
        policy.setRemark(reqVO.getRemark());
        policy.setUpdateBy(SecurityUtils.getUsername());

        if (policy.getId() == null) {
            save(policy);
        } else {
            updateById(policy);
        }
        return policy.getId();
    }

    @Override
    public WmsBillApprovePolicy findEnabledPolicy(Long tenantId, String billType) {
        if (tenantId == null || billType == null) {
            return null;
        }
        return getOne(new LambdaQueryWrapper<WmsBillApprovePolicy>()
                .eq(WmsBillApprovePolicy::getTenantId, tenantId)
                .eq(WmsBillApprovePolicy::getBillType, billType)
                .eq(WmsBillApprovePolicy::getStatus, "0")
                .last("LIMIT 1"));
    }

    @Override
    public boolean needApprove(Long tenantId, String billType) {
        WmsBillApprovePolicy policy = findEnabledPolicy(tenantId, billType);
        if (policy == null) {
            return true;
        }
        Integer needApprove = policy.getNeedApprove();
        return needApprove == null || needApprove == 1;
    }

    @Override
    public boolean isAutoPrint(Long tenantId, String billType) {
        WmsBillApprovePolicy policy = findEnabledPolicy(tenantId, billType);
        if (policy == null) {
            return false;
        }
        // 仅在免审时才自动打印
        Integer needApprove = policy.getNeedApprove();
        if (needApprove != null && needApprove == 0) {
            return Integer.valueOf(1).equals(policy.getAutoPrint());
        }
        return false;
    }
}
