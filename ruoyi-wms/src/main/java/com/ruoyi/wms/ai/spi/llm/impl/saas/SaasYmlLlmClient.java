package com.ruoyi.wms.ai.spi.llm.impl.saas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.wms.ai.spi.llm.LlmChatRequest;
import com.ruoyi.wms.ai.spi.llm.LlmChatResponse;
import com.ruoyi.wms.ai.spi.llm.LlmClient;
import com.ruoyi.wms.ai.spi.llm.LlmVisionRequest;
import com.ruoyi.wms.config.AiProperties;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * SaaS 模式下的 {@link LlmClient} 实现：直接读取 {@code ai.service.qwen.*} / {@code ai.service.vision.*}
 * yml 配置，调用 DashScope OpenAI 兼容接口。
 *
 * <p>这是历史 {@code QwenLlmService} + {@code MultimodalLlmService} 的行为抽取版，
 * 保持完全相同的请求体与响应解析逻辑，确保现网兼容。</p>
 *
 * @author ruoyi
 */
@Component
public class SaasYmlLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(SaasYmlLlmClient.class);

    public static final String MODE = "saas";

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private com.ruoyi.wms.service.ai.TenantAiConfigResolver tenantAiConfigResolver;

    @Autowired
    private ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build();

    @Override
    public LlmChatResponse chat(LlmChatRequest request) {
        long start = System.currentTimeMillis();
        AiProperties.QwenConfig qwen = aiProperties.getQwen();
        String apiKey = tenantAiConfigResolver.resolveApiKey();
        String apiUrl = resolveChatCompletionsUrl(tenantAiConfigResolver.resolveApiUrl());
        String model = tenantAiConfigResolver.resolveModel();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("[SaasYmlLlmClient] qwen.apiKey 未配置（租户及全局均未配置），返回 null content");
            return new LlmChatResponse()
                .setContent(null)
                .setModelName(model)
                .setRequestId(request.getRequestId())
                .setCostMs((int) (System.currentTimeMillis() - start));
        }

        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : qwen.getMaxTokens();
        Double temperature = request.getTemperature() != null ? request.getTemperature() : qwen.getTemperature();

        String body = buildChatRequestBody(model, request.resolvedMessages(), maxTokens, temperature,
            Boolean.TRUE.equals(request.getJsonMode()), qwen.getThinkingEnabled());

        Request httpRequest = new Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body, MediaType.parse("application/json; charset=utf-8")))
            .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            if (!response.isSuccessful() || responseBody == null) {
                log.error("[SaasYmlLlmClient] chat 调用失败: code={}, body={}", response.code(), responseBody);
                return new LlmChatResponse()
                    .setContent(null)
                    .setModelName(model)
                    .setRequestId(request.getRequestId())
                    .setCostMs((int) (System.currentTimeMillis() - start));
            }
            return parseChatCompletionResponse(responseBody, model, request.getRequestId(),
                (int) (System.currentTimeMillis() - start));
        } catch (Exception e) {
            log.error("[SaasYmlLlmClient] chat 异常", e);
            return new LlmChatResponse()
                .setContent(null)
                .setModelName(model)
                .setRequestId(request.getRequestId())
                .setCostMs((int) (System.currentTimeMillis() - start));
        }
    }

    @Override
    public LlmChatResponse vision(LlmVisionRequest request) {
        long start = System.currentTimeMillis();
        AiProperties.VisionConfig vision = aiProperties.getVision();
        String apiKey = vision.getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = tenantAiConfigResolver.resolveApiKey();
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("视觉模型 API Key 未配置");
        }
        String apiUrl = vision.getApiUrl();
        String model = tenantAiConfigResolver.resolveVisionModel();
        Integer maxTokens = request.getMaxTokens() != null ? request.getMaxTokens() : vision.getMaxTokens();

        if (request.getImageBase64() == null || request.getImageBase64().isBlank()) {
            throw new RuntimeException("SaaS 模式视觉调用仅支持 imageBase64 输入");
        }
        String mimeType = request.getImageMimeType() == null ? "image/jpeg" : request.getImageMimeType();

        String body = buildVisionRequestBody(model, maxTokens, request.getSystemPrompt(),
            request.getPrompt(), mimeType, request.getImageBase64());

        Request httpRequest = new Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body, MediaType.parse("application/json; charset=utf-8")))
            .build();

        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            if (!response.isSuccessful() || responseBody == null) {
                log.error("[SaasYmlLlmClient] vision 调用失败: model={}, code={}, body={}", model, response.code(), responseBody);
                throw new RuntimeException("截图识别服务暂不可用: " + extractUpstreamError(responseBody, response.code())
                    + "（模型: " + model + "）");
            }
            return parseChatCompletionResponse(responseBody, model, request.getRequestId(),
                (int) (System.currentTimeMillis() - start));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SaasYmlLlmClient] vision 异常", e);
            throw new RuntimeException("截图识别失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String mode() {
        return MODE;
    }

    /* ===================== private helpers ===================== */

    private String buildChatRequestBody(String model, List<LlmChatRequest.Message> messages,
                                        int maxTokens, double temperature, boolean jsonMode,
                                        Boolean thinkingEnabled) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"").append(escapeJson(model)).append("\",");
        sb.append("\"messages\":[");
        boolean first = true;
        for (LlmChatRequest.Message msg : messages) {
            if (!first) {
                sb.append(",");
            }
            sb.append("{\"role\":\"").append(escapeJson(msg.getRole())).append("\",");
            sb.append("\"content\":\"").append(escapeJson(msg.getContent())).append("\"}");
            first = false;
        }
        sb.append("],");
        sb.append("\"max_tokens\":").append(maxTokens).append(",");
        sb.append("\"temperature\":").append(temperature);
        if (thinkingEnabled != null) {
            sb.append(",\"enable_thinking\":").append(thinkingEnabled);
        }
        if (jsonMode) {
            sb.append(",\"response_format\":{\"type\":\"json_object\"}");
        }
        sb.append("}");
        return sb.toString();
    }

    private String buildVisionRequestBody(String model, int maxTokens, String systemPrompt, String userPrompt,
                                          String mimeType, String base64Image) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"").append(escapeJson(model)).append("\",");
        sb.append("\"max_tokens\":").append(maxTokens).append(",");
        sb.append("\"messages\":[");
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            sb.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"},");
        }
        sb.append("{\"role\":\"user\",\"content\":[");
        sb.append("{\"type\":\"image_url\",\"image_url\":{\"url\":\"data:")
            .append(mimeType).append(";base64,").append(base64Image).append("\"}},");
        sb.append("{\"type\":\"text\",\"text\":\"").append(escapeJson(userPrompt)).append("\"}");
        sb.append("]}");
        sb.append("]}");
        return sb.toString();
    }

    private LlmChatResponse parseChatCompletionResponse(String responseBody, String modelName,
                                                       String requestId, int costMs) {
        LlmChatResponse resp = new LlmChatResponse()
            .setModelName(modelName)
            .setRequestId(requestId)
            .setCostMs(costMs);
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String content = extractContent(root);
            resp.setContent(content);
            if (root.has("usage")) {
                JsonNode usage = root.get("usage");
                LlmChatResponse.TokenUsage tu = new LlmChatResponse.TokenUsage();
                tu.setInputTokens(usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : null);
                tu.setOutputTokens(usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : null);
                tu.setTotalTokens(usage.has("total_tokens") ? usage.get("total_tokens").asInt() : null);
                resp.setTokenUsage(tu);
            }
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode choice0 = root.get("choices").get(0);
                if (choice0.has("finish_reason")) {
                    resp.setFinishReason(choice0.get("finish_reason").asText());
                }
            }
        } catch (Exception e) {
            log.error("[SaasYmlLlmClient] 解析响应失败: body={}", responseBody, e);
        }
        return resp;
    }

    private String extractContent(JsonNode root) {
        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
            JsonNode message = root.get("choices").get(0).get("message");
            if (message != null && message.has("content")) {
                return stripCodeFence(message.get("content").asText());
            }
        }
        if (root.has("output")) {
            JsonNode output = root.get("output");
            if (output.has("choices") && output.get("choices").isArray() && output.get("choices").size() > 0) {
                JsonNode message = output.get("choices").get(0).get("message");
                if (message != null && message.has("content")) {
                    return stripCodeFence(message.get("content").asText());
                }
            }
        }
        return null;
    }

    private String stripCodeFence(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        return trimmed.trim();
    }

    private String resolveChatCompletionsUrl(String apiUrl) {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            return apiUrl;
        }
        String normalized = apiUrl.trim();
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized + "/chat/completions";
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * 从上游错误响应中提取可读的错误信息（截断到 120 字符，避免前端提示过长）
     */
    private String extractUpstreamError(String responseBody, int httpCode) {
        if (responseBody == null || responseBody.isBlank()) {
            return "HTTP " + httpCode;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode err = root.get("error");
            if (err != null) {
                String message = err.has("message") ? err.get("message").asText() : null;
                String code = err.has("code") ? err.get("code").asText() : null;
                if (message != null && !message.isBlank()) {
                    return code != null ? code + ": " + message : message;
                }
            }
        } catch (Exception ignore) {
            // 非 JSON 响应，直接截断原文
        }
        String text = responseBody.trim();
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }
}
