package com.ruoyi.wms.service.ai.pc;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * PC端多单据AI助手（V2，独立于旧链路）
 */
public interface PcMultiBillChatService {

    Map<String, Object> start(Long userId);

    Map<String, Object> chat(Long userId, String conversationId, String message);

    /**
     * 截图识别开单：调用视觉模型识别图片，将结果交给现有聊天流处理。
     */
    Map<String, Object> imageChat(Long userId, String conversationId, MultipartFile imageFile);

    /**
     * 判断本轮消息是否会调用第三方LLM，用于配额预检。
     */
    boolean willUseLlm(String conversationId, String message);

    void clear(String conversationId);

    List<Map<String, Object>> listActions();
}

