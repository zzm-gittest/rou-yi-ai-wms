package com.ruoyi.wms.ai.spi.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用 LLM 视觉对话请求。
 *
 * <p>imageBase64 / imageUrl 二选一，{@code LlmClient} 实现按 SaaS / Gateway 模式适配上游协议。</p>
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LlmVisionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户提示词
     */
    private String prompt;

    /**
     * 图片 base64（不含 data:URI 前缀），与 {@link #imageUrl} 二选一
     */
    private String imageBase64;

    /**
     * 图片 MIME 类型（如 image/jpeg）
     */
    private String imageMimeType;

    /**
     * 图片 URL（与 imageBase64 二选一）
     */
    private String imageUrl;

    /**
     * 期望的最大输出 token
     */
    private Integer maxTokens;

    /**
     * 采样温度
     */
    private Double temperature;

    /**
     * 是否要求 JSON 输出
     */
    private Boolean jsonMode;

    /**
     * 业务场景标识
     */
    private String usageScene;

    /**
     * 调用方业务 requestId
     */
    private String requestId;
}
