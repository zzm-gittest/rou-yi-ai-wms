package com.ruoyi.wms.domain.vo.ai.analysis;

import java.time.LocalDate;

/**
 * 经营分析时间段枚举
 *
 * @author ruoyi
 */
public enum AnalysisPeriod {

    /** 今日 */
    TODAY("today", "今日"),
    /** 昨日 */
    YESTERDAY("yesterday", "昨日"),
    /** 本周 */
    THIS_WEEK("this_week", "本周"),
    /** 本月 */
    THIS_MONTH("this_month", "本月"),
    /** 上月 */
    LAST_MONTH("last_month", "上月");

    private final String code;
    private final String label;

    AnalysisPeriod(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 根据 code 获取枚举，默认返回 TODAY
     */
    public static AnalysisPeriod fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return TODAY;
        }
        for (AnalysisPeriod p : values()) {
            if (p.code.equals(code)) {
                return p;
            }
        }
        return TODAY;
    }

    /**
     * 获取开始日期
     */
    public LocalDate getStartDate() {
        LocalDate today = LocalDate.now();
        switch (this) {
            case TODAY:
                return today;
            case YESTERDAY:
                return today.minusDays(1);
            case THIS_WEEK:
                return today.with(java.time.DayOfWeek.MONDAY);
            case THIS_MONTH:
                return today.withDayOfMonth(1);
            case LAST_MONTH:
                return today.minusMonths(1).withDayOfMonth(1);
            default:
                return today;
        }
    }

    /**
     * 获取结束日期
     */
    public LocalDate getEndDate() {
        LocalDate today = LocalDate.now();
        switch (this) {
            case TODAY:
                return today;
            case YESTERDAY:
                return today.minusDays(1);
            case THIS_WEEK:
                return today;
            case THIS_MONTH:
                return today;
            case LAST_MONTH:
                return today.minusMonths(1).withDayOfMonth(
                        today.minusMonths(1).lengthOfMonth());
            default:
                return today;
        }
    }

    /**
     * 获取上一周期（用于同比计算）
     */
    public AnalysisPeriod getPreviousPeriod() {
        switch (this) {
            case TODAY:
                return YESTERDAY;
            case THIS_MONTH:
                return LAST_MONTH;
            default:
                return this;
        }
    }
}
