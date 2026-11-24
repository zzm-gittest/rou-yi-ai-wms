package com.ruoyi.wms.ai.spi.quota.impl;

import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.system.service.AiPointsService;
import com.ruoyi.wms.ai.spi.quota.AiQuotaGuard;
import com.ruoyi.wms.ai.spi.quota.AiQuotaScene;
import com.ruoyi.wms.ai.spi.quota.QuotaResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SaaS 模式下的 {@link AiQuotaGuard} 实现：直接包装 {@link AiPointsService}，
 * 行为与原有 Controller 内联代码一致。
 *
 * <p>启用条件：{@code ai.client.mode=saas}（默认）。</p>
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "ai.client.mode", havingValue = "saas", matchIfMissing = true)
public class SaasPointsQuotaGuard implements AiQuotaGuard {

    private static final Logger log = LoggerFactory.getLogger(SaasPointsQuotaGuard.class);

    public static final String MODE = "saas";

    @Autowired
    private AiPointsService aiPointsService;

    @Override
    public QuotaResult tryConsume(AiQuotaScene scene, String bizRef) {
        int cost = costOf(scene);
        Long tenantId = TenantHolder.getTenantId();
        if (tenantId == null) {
            log.warn("[SaasPointsQuotaGuard] 当前请求无 tenantId，禁止 AI 调用");
            return QuotaResult.deny("当前会话未绑定租户，无法使用 AI 功能");
        }
        boolean ok = aiPointsService.tryConsumePoints(tenantId, cost, scene.code(), bizRef);
        if (!ok) {
            String msg = String.format("AI 点数不足，%s需要%d点，请充值", labelOf(scene), cost);
            return QuotaResult.deny(msg);
        }
        return QuotaResult.allow(cost, scene.code());
    }

    @Override
    public void refund(AiQuotaScene scene, String bizRef) {
        int cost = costOf(scene);
        Long tenantId = TenantHolder.getTenantId();
        if (tenantId == null) {
            log.warn("[SaasPointsQuotaGuard] refund 时无 tenantId，跳过");
            return;
        }
        aiPointsService.refundPoints(tenantId, cost, scene.code());
    }

    @Override
    public String mode() {
        return MODE;
    }

    @Override
    public boolean isMeteredMode() {
        return true;
    }

    private static int costOf(AiQuotaScene scene) {
        return switch (scene) {
            case TEXT -> AiPointsService.COST_TEXT;
            case VOICE -> AiPointsService.COST_VOICE;
            case IMAGE -> AiPointsService.COST_IMAGE;
        };
    }

    private static String labelOf(AiQuotaScene scene) {
        return switch (scene) {
            case TEXT -> "文字解析";
            case VOICE -> "语音识别";
            case IMAGE -> "截图识别";
        };
    }
}
