package com.ruoyi.wms.service.ai.conversation;

import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.service.ai.conversation.impl.PurchaseOrderConversationHandler;
import com.ruoyi.wms.service.ai.conversation.impl.SalesOrderConversationHandler;
import com.ruoyi.wms.service.ai.conversation.impl.StockQueryConversationHandler;
import com.ruoyi.wms.service.ai.conversation.impl.GoodsQueryConversationHandler;
import com.ruoyi.wms.service.ai.parser.ParsedOrderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对话管理器
 *
 * @author ruoyi
 */
@Service
public class ConversationManager {

    private static final Logger log = LoggerFactory.getLogger(ConversationManager.class);

    /**
     * 会话存储（生产环境建议使用Redis）
     */
    private final Map<String, ConversationState> sessionStore = new ConcurrentHashMap<>();

    /**
     * 会话超时时间（30分钟）
     */
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    @Autowired
    private PurchaseOrderConversationHandler purchaseOrderHandler;

    @Autowired
    private SalesOrderConversationHandler salesOrderHandler;

    @Autowired
    private StockQueryConversationHandler stockQueryHandler;

    @Autowired
    private GoodsQueryConversationHandler goodsQueryHandler;

    /**
     * 处理对话
     */
    public ChatResponseVO handle(ChatRequestVO request) {
        try {
            // 获取或创建会话
            ConversationState state = getOrCreateSession(request);

            // 已结束会话：自动按原业务类型开启新会话，避免用户继续输入时直接超时
            if (state.getStatus() == ConversationState.SessionStatus.COMPLETED
                    || state.getStatus() == ConversationState.SessionStatus.CANCELLED) {
                String operationType = state.getOperationType() != null ? state.getOperationType() : request.getOperationType();
                ConversationHandler handler = getHandler(operationType);
                if (handler == null) {
                    return handleUnknownOperation(request);
                }

                String newConversationId = UUID.randomUUID().toString();
                ConversationState newState = ConversationState.create(newConversationId, request.getUserId(), operationType);
                sessionStore.put(newConversationId, newState);

                ChatResponseVO startResponse = handler.start(newState);
                String reply = "当前会话已结束，我已为您开启新的同类型会话。\n\n" + startResponse.getReply();
                startResponse.setReply(reply);
                return startResponse;
            }

            // 检查会话是否超时
            if (isSessionExpired(state)) {
                sessionStore.remove(state.getConversationId());
                return handleExpiredSession(request);
            }

            // 获取对话处理器
            ConversationHandler handler = getHandler(request.getOperationType());
            if (handler == null) {
                return handleUnknownOperation(request);
            }

            // 处理特殊指令
            if (isSpecialCommand(state, request)) {
                return handleSpecialCommand(state, handler, request);
            }

            // 处理对话
            ChatResponseVO response = handler.handle(state, request);

            // 更新会话状态
            updateSession(state);

            return response;

        } catch (Exception e) {
            log.error("处理对话失败", e);
            return ChatResponseVO.builder()
                    .reply("抱歉，我遇到了一些问题：" + e.getMessage())
                    .build();
        }
    }

    /**
     * 开始新对话
     */
    public ChatResponseVO start(String operationType, Long userId) {
        ConversationHandler handler = getHandler(operationType);
        if (handler == null) {
            return ChatResponseVO.builder()
                    .reply("抱歉，我不支持这个操作。")
                    .build();
        }

        // 创建新会话
        String conversationId = UUID.randomUUID().toString();
        ConversationState state = ConversationState.create(conversationId, userId, operationType);
        sessionStore.put(conversationId, state);

        log.info("创建新会话: conversationId={}, operationType={}, userId={}",
                conversationId, operationType, userId);

        // 返回初始响应
        return handler.start(state);
    }

    /**
     * 清除会话
     */
    public void clearSession(String conversationId) {
        if (conversationId != null) {
            sessionStore.remove(conversationId);
            log.info("清除会话: conversationId={}", conversationId);
        }
    }

    /**
     * 获取会话状态
     */
    public ConversationState getSession(String conversationId) {
        return sessionStore.get(conversationId);
    }

    /**
     * 获取或创建会话
     */
    private ConversationState getOrCreateSession(ChatRequestVO request) {
        String conversationId = request.getConversationId();

        if (conversationId != null && !conversationId.isEmpty()) {
            ConversationState existing = sessionStore.get(conversationId);
            if (existing != null) {
                return existing;
            }
        }

        // 创建新会话
        String newConversationId = conversationId != null ? conversationId : UUID.randomUUID().toString();
        ConversationState state = ConversationState.create(
                newConversationId,
                request.getUserId(),
                request.getOperationType()
        );
        sessionStore.put(newConversationId, state);
        return state;
    }

