package com.ruoyi.wms.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.wms.config.AiProperties;
import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.service.WmsAiChatLogService;
import com.ruoyi.wms.service.ai.conversation.ConversationManager;
import com.ruoyi.wms.service.ai.conversation.ConversationState;
import com.ruoyi.wms.service.ai.parser.IntentType;
import com.ruoyi.wms.service.ai.parser.ParsedOrderResult;
import com.ruoyi.wms.service.ai.parser.SmartOrderParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * WMS对话式助手服务实现
 * <p>
 * 支持两种模式：
 * 1. 智能对话模式：使用ConversationManager进行状态驱动的多轮对话
 * 2. LLM对话模式：使用通义千问大模型进行对话
 *
 * @author ruoyi
 */
@Service
public class WmsChatServiceImpl implements WmsChatService {

    private static final Logger log = LoggerFactory.getLogger(WmsChatServiceImpl.class);

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private TenantAiConfigResolver tenantAiConfigResolver;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private SmartOrderParser smartOrderParser;

    @Autowired
    private WmsAiChatLogService chatLogService;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    // 跨业务检测关键词
    private static final String[] PURCHASE_KEYWORDS = {"入库", "采购入库", "进货入库", "采购", "进货", "买", "购买"};
    private static final String[] SALES_KEYWORDS = {"出库", "销售出库", "发货出库", "销售", "卖", "出货", "出售"};
    private static final String[] GOODS_QUERY_KEYWORDS = {"查询商品", "商品查询", "有没有", "找商品", "查商品"};
    private static final String[] STOCK_QUERY_KEYWORDS = {"库存", "库存查询", "还有多少", "查库存"};

    @Override
    public ChatResponseVO startChat(String operationType, Long userId) {
        return conversationManager.start(operationType, userId);
    }

