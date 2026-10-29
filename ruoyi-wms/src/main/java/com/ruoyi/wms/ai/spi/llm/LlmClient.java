package com.ruoyi.wms.ai.spi.llm;

/**
 * WMS 内部统一的 LLM 调用门面（SPI）。
 *
 * <p>实现：{@code com.ruoyi.wms.ai.spi.llm.impl.saas.SaasYmlLlmClient}，
 * 直接读取 {@code ai.service.qwen.api-key} / {@code ai.service.vision.api-key} 等 yml 配置调用 DashScope。</p>
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
     * 当前 LlmClient 的运行模式标识，便于日志与诊断
     */
    String mode();
}
