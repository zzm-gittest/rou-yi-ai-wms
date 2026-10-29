package com.ruoyi.wms.utils;

import cn.hutool.core.text.TextSimilarity;
import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 商品名称模糊匹配工具类。
 *
 * <p>面向语音、截图 OCR、文字识别等场景下的商品名称匹配：用户输入常常带空格、
 * 缺字、顺序颠倒或轻微错别字，传统 {@code LIKE '%x%'} 连续子串匹配无法命中。</p>
 *
 * <p>相似度打分委托给 Hutool 成熟的 {@link cn.hutool.core.text.TextSimilarity}
 * （基于最长公共子串，久经生产验证），本类只负责两件简单、确定性的事：
 * <ol>
 *     <li>{@link #normalize(String)} 归一化：去空格/标点、全角转半角、英文小写；</li>
 *     <li>{@link #bigrams(String)} 拆字符二元组，仅用于数据库粗召回的命中片段。</li>
 * </ol>
 * 这样避免自研复杂相似度算法，同时解决空格/缺字/乱序的匹配问题。</p>
 *
 * @author ruoyi
 */
public final class GoodsNameMatchUtils {

    private GoodsNameMatchUtils() {
    }

    /**
     * 触发模糊匹配的最小归一化字符数（低于该长度只做包含匹配，避免噪音）。
     */
    public static final int MIN_FUZZY_LENGTH = 2;

    /**
     * 归一化文本：去除所有空白与常见标点、全角转半角、英文统一小写、繁体转简体。
     *
     * <p>繁体→简体：使用 opencc4j 词典进行 T→S 转换，
     * 避免用户输入“九江雙蒸”而库里是“九江双蒸”时匹配不上。</p>
     *
     * @param text 原始文本
     * @return 归一化结果，入参为空时返回空串
     */
    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 先做繁体→简体转换（opencc4j 内置词典，含常用字符/词映射）。
        // 遇到异常时静默降级为原文本，保证匹配主流程不受影响。
        String zhSimplified;
        try {
            zhSimplified = ZhConverterUtil.toSimple(text);
            if (zhSimplified == null || zhSimplified.isEmpty()) {
                zhSimplified = text;
            }
        } catch (Throwable ignored) {
            zhSimplified = text;
        }
        StringBuilder sb = new StringBuilder(zhSimplified.length());
        for (int i = 0; i < zhSimplified.length(); i++) {
            char c = zhSimplified.charAt(i);
            // 全角空格转半角空格
            if (c == '\u3000') {
                c = ' ';
            }
            // 全角字符（！-～）转半角
            if (c >= '\uFF01' && c <= '\uFF5E') {
                c = (char) (c - 0xFEE0);
            }
            // 跳过空白字符
            if (Character.isWhitespace(c)) {
                continue;
            }
            // 跳过常见标点/分隔符，只保留字母、数字、及非标点的其它字符（如中文）
            if (isSkippablePunctuation(c)) {
                continue;
            }
            // 英文统一小写
            if (c >= 'A' && c <= 'Z') {
                c = Character.toLowerCase(c);
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean isSkippablePunctuation(char c) {
        // 常见 ASCII 标点
        if (c >= '!' && c <= '/') {
            return true;
        }
        if (c >= ':' && c <= '@') {
            return true;
        }
        if (c >= '[' && c <= '`') {
            return true;
        }
        if (c >= '{' && c <= '~') {
            return true;
        }
        // 常见中文标点
        switch (c) {
            case '，':
            case '。':
            case '、':
            case '；':
            case '：':
            case '？':
            case '！':
            case '“':
            case '”':
            case '‘':
            case '’':
            case '（':
            case '）':
            case '【':
            case '】':
            case '《':
            case '》':
            case '—':
            case '·':
            case '~':
                return true;
            default:
                return false;
        }
    }

    /**
     * 将归一化文本拆成字符 bigram 集合。长度为 1 时返回该单字本身。
     *
     * @param normalized 已归一化文本
     * @return bigram 集合
     */
    public static Set<String> bigrams(String normalized) {
        Set<String> set = new LinkedHashSet<>();
        if (normalized == null || normalized.isEmpty()) {
            return set;
        }
        if (normalized.length() == 1) {
            set.add(normalized);
            return set;
        }
        for (int i = 0; i < normalized.length() - 1; i++) {
            set.add(normalized.substring(i, i + 2));
        }
        return set;
    }

    /**
     * 计算两段文本的相似度（0~1）。
     * <ul>
     *     <li>任一方归一化后为空：0</li>
     *     <li>归一化后完全相等：1.0</li>
     *     <li>一方是另一方的连续子串：0.75 基础分 + 按长度覆盖比例加权，保证包含匹配总是高分</li>
     *     <li>否则回退到 Hutool {@link TextSimilarity#similar(String, String)}（最长公共子串相似度）</li>
     * </ul>
     *
     * @param query     用户输入（识别结果）
     * @param candidate 候选商品名称
     * @return 相似度分值，范围 [0,1]
     */
    public static double similarity(String query, String candidate) {
        String a = normalize(query);
        String b = normalize(candidate);
        if (a.isEmpty() || b.isEmpty()) {
            return 0d;
        }
        if (a.equals(b)) {
            return 1d;
        }

        // 包含关系：短串完全出现在长串中，给较高分并按长度覆盖比例加权
        String shorter = a.length() <= b.length() ? a : b;
        String longer = a.length() <= b.length() ? b : a;
        if (longer.contains(shorter)) {
            double coverage = (double) shorter.length() / longer.length();
            // 0.75 基础分 + 覆盖度加权，保证包含匹配总是高于一般相似度命中
            return 0.75d + 0.25d * coverage;
        }

        // 其余情况交给 Hutool 的成熟实现（最长公共子串相似度）
        return TextSimilarity.similar(a, b);
    }
}

