package com.ruoyi.web.controller.wms;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.WmsPurchaseInItem;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.ai.VoiceParseResultVO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.ruoyi.wms.domain.vo.ai.chat.ChatRequestVO;
import com.ruoyi.wms.domain.vo.ai.chat.ChatResponseVO;
import com.ruoyi.wms.domain.vo.ai.recommend.PurchaseOrderRecommendVO;
import com.ruoyi.wms.domain.vo.ai.recommend.SalesOrderRecommendVO;
import com.ruoyi.wms.service.ai.VoiceParseService;
import com.ruoyi.wms.service.ai.WmsChatService;
import com.ruoyi.wms.service.ai.WmsRecommendService;
import com.ruoyi.system.service.AiPointsService;
import com.ruoyi.wms.service.WmsPurchaseInItemService;
import com.ruoyi.wms.service.WmsPurchaseInService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * AI智能助手Controller
 *
 * @author ruoyi
 */
@Api(tags = "AI智能助手")
@RestController
@RequestMapping("/wms/ai")
public class WmsAiController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(WmsAiController.class);

    @Autowired
    private VoiceParseService voiceParseService;

    @Autowired
    private WmsRecommendService recommendService;

    @Autowired
    private WmsChatService chatService;

    @Autowired
    private AiPointsService aiPointsService;

    /**
     * 语音识别解析
     * <p>
     * 将语音文件转换为文本，然后使用LLM解析为结构化业务数据
     *
     * @param file 音频文件（支持mp3/wav/pcm格式）
     * @return 解析结果
     */
    @ApiOperation("语音识别解析")
    @PostMapping("/voiceParse")
    public AjaxResult voiceParse(
            @ApiParam("语音文件") @RequestParam("file") MultipartFile file) {

        // 校验文件
        if (file.isEmpty()) {
            return AjaxResult.error("请上传语音文件");
        }

        // 校验文件类型
        String filename = file.getOriginalFilename();
        if (filename == null ||
                (!filename.toLowerCase().endsWith(".mp3") &&
                        !filename.toLowerCase().endsWith(".wav") &&
                        !filename.toLowerCase().endsWith(".pcm"))) {
            return AjaxResult.error("只支持mp3/wav/pcm格式的音频文件");
        }

        // 校验文件大小 (10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            return AjaxResult.error("语音文件大小不能超过10MB");
        }

        try {
            // 点数检查：语音识别 + 文字解析
            Long tenantId = TenantHolder.getTenantId();
            if (!aiPointsService.tryConsumePoints(tenantId, AiPointsService.COST_VOICE, "voice")) {
                return AjaxResult.error("AI点数不足，语音识别需要" + AiPointsService.COST_VOICE + "点，请充值");
            }
            if (!aiPointsService.tryConsumePoints(tenantId, AiPointsService.COST_TEXT, "text")) {
                aiPointsService.refundPoints(tenantId, AiPointsService.COST_VOICE, "voice");
                return AjaxResult.error("AI点数不足，文字解析需要" + AiPointsService.COST_TEXT + "点，请充值");
            }

            log.info("开始处理语音文件: {}, 大小: {}", filename, file.getSize());

            // 调用语音解析服务
            VoiceParseResultVO result = voiceParseService.parseVoice(file);

            if (result.getSuccess()) {
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("语音解析失败", e);
            return AjaxResult.error("语音解析失败: " + e.getMessage());
        }
    }

    /**
     * 文本智能解析
     * <p>
     * 直接将文本解析为结构化业务数据（跳过语音识别步骤）
     * 可用于测试或直接文本输入场景
     *
     * @param text 输入文本
     * @return 解析结果
     */
    @ApiOperation("文本智能解析")
    @PostMapping("/textParse")
    public AjaxResult textParse(
            @ApiParam("输入文本") @RequestParam("text") String text) {

        if (text == null || text.trim().isEmpty()) {
            return AjaxResult.error("请输入文本");
        }

        try {
            // 点数检查：文字解析扣1点
            Long tenantId = TenantHolder.getTenantId();
            if (!aiPointsService.tryConsumePoints(tenantId, AiPointsService.COST_TEXT, "text")) {
                return AjaxResult.error("AI点数不足，文字解析需要" + AiPointsService.COST_TEXT + "点，请充值");
            }

            log.info("开始处理文本: {}", text);

            // 调用文本解析服务
            VoiceParseResultVO result = voiceParseService.parseText(text);

            if (result.getSuccess()) {
                return AjaxResult.success(result);
            } else {
                return AjaxResult.error(result.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("文本解析失败", e);
            return AjaxResult.error("文本解析失败: " + e.getMessage());
        }
    }

    /**
     * 获取采购订单推荐信息
     * <p>
     * 包括：常用供应商、常购商品、库存预警商品
     *
     * @return 推荐信息
     */
    @ApiOperation("获取采购订单推荐")
    @GetMapping("/recommend/purchase")
    public AjaxResult getPurchaseRecommend() {
        try {
            PurchaseOrderRecommendVO result = recommendService.getPurchaseOrderRecommend();
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("获取采购推荐失败", e);
            return AjaxResult.error("获取采购推荐失败: " + e.getMessage());
        }
    }

    /**
     * 获取销售订单推荐信息
     * <p>
     * 包括：常用客户、常购商品、库存状态
     *
     * @return 推荐信息
     */
    @ApiOperation("获取销售订单推荐")
    @GetMapping("/recommend/sales")
    public AjaxResult getSalesRecommend() {
        try {
            SalesOrderRecommendVO result = recommendService.getSalesOrderRecommend();
            return AjaxResult.success(result);
        } catch (Exception e) {
            log.error("获取销售推荐失败", e);
            return AjaxResult.error("获取销售推荐失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * <p>
     * 检查AI服务是否可用
     */
    @ApiOperation("AI服务健康检查")
    @GetMapping("/health")
    public AjaxResult health() {
        return AjaxResult.success("AI服务运行正常");
    }

    /**
     * 对话式助手接口
     * <p>
     * 支持多轮对话，引导式完成复杂业务操作
     *
     * @param request 对话请求
     * @return 对话响应
     */
    @ApiOperation("对话式助手")
    @PostMapping("/chat")
    public AjaxResult chat(@RequestBody ChatRequestVO request) {
        try {
            log.info("对话请求 - 会话ID: {}, 消息: {}",
                    request.getConversationId(), request.getMessage());

            // 设置当前用户ID
            if (request.getUserId() == null) {
                request.setUserId(getUserId());
            }

            // 点数检查：对话式助手每轮扣1点文字解析
            Long tenantId = TenantHolder.getTenantId();
            if (!aiPointsService.tryConsumePoints(tenantId, AiPointsService.COST_TEXT, "text")) {
                return AjaxResult.error("AI点数不足，文字解析需要" + AiPointsService.COST_TEXT + "点，请充值");
            }

            ChatResponseVO response = chatService.chat(request);
            return AjaxResult.success(response);
        } catch (Exception e) {
            log.error("对话处理失败", e);
            return AjaxResult.error("对话处理失败: " + e.getMessage());
        }
    }

    /**
     * 开始新对话
     * <p>
     * 指定操作类型，开始一个新的对话会话
     *
     * @param operationType 操作类型（create_purchase_order、create_sales_order等）
     * @return 对话响应
     */
    @ApiOperation("开始新对话")
    @PostMapping("/chat/start/{operationType}")
    public AjaxResult startChat(
            @ApiParam("操作类型") @PathVariable String operationType) {
        try {
            log.info("开始新对话 - 操作类型: {}", operationType);

            Long userId = getUserId();
            ChatResponseVO response = chatService.startChat(operationType, userId);
            return AjaxResult.success(response);
        } catch (Exception e) {
            log.error("开始对话失败", e);
            return AjaxResult.error("开始对话失败: " + e.getMessage());
        }
    }

    /**
     * 清除对话上下文
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @ApiOperation("清除对话上下文")
    @DeleteMapping("/chat/context/{conversationId}")
    public AjaxResult clearContext(
            @ApiParam("会话ID") @PathVariable String conversationId) {
        try {
            chatService.clearContext(conversationId);
            return AjaxResult.success("会话上下文已清除");
        } catch (Exception e) {
            log.error("清除会话上下文失败", e);
            return AjaxResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话上下文
     *
     * @param conversationId 会话ID
     * @return 会话上下文信息
     */
    @ApiOperation("获取对话上下文")
    @GetMapping("/chat/context/{conversationId}")
    public AjaxResult getContext(
            @ApiParam("会话ID") @PathVariable String conversationId) {
        try {
            ChatResponseVO response = chatService.getContext(conversationId);
            return AjaxResult.success(response);
        } catch (Exception e) {
            log.error("获取会话上下文失败", e);
            return AjaxResult.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 采购入库单保存回调（来自移动端）
     * <p>
     * 移动端保存成功后调用此接口，同步数据到聊天界面
     *
     * @param tempDataKey 临时数据 key
     * @param inId 新创建的入库单 ID（可选）
     * @return 操作结果
     */
    @ApiOperation("采购入库单保存回调")
    @PostMapping("/callback/purchase-in/saved")
    public AjaxResult purchaseInSavedCallback(
            @ApiParam("临时数据 key") @RequestParam String tempDataKey,
            @ApiParam("新创建的入库单 ID") @RequestParam(required = false) Long inId) {
        try {
            log.info("采购入库单保存回调 - tempDataKey: {}, inId: {}", tempDataKey, inId);

            // 从 Redis 获取临时数据
            RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
            Map<String, Object> orderData = redisCache.getCacheObject(tempDataKey);

            if (orderData == null) {
                return AjaxResult.error("未找到临时订单数据，可能已过期");
            }

            String conversationId = (String) orderData.get("conversationId");
            Long userId = (Long) orderData.get("userId");

            log.info("回调数据 - conversationId: {}, userId: {}", conversationId, userId);

            // 用于存储响应数据
            ChatResponseVO responseData = null;

            // 调用聊天服务，更新聊天卡片为完成状态
            if (conversationId != null && inId != null) {
                // 从数据库获取入库单信息
                WmsPurchaseInService purchaseInService =
                    SpringUtils.getBean(WmsPurchaseInService.class);
                if (purchaseInService != null) {
                    WmsPurchaseIn purchaseIn = purchaseInService.getById(inId);
                    if (purchaseIn != null) {
                        // 基于最新单据状态重新构建卡片（支持编辑页返回后二次刷新）
                        StringBuilder sb = new StringBuilder();
                        boolean approved = "1".equals(purchaseIn.getStatus());
                        sb.append("✅ 采购入库单已保存\n\n");
                        sb.append("入库单号：").append(purchaseIn.getInNo() != null ? purchaseIn.getInNo() : ("RK" + String.format("%012d", inId))).append("\n");

                        // 供应商信息
                        if (purchaseIn.getSupplierId() != null) {
                            WmsSupplierService supplierService =
                                SpringUtils.getBean(WmsSupplierService.class);
                            if (supplierService != null) {
                                WmsSupplier supplier =
                                    supplierService.getById(purchaseIn.getSupplierId());
                                if (supplier != null) {
                                    sb.append("供应商：").append(supplier.getSupplierName()).append("\n");
                                }
                            }
                        }

                        // 仓库信息
                        if (purchaseIn.getWarehouseId() != null) {
                            WmsWarehouseService warehouseService =
                                SpringUtils.getBean(WmsWarehouseService.class);
                            if (warehouseService != null) {
                                WmsWarehouse warehouse =
                                    warehouseService.getById(purchaseIn.getWarehouseId());
                                if (warehouse != null) {
                                    sb.append("仓库：").append(warehouse.getWarehouseName()).append("\n");
                                }
                            }
                        }

                        // 入库日期
                        if (purchaseIn.getInDate() != null) {
                            sb.append("入库日期：").append(purchaseIn.getInDate()).append("\n");
                        }

                        // 经办人
                        if (purchaseIn.getHandler() != null && !purchaseIn.getHandler().isEmpty()) {
                            sb.append("经办人：").append(purchaseIn.getHandler()).append("\n");
                        }

                        // 备注
                        if (purchaseIn.getRemark() != null && !purchaseIn.getRemark().isEmpty()) {
                            sb.append("备注：").append(purchaseIn.getRemark()).append("\n");
                        }

                        // 商品明细
                        WmsPurchaseInItemService itemService =
                            SpringUtils.getBean(WmsPurchaseInItemService.class);
                        if (itemService != null) {
                            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WmsPurchaseInItem> queryWrapper =
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
                            queryWrapper.eq(WmsPurchaseInItem::getInId, inId);
                            List<WmsPurchaseInItem> items = itemService.list(queryWrapper);
                            if (items != null && !items.isEmpty()) {
                                sb.append("\n商品明细：\n");
                                for (int i = 0; i < items.size(); i++) {
                                    WmsPurchaseInItem item = items.get(i);
                                    sb.append("\n").append(i + 1).append(". ").append(item.getSkuName());

                                    // 规格可选
                                    if (item.getSpecInfo() != null && !item.getSpecInfo().isEmpty()) {
                                        sb.append(" (").append(item.getSpecInfo()).append(")");
                                    }

                                    // 数量
                                    sb.append("\n   数量：").append(item.getQuantity() != null ? item.getQuantity() : 0);
                                    if (item.getUnitName() != null && !item.getUnitName().isEmpty()) {
                                        sb.append(item.getUnitName());
                                    } else {
                                        sb.append("件");
                                    }

                                    // 单价
                                    if (item.getPrice() != null) {
                                        sb.append("\n   单价：¥").append(item.getPrice());
                                    }

                                    // 金额
                                    if (item.getAmount() != null) {
                                        sb.append("\n   金额：¥").append(item.getAmount());
                                    }
                                }
                            }
                        }

                        // 总金额
                        if (purchaseIn.getTotalAmount() != null) {
                            sb.append("\n总金额：¥").append(purchaseIn.getTotalAmount()).append("\n");
                        }

                        // 关联采购订单状态：部分入库/全部入库
                        if (purchaseIn.getOrderId() != null) {
                            WmsPurchaseOrderService purchaseOrderService = SpringUtils.getBean(WmsPurchaseOrderService.class);
                            if (purchaseOrderService != null) {
                                try {
                                    WmsPurchaseOrderRespVO orderResp = purchaseOrderService.get(purchaseIn.getOrderId());
                                    if (orderResp != null) {
                                        String orderStateText = "状态已更新";
                                        if ("2".equals(orderResp.getOrderStatus())) {
                                            orderStateText = "部分入库";
                                        } else if ("3".equals(orderResp.getOrderStatus())) {
                                            orderStateText = "全部入库";
                                        }
                                        sb.append("\n采购订单状态：").append(orderStateText).append("\n");
                                    }
                                } catch (Exception ex) {
                                    log.warn("查询采购订单状态失败: orderId={}", purchaseIn.getOrderId(), ex);
                                }
                            }
                        }

                        if (approved) {
                            sb.append("\n审核状态：已审核通过\n库存已更新。\n");
                        } else {
                            sb.append("\n审核状态：待审核\n请点击下方“立即审核”按钮完成审核。\n");
                        }

                        // 构建响应
                        responseData = ChatResponseVO.builder()
                                .conversationId(conversationId)
                                .operationType("create_purchase_in")
                                .reply(sb.toString())
                                .isCompleted(approved)
                                .build();

                        // 跳转操作：回详情页查看最新数据
                        Map<String, Object> arguments = new HashMap<>();
                        arguments.put("id", inId.toString());
                        arguments.put("fromChat", true);

                        ChatResponseVO.FunctionCall action =
                            new ChatResponseVO.FunctionCall();
                        action.setName("edit_purchase_in");
                        action.setDescription("查看/编辑入库单");
                        action.setArguments(arguments);
                        responseData.setSuggestedAction(action);

                        // 快捷回复：未审核提供“立即审核+点击查看”；已审核仅“继续入库”
                        List<ChatResponseVO.QuickReply> quickReplies =
                            new ArrayList<>();
                        if (approved) {
                            quickReplies.add(new ChatResponseVO.QuickReply(
                                "继续入库", "continue_in", "primary"));
                        } else {
                            quickReplies.add(new ChatResponseVO.QuickReply(
                                "立即审核", "approve_in", "danger"));
                        }
                        responseData.setQuickReplies(quickReplies);

                        // 保存完成状态到 Redis，供前端轮询获取
                        String cacheKey = "ai_chat_completion_" + conversationId;
                        redisCache.setCacheObject(cacheKey, responseData, 10, TimeUnit.MINUTES);
                    }
                }
            }

            // 删除临时数据
            redisCache.deleteObject(tempDataKey);

            // 返回完整的入库单信息给前端
            if (responseData != null) {
                return AjaxResult.success("回调处理成功").put("purchaseInData", responseData);
            }
            return AjaxResult.success("回调处理成功");
        } catch (Exception e) {
            log.error("采购入库单保存回调失败", e);
            return AjaxResult.error("回调处理失败：" + e.getMessage());
        }
    }

    /**
     * 销售出库单保存回调（来自移动端）
     * <p>
     * 移动端保存成功后调用此接口，同步数据到聊天界面
     *
     * @param tempDataKey 临时数据 key
     * @param outId 新创建的出库单 ID（可选）
     * @return 操作结果
     */
    @ApiOperation("销售出库单保存回调")
    @PostMapping("/callback/sales-out/saved")
    public AjaxResult salesOutSavedCallback(
            @ApiParam("临时数据 key") @RequestParam String tempDataKey,
            @ApiParam("新创建的出库单 ID") @RequestParam(required = false) Long outId) {
        try {
            log.info("销售出库单保存回调 - tempDataKey: {}, outId: {}", tempDataKey, outId);

            // 从 Redis 获取临时数据
            RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
            Map<String, Object> orderData = redisCache.getCacheObject(tempDataKey);

            if (orderData == null) {
                return AjaxResult.error("未找到临时订单数据，可能已过期");
            }

            String conversationId = (String) orderData.get("conversationId");
            Long userId = (Long) orderData.get("userId");

            log.info("回调数据 - conversationId: {}, userId: {}", conversationId, userId);

            // 用于存储响应数据
            ChatResponseVO responseData = null;

            // 调用聊天服务，更新聊天卡片为完成状态
            if (conversationId != null && outId != null) {
                // 从数据库获取出库单信息
                com.ruoyi.wms.service.WmsSalesOutService salesOutService =
                    SpringUtils.getBean(com.ruoyi.wms.service.WmsSalesOutService.class);
                if (salesOutService != null) {
                    com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO salesOut = salesOutService.get(outId);
                    if (salesOut != null) {
                        // 构建完成消息
                        StringBuilder sb = new StringBuilder();
                        sb.append("✅ 销售出库单已保存成功！\n\n");
                        sb.append("📦 出库单号：").append(salesOut.getOutNo()).append("\n");

                        // 客户信息
                        if (salesOut.getCustomerId() != null) {
                            com.ruoyi.wms.service.WmsCustomerService customerService =
                                SpringUtils.getBean(com.ruoyi.wms.service.WmsCustomerService.class);
                            if (customerService != null) {
                                com.ruoyi.wms.domain.WmsCustomer customer =
                                    customerService.getById(salesOut.getCustomerId());
                                if (customer != null) {
                                    sb.append("👤 客户：").append(customer.getCustomerName()).append("\n");
                                }
                            }
                        }

                        // 仓库信息
                        if (salesOut.getWarehouseId() != null) {
                            WmsWarehouseService warehouseService =
                                SpringUtils.getBean(WmsWarehouseService.class);
                            if (warehouseService != null) {
                                WmsWarehouse warehouse =
                                    warehouseService.getById(salesOut.getWarehouseId());
                                if (warehouse != null) {
                                    sb.append("🏠 仓库：").append(warehouse.getWarehouseName()).append("\n");
                                }
                            }
                        }

                        // 出库日期
                        if (salesOut.getOutDate() != null) {
                            sb.append("📅 出库日期：").append(salesOut.getOutDate()).append("\n");
                        }

                        // 经办人
                        if (salesOut.getHandler() != null && !salesOut.getHandler().isEmpty()) {
                            sb.append("👤 经办人：").append(salesOut.getHandler()).append("\n");
                        }

                        // 商品明细
                        List<com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO> items = salesOutService.getItems(outId);
                        if (items != null && !items.isEmpty()) {
                            sb.append("\n📦 **商品明细：**\n");
                            for (int i = 0; i < items.size(); i++) {
                                com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO item = items.get(i);
                                sb.append("\n").append(i + 1).append(". **").append(item.getSkuName()).append("**");

                                // 规格可选
                                if (item.getSpecInfo() != null && !item.getSpecInfo().isEmpty()) {
                                    sb.append(" (").append(item.getSpecInfo()).append(")");
                                }

                                // 数量
                                sb.append("\n   数量：").append(item.getQuantity() != null ? item.getQuantity() : 0);
                                if (item.getUnitName() != null && !item.getUnitName().isEmpty()) {
                                    sb.append(item.getUnitName());
                                } else {
                                    sb.append("件");
                                }

                                // 单价
                                if (item.getPrice() != null) {
                                    sb.append("\n   单价：¥").append(item.getPrice());
                                }

                                // 金额
                                if (item.getAmount() != null) {
                                    sb.append("\n   金额：¥").append(item.getAmount());
                                }
                            }
                        }

                        // 总金额
                        if (salesOut.getTotalAmount() != null) {
                            sb.append("\n💰 总金额：¥").append(salesOut.getTotalAmount()).append("\n");
                        }

                        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
                        sb.append("数据已同步到系统，您可以在销售出库列表中查看详情。");

                        // 构建响应
                        responseData = ChatResponseVO.builder()
                                .conversationId(conversationId)
                                .operationType("create_sales_out")
                                .reply(sb.toString())
                                .isCompleted(true)
                                .build();

                        // 添加跳转到订单编辑页面的操作按钮
                        Map<String, Object> arguments = new HashMap<>();
                        arguments.put("id", outId.toString());

                        ChatResponseVO.FunctionCall action =
                            new ChatResponseVO.FunctionCall();
                        action.setName("edit_sales_out");
                        action.setDescription("查看/编辑出库单");
                        action.setArguments(arguments);
                        responseData.setSuggestedAction(action);

                        // 添加快捷回复选项：继续创建订单
                        List<ChatResponseVO.QuickReply> quickReplies =
                            new ArrayList<>();
                        quickReplies.add(new ChatResponseVO.QuickReply(
                            "创建下一条出库单", "创建销售出库", "primary"));
                        responseData.setQuickReplies(quickReplies);

                        // 保存完成状态到 Redis，供前端轮询获取
                        String cacheKey = "ai_chat_completion_" + conversationId;
                        redisCache.setCacheObject(cacheKey, responseData, 10, TimeUnit.MINUTES);
                    }
                }
            }

            // 删除临时数据
            redisCache.deleteObject(tempDataKey);

            // 返回完整的出库单信息给前端
            if (responseData != null) {
                return AjaxResult.success("回调处理成功").put("salesOutData", responseData);
            }
            return AjaxResult.success("回调处理成功");
        } catch (Exception e) {
            log.error("销售出库单保存回调失败", e);
            return AjaxResult.error("回调处理失败：" + e.getMessage());
        }
    }

    /**
     * 调试接口：查看当前用户权限
     */
    @ApiOperation("调试：查看当前用户权限")
    @GetMapping("/debug/perms")
    public AjaxResult debugPermissions() {
        try {
            LoginUser user = SecurityUtils.getLoginUser();
            log.info("=== 权限调试 ===");
            log.info("用户: {}", user != null ? user.getUsername() : "null");
            log.info("用户ID: {}", user != null ? user.getUserId() : "null");
            log.info("权限列表: {}", user != null ? user.getPermissions() : "null");
            log.info("是否有wms:ai:parse: {}", user != null ? user.getPermissions().contains("wms:ai:parse") : "null");
            log.info("是否有wms:ocr:upload: {}", user != null ? user.getPermissions().contains("wms:ocr:upload") : "null");
            log.info("是否有wms:ocr:batchRecognize: {}", user != null ? user.getPermissions().contains("wms:ocr:batchRecognize") : "null");
            log.info("================");

            return AjaxResult.success()
                .put("username", user != null ? user.getUsername() : "null")
                .put("userId", user != null ? user.getUserId() : "null")
                .put("permissions", user != null ? user.getPermissions() : "null")
                .put("has_ocr_recognize", user != null && user.getPermissions().contains("wms:ocr:recognize"))
                .put("has_ocr_upload", user != null && user.getPermissions().contains("wms:ocr:upload"))
                .put("has_ocr_batchRecognize", user != null && user.getPermissions().contains("wms:ocr:batchRecognize"))
                .put("has_ai_parse", user != null && user.getPermissions().contains("wms:ai:parse"));
        } catch (Exception e) {
            log.error("获取用户权限失败", e);
            return AjaxResult.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取临时订单数据（用于AI创建订单时的数据填充）
     *
     * @param tempDataKey 临时数据 key
     * @return 临时订单数据
     */
    @ApiOperation("获取临时订单数据")
    @GetMapping("/tempData")
    public AjaxResult getTempData(
            @ApiParam("临时数据 key") @RequestParam String tempDataKey) {
        try {
            log.info("获取临时订单数据 - tempDataKey: {}", tempDataKey);

            // 从 Redis 获取临时数据
            RedisCache redisCache = SpringUtils.getBean(RedisCache.class);
            Map<String, Object> orderData = redisCache.getCacheObject(tempDataKey);

            if (orderData == null) {
                return AjaxResult.error("未找到临时订单数据，可能已过期");
            }

            log.info("获取临时订单数据成功: {}", orderData);

            return AjaxResult.success("获取成功").put("data", orderData);
        } catch (Exception e) {
            log.error("获取临时订单数据失败", e);
            return AjaxResult.error("获取失败：" + e.getMessage());
        }
    }
}
