package com.ruoyi.wms.ai.spi.quota;

/**
 * AI 配额计量场景，与现有 SaaS 计费模型保持对齐。
 *
 * <p>对应 {@link com.ruoyi.system.service.AiPointsService} 现有常量：</p>
 * <ul>
 *   <li>{@link #TEXT}  → COST_TEXT = 1 点</li>
 *   <li>{@link #VOICE} → COST_VOICE = 4 点</li>
 *   <li>{@link #IMAGE} → COST_IMAGE = 6 点</li>
 * </ul>
 *
 * @author ruoyi
 */
public enum AiQuotaScene {

    /** 文字/对话类调用（最便宜） */
    TEXT("text"),

    /** 语音识别 + 文字解析的组合（语音 ASR 单独使用） */
    VOICE("voice"),

    /** 图片识别（OCR + 视觉模型） */
    IMAGE("ocr_image");

    private final String code;

    AiQuotaScene(String code) {
        this.code = code;
    }

    /**
     * 与 AiPointsService.source 字段一致的字符串编码（兼容现有流水）
     */
    public String code() {
        return code;
    }
}
