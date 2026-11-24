package com.ruoyi.wms.service.ai;

import com.ruoyi.wms.ai.spi.llm.LlmChatResponse;
import com.ruoyi.wms.ai.spi.llm.LlmClient;
import com.ruoyi.wms.ai.spi.llm.LlmVisionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * 多模态 LLM 服务（保持原有 public API；底层委托给 {@link LlmClient} SPI）。
 *
 * <p>SaaS 模式下行为完全等价于历史版本（DashScope qwen-vl-plus）；
 * Gateway 模式下通过 AIGW {@code /open/aigw/internal/ai/llm/vision} 调用模型。</p>
 *
 * <p>上层调用方（MobileChatServiceImpl / PcMultiBillChatServiceImpl）保持不变。</p>
 *
 * @author ruoyi
 */
@Service
public class MultimodalLlmService {

    private static final Logger log = LoggerFactory.getLogger(MultimodalLlmService.class);

    /**
     * 系统提示词 - 用于截图识别
     */
    private static final String SYSTEM_PROMPT =
            "你是一个仓库管理系统(WMS)的截图识别助手。用户会发送聊天截图、手写单据照片或其他订单相关图片。\n\n" +
            "你的任务：\n" +
            "1. 识别图片中的所有商品/货物信息\n" +
            "2. 提取供应商/客户名称（如果可见）\n" +
            "3. 提取每个商品的名称、数量、单价（如果可见）\n\n" +
            "请严格按照以下JSON格式返回，不要添加任何额外的说明文字：\n" +
            "```json\n" +
            "{\n" +
            "  \"supplier\": \"供应商或客户名称，无法识别填null\",\n" +
            "  \"items\": [\n" +
            "    {\n" +
            "      \"goodsName\": \"商品名称\",\n" +
            "      \"quantity\": 数量（数字）,\n" +
            "      \"unit\": \"单位（如：箱、包、袋、斤、个、瓶、件等，无法识别填null）\",\n" +
            "      \"price\": 单价（数字，无法识别填null）\n" +
            "    }\n" +
            "  ],\n" +
            "  \"remark\": \"图片中的其他备注信息\"\n" +
            "}\n" +
            "```\n\n" +
            "注意：\n" +
            "- 如果图片模糊或无法识别某些字段，相应字段填null\n" +
            "- 数量必须是数字，不能包含单位\n" +
            "- 单位必须单独提取到unit字段，例如\"10箱苹果\"中unit=\"箱\"，quantity=10\n" +
            "- 如果完全无法识别订单信息，返回 {\"error\": \"无法识别图片中的订单信息，请拍摄更清晰的图片\"}";

    @Autowired
    private LlmClient llmClient;

    /**
     * 识别截图中的订单信息（保持原签名）
     *
     * @param imageFile 图片文件
     * @return LLM 返回的 JSON 字符串（已剥离 markdown 代码块）
     */
    public String recognizeImage(MultipartFile imageFile) {
        try {
            byte[] imageBytes = imageFile.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = detectMimeType(imageFile.getOriginalFilename());

            log.info("MultimodalLlmService.recognizeImage start: mode={}, fileName={}, fileSize={}, mime={}",
                llmClient.mode(), imageFile.getOriginalFilename(), imageBytes.length, mimeType);

            LlmVisionRequest req = new LlmVisionRequest()
                .setSystemPrompt(SYSTEM_PROMPT)
                .setPrompt("请识别这张图片中的订单/商品信息，按要求的JSON格式返回。")
                .setImageBase64(base64Image)
                .setImageMimeType(mimeType)
                .setUsageScene("image_ocr_screenshot");

            LlmChatResponse resp = llmClient.vision(req);
            String content = resp == null ? null : resp.getContent();
            log.info("MultimodalLlmService.recognizeImage response: mode={}, contentPresent={}, requestId={}, durationMs={}",
                llmClient.mode(), content != null && !content.isBlank(), resp != null ? resp.getRequestId() : "null",
                resp != null ? resp.getCostMs() : null);
            if (content == null || content.isBlank()) {
                throw new RuntimeException("截图识别返回为空");
            }
            return content;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[{}] 截图识别失败", llmClient.mode(), e);
            throw new RuntimeException("截图识别失败: " + e.getMessage(), e);
        }
    }

    private String detectMimeType(String filename) {
        if (filename == null) return "image/jpeg";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
