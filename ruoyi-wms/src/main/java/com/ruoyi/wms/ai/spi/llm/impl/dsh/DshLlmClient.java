package com.ruoyi.wms.ai.spi.llm.impl.dsh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.wms.ai.spi.context.CallerContext;
import com.ruoyi.wms.ai.spi.context.CallerContextResolver;
import com.ruoyi.wms.ai.spi.llm.LlmChatRequest;
import com.ruoyi.wms.ai.spi.llm.LlmChatResponse;
import com.ruoyi.wms.ai.spi.llm.LlmClient;
import com.ruoyi.wms.ai.spi.llm.LlmVisionRequest;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * DSH 桥接模式下的 {@link LlmClient} 实现：把模型调用 POST 给 DeepSeek Harness
 * 暴露的桥接端点（{@code /wms-llm/chat} 与 {@code /wms-llm/vision}），
 * 由 dsh 使用自己在设置页配置的模型执行，WMS 本地不持有任何模型 Key。
 *
 * <p>启用条件：{@code ai.client.mode=dsh}。</p>
 *
 * <p>本类结构照抄 {@code AigwLlmClient}，请求/响应契约与 AIGW 内部代理端点对齐
 * （{@code {code, data:{answer, modelName, requestId, usage, finishReason}}}），
 * 仅基地址、鉴权 Header 与路径不同。</p>
 *
 * @author ruoyi
 */
