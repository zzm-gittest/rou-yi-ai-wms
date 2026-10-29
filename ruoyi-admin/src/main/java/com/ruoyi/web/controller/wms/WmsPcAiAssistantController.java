package com.ruoyi.web.controller.wms;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.system.service.AiPointsService;
import com.ruoyi.wms.service.ai.pc.PcMultiBillChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * PC端AI助手V2（独立新链路）
 */
@Api(tags = "PC端AI助手V2")
@RestController
@RequestMapping("/wms/ai/pc")
public class WmsPcAiAssistantController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(WmsPcAiAssistantController.class);

    private final PcMultiBillChatService chatService;
    private final AiPointsService aiPointsService;

    public WmsPcAiAssistantController(PcMultiBillChatService chatService, AiPointsService aiPointsService) {
        this.chatService = chatService;
        this.aiPointsService = aiPointsService;
    }

    @ApiOperation("获取AI助手可操作动作")
    @PreAuthorize("@ss.hasPermi('ai:assistantPc:query')")
    @GetMapping("/actions")
    public AjaxResult actions() {
        return success(chatService.listActions());
    }

    @ApiOperation("开始PC端AI对话")
    @PreAuthorize("@ss.hasPermi('ai:assistantPc:query')")
    @PostMapping("/start")
    public AjaxResult start() {
        return success(chatService.start(getUserId()));
    }

    @ApiOperation("PC端AI聊天")
    @PreAuthorize("@ss.hasPermi('ai:assistantPc:query')")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody Map<String, String> body) {
        String conversationId = body.get("conversationId");
        String message = body.get("message");
        if (message == null || message.trim().isEmpty()) {
            return AjaxResult.error("消息不能为空");
        }
        if (chatService.willUseLlm(conversationId, message.trim())) {
            Long tenantId = TenantHolder.getTenantId();
            if (!aiPointsService.tryConsumePoints(tenantId, AiPointsService.COST_TEXT, "text")) {
                return AjaxResult.error("AI点数不足，文字解析需要" + AiPointsService.COST_TEXT + "点，请充值");
            }
        }
        return success(chatService.chat(getUserId(), conversationId, message));
    }

    @ApiOperation("PC端截图识别开单")
    @PreAuthorize("@ss.hasPermi('ai:assistantPc:query')")
    @PostMapping("/imageChat")
    public AjaxResult imageChat(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") String conversationId
    ) {
        if (file.isEmpty()) {
            return error("请上传截图");
        }
        String filename = file.getOriginalFilename();
        if (filename == null ||
                (!filename.toLowerCase().endsWith(".jpg") &&
                        !filename.toLowerCase().endsWith(".jpeg") &&
                        !filename.toLowerCase().endsWith(".png") &&
                        !filename.toLowerCase().endsWith(".webp"))) {
            return error("只支持jpg/png/webp格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return error("图片大小不能超过5MB");
        }
        if (conversationId == null || conversationId.isEmpty()) {
            return error("会话ID不能为空");
        }
        Long userId;
        try {
            userId = getUserId();
        } catch (Exception e) {
            return error("登录已过期，请重新登录");
        }
        try {
            Long tenantId = TenantHolder.getTenantId();
            if (!aiPointsService.tryConsumePoints(tenantId, AiPointsService.COST_IMAGE, "pc_ocr_image")) {
                return error("AI点数不足，截图识别需要" + AiPointsService.COST_IMAGE + "点，请充值");
            }
            Map<String, Object> result = chatService.imageChat(userId, conversationId, file);
            return success(result);
        } catch (Exception e) {
            log.error("PC截图识别失败", e);
            Long tenantId = TenantHolder.getTenantId();
            aiPointsService.refundPoints(tenantId, AiPointsService.COST_IMAGE, "pc_ocr_image");
            return error("截图识别失败: " + e.getMessage());
        }
    }

    @ApiOperation("清理PC端对话上下文")
    @PreAuthorize("@ss.hasPermi('ai:assistantPc:query')")
    @DeleteMapping("/context/{conversationId}")
    public AjaxResult clear(
            @ApiParam("会话ID") @PathVariable String conversationId
    ) {
        chatService.clear(conversationId);
        return success("会话上下文已清理");
    }
}

