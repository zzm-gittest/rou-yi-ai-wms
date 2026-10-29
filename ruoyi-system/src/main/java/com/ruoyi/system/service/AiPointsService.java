package com.ruoyi.system.service;

import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import com.ruoyi.common.core.domain.entity.AiPointsLog;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.mapper.AiPointsLogMapper;
import com.ruoyi.system.mapper.SysTenantMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI点数服务
 *
 * 负责点数的消耗、退还、充值和查询
 * 使用原子SQL实现并发安全的点数扣减
 *
 * 点数消耗标准：文字=1点，语音=4点，截图=6点
 *
 * @author ruoyi
 */
@Service
public class AiPointsService
{
    private static final Logger log = LoggerFactory.getLogger(AiPointsService.class);

    /** 文字解析消耗点数 */
    public static final int COST_TEXT = 1;
    /** 语音识别消耗点数 */
    public static final int COST_VOICE = 4;
    /** 截图识别消耗点数 */
    public static final int COST_IMAGE = 6;

    @Autowired
    private SysTenantMapper tenantMapper;

    @Autowired
    private AiPointsLogMapper pointsLogMapper;

    /**
     * 租户是否配置了自定义 AI（使用自己的 API Key，不扣系统点数）
     */
    private boolean isTenantCustomAi(Long tenantId) {
        if (tenantId == null) {
            return false;
        }
        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        return tenant != null && StringUtils.isNotEmpty(tenant.getAiApiKey());
    }

    /**
     * 消耗AI点数（调用AI功能前调用）
     *
     * @param tenantId 租户ID
     * @param cost 消耗点数
     * @param source 来源类型（text/voice/ocr_image）
     * @param bizId 业务ID（可选）
     * @return true=扣减成功，false=点数不足或AI未启用
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryConsumePoints(Long tenantId, int cost, String source, String bizId)
    {
        // 租户配置了自己的 AI Key（用自己的资源），跳过扣点
        if (isTenantCustomAi(tenantId))
        {
            log.debug("[tenant-custom-ai] 租户使用自定义 AI，跳过积分扣减: tenantId={}, source={}", tenantId, source);
            return true;
        }
        int updated = tenantMapper.tryConsumePoints(tenantId, cost);
        if (updated == 0)
        {
            log.warn("租户[{}]AI点数不足或AI未启用，需要{}点（来源: {}）", tenantId, cost, source);
            return false;
        }
        // 记录流水
        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        AiPointsLog logEntry = new AiPointsLog();
        logEntry.setTenantId(tenantId);
        logEntry.setChangeType("consume");
        logEntry.setChangeAmount(-cost);
        logEntry.setBalanceAfter(tenant.getAiPoints());
        logEntry.setSource(source);
        logEntry.setBizId(bizId);
        pointsLogMapper.insertLog(logEntry);

        log.info("租户[{}]消耗{}点（来源: {}），剩余{}点", tenantId, cost, source, tenant.getAiPoints());
        return true;
    }

    /**
     * 消耗AI点数（无业务ID）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean tryConsumePoints(Long tenantId, int cost, String source)
    {
        return tryConsumePoints(tenantId, cost, source, null);
    }

    /**
     * 退还AI点数（AI调用失败时）
     *
     * @param tenantId 租户ID
     * @param amount 退还点数
     * @param source 来源类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundPoints(Long tenantId, int amount, String source)
    {
        // 租户配置了自己的 AI Key（用自己的资源），跳过退还
        if (isTenantCustomAi(tenantId))
        {
            log.debug("[tenant-custom-ai] 租户使用自定义 AI，跳过积分退还: tenantId={}, source={}", tenantId, source);
            return;
        }
        tenantMapper.refundPoints(tenantId, amount);
        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        AiPointsLog logEntry = new AiPointsLog();
        logEntry.setTenantId(tenantId);
        logEntry.setChangeType("refund");
        logEntry.setChangeAmount(amount);
        logEntry.setBalanceAfter(tenant.getAiPoints());
        logEntry.setSource(source);
        logEntry.setRemark("AI调用失败退还");
        pointsLogMapper.insertLog(logEntry);

        log.info("租户[{}]退还{}点（来源: {}），剩余{}点", tenantId, amount, source, tenant.getAiPoints());
    }

    /**
     * 充值AI点数
     *
     * @param tenantId 租户ID
     * @param amount 充值点数
     * @param bizId 充值单号
     * @param remark 备注
     * @return true=充值成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean chargePoints(Long tenantId, int amount, String bizId, String remark)
    {
        int updated = tenantMapper.chargePoints(tenantId, amount);
        if (updated == 0)
        {
            log.error("租户[{}]充值失败，租户不存在或已删除", tenantId);
            return false;
        }
        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        AiPointsLog logEntry = new AiPointsLog();
        logEntry.setTenantId(tenantId);
        logEntry.setChangeType("charge");
        logEntry.setChangeAmount(amount);
        logEntry.setBalanceAfter(tenant.getAiPoints());
        logEntry.setSource("recharge");
        logEntry.setBizId(bizId);
        logEntry.setRemark(remark);
        pointsLogMapper.insertLog(logEntry);

        log.info("租户[{}]充值{}点（单号: {}），余额{}点", tenantId, amount, bizId, tenant.getAiPoints());
        return true;
    }

    /**
     * 查询租户点数信息
     *
     * @param tenantId 租户ID
     * @return 点数信息Map
     */
    public Map<String, Object> getPointsInfo(Long tenantId)
    {
        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        Map<String, Object> info = new HashMap<>();
        if (tenant != null)
        {
            boolean subscriptionActive = tenant.getSubscriptionType() != null && tenant.isSubscriptionValid();
            info.put("aiPoints", tenant.getAiPoints() != null ? tenant.getAiPoints() : 0);
            info.put("totalCharged", tenant.getAiPointsTotalCharged() != null ? tenant.getAiPointsTotalCharged() : 0);
            info.put("totalUsed", tenant.getAiPointsTotalUsed() != null ? tenant.getAiPointsTotalUsed() : 0);
            info.put("aiEnabled", subscriptionActive || (tenant.getAiEnabled() != null && tenant.getAiEnabled() == 1));
            info.put("metered", true);
        }
        return info;
    }

