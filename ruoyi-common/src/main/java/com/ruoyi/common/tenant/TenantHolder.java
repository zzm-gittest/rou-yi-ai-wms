package com.ruoyi.common.tenant;

/**
 * 租户上下文持有者
 * 使用ThreadLocal存储当前请求的租户ID
 *
 * @author ruoyi
 */
public class TenantHolder
{
    /**
     * 租户ID的ThreadLocal
     */
    private static final ThreadLocal<Long> TENANT_ID = new ThreadLocal<>();

    /**
     * 默认租户ID
     */
    public static final Long DEFAULT_TENANT_ID = 1L;

    /**
     * 设置当前租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId)
    {
        if (tenantId == null)
        {
            TENANT_ID.set(DEFAULT_TENANT_ID);
        }
        else
        {
            TENANT_ID.set(tenantId);
        }
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static Long getTenantId()
    {
        Long tenantId = TENANT_ID.get();
        if (tenantId == null)
        {
            return DEFAULT_TENANT_ID;
        }
        return tenantId;
    }

    /**
     * 清除当前租户ID
     */
    public static void clear()
    {
        TENANT_ID.remove();
    }

    /**
     * 动态切换租户ID
     *
     * @param tenantId 租户ID
     * @param runnable 执行的任务
     */
    public static void dynamicTenant(Long tenantId, Runnable runnable)
    {
        Long oldTenantId = getTenantId();
        try
        {
            setTenantId(tenantId);
            runnable.run();
        }
        finally
        {
            setTenantId(oldTenantId);
        }
    }
}
