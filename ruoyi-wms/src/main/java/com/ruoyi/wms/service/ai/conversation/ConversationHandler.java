package com.ruoyi.wms.service.ai.conversation;

import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;

/**
 * 对话处理器接口
 *
 * @author ruoyi
 */
public interface ConversationHandler {

    /**
     * 支持的操作类型
     */
    String getOperationType();

    /**
     * 处理对话
     *
     * @param state   对话状态
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponseVO handle(ConversationState state, ChatRequestVO request);

    /**
     * 开始新对话（初始化响应）
     *
     * @param state 对话状态
     * @return 初始响应
     */
    ChatResponseVO start(ConversationState state);

    /**
     * 确认操作
     *
     * @param state 对话状态
     * @return 确认响应
     */
    ChatResponseVO confirm(ConversationState state);

    /**
     * 取消操作
     *
     * @param state 对话状态
     * @return 取消响应
     */
    ChatResponseVO cancel(ConversationState state);

    /**
     * 获取当前步骤的提示信息
     *
     * @param state 对话状态
     * @return 提示信息
     */
    String getStepPrompt(ConversationState state);
}
