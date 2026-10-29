package com.ruoyi.wms.domain.vo.ai.chat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * 对话响应VO
 *
 * @author ruoyi
 */
@ApiModel("对话响应")
public class ChatResponseVO {

    @ApiModelProperty(value = "AI回复消息")
    private String reply;

    @ApiModelProperty(value = "会话ID")
    private String conversationId;

    @ApiModelProperty(value = "当前操作类型")
    private String operationType;

    @ApiModelProperty(value = "是否需要继续收集信息")
    private Boolean needMoreInfo = false;

    @ApiModelProperty(value = "已收集的业务数据")
    private Map<String, Object> collectedData;

    @ApiModelProperty(value = "待收集的字段列表")
    private List<String> pendingFields;

    @ApiModelProperty(value = "Function Calling 调用建议")
    private FunctionCall suggestedAction;

    @ApiModelProperty(value = "是否完成业务操作")
    private Boolean isCompleted = false;

    @ApiModelProperty(value = "快捷回复选项")
    private List<QuickReply> quickReplies;

    @ApiModelProperty(value = "示例提示词列表（用于引导用户输入）")
    private List<String> examplePrompts;

    @ApiModelProperty(value = "需要创建的数据提示（用于引导用户去创建页面）")
    private CreatePrompt createPrompt;

    @ApiModelProperty(value = "单选列表（用于订单/仓库等选择场景）")
    private List<SelectionItem> selectionList;

    public ChatResponseVO() {
    }

    public ChatResponseVO(String reply, String conversationId, String operationType, Boolean needMoreInfo,
                          Map<String, Object> collectedData, List<String> pendingFields,
                          FunctionCall suggestedAction, Boolean isCompleted,
                          List<QuickReply> quickReplies, List<String> examplePrompts,
                          CreatePrompt createPrompt, List<SelectionItem> selectionList) {
        this.reply = reply;
        this.conversationId = conversationId;
        this.operationType = operationType;
        this.needMoreInfo = needMoreInfo == null ? false : needMoreInfo;
        this.collectedData = collectedData;
        this.pendingFields = pendingFields;
        this.suggestedAction = suggestedAction;
        this.isCompleted = isCompleted == null ? false : isCompleted;
        this.quickReplies = quickReplies;
        this.examplePrompts = examplePrompts;
        this.createPrompt = createPrompt;
        this.selectionList = selectionList;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public Boolean getNeedMoreInfo() { return needMoreInfo; }
    public void setNeedMoreInfo(Boolean needMoreInfo) { this.needMoreInfo = needMoreInfo; }
    public Map<String, Object> getCollectedData() { return collectedData; }
    public void setCollectedData(Map<String, Object> collectedData) { this.collectedData = collectedData; }
    public List<String> getPendingFields() { return pendingFields; }
    public void setPendingFields(List<String> pendingFields) { this.pendingFields = pendingFields; }
    public FunctionCall getSuggestedAction() { return suggestedAction; }
    public void setSuggestedAction(FunctionCall suggestedAction) { this.suggestedAction = suggestedAction; }
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean completed) { isCompleted = completed; }
    public List<QuickReply> getQuickReplies() { return quickReplies; }
    public void setQuickReplies(List<QuickReply> quickReplies) { this.quickReplies = quickReplies; }
    public List<String> getExamplePrompts() { return examplePrompts; }
    public void setExamplePrompts(List<String> examplePrompts) { this.examplePrompts = examplePrompts; }
    public CreatePrompt getCreatePrompt() { return createPrompt; }
    public void setCreatePrompt(CreatePrompt createPrompt) { this.createPrompt = createPrompt; }
    public List<SelectionItem> getSelectionList() { return selectionList; }
    public void setSelectionList(List<SelectionItem> selectionList) { this.selectionList = selectionList; }

    public static class Builder {
        private String reply;
        private String conversationId;
        private String operationType;
        private Boolean needMoreInfo = false;
        private Map<String, Object> collectedData;
        private List<String> pendingFields;
        private FunctionCall suggestedAction;
        private Boolean isCompleted = false;
        private List<QuickReply> quickReplies;
        private List<String> examplePrompts;
        private CreatePrompt createPrompt;
        private List<SelectionItem> selectionList;

        public Builder reply(String reply) { this.reply = reply; return this; }
        public Builder conversationId(String conversationId) { this.conversationId = conversationId; return this; }
        public Builder operationType(String operationType) { this.operationType = operationType; return this; }
        public Builder needMoreInfo(Boolean needMoreInfo) { this.needMoreInfo = needMoreInfo; return this; }
        public Builder collectedData(Map<String, Object> collectedData) { this.collectedData = collectedData; return this; }
        public Builder pendingFields(List<String> pendingFields) { this.pendingFields = pendingFields; return this; }
        public Builder suggestedAction(FunctionCall suggestedAction) { this.suggestedAction = suggestedAction; return this; }
        public Builder isCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; return this; }
        public Builder quickReplies(List<QuickReply> quickReplies) { this.quickReplies = quickReplies; return this; }
        public Builder examplePrompts(List<String> examplePrompts) { this.examplePrompts = examplePrompts; return this; }
        public Builder createPrompt(CreatePrompt createPrompt) { this.createPrompt = createPrompt; return this; }
        public Builder selectionList(List<SelectionItem> selectionList) { this.selectionList = selectionList; return this; }

        public ChatResponseVO build() {
            return new ChatResponseVO(reply, conversationId, operationType, needMoreInfo, collectedData,
                    pendingFields, suggestedAction, isCompleted, quickReplies, examplePrompts, createPrompt, selectionList);
        }
    }

    /**
     * 快捷回复选项
     */
    public static class QuickReply {
        private String label;      // 显示文本
        private String value;      // 实际发送的值
        private String type;       // 类型：primary/default/danger

        public QuickReply() {
        }

        public QuickReply(String label, String value, String type) {
            this.label = label;
            this.value = value;
            this.type = type;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    /**
     * Function Calling 调用建议
     */
    public static class FunctionCall {
        private String name;           // 函数名称
        private Map<String, Object> arguments;  // 函数参数
        private String description;    // 操作描述

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, Object> getArguments() { return arguments; }
        public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 需要创建的数据提示
     */
    public static class CreatePrompt {
        private String type;        // 类型：supplier/goods/customer
        private String name;        // 名称（用于预填充）
        private String prompt;      // 提示信息

        public CreatePrompt() {
        }

        public CreatePrompt(String type, String name, String prompt) {
            this.type = type;
            this.name = name;
            this.prompt = prompt;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

    /**
     * Fluent setter for isCompleted
     */
    public ChatResponseVO isCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
        return this;
    }

    /**
     * 添加是/否快捷回复
     */
    public ChatResponseVO withYesNoReplies() {
        this.quickReplies = List.of(
            new QuickReply("是", "是", "primary"),
            new QuickReply("否", "否", "default")
        );
        return this;
    }

    /**
     * 设置快捷回复选项
     */
    public ChatResponseVO withQuickReplies(List<QuickReply> quickReplies) {
        this.quickReplies = quickReplies;
        return this;
    }

    /**
     * 设置 Function Calling 调用建议
     */
    public ChatResponseVO suggestedAction(FunctionCall suggestedAction) {
        this.suggestedAction = suggestedAction;
        return this;
    }

    /**
     * 单选列表选项
     */
    public static class SelectionItem {
        private String label;
        private String description;
        private String value;

        public SelectionItem() {}

        public SelectionItem(String label, String description, String value) {
            this.label = label;
            this.description = description;
            this.value = value;
        }

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
