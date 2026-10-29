package com.ruoyi.wms.ai.spi.quota;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * AI 配额预扣结果，由 {@link AiQuotaGuard#tryConsume} 返回。
 *
 * @author ruoyi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotaResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** true=允许调用 */
    private boolean allowed;

    /** 预扣额度（SaaS = 点数，Gateway = 0） */
    private int consumed;

    /** 提示信息（被拒时给前端显示） */
    private String reason;

    /** 退款时使用的句柄；SaaS 是 source 字符串，Gateway 可为空 */
    private String refundHandle;

    public static QuotaResult allow(int consumed, String refundHandle) {
        return new QuotaResult(true, consumed, null, refundHandle);
    }

    public static QuotaResult deny(String reason) {
        return new QuotaResult(false, 0, reason, null);
    }
}
