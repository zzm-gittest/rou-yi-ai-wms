package com.ruoyi.wms.service.ai.pc.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.WmsAiChatLogService;
import com.ruoyi.wms.service.ai.analysis.AnalysisNlgService;
import com.ruoyi.wms.service.ai.analysis.AnalysisNluService;
import com.ruoyi.wms.service.ai.analysis.BusinessAnalysisDataService;
import com.ruoyi.wms.service.ai.analysis.DailyBriefingService;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.vo.ai.VoiceParseResultVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryRespVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecValueSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderPageReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutPageReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnItemRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnSaveReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckItemRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckItemSaveReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckSaveReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferItemRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferItemSaveReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferSaveReqVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitRespVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseSaveReqVO;
import com.ruoyi.wms.service.WmsCustomerService;
import com.ruoyi.wms.service.WmsGoodsCategoryService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsGoodsSpecService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsPurchaseInService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsPurchaseReturnService;
import com.ruoyi.wms.service.WmsSalesOrderService;
import com.ruoyi.wms.service.WmsSalesOutService;
import com.ruoyi.wms.service.WmsSalesReturnService;
import com.ruoyi.wms.service.WmsStockCheckService;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsStockTransferService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsUnitService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.service.ai.VoiceParseService;
import com.ruoyi.wms.service.ai.MultimodalLlmService;
import com.ruoyi.wms.service.ai.pc.PcMultiBillChatService;
import com.ruoyi.wms.service.ai.pc.vo.IdNameOption;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;
import com.ruoyi.wms.service.ai.pc.vo.PendingGoodsDraft;
import com.ruoyi.wms.service.ai.pc.vo.SkuDraft;
import com.ruoyi.wms.service.ai.pc.vo.SpecDraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * PC端AI助手V2（独立新链路）
 * 重点：采购语义识别、聊天内槽位补全、候选消歧、聊天内必填表单
 */
@Service
public class PcMultiBillChatServiceImpl implements PcMultiBillChatService {

    private static final Logger log = LoggerFactory.getLogger(PcMultiBillChatServiceImpl.class);

    static final String KEY_PREFIX = "wms:pcai:v2:";
    static final int TTL_MINUTES = 30;

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private static final String CHECK_TYPE_FULL = "FULL";
    private static final String CHECK_TYPE_SAMPLE = "SAMPLE";

    private static final Pattern SUPPLIER_AFTER_PREPOSITION = Pattern.compile("(?:向|从)(.+?)(?:采购|买|进货)");
    private static final Pattern SUPPLIER_BY_LABEL = Pattern.compile("供应商(?:是|为)?[:：]?(.+?)(?:[，,。；;\\s]|$)");
    private static final Pattern CUSTOMER_AFTER_SALE = Pattern.compile("(?:卖给|给)(.+?)(?:发货|出库|出售|卖|退回)");
    private static final Pattern CUSTOMER_BY_LABEL = Pattern.compile("客户(?:是|为)?[:：]?(.+?)(?:[，,。；;\\s]|$)");
    private static final Pattern LOCAL_PRICE_PATTERN = Pattern.compile("(?:单价|价格)(?:是|为)?\\s*(?<price>\\d+(?:\\.\\d+)?)\\s*元|(?<price2>\\d+(?:\\.\\d+)?)\\s*元");
    private static final Pattern LOCAL_QTY_PATTERN = Pattern.compile("(?<qty>\\d+(?:\\.\\d+)?)\\s*(?<unit>个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张)?");
    private static final Pattern LINK_ORDER_PATTERN = Pattern.compile("(?<no>[A-Z]{2,}[A-Z0-9-]{4,}|[A-Z]{1,}[0-9]{6,})");

