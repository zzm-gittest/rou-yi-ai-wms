package com.ruoyi.wms.ai.spi.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用 LLM 文本对话请求。
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LlmChatRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户消息（单轮调用，最常见的场景）
     */
    private String userMessage;

    /**
     * 多轮消息（如需要传完整对话历史时使用，优先于 systemPrompt+userMessage）
     */
    private List<Message> messages;

    /**
     * 期望的最大输出 token；null 表示走 LlmClient 默认值
     */
    private Integer maxTokens;

    /**
     * 采样温度；null 表示走默认
     */
    private Double temperature;

    /**
     * 是否要求 JSON 输出
     */
    private Boolean jsonMode;

    /**
     * 业务场景标识（落到审计日志）
     */
    private String usageScene;

    /**
     * 调用方业务 requestId（便于跨系统追踪）
     */
    private String requestId;

    public static LlmChatRequest of(String systemPrompt, String userMessage) {
        return new LlmChatRequest().setSystemPrompt(systemPrompt).setUserMessage(userMessage);
    }

    public List<Message> resolvedMessages() {
        if (messages != null && !messages.isEmpty()) {
            return messages;
        }
        List<Message> result = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            result.add(new Message("system", systemPrompt));
        }
        if (userMessage != null) {
            result.add(new Message("user", userMessage));
        }
        return result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * system / user / assistant
         */
        private String role;

        private String content;
    }
}
