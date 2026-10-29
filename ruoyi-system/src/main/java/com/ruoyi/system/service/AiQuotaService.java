package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.system.mapper.SysTenantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI接口配额服务
 *
 * 负责配额的预占、回滚、查询和重置
 * 使用原子SQL实现并发安全的配额扣减
 *
 * @author ruoyi
 */
@Service
public class AiQuotaService
{
    private static final Logger log = LoggerFactory.getLogger(AiQuotaService.class);

    @Autowired
    private SysTenantMapper tenantMapper;

    /**
     * 预占文字解析配额（调用LLM前调用）
     *
     * @param tenantId 租户ID
     * @return true=预占成功，false=配额不足或未启用
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryReserveTextQuota(Long tenantId)
    {
        int updated = tenantMapper.tryReserveTextQuota(tenantId);
        if (updated == 0)
        {
            log.warn("租户[{}]文字解析配额不足或AI未启用", tenantId);
        }
        return updated > 0;
    }

    /**
     * 预占语音识别配额（调用ASR前调用）
     *
     * @param tenantId 租户ID
     * @return true=预占成功，false=配额不足或未启用
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryReserveVoiceQuota(Long tenantId)
    {
        int updated = tenantMapper.tryReserveVoiceQuota(tenantId);
        if (updated == 0)
        {
            log.warn("租户[{}]语音识别配额不足或语音未启用", tenantId);
        }
        return updated > 0;
    }

    /**
     * 回滚文字解析配额（LLM调用失败时）
     *
     * @param tenantId 租户ID
     */
    public void rollbackTextQuota(Long tenantId)
    {
        tenantMapper.rollbackTextQuota(tenantId);
        log.info("租户[{}]文字解析配额已回滚", tenantId);
    }

    /**
     * 回滚语音识别配额（ASR调用失败时）
     *
     * @param tenantId 租户ID
     */
    public void rollbackVoiceQuota(Long tenantId)
    {
        tenantMapper.rollbackVoiceQuota(tenantId);
        log.info("租户[{}]语音识别配额已回滚", tenantId);
    }

    /**
     * 查询租户配额信息
     *
     * @param tenantId 租户ID
     * @return 租户信息（含配额字段）
     */
    public SysTenant getQuotaInfo(Long tenantId)
    {
        return tenantMapper.selectTenantById(tenantId);
    }

    /**
     * 月度配额重置（每月1号执行）
     *
     * @return 重置的租户数
     */
    @Transactional(rollbackFor = Exception.class)
    public int resetMonthlyQuota()
    {
        int count = tenantMapper.resetMonthlyQuota();
        log.info("月度配额重置完成，影响{}个租户", count);
        return count;
    }

    /**
     * 年度配额重置（每年1月1号执行）
     *
     * @return 重置的租户数
     */
    @Transactional(rollbackFor = Exception.class)
    public int resetAnnualQuota()
    {
        int count = tenantMapper.resetAnnualQuota();
        log.info("年度配额重置完成，影响{}个租户", count);
        return count;
    }
}
