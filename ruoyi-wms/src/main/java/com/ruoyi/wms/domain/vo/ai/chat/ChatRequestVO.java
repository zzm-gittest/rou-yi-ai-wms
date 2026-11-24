package com.ruoyi.wms.domain.vo.ai.chat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * 对话请求VO
 *
 * @author ruoyi
 */
@ApiModel("对话请求")
public class ChatRequestVO {

    @ApiModelProperty(value = "用户消息", required = true)
    private String message;

    @ApiModelProperty(value = "会话ID，用于多轮对话上下文维护")
    private String conversationId;

    @ApiModelProperty(value = "当前操作类型（如：create_purchase_order, create_sales_order等）")
    private String operationType;

    @ApiModelProperty(value = "已收集的业务数据（用于多轮对话场景）")
    private Map<String, Object> collectedData;

    @ApiModelProperty(value = "对话历史消息")
    private List<ChatMessage> history;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "租户ID")
    private Long tenantId;

    @ApiModelProperty(value = "输入类型(voice/text)")
    private String inputType;

    @ApiModelProperty(value = "语音文件路径")
    private String voiceFilePath;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public Map<String, Object> getCollectedData() { return collectedData; }
    public void setCollectedData(Map<String, Object> collectedData) { this.collectedData = collectedData; }
    public List<ChatMessage> getHistory() { return history; }
    public void setHistory(List<ChatMessage> history) { this.history = history; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }
    public String getVoiceFilePath() { return voiceFilePath; }
    public void setVoiceFilePath(String voiceFilePath) { this.voiceFilePath = voiceFilePath; }

    /**
     * 聊天消息
     */
    public static class ChatMessage {
        private String role;    // user/assistant/system
        private String content;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
