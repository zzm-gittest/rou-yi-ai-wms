package com.ruoyi.wms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI服务配置属性
 *
 * @author ruoyi
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai.service")
public class AiProperties {

    /**
     * AI服务是否启用
     */
    private Boolean enabled = true;

    /**
     * 通义千问配置
     */
    private QwenConfig qwen = new QwenConfig();

    /**
     * 多模态视觉模型配置（截图识别）
     */
    private VisionConfig vision = new VisionConfig();

    /**
     * 语音识别模型配置（ASR）
     */
    private AsrConfig asr = new AsrConfig();

    /**
     * 通义千问配置
     */
    public static class QwenConfig {
        /**
         * API Key
         */
        private String apiKey;

        /**
         * API地址（使用兼容OpenAI接口）
         */
        private String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        /**
         * 模型名称
         * qwen-turbo: 快速响应
         * qwen-plus: 平衡性能
         * qwen-max: 最强能力
         */
        private String model = "qwen-turbo";

        /**
         * 最大Token数
         */
        private Integer maxTokens = 2000;

        /**
         * 温度参数（0-1，越高越随机）
         */
        private Double temperature = 0.7;

        /**
         * 思考模式开关（qwen3.x 混合推理模型生效）。
         * null=不传该参数（跟随服务端默认）；false=显式关闭思考，显著降低首字延迟与 token 消耗；
         * true=开启深度思考（慢，适合复杂推理场景）。
         * 录单/解析类任务建议 false。
         */
        private Boolean thinkingEnabled;

        public Boolean getThinkingEnabled() {
            return thinkingEnabled;
        }

        public void setThinkingEnabled(Boolean thinkingEnabled) {
            this.thinkingEnabled = thinkingEnabled;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public QwenConfig getQwen() {
        return qwen;
    }

    public void setQwen(QwenConfig qwen) {
        this.qwen = qwen;
    }

    public VisionConfig getVision() {
        return vision;
    }

    public void setVision(VisionConfig vision) {
        this.vision = vision;
    }

    public AsrConfig getAsr() {
        return asr;
    }

    public void setAsr(AsrConfig asr) {
        this.asr = asr;
    }

    /**
     * 多模态视觉模型配置（截图识别开单）
     * 使用DashScope OpenAI兼容接口，支持 qwen-vl-plus / qwen-vl-max
     */
    public static class VisionConfig {
        /**
         * API Key（默认复用qwen的apiKey）
         */
        private String apiKey;

        /**
         * API地址
         */
        private String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        /**
         * 视觉模型名称
         */
        private String model = "qwen-vl-plus";

        /**
         * 最大Token数
         */
        private Integer maxTokens = 2000;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }
    }

    /**
     * 语音识别模型配置（ASR）
     * 使用 DashScope SDK，支持 paraformer-realtime-v2 / paraformer-v2 等
     */
    public static class AsrConfig {
        /**
         * 语音模型名称
         */
        private String model = "paraformer-realtime-v2";

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
