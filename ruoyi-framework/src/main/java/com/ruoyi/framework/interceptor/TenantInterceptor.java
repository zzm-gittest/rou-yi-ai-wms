package com.ruoyi.framework.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.PageHolder;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 租户拦截器
 * 在每个请求开始时设置租户ID到TenantHolder，请求结束时清除
 *
 * @author ruoyi
 */
@Component
public class TenantInterceptor implements HandlerInterceptor
{

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        try
        {
            Long tenantId = SecurityUtils.getTenantId();
            if (tenantId != null)
            {
                TenantHolder.setTenantId(tenantId);
            }
        }
        catch (Exception e)
        {
            // 未登录：不调用 setTenantId(null)，避免把 Filter 已写入的租户覆盖为默认租户 1
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
    {
        // 请求结束后清除租户ID和分页对象，避免内存泄漏
        TenantHolder.clear();
        PageHolder.clear();
    }
}
