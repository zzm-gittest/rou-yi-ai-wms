package com.ruoyi.wms.domain.vo.integration;

import lombok.Data;

/**
 * IM/网关经营分析问答结果（供 AIGW 飞书卡片展示）
 */
@Data
public class IntegrationAnalysisAnswerVO {

    /** 卡片标题，如「今日销售概况」 */
    private String title;

    /** 正文（纯文本，由 NLG 模板生成） */
    private String body;

    /** 识别到的意图编码 */
    private String intent;

    /** 时间范围编码 today / this_week 等 */
    private String period;
}
