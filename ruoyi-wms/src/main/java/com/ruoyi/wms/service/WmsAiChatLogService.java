package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsAiChatLog;

/**
 * AI会话日志表 Service接口
 *
 * @author ruoyi
 */
public interface WmsAiChatLogService extends IService<WmsAiChatLog> {

    /**
     * 记录会话日志
     *
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param operationType 操作类型
     * @param inputType 输入类型(voice/text)
     * @param inputContent 输入内容
     * @param voiceFilePath 语音文件路径
     * @param responseContent 响应内容
     * @param resultStatus 处理状态(success/failed)
     * @param errorMessage 错误信息
     * @param processTime 处理耗时(毫秒)
     */
    void log(String conversationId, Long userId, String operationType, String inputType,
             String inputContent, String voiceFilePath, String responseContent,
             String resultStatus, String errorMessage, Integer processTime);

    /**
     * 异步记录会话日志（不阻塞主流程）
     */
    void logAsync(String conversationId, Long userId, String operationType, String inputType,
                  String inputContent, String voiceFilePath, String responseContent,
                  String resultStatus, String errorMessage, Integer processTime);
}
