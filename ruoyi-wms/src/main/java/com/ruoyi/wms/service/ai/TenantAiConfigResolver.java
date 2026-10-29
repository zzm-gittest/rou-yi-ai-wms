package com.ruoyi.wms.service.ai;

import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysTenantService;
import com.ruoyi.wms.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 租户级 AI 配置解析器
 * <p>
 * 优先读取当前租户在 sys_tenant 中配置的 ai_api_key / ai_model；
 * 租户未配置（null/空）时，回退到 application.yml 中的全局 ai.service.qwen.* 配置。
 *
 * @author ruoyi
 */
@Component
public class TenantAiConfigResolver {

    private static final Logger log = LoggerFactory.getLogger(TenantAiConfigResolver.class);

    @Autowired
    private ISysTenantService tenantService;

    @Autowired
    private AiProperties aiProperties;

    /**
     * 解析当前租户应使用的 API Key
     */
    public String resolveApiKey() {
        String tenantKey = getTenantAiApiKey();
        if (StringUtils.isNotEmpty(tenantKey)) {
            log.debug("[TenantAiConfig] 使用租户级 API Key");
            return tenantKey;
        }
        String globalKey = aiProperties.getQwen().getApiKey();
        log.debug("[TenantAiConfig] 租户未配置 API Key，使用全局配置");
        return globalKey;
    }

    /**
     * 解析当前租户应使用的模型名称
     */
    public String resolveModel() {
        String tenantModel = getTenantAiModel();
        if (StringUtils.isNotEmpty(tenantModel)) {
            log.debug("[TenantAiConfig] 使用租户级模型: {}", tenantModel);
            return tenantModel;
        }
        String globalModel = aiProperties.getQwen().getModel();
        log.debug("[TenantAiConfig] 租户未配置模型，使用全局配置: {}", globalModel);
        return globalModel;
    }

    /**
     * 解析当前租户应使用的 API 地址
     */
    public String resolveApiUrl() {
        String tenantUrl = getTenantAiApiUrl();
        if (StringUtils.isNotEmpty(tenantUrl)) {
            log.debug("[TenantAiConfig] 使用租户级 API 地址: {}", tenantUrl);
            return tenantUrl;
        }
        String globalUrl = aiProperties.getQwen().getApiUrl();
        log.debug("[TenantAiConfig] 租户未配置 API 地址，使用全局配置: {}", globalUrl);
        return globalUrl;
    }

    /**
     * 获取当前租户的自定义 API Key（可能为空）
     */
    public String getTenantAiApiKey() {
        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return null;
        }
        try {
            SysTenant tenant = tenantService.selectTenantById(tenantId);
            if (tenant != null) {
                return tenant.getAiApiKey();
            }
        } catch (Exception e) {
            log.warn("[TenantAiConfig] 读取租户 AI 配置失败, tenantId={}", tenantId, e);
        }
        return null;
    }

    /**
     * 获取当前租户的自定义模型（可能为空）
     */
    public String getTenantAiModel() {
        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return null;
        }
        try {
            SysTenant tenant = tenantService.selectTenantById(tenantId);
            if (tenant != null) {
                return tenant.getAiModel();
            }
        } catch (Exception e) {
            log.warn("[TenantAiConfig] 读取租户 AI 配置失败, tenantId={}", tenantId, e);
        }
        return null;
    }

    /**
     * 解析当前租户应使用的视觉模型名称（截图识别）
     */
    public String resolveVisionModel() {
        String tenantModel = getTenantAiVisionModel();
        if (StringUtils.isNotEmpty(tenantModel)) {
            log.debug("[TenantAiConfig] 使用租户级视觉模型: {}", tenantModel);
            return tenantModel;
        }
        String globalModel = aiProperties.getVision().getModel();
        log.debug("[TenantAiConfig] 租户未配置视觉模型，使用全局配置: {}", globalModel);
        return globalModel;
    }

    /**
     * 获取当前租户的自定义视觉模型（可能为空）
     */
    public String getTenantAiVisionModel() {
        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return null;
        }
        try {
            SysTenant tenant = tenantService.selectTenantById(tenantId);
            if (tenant != null) {
                return tenant.getAiVisionModel();
            }
        } catch (Exception e) {
            log.warn("[TenantAiConfig] 读取租户视觉模型配置失败, tenantId={}", tenantId, e);
        }
        return null;
    }

    /**
     * 解析当前租户应使用的语音模型名称（语音识别）
     */
    public String resolveVoiceModel() {
        String tenantModel = getTenantAiVoiceModel();
        if (StringUtils.isNotEmpty(tenantModel)) {
            log.debug("[TenantAiConfig] 使用租户级语音模型: {}", tenantModel);
            return tenantModel;
        }
        String globalModel = aiProperties.getAsr().getModel();
        log.debug("[TenantAiConfig] 租户未配置语音模型，使用全局配置: {}", globalModel);
        return globalModel;
    }

    /**
     * 获取当前租户的自定义语音模型（可能为空）
     */
    public String getTenantAiVoiceModel() {
        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return null;
        }
        try {
            SysTenant tenant = tenantService.selectTenantById(tenantId);
            if (tenant != null) {
                return tenant.getAiVoiceModel();
            }
        } catch (Exception e) {
            log.warn("[TenantAiConfig] 读取租户语音模型配置失败, tenantId={}", tenantId, e);
        }
        return null;
    }

    /**
     * 获取当前租户的自定义 API 地址（可能为空）
     */
    public String getTenantAiApiUrl() {
        Long tenantId = resolveTenantId();
        if (tenantId == null) {
            return null;
        }
        try {
            SysTenant tenant = tenantService.selectTenantById(tenantId);
            if (tenant != null) {
                return tenant.getAiApiUrl();
            }
        } catch (Exception e) {
            log.warn("[TenantAiConfig] 读取租户 AI 配置失败, tenantId={}", tenantId, e);
        }
        return null;
    }

    private Long resolveTenantId() {
        try {
            return SecurityUtils.getTenantId();
        } catch (Exception e) {
            // 非登录上下文（如定时任务、飞书 Webhook）
            Long holderTenantId = TenantHolder.getTenantId();
            if (holderTenantId != null && !TenantHolder.DEFAULT_TENANT_ID.equals(holderTenantId)) {
                return holderTenantId;
            }
            return null;
        }
    }
}
