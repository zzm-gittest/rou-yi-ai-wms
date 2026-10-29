package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PcCardBuilderSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private final PcMultiBillChatServiceImpl owner;

    PcCardBuilderSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> buildDomainStartGuide(PcState state) {
        String domainType = state.getDomainType();
        return owner.conversationLifecycleSupport.buildReply(
                state,
                domainStartText(domainType),
                startTemplateCard(domainType),
                domainStartQuickActions(domainType));
    }

    Map<String, Object> buildAmbiguousReturnGuide(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", "“退货”存在两种单据路线，请先选择");
        List<Map<String, Object>> templates = new ArrayList<>();
        templates.add(template(
                "采购退货引导：请输入“采购退货，退给 XX + 商品 + 数量”",
                routeValue(DOMAIN_PR, "采购退货，退给云腾科技5台苹果笔记本，从一号仓库退货，原因是破损")));
        templates.add(template(
                "销售退货引导：请输入“销售退货，XX 退回 + 商品 + 数量”",
                routeValue(DOMAIN_SR, "销售退货，客户A退回2台苹果笔记本，退回一号仓库，原因是拒收")));
        card.put("templates", templates);
        return owner.conversationLifecycleSupport.buildReply(state,
                "你输入的是“退货”，当前无法直接判断是采购退货还是销售退货。请按下面任一模板继续。",
                card,
                owner.conversationLifecycleSupport.quickActions());
    }

    Map<String, Object> buildAmbiguousOrderGuide(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", "“订单/开单”存在两种单据路线，请先选择");
        List<Map<String, Object>> templates = new ArrayList<>();
        templates.add(template(
                "采购订单引导：请输入“向 XX 供应商采购 + 商品 + 数量 + 单价”",
                routeValue(DOMAIN_PO, "向云腾科技采购苹果笔记本50台，单价8000元")));
        templates.add(template(
                "销售订单引导：请输入“卖给 XX 客户 + 商品 + 数量 + 单价”",
                routeValue(DOMAIN_SO, "卖给客户A苹果笔记本10台，单价9000元")));
        card.put("templates", templates);
        return owner.conversationLifecycleSupport.buildReply(state,
                "你输入的是“订单/开单”，当前无法直接判断是采购订单还是销售订单。请按下面任一模板继续。",
                card,
                owner.conversationLifecycleSupport.quickActions());
    }

    Map<String, Object> domainChoiceCard() {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "请选择单据类型");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("采购订单", "__SELECT__DOMAIN:PO"));
        options.add(option("采购入库", "__SELECT__DOMAIN:PI"));
        card.put("options", options);
        return card;
    }

    Map<String, Object> supplierTemplateCard(String domainType) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", DOMAIN_PR.equals(domainType)
                ? "请按模板信息修改后发送"
                : (StringUtils.isBlank(domainType) ? "采购信息模板（建议一次说完整）" : owner.cardResultSupport.domainLabel(domainType) + "模板"));
        List<Map<String, Object>> templates = new ArrayList<>();
        if (DOMAIN_PI.equals(domainType)) {
            templates.add(template("榴莲100箱，苹果230箱，香蕉200打", routeValue(DOMAIN_PI, "榴莲100箱，苹果230箱，香蕉200打")));
            templates.add(template("苹果笔记本50台", routeValue(DOMAIN_PI, "苹果笔记本50台")));
            templates.add(template("按采购单 PO202603190001 入库到一号仓库", routeValue(DOMAIN_PI, "按采购单 PO202603190001 入库到一号仓库")));
        } else if (DOMAIN_PR.equals(domainType)) {
            templates.add(template("榴莲100箱，苹果20箱", routeValue(DOMAIN_PR, "榴莲100箱，苹果20箱")));
            templates.add(template("退给云腾科技5台坏的苹果笔记本，从一号仓库退货，每台8000元，原因是破损", routeValue(DOMAIN_PR, "退给云腾科技5台坏的苹果笔记本，从一号仓库退货，每台8000元，原因是破损")));
        } else {
            templates.add(template("向XX公司采购XX商品50台，单价8000元", routeValue(DOMAIN_PO, "向XX公司采购XX商品50台，单价8000元")));
            templates.add(template("采购XX商品50台，供应商是XX，单价8000元", routeValue(DOMAIN_PO, "采购XX商品50台，供应商是XX，单价8000元")));
        }
        card.put("templates", templates);
        return card;
    }

    Map<String, Object> goodsTemplateCard(String domainType) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "REQUIRED_FORM");
        String domainLabel = owner.cardResultSupport.domainLabel(domainType);
        card.put("title", StringUtils.isBlank(domainLabel) ? "请填写：商品明细" : domainLabel + "必填：商品明细");
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(formField("items", "商品明细", "例如：苹果笔记本,2,6800；无线鼠标,10,59 或 苹果笔记本50台，单价8000元", true));
        card.put("fields", fields);
        return card;
    }

    Map<String, Object> warehouseTemplateCard(String domainType) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", StringUtils.isBlank(domainType) ? "仓库补全模板" : owner.cardResultSupport.domainLabel(domainType) + "仓库模板");
        List<Map<String, Object>> templates = new ArrayList<>();
        if (DOMAIN_PI.equals(domainType)) {
            templates.add(template("榴莲100箱，苹果230箱，香蕉200打", routeValue(DOMAIN_PI, "榴莲100箱，苹果230箱，香蕉200打")));
            templates.add(template("苹果笔记本50台", routeValue(DOMAIN_PI, "苹果笔记本50台")));
        } else if (DOMAIN_PR.equals(domainType)) {
            templates.add(template("退给云腾科技苹果笔记本5台，从一号仓库退货，原因是破损", routeValue(DOMAIN_PR, "退给云腾科技苹果笔记本5台，从一号仓库退货，原因是破损")));
        } else if (DOMAIN_SOUT.equals(domainType)) {
            templates.add(template("给客户A从一号仓库发苹果笔记本10台", routeValue(DOMAIN_SOUT, "给客户A从一号仓库发苹果笔记本10台")));
        } else if (DOMAIN_SR.equals(domainType)) {
            templates.add(template("客户A退回苹果笔记本2台，退回一号仓库，原因是拒收", routeValue(DOMAIN_SR, "客户A退回苹果笔记本2台，退回一号仓库，原因是拒收")));
        } else if (DOMAIN_SC.equals(domainType)) {
            templates.add(template("盘点一号仓库所有商品，类型全盘", routeValue(DOMAIN_SC, "盘点一号仓库所有商品，类型全盘")));
        } else if (DOMAIN_ST.equals(domainType)) {
            templates.add(template("从一号仓库调30台笔记本到二号仓库", routeValue(DOMAIN_ST, "从一号仓库调30台笔记本到二号仓库")));
        } else {
            templates.add(template("采购入库，仓库A仓，供应商XX，商品A,2,6800", routeValue(DOMAIN_PI, "采购入库，仓库A仓，供应商XX，商品A,2,6800")));
        }
        card.put("templates", templates);
        return card;
    }

    Map<String, Object> startTemplateCard(String domainType) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", owner.cardResultSupport.domainLabel(domainType) + "起始模板");
        List<Map<String, Object>> templates = new ArrayList<>();
        if (DOMAIN_PO.equals(domainType)) {
            templates.add(template("苹果50箱，龙眼10包", routeValue(domainType, "苹果50箱，龙眼10包")));
        } else if (DOMAIN_PI.equals(domainType)) {
            templates.add(template("苹果50箱，龙眼10包", routeValue(domainType, "苹果50箱，龙眼10包")));
            templates.add(template("按采购单 PO202603190001 入库到一号仓库", routeValue(domainType, "按采购单 PO202603190001 入库到一号仓库")));
        } else if (DOMAIN_PR.equals(domainType)) {
            templates.add(template("苹果50箱，龙眼10包", routeValue(domainType, "苹果50箱，龙眼10包")));
            templates.add(template("采购退货，按入库单 PI202603190001 退给云腾科技5台苹果笔记本，每台8000元", routeValue(domainType, "采购退货，按入库单 PI202603190001 退给云腾科技5台苹果笔记本，每台8000元，原因是破损")));
        } else if (DOMAIN_SO.equals(domainType)) {
            templates.add(template("苹果50箱，龙眼10包", routeValue(domainType, "苹果50箱，龙眼10包")));
        } else if (DOMAIN_SOUT.equals(domainType)) {
            templates.add(template("苹果50箱，龙眼10包", routeValue(domainType, "苹果50箱，龙眼10包")));
        } else if (DOMAIN_SR.equals(domainType)) {
            templates.add(template("苹果50箱，龙眼10包", routeValue(domainType, "苹果50箱，龙眼10包")));
        } else if (DOMAIN_SC.equals(domainType)) {
            templates.add(template("盘点一号仓库所有商品，类型全盘", routeValue(domainType, "盘点一号仓库所有商品，类型全盘")));
            templates.add(template("盘点一号仓库苹果电脑、手机，类型抽盘", routeValue(domainType, "盘点一号仓库苹果电脑、手机，类型抽盘")));
        } else if (DOMAIN_ST.equals(domainType)) {
            templates.add(template("从一号仓库调30台笔记本到二号仓库", routeValue(domainType, "从一号仓库调30台笔记本到二号仓库")));
            templates.add(template("把一号仓库的苹果笔记本10台转到二号仓库", routeValue(domainType, "把一号仓库的苹果笔记本10台转到二号仓库")));
        }
        card.put("templates", templates);
        return card;
    }

    String domainStartText(String domainType) {
        if (DOMAIN_PO.equals(domainType)) {
            return "已切换到采购订单流程。请按模板输入。";
        }
        if (DOMAIN_PI.equals(domainType)) {
            return "已切换到采购入库流程。请按模板输入。";
        }
        if (DOMAIN_PR.equals(domainType)) {
            return "已切换到采购退货流程。请按模板输入。";
        }
        if (DOMAIN_SO.equals(domainType)) {
            return "已切换到销售订单流程。请按模板输入。";
        }
        if (DOMAIN_SOUT.equals(domainType)) {
            return "已切换到销售出库流程。请按模板输入。";
        }
        if (DOMAIN_SR.equals(domainType)) {
            return "已切换到销售退货流程。请按模板输入。";
        }
        if (DOMAIN_SC.equals(domainType)) {
            return "已切换到库存盘点流程。请按模板输入：仓库 + 盘点类型 + 商品。";
        }
        if (DOMAIN_ST.equals(domainType)) {
            return "已切换到库存调拨流程。请按模板输入：调出仓库 + 调入仓库 + 商品 + 数量。";
        }
        return "请按模板输入完整单据信息。";
    }

    List<String> domainStartQuickActions(String domainType) {
        if (DOMAIN_PO.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("苹果50箱，龙眼10包");
        }
        if (DOMAIN_PI.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("苹果50箱，龙眼10包", "按采购单 PO202603190001 入库到一号仓库");
        }
        if (DOMAIN_PR.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("苹果50箱，龙眼10包");
        }
        if (DOMAIN_SO.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("苹果50箱，龙眼10包");
        }
        if (DOMAIN_SOUT.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("苹果50箱，龙眼10包");
        }
        if (DOMAIN_SR.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("苹果50箱，龙眼10包");
        }
        if (DOMAIN_SC.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("盘点一号仓库所有商品，类型全盘");
        }
        if (DOMAIN_ST.equals(domainType)) {
            return owner.conversationLifecycleSupport.quickActions("从一号仓库调30台笔记本到二号仓库");
        }
        return owner.conversationLifecycleSupport.quickActions();
    }

    Map<String, Object> actionListCard() {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "ACTION_LIST");
        card.put("actions", owner.listActions());
        return card;
    }

    private Map<String, Object> option(String label, String value) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private Map<String, Object> formField(String key, String label, String placeholder, boolean required) {
        return formField(key, label, placeholder, required, null);
    }

    private Map<String, Object> formField(String key, String label, String placeholder, boolean required, Object defaultValue) {
        Map<String, Object> field = new HashMap<>();
        field.put("key", key);
        field.put("label", label);
        field.put("placeholder", placeholder);
        field.put("required", required);
        field.put("defaultValue", defaultValue);
        return field;
    }

    private Map<String, Object> template(String label, String value) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private String routeValue(String domainType, String text) {
        return "__ROUTE__" + domainType + "::" + text;
    }
}
