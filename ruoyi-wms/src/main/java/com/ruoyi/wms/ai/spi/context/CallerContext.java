package com.ruoyi.wms.ai.spi.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 调用方上下文。
 *
 * <p>由 {@link CallerContextResolver} 在调用前解析；
 * 用于将「谁在调用、属于哪个租户、什么场景」一并传到 LlmClient / AiQuotaGuard。</p>
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CallerContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名（便于日志）
     */
    private String userName;

    /**
     * 业务场景（如 voice_parse / image_ocr / order_chat）；与 AIGW 审计字段 usageScene 对齐
     */
    private String usageScene;

    /**
     * 业务侧 requestId
     */
    private String requestId;

    /**
     * 来源标识：sa-token / api-key / job / cli
     */
    private String source;
}