    final RedisCache redisCache;
    final WmsPurchaseOrderService purchaseOrderService;
    final WmsPurchaseInService purchaseInService;
    final WmsPurchaseReturnService purchaseReturnService;
    final WmsSalesOrderService salesOrderService;
    final WmsSalesOutService salesOutService;
    final WmsSalesReturnService salesReturnService;
    final WmsStockCheckService stockCheckService;
    final WmsStockTransferService stockTransferService;
    final WmsStockService stockService;
    final WmsSupplierService supplierService;
    final WmsCustomerService customerService;
    final WmsWarehouseService warehouseService;
    final WmsGoodsService goodsService;
    final WmsGoodsSpecService goodsSpecService;
    final WmsGoodsSkuService goodsSkuService;
    final WmsUnitService unitService;
    final WmsGoodsCategoryService goodsCategoryService;
    final VoiceParseService voiceParseService;
    final WmsAiChatLogService aiChatLogService;
    final ObjectMapper objectMapper;
    private final PcPurchaseBillCreateProcessor purchaseBillCreateProcessor;
    private final PcSalesBillCreateProcessor salesBillCreateProcessor;
    private final PcStockBillCreateProcessor stockBillCreateProcessor;
    final PcGoodsFlowSupport goodsFlowSupport;
    private final PcPartyFlowSupport partyFlowSupport;
    private final PcLinkedSourceSupport linkedSourceSupport;
    private final PcQueryFlowSupport queryFlowSupport;
    private final PcSelectionFlowSupport selectionFlowSupport;
    private final PcFormFlowSupport formFlowSupport;
    private final PcCreateFlowSupport createFlowSupport;
    final PcItemParseSupport itemParseSupport;
    private final PcSemanticIntentSupport semanticIntentSupport;
    final PcCardResultSupport cardResultSupport;
    final PcCardBuilderSupport cardBuilderSupport;
    final PcConfirmActionSupport confirmActionSupport;
    private final PcActionRoutingSupport actionRoutingSupport;
    private final PcPurchaseCreateFlowSupport purchaseCreateFlowSupport;
    private final PcPendingStepSupport pendingStepSupport;
    private final PcPreviewFormSupport previewFormSupport;
    final PcAuditLogSupport auditLogSupport;
    final PcPurchaseDraftSupport purchaseDraftSupport;
    private final PcPreviewItemSupport previewItemSupport;
    final PcChatDispatchSupport chatDispatchSupport;
    private final PcPendingGoodsResolveSupport pendingGoodsResolveSupport;
    private final PcDraftFlowSupport draftFlowSupport;
    final PcConversationLifecycleSupport conversationLifecycleSupport;
    private final PcBusinessAnalysisSupport businessAnalysisSupport;
    private final MultimodalLlmService multimodalLlmService;

    public PcMultiBillChatServiceImpl(
            RedisCache redisCache,
            WmsPurchaseOrderService purchaseOrderService,
            WmsPurchaseInService purchaseInService,
            WmsPurchaseReturnService purchaseReturnService,
            WmsSalesOrderService salesOrderService,
            WmsSalesOutService salesOutService,
            WmsSalesReturnService salesReturnService,
            WmsStockCheckService stockCheckService,
            WmsStockTransferService stockTransferService,
            WmsStockService stockService,
            WmsSupplierService supplierService,
            WmsCustomerService customerService,
            WmsWarehouseService warehouseService,
            WmsGoodsService goodsService,
            WmsGoodsSpecService goodsSpecService,
            WmsGoodsSkuService goodsSkuService,
            WmsUnitService unitService,
            WmsGoodsCategoryService goodsCategoryService,
            VoiceParseService voiceParseService,
            WmsAiChatLogService aiChatLogService,
            ObjectMapper objectMapper,
            DailyBriefingService dailyBriefingService,
            AnalysisNluService analysisNluService,
            AnalysisNlgService analysisNlgService,
            BusinessAnalysisDataService businessAnalysisDataService,
            MultimodalLlmService multimodalLlmService
    ) {
        this.redisCache = redisCache;
        this.purchaseOrderService = purchaseOrderService;
        this.purchaseInService = purchaseInService;
        this.purchaseReturnService = purchaseReturnService;
        this.salesOrderService = salesOrderService;
        this.salesOutService = salesOutService;
        this.salesReturnService = salesReturnService;
        this.stockCheckService = stockCheckService;
        this.stockTransferService = stockTransferService;
        this.stockService = stockService;
        this.supplierService = supplierService;
        this.customerService = customerService;
        this.warehouseService = warehouseService;
        this.goodsService = goodsService;
        this.goodsSpecService = goodsSpecService;
        this.goodsSkuService = goodsSkuService;
        this.unitService = unitService;
        this.goodsCategoryService = goodsCategoryService;
        this.voiceParseService = voiceParseService;
        this.aiChatLogService = aiChatLogService;
        this.objectMapper = objectMapper;
        this.purchaseBillCreateProcessor = new PcPurchaseBillCreateProcessor(this);
        this.salesBillCreateProcessor = new PcSalesBillCreateProcessor(this);
        this.stockBillCreateProcessor = new PcStockBillCreateProcessor(this);
        this.goodsFlowSupport = new PcGoodsFlowSupport(this);
        this.partyFlowSupport = new PcPartyFlowSupport(this);
        this.linkedSourceSupport = new PcLinkedSourceSupport(this);
        this.queryFlowSupport = new PcQueryFlowSupport(this);
        this.selectionFlowSupport = new PcSelectionFlowSupport(this);
        this.formFlowSupport = new PcFormFlowSupport(this);
        this.createFlowSupport = new PcCreateFlowSupport(this);
        this.itemParseSupport = new PcItemParseSupport(this);
        this.semanticIntentSupport = new PcSemanticIntentSupport(this);
        this.cardResultSupport = new PcCardResultSupport();
        this.cardBuilderSupport = new PcCardBuilderSupport(this);
        this.confirmActionSupport = new PcConfirmActionSupport(this);
        this.actionRoutingSupport = new PcActionRoutingSupport(this);
        this.purchaseCreateFlowSupport = new PcPurchaseCreateFlowSupport(this);
        this.pendingStepSupport = new PcPendingStepSupport(this);
        this.previewFormSupport = new PcPreviewFormSupport(this);
        this.auditLogSupport = new PcAuditLogSupport(this);
        this.purchaseDraftSupport = new PcPurchaseDraftSupport(this);
        this.previewItemSupport = new PcPreviewItemSupport(this);
        this.chatDispatchSupport = new PcChatDispatchSupport(this);
        this.pendingGoodsResolveSupport = new PcPendingGoodsResolveSupport(this);
        this.draftFlowSupport = new PcDraftFlowSupport(this);
        this.conversationLifecycleSupport = new PcConversationLifecycleSupport(this);
        this.businessAnalysisSupport = new PcBusinessAnalysisSupport(this,
                dailyBriefingService, analysisNluService, analysisNlgService, businessAnalysisDataService);
        this.multimodalLlmService = multimodalLlmService;
    }

