package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.wms.domain.WmsAiChatLog;
import com.ruoyi.wms.mapper.WmsAiChatLogMapper;
import com.ruoyi.wms.service.WmsAiChatLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * AI会话日志表 Service实现
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class WmsAiChatLogServiceImpl extends ServiceImpl<WmsAiChatLogMapper, WmsAiChatLog> implements WmsAiChatLogService {

    @Override
    public void log(String conversationId, Long userId, String operationType, String inputType,
                    String inputContent, String voiceFilePath, String responseContent,
                    String resultStatus, String errorMessage, Integer processTime) {
        try {
            WmsAiChatLog chatLog = new WmsAiChatLog();
            chatLog.setConversationId(conversationId);
            chatLog.setUserId(userId);
            chatLog.setOperationType(truncate(operationType, 32));
            chatLog.setInputType(truncate(inputType, 16));
            chatLog.setInputContent(truncate(inputContent, 2000));
            chatLog.setVoiceFilePath(voiceFilePath);
            chatLog.setResponseContent(truncate(responseContent, 4000));
            chatLog.setResultStatus(truncate(resultStatus, 16));
            chatLog.setErrorMessage(truncate(errorMessage, 1000));
            chatLog.setProcessTime(processTime);
            chatLog.setCreateTime(LocalDateTime.now());

            save(chatLog);
        } catch (Exception e) {
            log.error("保存AI会话日志失败", e);
        }
    }

    @Override
    @Async
    public void logAsync(String conversationId, Long userId, String operationType, String inputType,
                         String inputContent, String voiceFilePath, String responseContent,
                         String resultStatus, String errorMessage, Integer processTime) {
        log(conversationId, userId, operationType, inputType, inputContent, voiceFilePath,
            responseContent, resultStatus, errorMessage, processTime);
    }

    /**
     * 截断字符串，避免超长内容
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
}