@Component
@ConditionalOnProperty(name = "ai.client.mode", havingValue = "dsh")
public class DshLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(DshLlmClient.class);

    public static final String MODE = "dsh";

    private static final String CHAT_PATH = "/wms-llm/chat";
    private static final String VISION_PATH = "/wms-llm/vision";

    @Autowired
    private DshGatewayProperties properties;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired(required = false)
    private CallerContextResolver callerContextResolver;

    private volatile OkHttpClient httpClient;

    private OkHttpClient client() {
        OkHttpClient local = this.httpClient;
        if (local == null) {
            synchronized (this) {
                local = this.httpClient;
                if (local == null) {
                    local = new OkHttpClient.Builder()
                        .connectTimeout(properties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                        .readTimeout(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                        .writeTimeout(properties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                        .build();
                    this.httpClient = local;
                }
            }
        }
        return local;
    }

    @Override
    public LlmChatResponse chat(LlmChatRequest request) {
        long start = System.currentTimeMillis();
        validateBaseConfig();

        try {
            ObjectNode root = objectMapper.createObjectNode();
            if (properties.getTextModel() != null && !properties.getTextModel().isBlank()) {
                root.put("modelName", properties.getTextModel());
            }

            ArrayNode messages = root.putArray("messages");
            for (LlmChatRequest.Message msg : request.resolvedMessages()) {
                ObjectNode m = messages.addObject();
                m.put("role", msg.getRole());
                m.put("content", msg.getContent() == null ? "" : msg.getContent());
            }
            if (request.getTemperature() != null) {
                root.put("temperature", request.getTemperature());
            }
            if (request.getMaxTokens() != null) {
                root.put("maxTokens", request.getMaxTokens());
            }
            if (Boolean.TRUE.equals(request.getJsonMode())) {
                root.put("jsonMode", true);
            }
            attachCallerFields(root, request.getRequestId(), request.getUsageScene());

            String responseBody = post(CHAT_PATH, root.toString());
            return parseResponse(responseBody, request.getRequestId(),
                (int) (System.currentTimeMillis() - start));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[DshLlmClient] chat 调用异常", e);
            throw new RuntimeException("DSH 模型调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public LlmChatResponse vision(LlmVisionRequest request) {
        long start = System.currentTimeMillis();
        validateBaseConfig();

        try {
            ObjectNode root = objectMapper.createObjectNode();
            if (properties.getVisionModel() != null && !properties.getVisionModel().isBlank()) {
                root.put("modelName", properties.getVisionModel());
            }
            if (request.getSystemPrompt() != null) {
                root.put("systemPrompt", request.getSystemPrompt());
            }
            root.put("prompt", request.getPrompt() == null ? "" : request.getPrompt());
            if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
                root.put("imageBase64", request.getImageBase64());
                root.put("imageMimeType", request.getImageMimeType() == null ? "image/jpeg" : request.getImageMimeType());
            } else if (request.getImageUrl() != null) {
                root.put("imageUrl", request.getImageUrl());
            } else {
                throw new RuntimeException("DSH 模式视觉调用需提供 imageBase64 或 imageUrl");
            }
            if (request.getTemperature() != null) {
                root.put("temperature", request.getTemperature());
            }
            if (request.getMaxTokens() != null) {
                root.put("maxTokens", request.getMaxTokens());
            }
            if (Boolean.TRUE.equals(request.getJsonMode())) {
                root.put("jsonMode", true);
            }
            attachCallerFields(root, request.getRequestId(), request.getUsageScene());

            String responseBody = post(VISION_PATH, root.toString());
            return parseResponse(responseBody, request.getRequestId(),
                (int) (System.currentTimeMillis() - start));
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[DshLlmClient] vision 调用异常", e);
            throw new RuntimeException("DSH 模型调用失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String mode() {
        return MODE;
    }

    /* ===================== private helpers ===================== */

    private void validateBaseConfig() {
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isBlank()) {
            throw new IllegalStateException("dsh.gateway.base-url 未配置");
        }
        if (properties.getBridgeToken() == null || properties.getBridgeToken().isBlank()) {
            throw new IllegalStateException("dsh.gateway.bridge-token 未配置");
        }
    }

    private void attachCallerFields(ObjectNode root, String requestId, String usageScene) {
        if (requestId != null && !requestId.isBlank()) {
            root.put("requestId", requestId);
        }
        String tenantTag = properties.getTenantTag();
        String userTag = properties.getUserTag();
        if (callerContextResolver != null) {
            try {
                CallerContext ctx = callerContextResolver.current();
                if (ctx != null) {
                    if (ctx.getTenantId() != null) {
                        tenantTag = String.valueOf(ctx.getTenantId());
                    }
                    if (ctx.getUserId() != null) {
                        userTag = String.valueOf(ctx.getUserId());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (tenantTag != null) {
            root.put("callerTenantId", tenantTag);
        }
        if (userTag != null) {
            root.put("callerUserId", userTag);
        }
        if (usageScene != null && !usageScene.isBlank()) {
            root.put("usageScene", usageScene);
        }
    }

    private String post(String path, String jsonBody) throws Exception {
        String url = properties.getBaseUrl().replaceAll("/+$", "") + path;
        Request httpRequest = new Request.Builder()
            .url(url)
            .addHeader(properties.getHeaderName(), properties.getBridgeToken())
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8")))
            .build();

        try (Response response = client().newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            if (!response.isSuccessful()) {
                log.error("[DshLlmClient] dsh 返回非 2xx: code={}, body={}", response.code(), responseBody);
                String msg = extractErrorMessage(responseBody);
                throw new RuntimeException("DSH 模型调用失败: HTTP " + response.code()
                    + (msg == null ? "" : ", " + msg));
            }
            return responseBody;
        }
    }

    private LlmChatResponse parseResponse(String responseBody, String requestIdHint, int costMs) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            int code = root.has("code") ? root.get("code").asInt() : 200;
            if (code != 200) {
                String msg = root.has("msg") ? root.get("msg").asText() : "未知错误";
                throw new RuntimeException("DSH 模型调用失败: " + msg);
            }
            JsonNode data = root.get("data");
            if (data == null || data.isNull()) {
                throw new RuntimeException("DSH 响应缺少 data 字段");
            }
            LlmChatResponse resp = new LlmChatResponse()
                .setContent(data.has("answer") ? data.get("answer").asText() : null)
                .setModelName(data.has("modelName") ? data.get("modelName").asText() : null)
                .setRequestId(data.has("requestId") ? data.get("requestId").asText() : requestIdHint)
                .setCostMs(data.has("costMs") ? data.get("costMs").asInt() : costMs)
                .setFinishReason(data.has("finishReason") ? data.get("finishReason").asText() : null);
            if (data.has("usage") && !data.get("usage").isNull()) {
                JsonNode usage = data.get("usage");
                LlmChatResponse.TokenUsage tu = new LlmChatResponse.TokenUsage();
                tu.setInputTokens(usage.has("inputTokens") ? usage.get("inputTokens").asInt() : null);
                tu.setOutputTokens(usage.has("outputTokens") ? usage.get("outputTokens").asInt() : null);
                tu.setTotalTokens(usage.has("totalTokens") ? usage.get("totalTokens").asInt() : null);
                resp.setTokenUsage(tu);
            }
            return resp;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[DshLlmClient] 解析 DSH 响应失败: body={}", responseBody, e);
            throw new RuntimeException("DSH 响应解析失败: " + e.getMessage(), e);
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.has("msg") ? root.get("msg").asText() : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}