    /**
     * 获取对话处理器
     */
    private ConversationHandler getHandler(String operationType) {
        if (operationType == null) {
            return null;
        }

        switch (operationType) {
            case "create_purchase_in":
            case "create_purchase_order":
                return purchaseOrderHandler;
            case "create_sales_out":
            case "create_sales_order":
                return salesOrderHandler;
            case "query_stock":
                return stockQueryHandler;
            case "query_goods":
                return goodsQueryHandler;
            default:
                return null;
        }
    }

    /**
     * 判断是否是特殊指令（取消操作）
     * 注意：确认和完成操作由各处理器自行处理，不在此拦截
     */
    private boolean isSpecialCommand(ConversationState state, ChatRequestVO request) {
        String message = request.getMessage();

        if (message == null) {
            return false;
        }

        // 在关键步骤中，"取消"优先交给业务处理器，避免误结束整段会话
        if (state != null) {
            String step = state.getCurrentStep();
            if ("goods_create_confirm".equals(step)
                    || "goods_create_page".equals(step)
                    || "confirm_order".equals(step)) {
                return false;
            }
        }

        // 只拦截取消操作，确认操作由各处理器根据当前步骤自行处理
        return "取消".equals(message) || "cancel".equalsIgnoreCase(message);
    }

    /**
     * 处理特殊指令
     */
    private ChatResponseVO handleSpecialCommand(ConversationState state, ConversationHandler handler, ChatRequestVO request) {
        String message = request.getMessage();

        if ("取消".equals(message) || "cancel".equalsIgnoreCase(message)) {
            return handler.cancel(state);
        }

        // 其他情况正常处理
        return handler.handle(state, request);
    }

    /**
     * 处理未知操作
     */
    private ChatResponseVO handleUnknownOperation(ChatRequestVO request) {
        return ChatResponseVO.builder()
                .reply("抱歉，我不太理解您的需求。请告诉我您想要做什么？\n\n" +
                        "例如：\n" +
                    "- \"创建采购入库单\"\n" +
                    "- \"创建销售出库单\"")
                .build();
    }

    /**
     * 处理过期会话
     */
    private ChatResponseVO handleExpiredSession(ChatRequestVO request) {
        return ChatResponseVO.builder()
                .reply("会话已超时，请重新开始。")
                .build();
    }

    /**
     * 检查会话是否过期
     */
    private boolean isSessionExpired(ConversationState state) {
        if (state == null) return true;
        long elapsed = System.currentTimeMillis() - state.getLastUpdateTime();
        return elapsed > SESSION_TIMEOUT;
    }

    /**
     * 更新会话状态
     */
    private void updateSession(ConversationState state) {
        state.setLastUpdateTime(System.currentTimeMillis());
        sessionStore.put(state.getConversationId(), state);
    }

    /**
     * 清理过期会话
     */
    public void cleanExpiredSessions() {
        Iterator<Map.Entry<String, ConversationState>> it = sessionStore.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ConversationState> entry = it.next();
            if (isSessionExpired(entry.getValue())) {
                it.remove();
                log.info("清理过期会话: conversationId={}", entry.getKey());
            }
        }
    }

    /**
     * 处理完整订单（智能解析后直接生成预览）
     */
    public ChatResponseVO handleCompleteOrder(ChatRequestVO request, ParsedOrderResult parsedResult) {
        String operationType = parsedResult.getOperationType();

        // 根据操作类型获取对应的处理器
        ConversationHandler handler = getHandler(operationType);
        if (handler == null) {
            return handleUnknownOperation(request);
        }

        // 创建或获取会话状态
        ConversationState state = getOrCreateSession(request);
        state.setOperationType(operationType);

        // 调用处理器的完整订单处理方法
        if (handler instanceof PurchaseOrderConversationHandler) {
            return ((PurchaseOrderConversationHandler) handler).handleCompleteOrder(state, parsedResult);
        } else if (handler instanceof SalesOrderConversationHandler) {
            return ((SalesOrderConversationHandler) handler).handleCompleteOrder(state, parsedResult);
        }

        // 如果处理器不支持完整订单处理，走普通流程
        return handler.handle(state, request);
    }
}