    @Override
    public Map<String, Object> start(Long userId) {
        return conversationLifecycleSupport.start(userId);
    }

    @Override
    public Map<String, Object> chat(Long userId, String conversationId, String message) {
        return conversationLifecycleSupport.chat(userId, conversationId, message);
    }

    @Override
    public Map<String, Object> imageChat(Long userId, String conversationId, MultipartFile imageFile) {
        long startTime = System.currentTimeMillis();
        log.info("PC imageChat start: conversationId={}, userId={}, fileName={}, fileSize={}",
            conversationId, userId, imageFile.getOriginalFilename(), imageFile.getSize());
        try {
            String recognitionResult = multimodalLlmService.recognizeImage(imageFile);
            log.info("PC imageChat vision result: conversationId={}, userId={}, recognitionResult={}",
                conversationId, userId, recognitionResult);

            String textForChat = "\u622A\u56FE\u8BC6\u522B\u7ED3\u679C: " + recognitionResult;
            log.info("PC imageChat dispatch to conversationLifecycleSupport.chat: conversationId={}, userId={}, textLen={}",
                conversationId, userId, textForChat.length());
            Map<String, Object> result = conversationLifecycleSupport.chat(userId, conversationId, textForChat);
            result.put("imageRecognition", recognitionResult);

            aiChatLogService.logAsync(conversationId, userId, "PC_IMAGE", "image",
                    "[\u622A\u56FE\u8BC6\u522B] " + imageFile.getOriginalFilename(), null,
                    result.getOrDefault("text", "").toString(), "success", null,
                    (int)(System.currentTimeMillis() - startTime));

            return result;
        } catch (Exception e) {
            log.error("PC\u622A\u56FE\u8BC6\u522B\u5931\u8D25", e);
            aiChatLogService.logAsync(conversationId, userId, "PC_IMAGE", "image",
                    "[\u622A\u56FE\u8BC6\u522B\u5931\u8D25] " + imageFile.getOriginalFilename(), null,
                    e.getMessage(), "failed", "image_recognition_error",
                    (int)(System.currentTimeMillis() - startTime));
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean willUseLlm(String conversationId, String message) {
        if (StringUtils.isBlank(message)) {
            return false;
        }
        String input = message.trim();
        if (input.startsWith("__ACTION__:")
                || input.startsWith("__SELECT__")
                || input.startsWith("__FORM__")) {
            return false;
        }
        PcState state = conversationLifecycleSupport.peekState(conversationId);
        if (state != null && businessAnalysisSupport.isInAnalysisFlow(state)) {
            return businessAnalysisSupport.willUseLlm(state, input);
        }
        String action = detectAction(input);
        if ("BUSINESS_ANALYSIS".equals(action)
                || "QUERY_BILL_CENTER".equals(action)
                || "QUERY_PO".equals(action)) {
            return false;
        }
        return true;
    }

    @Override
    public void clear(String conversationId) {
        conversationLifecycleSupport.clear(conversationId);
    }

    @Override
    public List<Map<String, Object>> listActions() {
        return conversationLifecycleSupport.listActions();
    }

    Map<String, Object> handleAnalysisEntry(PcState state) {
        return businessAnalysisSupport.handleAnalysisEntry(state);
    }

    Map<String, Object> handleAnalysisChat(PcState state, String input) {
        return businessAnalysisSupport.handleAnalysisChat(state, input);
    }

    boolean isInAnalysisFlow(PcState state) {
        return businessAnalysisSupport.isInAnalysisFlow(state);
    }

    Map<String, Object> handlePendingStep(PcState state, String input) {
        return pendingStepSupport.handlePendingStep(state, input);
    }

    Map<String, Object> handleSemanticCreateIntent(PcState state, String input, String action) {
        return semanticIntentSupport.handleSemanticCreateIntent(state, input, action);
    }

    Map<String, Object> handleActionCommand(PcState state, String command) {
        return actionRoutingSupport.handleActionCommand(state, command);
    }

    Map<String, Object> handleRouteCommand(PcState state, String input) {
        return actionRoutingSupport.handleRouteCommand(state, input);
    }

    Map<String, Object> buildDomainStartGuide(PcState state) {
        return cardBuilderSupport.buildDomainStartGuide(state);
    }

    Map<String, Object> buildAmbiguousReturnGuide(PcState state) {
        return cardBuilderSupport.buildAmbiguousReturnGuide(state);
    }

    Map<String, Object> tryRestartSemanticFlow(PcState state, String input) {
        return actionRoutingSupport.tryRestartSemanticFlow(state, input);
    }

    boolean isCreateAction(String action) {
        return actionRoutingSupport.isCreateAction(action);
    }

    String domainByAction(String action) {
        return actionRoutingSupport.domainByAction(action);
    }

    String actionByDomain(String domainType) {
        return actionRoutingSupport.actionByDomain(domainType);
    }

    boolean isPurchaseDomain(String domainType) {
        return DOMAIN_PO.equals(domainType) || DOMAIN_PI.equals(domainType) || DOMAIN_PR.equals(domainType);
    }

    Map<String, Object> resumeCurrentFlow(PcState state) {
        return isPurchaseDomain(state.getDomainType()) ? continueCreateFlow(state) : continueExtendedFlow(state);
    }

    Map<String, Object> continueCreateFlow(PcState state) {
        return purchaseCreateFlowSupport.continueCreateFlow(state);
    }

    Map<String, Object> handlePurchaseInModeChoice(PcState state, String input) {
        return purchaseCreateFlowSupport.handlePurchaseInModeChoice(state, input);
    }

    Map<String, Object> handlePurchaseReturnModeChoice(PcState state, String input) {
        return purchaseCreateFlowSupport.handlePurchaseReturnModeChoice(state, input);
    }

    Map<String, Object> handleSalesReturnModeChoice(PcState state, String input) {
        return createFlowSupport.handleSalesReturnModeChoice(state, input);
    }

    Map<String, Object> handleSalesOutModeChoice(PcState state, String input) {
        return createFlowSupport.handleSalesOutModeChoice(state, input);
    }

    void tryAutoSelectPurchaseSupplier(PcState state) {
        draftFlowSupport.tryAutoSelectPurchaseSupplier(state);
    }

    LineDraft buildDirectPurchaseLine(ItemDraft draft, WmsGoodsSkuRespVO sku) {
        return draftFlowSupport.buildDirectPurchaseLine(draft, sku);
    }

    List<LineDraft> compactPreparedLines(List<LineDraft> lines) {
        return draftFlowSupport.compactPreparedLines(lines);
    }

    Map<String, Object> buildDirectPurchasePreviewCard(PcState state, List<LineDraft> lines) {
        return previewItemSupport.buildDirectPurchasePreviewCard(state, lines);
    }

    List<WmsPurchaseOrderItemSaveReqVO> buildPurchaseOrderItemsFromLines(List<LineDraft> lines) {
        return previewItemSupport.buildPurchaseOrderItemsFromLines(lines);
    }

    Map<String, Object> continuePurchaseInLinkedFlow(PcState state) {
        return linkedSourceSupport.continuePurchaseInLinkedFlow(state);
    }

    Map<String, Object> continuePurchaseReturnLinkedFlow(PcState state) {
        return linkedSourceSupport.continuePurchaseReturnLinkedFlow(state);
    }

    Map<String, Object> continueSalesOutLinkedFlow(PcState state) {
        return linkedSourceSupport.continueSalesOutLinkedFlow(state);
    }

    Map<String, Object> continueExtendedFlow(PcState state) {
        return createFlowSupport.continueExtendedFlow(state);
    }

    Map<String, Object> executeCreate(PcState state) {
        String domainType = state.getDomainType();
        if (DOMAIN_PO.equals(domainType) || DOMAIN_PI.equals(domainType) || DOMAIN_PR.equals(domainType)) {
            return purchaseBillCreateProcessor.execute(state);
        }
        if (DOMAIN_SO.equals(domainType) || DOMAIN_SOUT.equals(domainType) || DOMAIN_SR.equals(domainType)) {
            return salesBillCreateProcessor.execute(state);
        }
        if (DOMAIN_SC.equals(domainType) || DOMAIN_ST.equals(domainType)) {
            return stockBillCreateProcessor.execute(state);
        }
        throw new ServiceException("未识别单据类型");
    }

    void fillTransferWarehousesFromText(PcState state, String input) {
        createFlowSupport.fillTransferWarehousesFromText(state, input);
    }

    boolean containsAllGoodsKeyword(String input) {
        return createFlowSupport.containsAllGoodsKeyword(input);
    }

    Map<String, Object> ensureTransferWarehouses(PcState state) {
        return createFlowSupport.ensureTransferWarehouses(state);
    }

    Map<String, Object> startStockCheckGuidedFlow(PcState state) {
        return createFlowSupport.startStockCheckGuidedFlow(state);
    }

    Map<String, Object> showStockCheckGoodsSelection(PcState state) {
        return createFlowSupport.showStockCheckGoodsSelection(state);
    }

    Map<String, Object> handleStockCheckGoodsSelect(PcState state, List<Long> skuIds) {
        return createFlowSupport.handleStockCheckGoodsSelect(state, skuIds);
    }

    Map<String, Object> startStockTransferGuidedFlow(PcState state) {
        return createFlowSupport.startStockTransferGuidedFlow(state);
    }

    Map<String, Object> showStockTransferGoodsSelection(PcState state) {
        return createFlowSupport.showStockTransferGoodsSelection(state);
    }

    Map<String, Object> handleStockTransferGoodsSelect(PcState state, List<Long> skuIds) {
        return createFlowSupport.handleStockTransferGoodsSelect(state, skuIds);
    }

    void resolveWarehouseForSlot(PcState state, String input, String slot) {
        createFlowSupport.resolveWarehouseForSlot(state, input, slot);
    }

    void applyWarehouseToSlot(PcState state, WmsWarehouseRespVO warehouse, String slot) {
        if ("OUT".equals(slot)) {
            state.outWarehouseId = warehouse.getId();
            state.outWarehouseName = warehouse.getWarehouseName();
        } else {
            state.inWarehouseId = warehouse.getId();
            state.inWarehouseName = warehouse.getWarehouseName();
        }
    }

    Map<String, Object> buildTransferWarehouseFollowup(PcState state, String slot) {
        return createFlowSupport.buildTransferWarehouseFollowup(state, slot);
    }

    Map<String, Object> resolveExtendedLines(PcState state, boolean requirePrice) {
        return createFlowSupport.resolveExtendedLines(state, requirePrice);
    }

    String defaultGoodsExample(String domainType) {
        return createFlowSupport.defaultGoodsExample(domainType);
    }

    Map<String, Object> buildExtendedPreviewCard(PcState state) {
        return previewItemSupport.buildExtendedPreviewCard(state);
    }

    List<String> confirmCancelQuickActions() {
        return confirmActionSupport.confirmCancelQuickActions();
    }

    boolean isConfirmInput(String input) {
        return confirmActionSupport.isConfirmInput(input);
    }

    boolean isCancelInput(String input) {
        return confirmActionSupport.isCancelInput(input);
    }

    String buildConfirmPrompt(String subject) {
        return confirmActionSupport.buildConfirmPrompt(subject);
    }

    String confirmHintText() {
        return confirmActionSupport.confirmHintText();
    }

    private List<WmsPurchaseReturnItemSaveReqVO> buildPurchaseReturnItems(PcState state) {
        return previewItemSupport.buildPurchaseReturnItems(state);
    }

    List<WmsPurchaseReturnItemSaveReqVO> buildPurchaseReturnItemsFromLines(List<LineDraft> lines) {
        return previewItemSupport.buildPurchaseReturnItemsFromLines(lines);
    }

    List<WmsPurchaseInItemSaveReqVO> buildPurchaseInItemsFromLines(List<LineDraft> lines) {
        return previewItemSupport.buildPurchaseInItemsFromLines(lines);
    }

    List<WmsSalesOrderItemSaveReqVO> buildSalesOrderItems(PcState state) {
        return previewItemSupport.buildSalesOrderItems(state);
    }

    List<WmsSalesOutItemSaveReqVO> buildSalesOutItems(PcState state) {
        return previewItemSupport.buildSalesOutItems(state);
    }

    List<WmsSalesReturnItemSaveReqVO> buildSalesReturnItems(PcState state) {
        return previewItemSupport.buildSalesReturnItems(state);
    }

    List<WmsStockCheckItemSaveReqVO> buildStockCheckItems(PcState state) {
        return previewItemSupport.buildStockCheckItems(state);
    }

    List<WmsStockTransferItemSaveReqVO> buildStockTransferItems(PcState state) {
        return previewItemSupport.buildStockTransferItems(state);
    }

    Map<String, Object> handleSelection(PcState state, String input) {
        return selectionFlowSupport.handleSelection(state, input);
    }

    Map<String, Object> handleFormSubmit(PcState state, String input) {
        return formFlowSupport.handleFormSubmit(state, input);
    }

    Map<String, Object> parseFormData(String input) {
        String json = input.substring("__FORM__".length());
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new ServiceException("表单数据格式错误");
        }
    }

    Map<String, Object> handleSupplierFormSubmit(PcState state, Map<String, Object> formData) {
        return partyFlowSupport.handleSupplierFormSubmit(state, formData);
    }

    Map<String, Object> handleWarehouseFormSubmit(PcState state, Map<String, Object> formData) {
        return partyFlowSupport.handleWarehouseFormSubmit(state, formData);
    }

    Map<String, Object> handleCustomerFormSubmit(PcState state, Map<String, Object> formData) {
        return partyFlowSupport.handleCustomerFormSubmit(state, formData);
    }

    Map<String, Object> handleGoodsBaseFormSubmit(PcState state, Map<String, Object> formData) {
        return goodsFlowSupport.handleGoodsBaseFormSubmit(state, formData);
    }

    Map<String, Object> handleGoodsSpecFormSubmit(PcState state, Map<String, Object> formData) {
        return goodsFlowSupport.handleGoodsSpecFormSubmit(state, formData);
    }

    Map<String, Object> handleGoodsSkuFormSubmit(PcState state, Map<String, Object> formData) {
        return goodsFlowSupport.handleGoodsSkuFormSubmit(state, formData);
    }

    Map<String, Object> handleQueryPurchaseOrder(PcState state, String input) {
        return queryFlowSupport.handleQueryPurchaseOrder(state, input);
    }

    Map<String, Object> handleQueryBillCenter(PcState state, String input) {
        return queryFlowSupport.handleQueryBillCenter(state, input);
    }

    Map<String, Object> handleQueryBillCenterEntry(PcState state) {
        return queryFlowSupport.handleQueryBillCenterEntry(state);
    }

    Map<String, Object> handleBillDetailById(PcState state, String domainType, Long id) {
        return queryFlowSupport.handleBillDetailById(state, domainType, id);
    }

    void resolveSupplierByInput(PcState state, String input) {
        partyFlowSupport.resolveSupplierByInput(state, input);
    }

    private Map<String, Object> buildRecentOrderReply(PcState state) {
        return queryFlowSupport.buildRecentOrderReply(state);
    }

    void resolveWarehouseByInput(PcState state, String input) {
        partyFlowSupport.resolveWarehouseByInput(state, input);
    }

    void resolveCustomerByInput(PcState state, String input, boolean allowCreate) {
        partyFlowSupport.resolveCustomerByInput(state, input, allowCreate);
    }

    Map<String, Object> buildSupplierFollowup(PcState state) {
        return partyFlowSupport.buildSupplierFollowup(state);
    }

    Map<String, Object> buildWarehouseFollowup(PcState state) {
        return partyFlowSupport.buildWarehouseFollowup(state);
    }

    Map<String, Object> buildCustomerFollowup(PcState state, boolean allowCreate) {
        return partyFlowSupport.buildCustomerFollowup(state, allowCreate);
    }

    Map<String, Object> domainChoiceCard() {
        return cardBuilderSupport.domainChoiceCard();
    }

    private Map<String, Object> supplierCandidateCard(PcState state) {
        return partyFlowSupport.supplierCandidateCard(state);
    }

    Map<String, Object> warehouseCandidateCard(PcState state) {
        return partyFlowSupport.warehouseCandidateCard(state);
    }

    private Map<String, Object> goodsCandidateCard(PcState state) {
        return goodsFlowSupport.goodsCandidateCard(state);
    }

    Map<String, Object> supplierTemplateCard() {
        return supplierTemplateCard(null);
    }

    Map<String, Object> supplierTemplateCard(String domainType) {
        return cardBuilderSupport.supplierTemplateCard(domainType);
    }

    Map<String, Object> goodsTemplateCard(String domainType) {
        return cardBuilderSupport.goodsTemplateCard(domainType);
    }

    Map<String, Object> supplierFormCard(PcState state) {
        return partyFlowSupport.supplierFormCard(state);
    }

    Map<String, Object> warehouseFormCard(PcState state) {
        return partyFlowSupport.warehouseFormCard(state);
    }

    Map<String, Object> warehouseTemplateCard() {
        return warehouseTemplateCard(null);
    }

    Map<String, Object> warehouseTemplateCard(String domainType) {
        return cardBuilderSupport.warehouseTemplateCard(domainType);
    }

    Map<String, Object> customerTemplateCard(String domainType, boolean allowCreate) {
        return partyFlowSupport.customerTemplateCard(domainType, allowCreate);
    }

    Map<String, Object> customerFormCard(PcState state) {
        return partyFlowSupport.customerFormCard(state);
    }

    WmsWarehouseRespVO findWarehouseById(Long id) {
        return warehouseService.selectList().stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
    }

    WmsSupplierRespVO findSupplierById(Long id) {
        return supplierService.selectList().stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
    }

    WmsCustomerRespVO findCustomerById(Long id) {
        return customerService.selectList().stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
    }

    void replacePendingGoodsBySku(PcState state, WmsGoodsSkuRespVO sku) {
        pendingGoodsResolveSupport.replacePendingGoodsBySku(state, sku);
    }

    List<Long> parseSelectedSkuIds(String value) {
        return pendingGoodsResolveSupport.parseSelectedSkuIds(value);
    }

    void replacePendingGoodsBySelectedSkus(PcState state, List<Long> skuIds) {
        pendingGoodsResolveSupport.replacePendingGoodsBySelectedSkus(state, skuIds);
    }

    Map<String, Object> handleSupplierCandidateText(PcState state, String input) {
        return partyFlowSupport.handleSupplierCandidateText(state, input);
    }

    Map<String, Object> handleSupplierCustomText(PcState state, String input) {
        return partyFlowSupport.handleSupplierCustomText(state, input);
    }

    Map<String, Object> handleSupplierCreateConfirm(PcState state, String input) {
        return draftFlowSupport.handleSupplierCreateConfirm(state, input);
    }

    Map<String, Object> handleWarehouseCandidateText(PcState state, String input) {
        return partyFlowSupport.handleWarehouseCandidateText(state, input);
    }

    Map<String, Object> handleWarehouseCustomText(PcState state, String input) {
        return partyFlowSupport.handleWarehouseCustomText(state, input);
    }

    Map<String, Object> handleCustomerCandidateText(PcState state, String input, boolean allowCreate) {
        return partyFlowSupport.handleCustomerCandidateText(state, input, allowCreate);
    }

    Map<String, Object> handleCustomerCustomText(PcState state, String input) {
        return partyFlowSupport.handleCustomerCustomText(state, input);
    }

    Map<String, Object> handleItemsText(PcState state, String input) {
        return draftFlowSupport.handleItemsText(state, input);
    }

    Map<String, Object> handleGoodsCandidateText(PcState state, String input) {
        return goodsFlowSupport.handleGoodsCandidateText(state, input);
    }

    WmsGoodsSkuRespVO chooseExactOrSingle(List<WmsGoodsSkuRespVO> list, String name) {
        return draftFlowSupport.chooseExactOrSingle(list, name);
    }

    String detectAction(String input) {
        return semanticIntentSupport.detectAction(input);
    }

    String resolveContextualCreateAction(PcState state, String input, String detectedAction) {
        return semanticIntentSupport.resolveContextualCreateAction(state, input, detectedAction);
    }

    private boolean looksLikeCommodityInput(String input) {
        return semanticIntentSupport.looksLikeCommodityInput(input);
    }

    private String detectDomainByText(String input) {
        return semanticIntentSupport.detectDomainByText(input);
    }

    private void fillSlotsFromSemanticResult(PcState state, String input, VoiceParseResultVO parsed) {
        semanticIntentSupport.fillSlotsFromSemanticResult(state, input, parsed);
    }

    Pattern linkOrderPattern() {
        return LINK_ORDER_PATTERN;
    }

    boolean supportsLinkedSource(String domainType) {
        return linkedSourceSupport.supportsLinkedSource(domainType);
    }

    boolean hasLinkedSource(PcState state) {
        return state != null && state.linkOrderId != null && StringUtils.isNotBlank(state.linkOrderType);
    }

    boolean usesLinkedSource(PcState state) {
        return linkedSourceSupport.usesLinkedSource(state);
    }

    boolean shouldAskLinkOrder(PcState state) {
        return linkedSourceSupport.shouldAskLinkOrder(state);
    }

    private boolean hasLinkedSourceIntent(String input) {
        return linkedSourceSupport.hasLinkedSourceIntent(input);
    }

    String linkOrderExample(String domainType) {
        return linkedSourceSupport.linkOrderExample(domainType);
    }

    Map<String, Object> linkOrderTemplateCard(PcState state) {
        return linkedSourceSupport.linkOrderTemplateCard(state);
    }

    Map<String, Object> resolveLinkedSource(PcState state) {
        return linkedSourceSupport.resolveLinkedSource(state);
    }

    private List<IdNameOption> searchLinkedSourceCandidates(PcState state) {
        return linkedSourceSupport.searchLinkedSourceCandidates(state);
    }

    Map<String, Object> buildLinkOrderFollowup(PcState state) {
        return linkedSourceSupport.buildLinkOrderFollowup(state);
    }

    private String buildLinkOrderCandidateText(PcState state) {
        return linkedSourceSupport.buildLinkOrderCandidateText(state);
    }

    Map<String, Object> linkOrderCandidateCard(PcState state) {
        return linkedSourceSupport.linkOrderCandidateCard(state);
    }

    Map<String, Object> handleLinkOrderCandidateText(PcState state, String input) {
        return linkedSourceSupport.handleLinkOrderCandidateText(state, input);
    }

    void applyLinkedSource(PcState state, Long sourceId) {
        linkedSourceSupport.applyLinkedSource(state, sourceId);
    }

    void applyLinkedPreviewItems(PcState state, Object rawItems) {
        linkedSourceSupport.applyLinkedPreviewItems(state, rawItems);
    }

    void applyStockCheckPreviewItems(PcState state, Object rawItems) {
        pendingGoodsResolveSupport.applyStockCheckPreviewItems(state, rawItems);
    }

    void applyDirectPurchasePreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applyDirectPurchasePreviewForm(state, formData);
    }

