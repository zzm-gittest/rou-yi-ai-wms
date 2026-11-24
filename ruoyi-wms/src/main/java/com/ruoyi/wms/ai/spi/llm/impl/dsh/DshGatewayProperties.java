package com.ruoyi.wms.ai.spi.llm.impl.dsh;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * DSH 桥接模式 LlmClient 配置（{@code ai.client.mode=dsh} 时生效）。
 *
 * <p>该模式与 gateway 模式同构：WMS 不持有任何模型 Key，
 * 把模型调用 POST 给 DeepSeek Harness（dsh）插件暴露的桥接端点，
 * 由 dsh 使用自己在「设置 → 模型」里配置的模型执行。</p>
 *
 * <p>yml 示例：</p>
 * <pre>
 * ai:
 *   client:
 *     mode: dsh
 * dsh:
 *   gateway:
 *     base-url: http://127.0.0.1:3080
 *     bridge-token: ${DSH_BRIDGE_TOKEN:please-change-me}
 *     header-name: X-WMS-Bridge-Token
 *     text-model: deepseek-chat
 *     connect-timeout-ms: 5000
 *     read-timeout-ms: 90000
 * </pre>
 *
 * @author ruoyi
 */
@Data
@Component
@ConfigurationProperties(prefix = "dsh.gateway")
public class DshGatewayProperties {

    /**
     * dsh 服务基地址（带 webServer 的主机），不带末尾斜杠。
     * 本地联调默认 http://127.0.0.1:3080；线上应为 dsh 部署机器的内网地址。
     */
    private String baseUrl;

    /**
     * 调用 dsh 桥接端点时携带的共享密钥。
     * 必须与 dsh 侧 wms-llm-bridge 插件的 sharedSecret 一致。
     */
    private String bridgeToken;

    /**
     * 共享密钥的 Header 名（与 dsh 侧配置保持一致）。
     */
    private String headerName = "X-WMS-Bridge-Token";

    /**
     * 文本对话模型名。可选：留空则由 dsh 使用其当前默认模型；
     * 填写则 dsh 按该 provider/model 路由（需在 dsh 设置页已配置）。
     */
    private String textModel;

    /**
     * 视觉对话模型名。可选，同 textModel。
     */
    private String visionModel;

    /**
     * 连接超时（毫秒）
     */
    private int connectTimeoutMs = 5000;

    /**
     * 读超时（毫秒）；模型推理较慢，默认 90 秒。
     */
    private int readTimeoutMs = 90000;

    /**
     * 调用方租户标识（审计字段，可留空由 CallerContextResolver 动态填充）
     */
    private String tenantTag;

    /**
     * 调用方用户标识（审计字段）
     */
    private String userTag;
}