    @Override
    public ChatResponseVO chat(ChatRequestVO request) {
        long startTime = System.currentTimeMillis();
        ChatResponseVO response = null;
        String resultStatus = "success";
        String errorMessage = null;

        try {
            // 检查是否是"继续创建订单"类型的请求，需要创建新会话
            if (isContinueOrderRequest(request.getMessage())) {
                // 清除旧会话，创建新会话
                if (request.getConversationId() != null) {
                    conversationManager.clearSession(request.getConversationId());
                }
                // 根据请求类型创建新会话
                String operationType = determineOperationType(request.getMessage());
                request.setOperationType(operationType);
                request.setConversationId(null); // 清除会话ID，让系统创建新会话
                response = conversationManager.handle(request);
                return response;
            }

            // 如果有会话ID，检查会话隔离
            if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
                ConversationState state = conversationManager.getSession(request.getConversationId());
                if (state != null) {
                    // 检查是否超出当前会话的业务范围
                    if (isOutOfScope(state.getOperationType(), request.getMessage())) {
                        response = buildOutOfScopeResponse(state.getOperationType(), request.getMessage());
                        return response;
                    }
                    // 在当前会话范围内，正常处理
                    response = conversationManager.handle(request);
                    return response;
                }
            }

            // 如果有指定的操作类型，使用智能对话模式（ConversationManager）
            if (request.getOperationType() != null && !request.getOperationType().isEmpty()) {
                response = conversationManager.handle(request);
                return response;
            }

            // 否则使用LLM识别意图
            response = chatWithIntentRecognition(request);
            return response;
        } catch (Exception e) {
            log.error("对话处理失败", e);
            resultStatus = "failed";
            errorMessage = e.getMessage();
            response = buildErrorResponse(e.getMessage());
            return response;
        } finally {
            // 异步记录会话日志
            int processTime = (int) (System.currentTimeMillis() - startTime);
            String responseContent = response != null ? response.getReply() : null;
            String operationType = response != null ? response.getOperationType() : request.getOperationType();

            chatLogService.logAsync(
                request.getConversationId(),
                request.getUserId(),
                operationType,
                request.getInputType(),
                request.getMessage(),
                request.getVoiceFilePath(),
                responseContent,
                resultStatus,
                errorMessage,
                processTime
            );
        }
    }

    /**
     * 检测是否超出当前会话的业务范围
     */
    private boolean isOutOfScope(String operationType, String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        // 采购单会话：禁止销售、商品查询、库存查询
        if ("create_purchase_in".equals(operationType) || "create_purchase_order".equals(operationType)) {
            return containsKeywords(message, SALES_KEYWORDS, GOODS_QUERY_KEYWORDS, STOCK_QUERY_KEYWORDS);
        }
        // 销售单会话：禁止采购、商品查询、库存查询
        if ("create_sales_out".equals(operationType) || "create_sales_order".equals(operationType)) {
            return containsKeywords(message, PURCHASE_KEYWORDS, GOODS_QUERY_KEYWORDS, STOCK_QUERY_KEYWORDS);
        }
        // 商品查询会话：禁止采购、销售、库存查询
        if ("query_goods".equals(operationType)) {
            return containsKeywords(message, PURCHASE_KEYWORDS, SALES_KEYWORDS, STOCK_QUERY_KEYWORDS);
        }
        // 库存查询会话：禁止采购、销售、商品查询
        if ("query_stock".equals(operationType)) {
            return containsKeywords(message, PURCHASE_KEYWORDS, SALES_KEYWORDS, GOODS_QUERY_KEYWORDS);
        }
        return false;
    }

    /**
     * 检查消息是否包含关键词
     */
    private boolean containsKeywords(String message, String[]... keywordArrays) {
        for (String[] keywords : keywordArrays) {
            for (String keyword : keywords) {
                if (message.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 构建跨业务提示响应
     */
    private ChatResponseVO buildOutOfScopeResponse(String operationType, String message) {
        String sessionName = getSessionName(operationType);
        String suggestAction = getSuggestAction(message);

        String reply = String.format(
            "⚠️ 当前是【%s】会话\n\n%s请返回主页，点击对应功能入口",
            sessionName, suggestAction
        );

        // 构建返回主页的操作按钮
        ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
        action.setName("return_home");
        action.setDescription("返回主页");

        return ChatResponseVO.builder()
                .reply(reply)
                .operationType(operationType)
                .suggestedAction(action)
                .needMoreInfo(true)
                .build();
    }

    /**
     * 获取会话名称
     */
    private String getSessionName(String operationType) {
        if ("create_purchase_in".equals(operationType) || "create_purchase_order".equals(operationType)) {
            return "采购入库";
        }
        if ("create_sales_out".equals(operationType) || "create_sales_order".equals(operationType)) {
            return "销售出库";
        }
        if ("query_goods".equals(operationType)) {
            return "商品查询";
        }
        if ("query_stock".equals(operationType)) {
            return "库存查询";
        }
        return operationType;
    }

    /**
     * 获取建议操作
     */
    private String getSuggestAction(String message) {
        if (containsKeywords(message, PURCHASE_KEYWORDS)) {
            return "创建采购入库单";
        }
        if (containsKeywords(message, SALES_KEYWORDS)) {
            return "创建销售出库单";
        }
        if (containsKeywords(message, GOODS_QUERY_KEYWORDS)) {
            return "查询商品";
        }
        if (containsKeywords(message, STOCK_QUERY_KEYWORDS)) {
            return "查询库存";
        }
        return "该操作";
    }

    /**
     * 带意图识别的对话
     */
    private ChatResponseVO chatWithIntentRecognition(ChatRequestVO request) {
        try {
            // 检查是否有会话ID，有则继续已有会话
            if (request.getConversationId() != null && !request.getConversationId().isEmpty()) {
                var existingSession = conversationManager.getSession(request.getConversationId());
                if (existingSession != null) {
                    request.setOperationType(existingSession.getOperationType());
                    return conversationManager.handle(request);
                }
            }

            // 没有会话，使用LLM识别意图
            String apiKey = tenantAiConfigResolver.resolveApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                // API未配置，使用简单的意图识别
                return chatWithSimpleIntent(request);
            }

            // 调用LLM识别意图
            String intent = recognizeIntent(request.getMessage());
            if (intent != null && !intent.isEmpty()) {
                request.setOperationType(intent);
                return conversationManager.handle(request);
            }

            // 意图识别失败，返回帮助消息
            return buildHelpResponse();

        } catch (Exception e) {
            log.error("对话处理失败", e);
            return buildErrorResponse(e.getMessage());
        }
    }

    /**
     * 简单意图识别（当LLM不可用时）
     * 使用智能解析服务进行意图识别和信息提取
     */
    private ChatResponseVO chatWithSimpleIntent(ChatRequestVO request) {
        String message = request.getMessage();

        // 使用智能解析服务检测意图
        IntentType intent = smartOrderParser.detectIntent(message);

        log.info("智能解析 - 消息: {}, 意图: {}", message, intent);

        switch (intent) {
            case PURCHASE_ORDER:
            case SALES_ORDER:
                // 订单类意图，解析详细信息
                ParsedOrderResult result = smartOrderParser.parseOrder(message);
                request.setOperationType(intent.getCode());

                if (result.isComplete()) {
                    // 完整订单，直接交给处理器生成预览
                    log.info("检测到完整订单: {}", result);
                    return conversationManager.handleCompleteOrder(request, result);
                } else {
                    // 部分信息，走引导流程
                    log.info("检测到部分订单信息，缺少: {}", result.getMissingFields());
                    return conversationManager.handle(request);
                }

            case QUERY_GOODS:
                // 商品查询
                request.setOperationType("query_goods");
                return conversationManager.handle(request);

            case QUERY_STOCK:
                // 库存查询
                request.setOperationType("query_stock");
                return conversationManager.handle(request);

            default:
                // 未知意图，返回引导提示
                return buildGuidanceResponse(request);
        }
    }

    /**
     * 构建引导提示响应
     */
    private ChatResponseVO buildGuidanceResponse(ChatRequestVO request) {
        String guidance = smartOrderParser.buildGuidanceMessage();

        return ChatResponseVO.builder()
                .conversationId(request.getConversationId())
                .operationType("guidance")
                .reply(guidance)
                .needMoreInfo(true)
                .build();
    }

    /**
     * 构建帮助响应
     */
    private ChatResponseVO buildHelpResponse() {
        return ChatResponseVO.builder()
                .reply("您好！我是羽辽进销存智能助手，可以帮您完成以下操作：\n\n" +
                    "📥 **采购入库**\n" +
                    "   - \"创建采购入库单\" / \"入库\" / \"采购入库\" / \"进货入库\"\n\n" +
                        "📋 **销售相关**\n" +
                    "   - \"创建销售出库单\" / \"出库\" / \"销售出库\" / \"出货\"\n\n" +
                        "🔍 **库存查询**\n" +
                        "   - \"查询库存\" / \"查库存\" / \"库存有多少\"\n\n" +
                        "🔎 **商品查询**\n" +
                        "   - \"查询商品\" / \"商品\" / \"找一下XX商品\" / \"有没有XX\"\n\n" +
                        "💡 **智能语音**\n" +
                        "   - 您可以直接说\"入库5台iPhone，1000元，一鸣供应商\"\n" +
                        "   - 系统会自动识别并生成入库/出库单\n\n" +
                        "请告诉我您需要什么帮助？")
                .build();
    }

    @Override
    public void clearContext(String conversationId) {
        conversationManager.clearSession(conversationId);
    }

    @Override
    public ChatResponseVO getContext(String conversationId) {
        var state = conversationManager.getSession(conversationId);
        if (state != null) {
            return ChatResponseVO.builder()
                    .conversationId(conversationId)
                    .operationType(state.getOperationType())
                    .collectedData(state.getCollectedData())
                    .reply("当前会话进行中，操作类型：" + state.getOperationType())
                    .build();
        }
        return ChatResponseVO.builder()
                .conversationId(conversationId)
                .reply("会话不存在或已过期")
                .build();
    }

    /**
     * 使用LLM识别用户意图
     */
    private String recognizeIntent(String message) {
        try {
            log.info("=== recognizeIntent 开始 ===");
            log.info("用户输入: {}", message);
            
            String prompt = "请识别用户的意图，只返回以下操作类型之一：\n" +
                    "- create_purchase_in (采购入库、入库、进货入库)\n" +
                    "- create_sales_out (销售出库、出库、发货出库、出货)\n" +
                    "- query_stock (查询库存)\n" +
                    "- query_goods (查询商品、找商品)\n" +
                    "- unknown (无法识别)\n\n" +
                    "用户输入：" + message;

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", "你是一个意图识别助手，只返回操作类型代码。");
            messages.add(systemMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);

            String llmResponse = callLlm(messages);
            log.info("LLM原始响应: {}", llmResponse);
            
            if (llmResponse != null) {
                // 提取操作类型
                llmResponse = llmResponse.toLowerCase().trim();
                log.info("LLM处理后响应: {}", llmResponse);
                if (llmResponse.contains("create_purchase_in") || llmResponse.contains("create_purchase_order") || llmResponse.contains("采购") || llmResponse.contains("入库")) {
                    log.info("=== recognizeIntent 结果: create_purchase_in ===");
                    return "create_purchase_in";
                } else if (llmResponse.contains("create_sales_out") || llmResponse.contains("create_sales_order") || llmResponse.contains("销售") || llmResponse.contains("出库") || llmResponse.contains("出货")) {
                    log.info("=== recognizeIntent 结果: create_sales_out ===");
                    return "create_sales_out";
                } else if (llmResponse.contains("query_stock") || llmResponse.contains("库存")) {
                    log.info("=== recognizeIntent 结果: query_stock ===");
                    return "query_stock";
                } else if (llmResponse.contains("query_goods") || llmResponse.contains("商品")) {
                    log.info("=== recognizeIntent 结果: query_goods ===");
                    return "query_goods";
                }
            }
            log.warn("=== recognizeIntent 无法识别意图 ===");
        } catch (Exception e) {
            log.error("LLM意图识别失败", e);
        }
        return null;
    }

    /**
     * 调用通义千问LLM
     */
    private String callLlm(List<Map<String, String>> messages) throws Exception {
        String apiUrl = tenantAiConfigResolver.resolveApiUrl();
        if (apiUrl != null && !apiUrl.trim().isEmpty() && !apiUrl.trim().endsWith("/chat/completions")) {
            apiUrl = apiUrl.trim();
            if (apiUrl.endsWith("/")) {
                apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
            }
            apiUrl = apiUrl + "/chat/completions";
        }

        String model = tenantAiConfigResolver.resolveModel();
        String apiKey = tenantAiConfigResolver.resolveApiKey();

        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{");
        requestBody.append("\"model\":\"").append(model).append("\",");
        requestBody.append("\"messages\":[");

        for (int i = 0; i < messages.size(); i++) {
            Map<String, String> msg = messages.get(i);
            if (i > 0) requestBody.append(",");
            requestBody.append("{");
            requestBody.append("\"role\":\"").append(msg.get("role")).append("\",");
            requestBody.append("\"content\":\"").append(escapeJson(msg.get("content"))).append("\"");
            requestBody.append("}");
        }

        requestBody.append("],");
        requestBody.append("\"max_tokens\":").append(aiProperties.getQwen().getMaxTokens()).append(",");
        requestBody.append("\"temperature\":").append(aiProperties.getQwen().getTemperature());
        requestBody.append("}");

        Request request = new Request.Builder()
            .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json; charset=utf-8")))
                .build();

        log.info("调用通义千问对话API");

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : null;
            log.info("通义千问响应状态: {}", response.code());

            if (response.isSuccessful() && responseBody != null) {
                return extractContentFromLlm(responseBody);
            } else {
                throw new RuntimeException("LLM调用失败: " + response.code());
            }
        }
    }

    /**
     * 从LLM响应中提取内容
     */
    private String extractContentFromLlm(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // OpenAI格式: choices[0].message.content
            if (jsonNode.has("choices")) {
                JsonNode choices = jsonNode.get("choices");
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode choice = choices.get(0);
                    if (choice.has("message")) {
                        JsonNode message = choice.get("message");
                        if (message.has("content")) {
                            return message.get("content").asText();
                        }
                    }
                }
            }

            // 通义千问原生格式
            if (jsonNode.has("output")) {
                JsonNode output = jsonNode.get("output");
                if (output.has("choices")) {
                    JsonNode choices = output.get("choices");
                    if (choices.isArray() && choices.size() > 0) {
                        JsonNode choice = choices.get(0);
                        if (choice.has("message")) {
                            JsonNode message = choice.get("message");
                            if (message.has("content")) {
                                return message.get("content").asText();
                            }
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.error("解析LLM响应失败", e);
            return null;
        }
    }

    /**
     * 解析LLM响应
     */
    private ChatResponseVO parseLlmResponse(String llmResponse) {
        ChatResponseVO response = new ChatResponseVO();

        if (llmResponse == null) {
            response.setReply("抱歉，我遇到了一些问题，请稍后再试。");
            return response;
        }

        try {
            // 提取JSON部分
            String jsonStr = extractJson(llmResponse);
            JsonNode jsonNode = objectMapper.readTree(jsonStr);

            response.setReply(jsonNode.has("reply") ? jsonNode.get("reply").asText() : "");
            response.setOperationType(jsonNode.has("operationType") ? jsonNode.get("operationType").asText() : null);
            response.setNeedMoreInfo(jsonNode.has("needMoreInfo") ? jsonNode.get("needMoreInfo").asBoolean() : false);
            response.setIsCompleted(jsonNode.has("isCompleted") ? jsonNode.get("isCompleted").asBoolean() : false);

            // 解析collectedData
            if (jsonNode.has("collectedData")) {
                Map<String, Object> data = objectMapper.convertValue(
                        jsonNode.get("collectedData"),
                        new TypeReference<Map<String, Object>>() {}
                );
                response.setCollectedData(data);
            }

            // 解析pendingFields
            if (jsonNode.has("pendingFields") && jsonNode.get("pendingFields").isArray()) {
                List<String> fields = new ArrayList<>();
                for (JsonNode field : jsonNode.get("pendingFields")) {
                    fields.add(field.asText());
                }
                response.setPendingFields(fields);
            }

            // 解析suggestedAction
            if (jsonNode.has("suggestedAction")) {
                JsonNode actionNode = jsonNode.get("suggestedAction");
                ChatResponseVO.FunctionCall action = new ChatResponseVO.FunctionCall();
                action.setName(actionNode.has("name") ? actionNode.get("name").asText() : null);
                action.setDescription(actionNode.has("description") ? actionNode.get("description").asText() : null);
                if (actionNode.has("arguments")) {
                    Map<String, Object> args = objectMapper.convertValue(
                            actionNode.get("arguments"),
                            new TypeReference<Map<String, Object>>() {}
                    );
                    action.setArguments(args);
                }
                response.setSuggestedAction(action);
            }

        } catch (Exception e) {
            log.error("解析LLM响应JSON失败，原始响应: {}", llmResponse, e);
            response.setReply(llmResponse); // 直接返回LLM的原始回复
        }

        return response;
    }

    /**
     * 从文本中提取JSON部分
     */
    private String extractJson(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // 查找第一个 { 和最后一个 }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        // 查找第一个 [ 和最后一个 ]
        start = text.indexOf('[');
        end = text.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * 转义JSON字符串
     */
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
     * 检查是否是"继续创建订单"类型的请求
     */
    private boolean isContinueOrderRequest(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String msg = message.trim();
         return msg.contains("继续创建采购入库") ||
             msg.contains("继续创建销售出库") ||
             msg.contains("继续创建采购订单") ||
             msg.contains("继续创建销售订单") ||
             msg.contains("创建采购入库") ||
             msg.contains("创建销售出库") ||
             msg.contains("创建采购订单") ||
             msg.contains("创建销售订单") ||
             msg.contains("创建采购单") ||
             msg.contains("创建销售单") ||
               msg.contains("创建订单");
    }

    /**
     * 根据消息内容确定操作类型
     */
    private String determineOperationType(String message) {
        if (message == null) {
            return "create_purchase_in";
        }
        String msg = message.trim();
        // 优先检查是否明确指定销售出库
        if (msg.contains("销售出库") || msg.contains("出库") || msg.contains("出货") || msg.contains("发货")) {
            return "create_sales_out";
        }
        // 检查是否明确指定采购入库
        if (msg.contains("采购入库") || msg.contains("入库") || msg.contains("采购订单") || msg.contains("采购单")) {
            return "create_purchase_in";
        }
        // 默认为采购入库
        return "create_purchase_in";
    }

    /**
     * 构建错误响应
     */
    private ChatResponseVO buildErrorResponse(String errorMsg) {
        return ChatResponseVO.builder()
                .reply("抱歉，我遇到了一些问题：" + errorMsg + "。请稍后再试。")
                .build();
    }
}
