package com.ruoyi.wms.ai.spi.quota;

/**
 * AI 配额守卫（SPI）。
 *
 * <p>核心职责：在调用 LLM 之前预扣配额、调用失败后退还。
 * SaaS 模式包装现有 {@code AiPointsService} 的积分扣费；
 * 私有部署的 Gateway 模式直接 no-op，保持业务逻辑不变。</p>
 *
 * <p>这是为「私有部署不需要计量」需求专门抽象出来的 SPI，
 * 让 WMS Controller 与 Service 不再硬依赖 SaaS 计费体系。</p>
 *
 * @author ruoyi
 */
public interface AiQuotaGuard {

    /**
     * 尝试预扣配额。
     *
     * @param scene  调用场景
     * @param bizRef 业务关联 ID（用于审计与流水）
     * @return 预扣结果，allowed=true 才能继续业务
     */
    QuotaResult tryConsume(AiQuotaScene scene, String bizRef);

    /**
     * 业务失败后退还配额。
     * SaaS 实现按场景定额退还；NoOp 实现什么都不做。
     */
    void refund(AiQuotaScene scene, String bizRef);

    /**
     * 当前守卫的运行模式标识，便于日志与诊断
     */
    String mode();

    /**
     * 是否启用积分模式（前端展示充值入口时判断）
     */
    boolean isMeteredMode();
}
