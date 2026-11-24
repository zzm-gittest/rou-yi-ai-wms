package com.ruoyi.wms.ai.spi.quota.impl;

import com.ruoyi.wms.ai.spi.quota.AiQuotaGuard;
import com.ruoyi.wms.ai.spi.quota.AiQuotaScene;
import com.ruoyi.wms.ai.spi.quota.QuotaResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * 私有部署 Gateway 模式下的 {@link AiQuotaGuard} 实现：完全 no-op。
 *
 * <p>私有部署内不计量积分，所有 AI 功能默认放行；
 * 真正的用量审计由 AIGW 侧的 {@code aigw_call_log} / {@code aigw_token_usage} 完成。</p>
 *
 * <p>启用条件：{@code ai.client.mode=gateway} 或 {@code ai.client.mode=dsh}——
 * 两种模式都由外部系统提供模型，WMS 本地不计量积分。</p>
 *
 * @author ruoyi
 */
@Component
@ConditionalOnExpression("'${ai.client.mode:saas}' == 'gateway' or '${ai.client.mode:saas}' == 'dsh'")
public class NoOpQuotaGuard implements AiQuotaGuard {

    public static final String MODE = "gateway";

    @Override
    public QuotaResult tryConsume(AiQuotaScene scene, String bizRef) {
        return QuotaResult.allow(0, null);
    }

    @Override
    public void refund(AiQuotaScene scene, String bizRef) {
    }

    @Override
    public String mode() {
        return MODE;
    }

    @Override
    public boolean isMeteredMode() {
        return false;
    }
}
