package com.ruoyi.wms.ai.spi.context;

/**
 * AI 调用方上下文解析器（SPI）。
 *
 * <p>SaaS 模式从 sa-token + TenantHolder 拿到 tenantId / userId；
 * Gateway 模式可从请求 Header 或 yml 注入固定的"私有部署租户"。</p>
 *
 * @author ruoyi
 */
public interface CallerContextResolver {

    /**
     * 解析当前调用方上下文（始终返回非 null；缺失字段允许为 null）
     */
    CallerContext current();

    /**
     * 当前解析器模式标识，便于日志与诊断
     */
    String mode();
}
