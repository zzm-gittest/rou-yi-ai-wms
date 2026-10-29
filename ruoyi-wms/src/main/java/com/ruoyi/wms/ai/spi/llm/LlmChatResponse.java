package com.ruoyi.wms.ai.spi.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用 LLM 调用响应。
 *
 * <p>所有 {@link LlmClient} 实现都应填充 {@link #content} 与（如可获取）{@link #tokenUsage}。</p>
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LlmChatResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模型返回的文本内容
     */
    private String content;

    /**
     * 实际使用的模型名称
     */
    private String modelName;

    /**
     * 调用方传入或上游生成的 requestId
     */
    private String requestId;

    /**
     * 端到端耗时（毫秒）
     */
    private Integer costMs;

    /**
     * Token 用量；上游若未返回则为 null
     */
    private TokenUsage tokenUsage;

    /**
     * 完成原因（stop / length / ...）
     */
    private String finishReason;

    public static LlmChatResponse ofContent(String content) {
        return new LlmChatResponse().setContent(content);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
    }
}
