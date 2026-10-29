package com.ruoyi.wms.service.ai.query;



import com.ruoyi.common.utils.StringUtils;



import java.util.Locale;

import java.util.regex.Matcher;

import java.util.regex.Pattern;



/**

 * 统一查单意图解析（从 PC {@code PcQueryFlowSupport#parseQueryIntent} 规则抽取，独立维护，不修改 PC 代码）

 */

public final class BillQueryIntentParser {



    private static final Pattern BILL_NO_LABELED = Pattern.compile(

        "(?:单号|单据号|订单号)\\s*[:：]?\\s*([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE);



    /** 裸单号：如 CK202605210002、XS202605210001 */

    private static final Pattern BILL_NO_STANDALONE = Pattern.compile(

        "(?:^|[\\s　])([A-Z]{2}\\d{8,})\\b", Pattern.CASE_INSENSITIVE);



    private BillQueryIntentParser() {

    }



    public static BillQueryIntent parse(String input) {

        String text = StringUtils.defaultString(input).trim();

        BillQueryIntent intent = new BillQueryIntent();



        String billNo = extractBillNo(text);

        if (StringUtils.isNotBlank(billNo)) {

            intent.setBillNo(billNo);

            intent.setExactBillNoOnly(true);

            intent.setKeyword(null);

            intent.setPendingOnly(false);

            intent.setDays(0);

            intent.getDomainTypes().add(BillDomainCodes.PO);

            intent.getDomainTypes().add(BillDomainCodes.PI);

            intent.getDomainTypes().add(BillDomainCodes.PR);

            intent.getDomainTypes().add(BillDomainCodes.SO);

            intent.getDomainTypes().add(BillDomainCodes.SOUT);

            intent.getDomainTypes().add(BillDomainCodes.SR);

            intent.getDomainTypes().add(BillDomainCodes.SC);

            intent.getDomainTypes().add(BillDomainCodes.ST);

            return intent;

        }



        if (text.contains("采购订单")) {

            intent.getDomainTypes().add(BillDomainCodes.PO);

        }

        if (text.contains("采购入库")) {

            intent.getDomainTypes().add(BillDomainCodes.PI);

        }

        if (text.contains("采购退货")) {

            intent.getDomainTypes().add(BillDomainCodes.PR);

        }

        if (text.contains("销售订单")) {

            intent.getDomainTypes().add(BillDomainCodes.SO);

        }

        if (text.contains("销售出库")) {

            intent.getDomainTypes().add(BillDomainCodes.SOUT);

        }

        if (text.contains("销售退货")) {

            intent.getDomainTypes().add(BillDomainCodes.SR);

        }

        if (text.contains("库存盘点") || text.contains("盘点")) {

            intent.getDomainTypes().add(BillDomainCodes.SC);

        }

        if (text.contains("库存调拨") || text.contains("调拨")) {

            intent.getDomainTypes().add(BillDomainCodes.ST);

        }

        if (intent.getDomainTypes().isEmpty()) {

            intent.getDomainTypes().add(BillDomainCodes.PO);

            intent.getDomainTypes().add(BillDomainCodes.PI);

            intent.getDomainTypes().add(BillDomainCodes.PR);

            intent.getDomainTypes().add(BillDomainCodes.SO);

            intent.getDomainTypes().add(BillDomainCodes.SOUT);

            intent.getDomainTypes().add(BillDomainCodes.SR);

            intent.getDomainTypes().add(BillDomainCodes.SC);

            intent.getDomainTypes().add(BillDomainCodes.ST);

        }

        intent.setPendingOnly(text.contains("待审核") || text.contains("待审批"));

        intent.setDays(text.contains("今天") ? 1 : (text.contains("近7天") ? 7 : 0));

        String keyword = text

            .replace("查询", "")

            .replace("查", "")

            .replace("全部", "")

            .replace("单据", "")

            .replace("单号", "")

            .replace("单据号", "")

            .replace("订单号", "")

            .replace("相关", "")

            .replace("最近7天", "")

            .replace("近7天", "")

            .replace("今天", "")

            .replace("待审核", "")

            .replace("采购订单", "")

            .replace("采购入库", "")

            .replace("采购退货", "")

            .replace("销售订单", "")

            .replace("销售出库", "")

            .replace("销售退货", "")

            .replace("库存盘点", "")

            .replace("盘点", "")

            .replace("库存调拨", "")

            .replace("调拨", "")

            .replace("统一查询", "")

            .trim();

        intent.setKeyword(keyword.length() >= 2 ? keyword : null);

        return intent;

    }



    /**

     * 从用户输入提取完整业务单号（用于 IM 精准查单）

     */

    public static String extractBillNo(String input) {

        if (StringUtils.isBlank(input)) {

            return null;

        }

        String text = input.trim();

        Matcher labeled = BILL_NO_LABELED.matcher(text);

        if (labeled.find()) {

            return normalizeBillNo(labeled.group(1));

        }

        Matcher standalone = BILL_NO_STANDALONE.matcher(text);

        if (standalone.find()) {

            return normalizeBillNo(standalone.group(1));

        }

        return null;

    }



    private static String normalizeBillNo(String raw) {

        if (StringUtils.isBlank(raw)) {

            return null;

        }

        return raw.trim().toUpperCase(Locale.ROOT);

    }

}

