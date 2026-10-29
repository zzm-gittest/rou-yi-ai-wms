package com.ruoyi.framework.security.filter;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.tenant.TenantHelper;
import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.TokenService;
import com.ruoyi.system.service.ISysTenantService;

/**
 * token过滤器 验证token有效性
 * 
 * @author ruoyi
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    private static final String[] RESTRICTED_ALLOWED_PATHS = new String[] {
        "/getInfo",
        "/logout",
        "/wms/iap/verify",
        "/wms/iap/subscribe",
        "/wms/iap/restore",
        "/wms/iap/subscription/status",
        "/wms/ai/mobile/points"
    };

    private static final String[] RESTRICTED_ALLOWED_PREFIXES = new String[] {
        "/system/user/profile"
    };

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysTenantService tenantService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException
    {
        try
        {
            LoginUser loginUser = tokenService.getLoginUser(request);

            if (StringUtils.isNotNull(loginUser) && !validateTenant(loginUser, request, response))
            {
                return;
            }

            if (StringUtils.isNotNull(loginUser) && StringUtils.isNull(SecurityUtils.getAuthentication()))
            {
                tokenService.verifyToken(loginUser);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

            if (StringUtils.isNotNull(loginUser))
            {
                TenantHolder.setTenantId(loginUser.getTenantId());
            }

            chain.doFilter(request, response);
        }
        finally
        {
            // 清除租户ID，防止内存泄漏
            TenantHolder.clear();
        }
    }

    private boolean validateTenant(LoginUser loginUser, HttpServletRequest request, HttpServletResponse response)
    {
        if (isSuperAdmin(loginUser))
        {
            return true;
        }

        Long tenantId = loginUser.getTenantId();
        if (tenantId == null && loginUser.getUser() != null)
        {
            tenantId = loginUser.getUser().getTenantId();
        }
        if (tenantId == null)
        {
            return true;
        }

        TenantHolder.setTenantId(tenantId);
        SysTenant tenant = tenantService.selectTenantById(tenantId);
        if (tenant == null)
        {
            return denyInvalidTenantAccess(loginUser, response, "租户不存在，无法继续登录");
        }
        if (!tenant.isNormal())
        {
            return denyInvalidTenantAccess(loginUser, response, "租户已停用，无权登录");
        }

        if (tenant.isAccessRestricted() && !isRestrictedAllowedRequest(request))
        {
            return denyRestrictedTenantAccess(response, "当前租户服务已到期，当前仅开放个人中心和续费相关功能");
        }
        return true;
    }

    private boolean isRestrictedAllowedRequest(HttpServletRequest request)
    {
        String requestUri = request.getRequestURI();
        for (String path : RESTRICTED_ALLOWED_PATHS)
        {
            if (path.equals(requestUri))
            {
                return true;
            }
        }
        for (String prefix : RESTRICTED_ALLOWED_PREFIXES)
        {
            if (requestUri.startsWith(prefix))
            {
                return true;
            }
        }
        return false;
    }

    private boolean denyInvalidTenantAccess(LoginUser loginUser, HttpServletResponse response, String message)
    {
        SecurityContextHolder.clearContext();
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            tokenService.delLoginUser(loginUser.getToken());
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpStatus.UNAUTHORIZED, message)));
        return false;
    }

    private boolean denyRestrictedTenantAccess(HttpServletResponse response, String message)
    {
        SecurityContextHolder.clearContext();
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(HttpStatus.FORBIDDEN, message)));
        return false;
    }

    private boolean isSuperAdmin(LoginUser loginUser)
    {
        if (loginUser == null)
        {
            return false;
        }
        Long userId = loginUser.getUserId();
        if (userId == null && loginUser.getUser() != null)
        {
            userId = loginUser.getUser().getUserId();
        }
        return SecurityUtils.isAdmin(userId);
    }
}
