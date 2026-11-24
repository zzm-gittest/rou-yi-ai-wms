package com.ruoyi.wms.ai.spi.context.impl;

import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.ai.spi.context.CallerContext;
import com.ruoyi.wms.ai.spi.context.CallerContextResolver;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 私有部署 Gateway 模式下的 {@link CallerContextResolver} 实现：
 * 优先从 sa-token + TenantHolder 解析（保持与 SaaS 一致的用户审计能力）；
 * 解析失败时回退到 yml 中预置的 {@code aigw.gateway.tenant-tag} / {@code user-tag}。
 *
 * <p>启用条件：{@code ai.client.mode=gateway} 或 {@code ai.client.mode=dsh}——
 * 两种外部模型模式都需要把租户/用户/场景审计字段传给模型提供方。</p>
 *
 * @author ruoyi
 */
@Component
@ConditionalOnExpression("'${ai.client.mode:saas}' == 'gateway' or '${ai.client.mode:saas}' == 'dsh'")
public class GatewayCallerContextResolver implements CallerContextResolver {

    private static final Logger log = LoggerFactory.getLogger(GatewayCallerContextResolver.class);

    public static final String MODE = "gateway";

    @Autowired
    private GatewayCallerFallbackProperties fallback;

    @Override
    public CallerContext current() {
        CallerContext ctx = new CallerContext().setSource("api-key");
        try {
            ctx.setTenantId(TenantHolder.getTenantId());
        } catch (Exception e) {
            log.debug("[GatewayCallerContextResolver] tenantId 解析失败: {}", e.getMessage());
        }
        try {
            ctx.setUserId(SecurityUtils.getUserId());
        } catch (Exception ignored) {
        }
        try {
            ctx.setUserName(SecurityUtils.getUsername());
        } catch (Exception ignored) {
        }
        if (ctx.getTenantId() == null && fallback.getTenantId() != null) {
            ctx.setTenantId(fallback.getTenantId());
        }
        if (ctx.getUserName() == null && fallback.getUserName() != null) {
            ctx.setUserName(fallback.getUserName());
        }
        return ctx;
    }

    @Override
    public String mode() {
        return MODE;
    }

    /**
     * Gateway 模式下，当 sa-token 上下文不可用（如后台任务）时使用的默认值。
     */
    @Data
    @Component
    @ConditionalOnExpression("'${ai.client.mode:saas}' == 'gateway' or '${ai.client.mode:saas}' == 'dsh'")
    @ConfigurationProperties(prefix = "ai.gateway-caller")
    public static class GatewayCallerFallbackProperties {

        /**
         * 默认租户 ID（私有部署通常 = 1）
         */
        private Long tenantId;

        /**
         * 默认用户名（用于审计）
         */
        private String userName;
    }
}
