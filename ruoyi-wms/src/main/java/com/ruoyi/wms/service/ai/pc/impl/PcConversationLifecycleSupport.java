package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

class PcConversationLifecycleSupport {

    private final PcMultiBillChatServiceImpl owner;

    PcConversationLifecycleSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> start(Long userId) {
        long startTime = System.currentTimeMillis();
        PcState state = new PcState();
        state.setConversationId(UUID.randomUUID().toString().replace("-", ""));
        state.setUserId(userId);
        state.setStep("IDLE");
        saveState(state);

        Map<String, Object> card = owner.cardBuilderSupport.actionListCard();
        card.put("tip", "支持采购入库、采购退货、销售订单、销售出库、销售退货、库存盘点、库存调拨，以及已跑通的采购订单。还支持经营分析，查看销售采购数据、库存预警和待办事项。\n");

        Map<String, Object> reply = buildReply(
                state,
                "我是单据创建助手。你可以直接输入一句业务话术，例如：\n"
                        + "- 向云腾科技采购苹果笔记本50台，单价8000元\n"
                        + "- 卖给客户A苹果笔记本10台，单价9000元\n"
                        + "- 从一号仓库调30台笔记本到二号仓库\n"
                        + "- 盘点一号仓库所有商品，类型抽盘\n"
                        + "我会按对应单据自动识别、补问、候选确认、预览并创建。",
                card,
                quickActions(
                        "向云腾科技有限公司采购苹果笔记本50台，单价是8000元",
                        "卖给客户A苹果笔记本10台，单价9000元",
                        "从一号仓库调30台笔记本到二号仓库",
                        "盘点一号仓库所有商品，类型抽盘")
        );
        owner.auditLogSupport.logConversation(state.getConversationId(), userId, "PC_MULTI_BILL_ASSISTANT_START", "text", "__START__", reply, "success", null, startTime, state);
        return reply;
    }

    Map<String, Object> chat(Long userId, String conversationId, String message) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> response = null;
        String resultStatus = "success";
        String errorMessage = null;
        PcState state = null;
        if (StringUtils.isBlank(message)) {
            throw new ServiceException("消息不能为空");
        }
        try {
            state = loadOrCreate(conversationId, userId);
            String input = message.trim();
            response = owner.chatDispatchSupport.dispatch(state, input);
            return response;
        } catch (Exception e) {
            resultStatus = "failed";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            String actualConversationId = state != null ? state.getConversationId() : conversationId;
            owner.auditLogSupport.logConversation(actualConversationId, userId, owner.auditLogSupport.buildLogOperationType(state), "text", message, response, resultStatus, errorMessage, startTime, state);
        }
    }

    void clear(String conversationId) {
        if (StringUtils.isNotBlank(conversationId)) {
            owner.redisCache.deleteObject(owner.KEY_PREFIX + conversationId);
        }
    }

    PcState peekState(String conversationId) {
        if (StringUtils.isBlank(conversationId)) {
            return null;
        }
        return owner.redisCache.getCacheObject(owner.KEY_PREFIX + conversationId);
    }

    List<Map<String, Object>> listActions() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(action("PURCHASE_ORDER", "采购订单", "支持供应商、商品、数量、单价完整建单"));
        list.add(action("PURCHASE_IN", "采购入库", "支持按商品直接入库或按采购单入库"));
        list.add(action("PURCHASE_RETURN", "采购退货", "支持按商品或按入库单退货，自动校验可退数量"));
        list.add(action("SALES_ORDER", "销售订单", "支持现有商品多选、客户候选和聊天内新增客户"));
        list.add(action("SALES_OUT", "销售出库", "支持按商品出库或按销售单出库"));
        list.add(action("SALES_RETURN", "销售退货", "支持按商品或按出库单退货"));
        list.add(action("STOCK_CHECK", "库存盘点", "支持盘点类型、商品多选和全仓盘点"));
        list.add(action("STOCK_TRANSFER", "库存调拨", "支持调出仓、调入仓和多商品调拨"));
        list.add(action("QUERY_BILL_CENTER", "统一查询单据", "支持跨8类单据统一查询、筛选与回看"));
        list.add(action("QUERY_PO", "查看刚创建的订单", "回看最近一次通过助手创建的单据详情"));
        list.add(action("BUSINESS_ANALYSIS", "经营分析", "查看今日经营简报、销售采购分析、库存预警、待办事项等"));
        return list;
    }

    Map<String, Object> action(String code, String name, String desc) {
        Map<String, Object> action = new HashMap<>();
        action.put("code", code);
        action.put("name", name);
        action.put("desc", desc);
        if ("QUERY_PO".equals(code) || "QUERY_BILL_CENTER".equals(code) || "BUSINESS_ANALYSIS".equals(code)) {
            action.put("value", "__ACTION__:" + code);
        } else {
            action.put("value", "__ACTION__:" + owner.actionByDomain(owner.domainByAction(code)));
        }
        return action;
    }

    Map<String, Object> buildReply(PcState state, String text, Map<String, Object> card, List<String> quickActions) {
        Map<String, Object> reply = new HashMap<>();
        reply.put("conversationId", state.getConversationId());
        reply.put("text", text);
        reply.put("card", card);
        reply.put("quickActions", quickActions);
        Map<String, Object> context = new HashMap<>();
        context.put("action", state.getAction());
        context.put("step", state.getStep());
        context.put("domainType", state.getDomainType());
        context.put("supplier", state.getSupplierName());
        context.put("warehouse", state.getWarehouseName());
        context.put("lastOrderNo", state.getLastCreatedOrderNo());
        context.put("lastCreatedDomainType", state.getLastCreatedDomainType());
        reply.put("context", context);
        return reply;
    }

    List<String> quickActions(String... values) {
        List<String> list = new ArrayList<>();
        for (String value : values) {
            list.add(value);
        }
        return list;
    }

    PcState loadOrCreate(String conversationId, Long userId) {
        if (StringUtils.isBlank(conversationId)) {
            PcState state = new PcState();
            state.setConversationId(UUID.randomUUID().toString().replace("-", ""));
            state.setUserId(userId);
            state.setStep("IDLE");
            saveState(state);
            return state;
        }
        PcState state = owner.redisCache.getCacheObject(owner.KEY_PREFIX + conversationId);
        if (state == null) {
            state = new PcState();
            state.setConversationId(conversationId);
            state.setUserId(userId);
            state.setStep("IDLE");
            saveState(state);
        }
        return state;
    }

    void saveState(PcState state) {
        owner.redisCache.setCacheObject(owner.KEY_PREFIX + state.getConversationId(), state, owner.TTL_MINUTES, TimeUnit.MINUTES);
    }
}

