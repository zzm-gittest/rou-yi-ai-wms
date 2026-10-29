package com.ruoyi.common.tenant;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ruoyi.common.utils.StringUtils;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;

/**
 * MyBatis-Plus租户行处理器
 *
 * @author ruoyi
 */
public class TenantLineHandlerImpl implements TenantLineHandler
{
    /**
     * 忽略租户隔离的表（全局共享的表）
     */
    private static final List<String> IGNORE_TABLES = Arrays.asList(
        "sys_tenant",           // 租户表本身
        "sys_user",             // 用户表（登录时需要跨租户查询，用户携带tenant_id字段）
        "sys_dept",             // 部门表（通过用户关联，自动隔离）
        "sys_menu",             // 菜单表（全局共享）
        "sys_role",             // 角色表（全局共享，超管可管理所有租户角色）
        "sys_user_role",        // 用户角色关联表（通过user_id关联，自动隔离）
        "sys_user_post",        // 用户岗位关联表（通过user_id关联，自动隔离）
        "sys_role_dept",        // 角色部门关联表（通过role_id关联，自动隔离）
        "sys_role_menu",        // 角色菜单关联表（全局共享）
        "sys_config",           // 系统配置（全局共享）
        "sys_dict_type",        // 字典类型（全局共享）
        "sys_dict_data",        // 字典数据（全局共享）
        "sys_notice",           // 通知公告（可选择性隔离）
        "sys_oper_log",         // 操作日志（不隔离）
        "sys_logininfor",       // 登录日志（不隔离）
        "sys_job",              // 定时任务（全局）
        "sys_job_log",          // 定时任务日志（不隔离）
        "sys_post",             // 岗位表（全局共享）
        "gen_table",            // 代码生成业务表
        "gen_table_column"      // 代码生成业务列
    );

    /**
     * 忽略租户隔离的表前缀
     */
    private static final List<String> IGNORE_TABLE_PREFIXES = Arrays.asList(
        "qrtz_"
    );

    /**
     * 获取租户ID
     */
    @Override
    public Expression getTenantId()
    {
        Long tenantId = TenantHolder.getTenantId();
        if (tenantId == null)
        {
            return new NullValue();
        }
        return new LongValue(tenantId);
    }

    /**
     * 获取租户字段名
     */
    @Override
    public String getTenantIdColumn()
    {
        return "tenant_id";
    }

    /**
     * 判断是否忽略该表
     */
    @Override
    public boolean ignoreTable(String tableName)
    {
        if (StringUtils.isEmpty(tableName))
        {
            return false;
        }

        String lowerTableName = tableName.toLowerCase();

        // 检查是否在忽略列表中
        if (IGNORE_TABLES.contains(lowerTableName))
        {
            return true;
        }

        // 检查是否是忽略的表前缀
        for (String prefix : IGNORE_TABLE_PREFIXES)
        {
            if (lowerTableName.startsWith(prefix))
            {
                return true;
            }
        }

        return false;
    }
}
