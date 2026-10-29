package com.ruoyi.quartz.task;

import com.ruoyi.system.service.AiQuotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AI配额重置定时任务
 *
 * 需要在 sys_job 中配置：
 * 1. 月度重置：aiQuotaTask.resetMonthlyQuota()  cron: 0 0 0 1 * ?  （每月1号0点）
 * 2. 年度重置：aiQuotaTask.resetAnnualQuota()   cron: 0 0 0 1 1 ?  （每年1月1号0点）
 *
 * @author ruoyi
 */
@Component("aiQuotaTask")
public class AiQuotaTask
{
    private static final Logger log = LoggerFactory.getLogger(AiQuotaTask.class);

    @Autowired
    private AiQuotaService aiQuotaService;

    /**
     * 月度配额重置
     * 每月1号0点执行，重置所有租户的月度已用次数
     */
    public void resetMonthlyQuota()
    {
        log.info("开始执行月度配额重置...");
        int count = aiQuotaService.resetMonthlyQuota();
        log.info("月度配额重置完成，共重置{}个租户", count);
    }

    /**
     * 年度配额重置
     * 每年1月1号0点执行，重置所有租户的年度已用次数
     */
    public void resetAnnualQuota()
    {
        log.info("开始执行年度配额重置...");
        int count = aiQuotaService.resetAnnualQuota();
        log.info("年度配额重置完成，共重置{}个租户", count);
    }
}
