package com.ruoyi.wms.service.ai;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.alibaba.dashscope.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.wms.config.AiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 阿里云 DashScope 语音识别服务
 *
 * 使用 DashScope Java SDK 的 Recognition 类（实时语音识别）
 * 参考 Python SDK: Recognition(model='paraformer-realtime-v2', ...).call('file.wav')
 *
 * 复用现有 qwen 的 API Key，无需额外申请
 * 支持 mp3/wav/pcm/m4a/flac/amr 等多种格式，不限采样率
 */
@Service
public class DashScopeAsrService {

    private static final Logger log = LoggerFactory.getLogger(DashScopeAsrService.class);

    @Autowired
    private AiProperties aiProperties;

    @Autowired
    private TenantAiConfigResolver tenantAiConfigResolver;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int WAV_HEADER_BYTES = 44;

    /**
     * 语音识别（主入口）
     */
    public String recognize(MultipartFile audioFile) {
        Path tempFile = null;
        try {
            log.info("DashScope ASR开始识别，文件名: {}, 大小: {} bytes",
                    audioFile.getOriginalFilename(), audioFile.getSize());

            byte[] audioData = audioFile.getBytes();
            validateAudioData(audioFile.getOriginalFilename(), audioData);
            saveForDebug(audioFile.getOriginalFilename(), audioData);

            // SDK 需要本地文件，保存到临时路径
            String suffix = ".mp3";
            String originalName = audioFile.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                suffix = originalName.substring(originalName.lastIndexOf("."));
            }
            tempFile = Files.createTempFile("asr_", suffix);
            Files.write(tempFile, audioData);

            String apiKey = tenantAiConfigResolver.resolveApiKey();
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("DashScope API Key 未配置");
            }

            // 构建识别参数 - 支持租户自定义语音模型
            String voiceModel = tenantAiConfigResolver.resolveVoiceModel();
            RecognitionParam param = RecognitionParam.builder()
                    .model(voiceModel)
                    .format(suffix.replace(".", ""))
                    .sampleRate(16000)
                    .apiKey(apiKey)
                    .build();

            // 调用识别（对应 Python: result = recognition.call('file.wav')）
            Recognition recognition = new Recognition();
            String rawResult = recognition.call(param, tempFile.toFile());

            log.info("DashScope ASR原始结果: {}", rawResult);

            String text = extractTextFromResult(rawResult);
            log.info("DashScope ASR识别成功，结果: {}", text);
            return text;

        } catch (ApiException e) {
            if (isEmptyAudioError(e)) {
                log.warn("DashScope ASR空音频，按空识别处理: {}", e.getMessage());
                return "";
            }
            log.error("DashScope ASR识别失败", e);
            throw new RuntimeException(buildFriendlyErrorMessage(e), e);
        } catch (Exception e) {
            log.error("DashScope ASR识别失败", e);
            throw new RuntimeException(buildFriendlyErrorMessage(e), e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }

    private void validateAudioData(String originalName, byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            throw new IllegalArgumentException("音频数据为空，请重新录音后重试");
        }
        String lowerName = originalName == null ? "" : originalName.toLowerCase();
        if (lowerName.endsWith(".wav") && audioData.length <= WAV_HEADER_BYTES) {
            throw new IllegalArgumentException("音频内容过短，请重新录音后重试");
        }
    }

    private boolean isEmptyAudioError(ApiException e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return false;
        }
        return msg.contains("\"code\":\"EmptyAudio\"") || msg.contains("EmptyAudio");
    }

    private String buildFriendlyErrorMessage(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return "语音识别服务暂时不可用，请稍后重试";
        }
        if (msg.contains("EmptyAudio")) {
            return "语音内容为空或过短，请重新录音后重试";
        }
        return "语音识别服务暂时不可用，请稍后重试";
    }

    /**
     * 从SDK返回的JSON结果中提取纯文本
     * SDK返回格式: {"sentences":[{"text":"我打算要苹果13。",...},{"text":"辣条28。",...}]}
     * 提取所有 sentence 的 text 拼接
     */
    private String extractTextFromResult(String rawResult) {
        if (rawResult == null || rawResult.isBlank()) return "";

        String trimmed = rawResult.trim();
        // 只要是 JSON 结构（以 { 或 [ 开头），就严格按 JSON 解析；
        // 解析不出有效文字一律返回空串，避免把 {"sentences":[]} 这类空识别结果当成用户输入
        boolean looksLikeJson = trimmed.startsWith("{") || trimmed.startsWith("[");
        if (looksLikeJson) {
            try {
                JsonNode root = objectMapper.readTree(trimmed);
                JsonNode sentences = root.path("sentences");
                if (sentences.isArray() && sentences.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode sentence : sentences) {
                        String sentenceText = sentence.path("text").asText("");
                        if (sentenceText != null) {
                            sb.append(sentenceText);
                        }
                    }
                    return cleanText(sb.toString());
                }
                if (root.has("text")) {
                    return cleanText(root.path("text").asText(""));
                }
                // JSON 结构但既没有 sentences 也没有 text —— 视为空识别
                log.info("DashScope ASR 返回空识别结果（JSON 无有效文字字段），rawResult={}", rawResult);
                return "";
            } catch (Exception e) {
                // JSON 解析失败，也不允许把原始 JSON 字符串当成识别文字返回
                log.warn("DashScope ASR 返回疑似 JSON 但解析失败，按空识别处理，rawResult={}", rawResult);
                return "";
            }
        }

        // 纯文本结果才走清理逻辑返回
        return cleanText(trimmed);
    }

    /**
     * 清理文本（去掉句末标点）
     */
    private String cleanText(String text) {
        if (text == null) return "";
        text = text.trim();
        // 去掉列表包装 [...] 如果有
        if (text.startsWith("[") && text.endsWith("]")) {
            text = text.substring(1, text.length() - 1).trim();
        }
        if (text.endsWith("\u3002") || text.endsWith("\uff0c")
                || text.endsWith("\u3001")) {
            text = text.substring(0, text.length() - 1).trim();
        }
        return text;
    }

    /**
     * 保存音频文件到调试目录
     */
    private void saveForDebug(String originalName, byte[] audioData) {
        if (!log.isDebugEnabled()) {
            return;
        }
        try {
            Path debugDir = Path.of(System.getProperty("java.io.tmpdir"), "debug-audio");
            Files.createDirectories(debugDir);
            String fileName = System.currentTimeMillis() + "_" + (originalName != null ? originalName : "audio.bin");
            Path dest = debugDir.resolve(fileName);
            Files.write(dest, audioData);
            log.debug("音频文件已保存到调试目录: {}", dest);
        } catch (Exception e) {
            log.warn("保存调试音频失败: {}", e.getMessage());
        }
    }
}
