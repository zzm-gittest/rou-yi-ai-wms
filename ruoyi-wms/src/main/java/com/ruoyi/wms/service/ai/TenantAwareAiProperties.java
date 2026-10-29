package com.ruoyi.wms.service.ai;

import com.ruoyi.wms.config.AiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 租户感知的 AI 配置包装器
 * <p>
 * 对外提供与 {@link AiProperties.QwenConfig} 相同的 getter 语义，
 * 但 apiKey / model 会优先返回租户在"企业设置"中自定义的值；
 * 租户未配置时，回退到 application.yml 全局配置。
 * <p>
 * 其余字段（apiUrl、maxTokens、temperature）始终读取全局配置，暂不支持租户级覆盖。
 *
 * @author ruoyi
 */
@Component
public class TenantAwareAiProperties {

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private TenantAiConfigResolver tenantAiConfigResolver;

    public String getApiKey() {
        return tenantAiConfigResolver.resolveApiKey();
    }

    public String getApiUrl() {
        return tenantAiConfigResolver.resolveApiUrl();
    }

    public String getModel() {
        return tenantAiConfigResolver.resolveModel();
    }

    public Integer getMaxTokens() {
        return aiProperties.getQwen().getMaxTokens();
    }

    public Double getTemperature() {
        return aiProperties.getQwen().getTemperature();
    }
}
