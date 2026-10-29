package com.ruoyi.common.tenant;

import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.utils.StringUtils;

/**
 * 租户工具类
 * 用于处理实体类的租户ID自动填充
 *
 * @author ruoyi
 */
public class TenantHelper
{
    /**
     * 自动填充实体类的租户ID
     *
     * @param entity 实体类
     */
    public static void fillTenantId(BaseEntity entity)
    {
        if (entity != null)
        {
            entity.setTenantId(TenantHolder.getTenantId());
        }
    }

    /**
     * 自动填充实体类的租户ID（指定租户ID）
     *
     * @param entity 实体类
     * @param tenantId 租户ID
     */
    public static void fillTenantId(BaseEntity entity, Long tenantId)
    {
        if (entity != null)
        {
            entity.setTenantId(tenantId);
        }
    }

    /**
     * 获取当前租户ID
     *
     * @return 租户ID
     */
    public static Long getTenantId()
    {
        return TenantHolder.getTenantId();
    }

    /**
     * 动态切换租户，执行完成后恢复原租户
     *
     * @param tenantId 租户ID
     * @param runnable 执行的任务
     */
    public static void dynamicTenant(Long tenantId, Runnable runnable)
    {
        TenantHolder.dynamicTenant(tenantId, runnable);
    }

    /**
     * 判断是否为默认租户
     *
     * @param tenantId 租户ID
     * @return 是否为默认租户
     */
    public static boolean isDefaultTenant(Long tenantId)
    {
        return TenantHolder.DEFAULT_TENANT_ID.equals(tenantId);
    }

    /**
     * 判断当前是否为默认租户
     *
     * @return 是否为默认租户
     */
    public static boolean isDefaultTenant()
    {
        return TenantHolder.DEFAULT_TENANT_ID.equals(getTenantId());
    }

    /**
     * 构建租户条件SQL（用于手动拼接SQL）
     *
     * @param alias 表别名
     * @return 租户条件SQL
     */
    public static String getTenantCondition(String alias)
    {
        Long tenantId = getTenantId();
        if (StringUtils.isNull(alias))
        {
            return "tenant_id = " + tenantId;
        }
        return alias + ".tenant_id = " + tenantId;
    }

    /**
     * 构建租户条件SQL（用于手动拼接SQL，无别名）
     *
     * @return 租户条件SQL
     */
    public static String getTenantCondition()
    {
        return "tenant_id = " + getTenantId();
    }
}
