package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.StringUtils;

/**
 * 租户参数自动注入切面
 * 拦截Controller和Service层方法，自动为参数对象注入tenantId
 *
 * @author ruoyi
 */
@Aspect
@Component
public class TenantParamAspect
{
    private static final Logger log = LoggerFactory.getLogger(TenantParamAspect.class);

    /**
     * 拦截所有Controller和Service层的方法
     */
    @Around("execution(* com.ruoyi.web.controller..*.*(..)) || execution(* com.ruoyi.system.service..*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable
    {
        // 在方法执行前，为参数对象注入tenantId
        injectTenantIdToArgs(point.getArgs(), point);
        return point.proceed();
    }

    /**
     * 为参数数组中的对象注入tenantId
     */
    private void injectTenantIdToArgs(Object[] args, ProceedingJoinPoint point)
    {
        Long tenantId = TenantHolder.getTenantId();
        String methodName = point.getSignature().getName();

        if (tenantId == null)
        {
            log.debug("TenantId is null for method: {}", methodName);
            return;
        }

        log.debug("Injecting tenantId={} for method: {}, argsCount={}", tenantId, methodName, args == null ? 0 : args.length);

        if (args == null || args.length == 0)
        {
            return;
        }

        for (Object arg : args)
        {
            if (arg != null)
            {
                injectTenantId(arg, tenantId);
            }
        }
    }

    /**
     * 为对象注入tenantId
     */
    private void injectTenantId(Object obj, Long tenantId)
    {
        if (obj == null)
        {
            return;
        }

        // 跳过基本类型和String
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean)
        {
            return;
        }

        Class<?> clazz = obj.getClass();

        // 尝试调用setTenantId方法
        try
        {
            Method setTenantId = clazz.getMethod("setTenantId", Long.class);
            // 先尝试调用getTenantId检查当前值
            try
            {
                Method getTenantId = clazz.getMethod("getTenantId");
                Object currentValue = getTenantId.invoke(obj);
                // 只有当前值为null时才自动注入
                if (currentValue == null)
                {
                    setTenantId.invoke(obj, tenantId);
                    log.debug("Set tenantId={} for class: {}", tenantId, clazz.getSimpleName());
                }
            }
            catch (Exception e)
            {
                // 没有getTenantId方法，直接设置
                setTenantId.invoke(obj, tenantId);
                log.debug("Set tenantId={} for class: {} (no getter)", tenantId, clazz.getSimpleName());
            }
        }
        catch (NoSuchMethodException e)
        {
            // 没有setTenantId方法，跳过
        }
        catch (Exception e)
        {
            // 设置失败，静默忽略
            log.warn("Failed to set tenantId for class: {}", clazz.getSimpleName(), e);
        }
    }
}
