package com.ruoyi.wms.ai.spi.llm.impl.gateway;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway 模式 LlmClient 配置（{@code ai.client.mode=gateway} 时生效）。
 *
 * <p>yml 示例：</p>
 * <pre>
 * aigw:
 *   gateway:
 *     base-url: https://aigw.example.com
 *     api-key: ${AIGW_INTERNAL_KEY_WMS:please-change-me}
 *     header-name: X-AIGW-Internal-Key
 *     text-model: qwen-max
 *     vision-model: qwen-vl-plus
 *     connect-timeout-ms: 5000
 *     read-timeout-ms: 90000
 * </pre>
 *
 * @author ruoyi
 */
@Data
@Component
@ConfigurationProperties(prefix = "aigw.gateway")
public class AigwGatewayProperties {

    /**
     * AIGW 服务基地址，如 https://aigw.example.com，不带末尾斜杠
     */
    private String baseUrl;

    /**
     * 调用 AIGW 时携带的内部 API Key（必须在 AIGW 侧 aigw.internal.keys 中配置）
     */
    private String apiKey;

    /**
     * Header 名（与 AIGW 侧 aigw.internal.header-name 保持一致）
     */
    private String headerName = "X-AIGW-Internal-Key";

    /**
     * 文本对话默认模型名（AIGW chat_model.model_name 之一）
     */
    private String textModel = "qwen-max";

    /**
     * 视觉对话默认模型名
     */
    private String visionModel = "qwen-vl-plus";

    /**
     * 连接超时（毫秒）
     */
    private int connectTimeoutMs = 5000;

    /**
     * 读超时（毫秒）；视觉调用建议 >= 60000
     */
    private int readTimeoutMs = 90000;

    /**
     * 私有部署的"租户标识"，会作为审计字段传给 AIGW（callerTenantId）
     */
    private String tenantTag;

    /**
     * 私有部署的"用户标识"，会作为审计字段传给 AIGW（callerUserId）；
     * 通常用部署实例名或留空，由 CallerContextResolver 提供更具体值
     */
    private String userTag;
}
