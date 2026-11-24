package com.ruoyi.wms.service.ai.conversation;

import java.util.HashMap;
import java.util.Map;

/**
 * 对话状态
 *
 * @author ruoyi
 */
public class ConversationState {

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前操作类型
     * create_purchase_order, create_sales_order, query_stock 等
     */
    private String operationType;

    /**
     * 当前步骤
     */
    private String currentStep;

    /**
     * 已收集的数据
     */
    private Map<String, Object> collectedData = new HashMap<>();

    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;

    /**
     * 会话状态
     */
    private SessionStatus status = SessionStatus.ACTIVE;

    public ConversationState() {
    }

    public ConversationState(String conversationId, Long userId, String operationType, String currentStep,
                             Map<String, Object> collectedData, Long lastUpdateTime, SessionStatus status) {
        this.conversationId = conversationId;
        this.userId = userId;
        this.operationType = operationType;
        this.currentStep = currentStep;
        this.collectedData = collectedData == null ? new HashMap<>() : collectedData;
        this.lastUpdateTime = lastUpdateTime;
        this.status = status == null ? SessionStatus.ACTIVE : status;
    }

    /**
     * 会话状态枚举
     */
    public enum SessionStatus {
        /**
         * 活跃
         */
        ACTIVE,
        /**
         * 等待确认
         */
        WAITING_CONFIRMATION,
        /**
         * 已完成
         */
        COMPLETED,
        /**
         * 已取消
         */
        CANCELLED
    }

    /**
     * 获取数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getData(String key, Class<T> clazz) {
        Object value = collectedData.get(key);
        if (value == null) return null;
        return (T) value;
    }

    /**
     * 获取数据（不指定类型）
     */
    public Object getData(String key) {
        return collectedData.get(key);
    }

    /**
     * 获取字符串数据（带默认值）
     */
    @SuppressWarnings("unchecked")
    public String getData(String key, String defaultValue) {
        Object value = collectedData.get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }

    /**
     * 设置数据
     */
    public void setData(String key, Object value) {
        if (collectedData == null) {
            collectedData = new HashMap<>();
        }
        collectedData.put(key, value);
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * 是否有某个数据
     */
    public boolean hasData(String key) {
        return collectedData != null && collectedData.containsKey(key);
    }

    /**
     * 移除数据
     */
    public void removeData(String key) {
        if (collectedData != null) {
            collectedData.remove(key);
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public Map<String, Object> getCollectedData() {
        return collectedData;
    }

    public void setCollectedData(Map<String, Object> collectedData) {
        this.collectedData = collectedData == null ? new HashMap<>() : collectedData;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 创建新会话
     */
    public static ConversationState create(String conversationId, Long userId, String operationType) {
        return ConversationState.builder()
                .conversationId(conversationId)
                .userId(userId)
                .operationType(operationType)
                .currentStep("start")
                .collectedData(new HashMap<>())
                .lastUpdateTime(System.currentTimeMillis())
                .status(SessionStatus.ACTIVE)
                .build();
    }

    public static class Builder {
        private String conversationId;
        private Long userId;
        private String operationType;
        private String currentStep;
        private Map<String, Object> collectedData = new HashMap<>();
        private Long lastUpdateTime;
        private SessionStatus status = SessionStatus.ACTIVE;

        public Builder conversationId(String conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder currentStep(String currentStep) {
            this.currentStep = currentStep;
            return this;
        }

        public Builder collectedData(Map<String, Object> collectedData) {
            this.collectedData = collectedData == null ? new HashMap<>() : collectedData;
            return this;
        }

        public Builder lastUpdateTime(Long lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
            return this;
        }

        public Builder status(SessionStatus status) {
            this.status = status == null ? SessionStatus.ACTIVE : status;
            return this;
        }

        public ConversationState build() {
            return new ConversationState(conversationId, userId, operationType, currentStep, collectedData, lastUpdateTime, status);
        }
    }
}