    /**
     * 查询租户点数流水
     *
     * @param tenantId 租户ID
     * @param limit 返回条数
     * @return 流水列表
     */
    public List<AiPointsLog> getPointsLog(Long tenantId, int limit)
    {
        return pointsLogMapper.selectLogsByTenantId(tenantId, limit);
    }

    /**
     * 检查交易是否已处理（防重复充值）
     *
     * @param transactionId Apple交易ID
     * @return true=已处理过
     */
    public boolean isTransactionProcessed(String transactionId)
    {
        return pointsLogMapper.countByBizId(transactionId) > 0;
    }

    /**
     * 更新租户订阅信息
     *
     * @param tenantId 租户ID
     * @param subscriptionType 订阅类型（monthly/yearly）
     * @param expireTime 到期时间
     * @param originalTransactionId Apple原始交易ID
     * @return true=更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSubscription(Long tenantId, String subscriptionType,
                                      Date expireTime, String originalTransactionId)
    {
        int updated = tenantMapper.updateSubscription(tenantId, subscriptionType,
                expireTime, originalTransactionId);
        if (updated == 0)
        {
            log.error("租户[{}]订阅更新失败", tenantId);
            return false;
        }
        log.info("租户[{}]订阅更新: type={}, expire={}, originalTxId={}",
                tenantId, subscriptionType, expireTime, originalTransactionId);
        return true;
    }

    /**
     * 获取租户订阅状态信息
     *
     * @param tenantId 租户ID
     * @return 订阅状态Map
     */
    public Map<String, Object> getSubscriptionInfo(Long tenantId)
    {
        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        Map<String, Object> info = new HashMap<>();
        if (tenant == null)
        {
            info.put("status", "unknown");
            return info;
        }

        String subType = tenant.getSubscriptionType();
        Date subExpire = tenant.getSubscriptionExpireTime();
        Date expireTime = tenant.getExpireTime();
        Date createTime = tenant.getCreateTime();
        Date now = new Date();
        long trialEnd = createTime != null ? createTime.getTime() + 7L * 24 * 60 * 60 * 1000 : 0L;

        if (subType != null && subExpire != null && subExpire.after(now))
        {
            // 有效的Apple订阅
            info.put("status", "active");
            info.put("type", subType);
            info.put("expireTime", subExpire);
        }
        else if (subType == null && createTime != null && now.getTime() < trialEnd
                && (expireTime == null || expireTime.getTime() <= trialEnd + 86400000L))
        {
            // 试用期用户：兼容未写入expireTime的历史数据，按注册后7天计算
            Date trialExpireTime = expireTime != null ? expireTime : new Date(trialEnd);
            info.put("status", "trial");
            long leftMs = trialExpireTime.getTime() - now.getTime();
            int daysLeft = (int) Math.ceil(leftMs / (double) (24 * 60 * 60 * 1000));
            info.put("trialDaysLeft", Math.max(daysLeft, 0));
            info.put("trialExpireTime", trialExpireTime);
        }
        else if (expireTime != null && expireTime.after(now))
        {
            // 管理员手动开通/安卓人工续期：即使历史上存在已过期的 subscriptionType，也以最新手工有效期为准
            info.put("status", "active");
            info.put("type", "manual");
            info.put("expireTime", expireTime);
        }
        else if (subType == null)
        {
            info.put("status", "expired");
        }
        else
        {
            // 订阅已过期
            info.put("status", "expired");
            if (subType != null)
            {
                info.put("type", subType);
                info.put("expireTime", subExpire);
            }
        }
        return info;
    }

    /**
     * 记录订阅变更流水（写入 ai_points_log，change_type=subscribe）
     *
     * @param tenantId 租户ID
     * @param subscriptionType 订阅类型
     * @param expireTime 到期时间
     * @param remark 备注
     */
    public void logSubscriptionChange(Long tenantId, String subscriptionType,
                                      Date expireTime, String remark)
    {
        AiPointsLog logEntry = new AiPointsLog();
        logEntry.setTenantId(tenantId);
        logEntry.setChangeType("subscribe");
        logEntry.setChangeAmount(0);
        logEntry.setBalanceAfter(0);
        logEntry.setSource("monthly".equals(subscriptionType) ? "iap_monthly" : "iap_yearly");
        logEntry.setRemark(remark);
        pointsLogMapper.insertLog(logEntry);
    }

    /**
     * 根据Apple原始交易ID查找租户
     *
     * @param originalTransactionId Apple订阅原始交易ID
     * @return 租户信息，未找到返回null
     */
    public SysTenant findTenantByOriginalTransactionId(String originalTransactionId)
    {
        return tenantMapper.selectTenantByAppleOriginalTxId(originalTransactionId);
    }
}