    void applyPurchaseInPreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applyPurchaseInPreviewForm(state, formData);
    }

    void applyPurchaseReturnPreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applyPurchaseReturnPreviewForm(state, formData);
    }

    void applySalesOrderPreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applySalesOrderPreviewForm(state, formData);
    }

    void applySalesOutPreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applySalesOutPreviewForm(state, formData);
    }

    void applySalesReturnPreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applySalesReturnPreviewForm(state, formData);
    }

    void applyStockTransferPreviewForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applyStockTransferPreviewForm(state, formData);
    }

    void applyLinkedPurchaseInWarehouseForm(PcState state, Map<String, Object> formData) {
        previewFormSupport.applyLinkedPurchaseInWarehouseForm(state, formData);
    }

    IdNameOption toWarehouseOption(WmsWarehouseRespVO vo) {
        return new IdNameOption(vo.getId(), vo.getWarehouseName());
    }

    IdNameOption toGoodsOption(WmsGoodsSkuRespVO vo) {
        return goodsFlowSupport.toGoodsOption(vo);
    }

    boolean containsRecentOrderQuery(String input) {
        return input != null && (input.contains("刚创建") || input.contains("刚才的单") || input.contains("刚刚的单") || input.contains("最近创建的订单"));
    }

    private Long resolveOrCreateUnit(String unitName) {
        return goodsFlowSupport.resolveOrCreateUnit(unitName);
    }

    private Long resolveCategoryId(Object rawCategoryId, String categoryName) {
        return goodsFlowSupport.resolveCategoryId(rawCategoryId, categoryName);
    }

    private String resolveDraftCategoryName(Long categoryId, String categoryName) {
        return goodsFlowSupport.resolveDraftCategoryName(categoryId, categoryName);
    }

    private WmsGoodsCategoryRespVO resolveDefaultCategory() {
        return goodsFlowSupport.resolveDefaultCategory();
    }

    void backfillPurchaseDraftsFromRawInput(PcState state, String input) {
        purchaseDraftSupport.backfillPurchaseDraftsFromRawInput(state, input);
    }

    private void ensureCustomerResolved(PcState state) {
        purchaseDraftSupport.ensureCustomerResolved(state);
    }

    void preparePendingGoods(PcState state, ItemDraft draft, int index, List<IdNameOption> candidates) {
        goodsFlowSupport.preparePendingGoods(state, draft, index, candidates);
    }

    private String resolveDraftUnitName(String unitName, PcState state) {
        return goodsFlowSupport.resolveDraftUnitName(unitName, state);
    }

    private BigDecimal resolvePendingPrice(PcState state) {
        return goodsFlowSupport.resolvePendingPrice(state);
    }

    private BigDecimal resolvePendingQuantity(PcState state) {
        return goodsFlowSupport.resolvePendingQuantity(state);
    }

    private Map<String, Object> createNoSpecGoodsAndContinue(PcState state, PendingGoodsDraft draft) {
        return goodsFlowSupport.createNoSpecGoodsAndContinue(state, draft);
    }

    private WmsGoodsSkuRespVO createNoSpecGoods(PcState state, PendingGoodsDraft draft) {
        return goodsFlowSupport.createNoSpecGoods(state, draft);
    }

    private Map<String, Object> createMultiSpecGoodsAndContinue(PcState state, PendingGoodsDraft draft) {
        return goodsFlowSupport.createMultiSpecGoodsAndContinue(state, draft);
    }

    private void persistGoodsSpecs(Long goodsId, List<SpecDraft> specs) {
        goodsFlowSupport.persistGoodsSpecs(goodsId, specs);
    }

    private void replacePendingGoodsBySkuList(PcState state, List<WmsGoodsSkuRespVO> createdSkus, List<SkuDraft> skuDrafts) {
        goodsFlowSupport.replacePendingGoodsBySkuList(state, createdSkus, skuDrafts);
    }

    void clearPendingGoodsState(PcState state) {
        goodsFlowSupport.clearPendingGoodsState(state);
    }

    private List<Map<String, Object>> buildSpecFormData(PendingGoodsDraft draft) {
        return goodsFlowSupport.buildSpecFormData(draft);
    }

    private Map<String, Object> specValueRow(String value) {
        return goodsFlowSupport.specValueRow(value);
    }

    private List<Map<String, Object>> buildSkuFormData(List<SkuDraft> skuDrafts) {
        return goodsFlowSupport.buildSkuFormData(skuDrafts);
    }

    private List<SpecDraft> parseSpecDrafts(Object raw) {
        return goodsFlowSupport.parseSpecDrafts(raw);
    }

    private List<SkuDraft> buildSkuDraftsFromSpecs(PendingGoodsDraft draft) {
        return goodsFlowSupport.buildSkuDraftsFromSpecs(draft);
    }

    private List<Map<String, String>> buildSpecCombinations(List<SpecDraft> specs, int index,
                                                            Map<String, String> current,
                                                            List<Map<String, String>> result) {
        return goodsFlowSupport.buildSpecCombinations(specs, index, current, result);
    }

    private List<SkuDraft> parseSkuDrafts(Object raw, PendingGoodsDraft draft) {
        return goodsFlowSupport.parseSkuDrafts(raw, draft);
    }

    Logger log() {
        return log;
    }

    static String str(Object obj) {
        return obj == null ? null : String.valueOf(obj).trim();
    }
}

