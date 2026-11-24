package com.ruoyi.wms.ai.spi.llm;

/**
 * WMS 内部统一的 LLM 调用门面（SPI）。
 *
 * <p>两类实现：</p>
 * <ul>
 *   <li><b>SaaS 模式</b>（默认）：{@code com.ruoyi.wms.ai.spi.llm.impl.saas.SaasYmlLlmClient}，
 *       直接读取 {@code ai.service.qwen.api-key} / {@code ai.service.vision.api-key} 等 yml 配置调用 DashScope。</li>
 *   <li><b>Gateway 模式</b>（私有部署对接 AIGW）：{@code com.ruoyi.wms.ai.spi.llm.impl.gateway.AigwLlmClient}，
 *       通过 {@code aigw.gateway.base-url} + {@code X-AIGW-Internal-Key} 调 AIGW 代理端点。</li>
 * </ul>
 *
 * <p>切换由配置 {@code ai.client.mode=saas|gateway} 控制（默认 saas，保持现网行为不变）。</p>
 *
 * @author ruoyi
 */
public interface LlmClient {

    /**
     * 文本对话
     */
    LlmChatResponse chat(LlmChatRequest request);

    /**
     * 图文（视觉）对话
     */
    LlmChatResponse vision(LlmVisionRequest request);

    /**
     * 当前 LlmClient 的运行模式标识（saas / gateway / mock），便于日志与诊断
     */
    String mode();
}
