package com.ruoyi.wms.service.ai;

import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;

/**
 * WMS对话式助手服务接口
 * <p>
 * 支持多轮对话，引导式完成复杂业务操作
 *
 * @author ruoyi
 */
public interface WmsChatService {

    /**
     * 处理用户对话消息
     *
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponseVO chat(ChatRequestVO request);

    /**
     * 开始新对话
     *
     * @param operationType 操作类型
     * @param userId        用户ID
     * @return 对话响应
     */
    ChatResponseVO startChat(String operationType, Long userId);

    /**
     * 清除会话上下文
     *
     * @param conversationId 会话ID
     */
    void clearContext(String conversationId);

    /**
     * 获取会话上下文
     *
     * @param conversationId 会话ID
     * @return 上下文数据
     */
    ChatResponseVO getContext(String conversationId);
}
