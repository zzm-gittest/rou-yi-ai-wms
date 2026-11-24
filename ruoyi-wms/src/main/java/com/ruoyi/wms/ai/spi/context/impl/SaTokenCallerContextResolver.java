package com.ruoyi.wms.ai.spi.context.impl;

import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.ai.spi.context.CallerContext;
import com.ruoyi.wms.ai.spi.context.CallerContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SaaS 模式下的 {@link CallerContextResolver} 实现：
 * 从 sa-token 的 SecurityUtils + TenantHolder 解析。
 *
 * <p>启用条件：{@code ai.client.mode=saas}（默认）。</p>
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "ai.client.mode", havingValue = "saas", matchIfMissing = true)
public class SaTokenCallerContextResolver implements CallerContextResolver {

    private static final Logger log = LoggerFactory.getLogger(SaTokenCallerContextResolver.class);

    public static final String MODE = "saas";

    @Override
    public CallerContext current() {
        CallerContext ctx = new CallerContext().setSource("sa-token");
        try {
            ctx.setTenantId(TenantHolder.getTenantId());
        } catch (Exception e) {
            log.debug("[SaTokenCallerContextResolver] 获取 tenantId 失败: {}", e.getMessage());
        }
        try {
            ctx.setUserId(SecurityUtils.getUserId());
        } catch (Exception e) {
            log.debug("[SaTokenCallerContextResolver] 获取 userId 失败: {}", e.getMessage());
        }
        try {
            ctx.setUserName(SecurityUtils.getUsername());
        } catch (Exception e) {
            log.debug("[SaTokenCallerContextResolver] 获取 userName 失败: {}", e.getMessage());
        }
        return ctx;
    }

    @Override
    public String mode() {
        return MODE;
    }
}
