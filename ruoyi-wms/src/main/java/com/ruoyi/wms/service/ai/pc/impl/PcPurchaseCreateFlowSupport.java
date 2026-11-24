package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.ai.VoiceParseService;
import com.ruoyi.wms.service.ai.pc.vo.IdNameOption;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PcPurchaseCreateFlowSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";

    private final PcMultiBillChatServiceImpl owner;

    PcPurchaseCreateFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> continueCreateFlow(PcState state) {
        if (DOMAIN_PI.equals(state.getDomainType()) && shouldAskPurchaseInModeChoice(state)) {
            state.setStep("WAIT_PI_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请问你是想直接入库，还是根据采购订单入库？回复“1”直接入库，回复“2”根据采购订单入库。",
                    purchaseInModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

                if (DOMAIN_PR.equals(state.getDomainType()) && shouldAskPurchaseReturnModeChoice(state)) {
                    state.setStep("WAIT_PR_MODE_CHOICE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.conversationLifecycleSupport.buildReply(
                        state,
                        "请选择采购退货方式：回复“1”直接退货，或回复“2”根据采购入库单退货。",
                        purchaseReturnModeChoiceCard(),
                        owner.conversationLifecycleSupport.quickActions("1", "2")
                    );
                }
        if (DOMAIN_PI.equals(state.getDomainType()) && owner.usesLinkedSource(state)) {
            return owner.continuePurchaseInLinkedFlow(state);
        }

        if (DOMAIN_PR.equals(state.getDomainType()) && owner.usesLinkedSource(state)) {
            return owner.continuePurchaseReturnLinkedFlow(state);
        }

        boolean requireDirectPrice = false;
        if (StringUtils.isNotBlank(state.getItemsInput()) && state.getItemsInput().startsWith(VoiceParseService.IMAGE_RECOGNITION_PREFIX)) {
            state.setItemsInput(null);
        }
        if (state.getItemDrafts().isEmpty() && StringUtils.isNotBlank(state.getItemsInput())) {
            List<ItemDraft> reparsedDrafts = requireDirectPrice ? owner.itemParseSupport.parseItems(state.getItemsInput()) : owner.itemParseSupport.parseItemsAllowPriceOptional(state.getItemsInput());
            if (!reparsedDrafts.isEmpty()) {
                state.setItemDrafts(reparsedDrafts);
            }
        }

        if (state.getItemDrafts().isEmpty()) {
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.purchaseDraftSupport.buildIncompletePurchaseReply(state);
        }

        if (owner.itemParseSupport.hasIncompleteItemDrafts(state.getItemDrafts(), requireDirectPrice) && StringUtils.isNotBlank(state.getItemsInput())) {
            if (state.getItemsInput().startsWith(VoiceParseService.IMAGE_RECOGNITION_PREFIX)) {
                state.setItemsInput(null);
            } else {
                List<ItemDraft> reparsedDrafts = requireDirectPrice ? owner.itemParseSupport.parseItems(state.getItemsInput()) : owner.itemParseSupport.parseItemsAllowPriceOptional(state.getItemsInput());
                if (!reparsedDrafts.isEmpty() && !owner.itemParseSupport.hasIncompleteItemDrafts(reparsedDrafts, requireDirectPrice)) {
                    state.setItemDrafts(reparsedDrafts);
                }
            }
        }

        List<LineDraft> resolvedLines = new ArrayList<>();
        List<Integer> unmatchedIndexes = new ArrayList<>();
        for (int index = 0; index < state.getItemDrafts().size(); index++) {
            ItemDraft draft = state.getItemDrafts().get(index);
            if (draft == null || StringUtils.isBlank(draft.name) || draft.quantity == null || (requireDirectPrice && draft.price == null)) {
                state.resetFlow();
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        DOMAIN_PI.equals(state.getDomainType())
                    ? "采购入库信息不完整。直接入库至少需要【商品、数量】；供应商、仓库、单价可在确认卡里补齐；如果按采购单入库，请输入关联采购单号。"
                                : "当前采购信息缺少商品、数量或单价，请重新输入完整的一句话采购信息。",
                        owner.supplierTemplateCard(state.getDomainType()),
                        DOMAIN_PI.equals(state.getDomainType())
                    ? owner.conversationLifecycleSupport.quickActions("榴莲100箱，苹果230箱，香蕉200打", "苹果笔记本50台", "按采购单 PO202603190001 入库到一号仓库")
                                : owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元", "向宇树科技有限公司采购智能机器人50台，单价是80000元"));
            }
            resolvedLines.add(null);
            WmsGoodsSkuRespVO chosen = draft.forceSkuId == null ? null : owner.goodsSkuService.get(draft.forceSkuId);
            if (chosen == null) {
                if (StringUtils.isBlank(draft.name)) {
                    state.resetFlow();
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.conversationLifecycleSupport.buildReply(state,
                            "当前商品信息已失效，请重新输入完整的一句话采购信息。",
                            owner.supplierTemplateCard(state.getDomainType()),
                            owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元", "向宇树科技有限公司采购智能机器人50台，单价是80000元"));
                }
                List<WmsGoodsSkuRespVO> candidates = owner.goodsSkuService.searchByName(draft.name, 10);
                if (candidates == null || candidates.isEmpty()) {
                    unmatchedIndexes.add(index);
                    continue;
                }
                // 候选中恰好 1 个且命名完全相同 → 直接自动选中；
                // 其余情况（含恰好 1 个但名字不完全相同）统一进候选卡（带「以上都不是」）。
                WmsGoodsSkuRespVO exactNamed = owner.chooseExactlyNamedSingle(candidates, draft.name);
                if (exactNamed != null) {
                    chosen = exactNamed;
                } else {
                    state.setPreparedLines(resolvedLines);
                    owner.preparePendingGoods(state, draft, index, candidates.stream().map(owner::toGoodsOption).collect(Collectors.toList()));
                    state.setStep("WAIT_GOODS_CANDIDATE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.goodsFlowSupport.buildGoodsCandidateReply(state);
                }
            }

            resolvedLines.set(index, owner.buildDirectPurchaseLine(draft, chosen));
        }

        state.setPreparedLines(resolvedLines);

        if (!unmatchedIndexes.isEmpty()) {
            state.pendingUnmatchedDraftIndexes = new ArrayList<>(unmatchedIndexes);
            state.setStep("WAIT_UNMATCHED_GOODS_ACTION");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.goodsFlowSupport.buildUnmatchedGoodsReply(state);
        }

        if (state.getSupplierId() == null) {
            if (DOMAIN_PO.equals(state.getDomainType())) {
                owner.tryAutoSelectPurchaseSupplier(state);
                owner.conversationLifecycleSupport.saveState(state);
            } else if (DOMAIN_PI.equals(state.getDomainType()) || DOMAIN_PR.equals(state.getDomainType())) {
                if (StringUtils.isNotBlank(state.getSupplierInput())) {
                    owner.resolveSupplierByInput(state, state.getSupplierInput());
                    owner.conversationLifecycleSupport.saveState(state);
                    if ("WAIT_SUPPLIER_CANDIDATE".equals(state.getStep())
                            || "WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())
                            || "WAIT_SUPPLIER_CREATE_CONFIRM".equals(state.getStep())) {
                        return owner.buildSupplierFollowup(state);
                    }
                } else if (StringUtils.isBlank(state.getSupplierName())) {
                    // supplier optional for PI/PR short sentence; user can fill in confirm card
                }
            } else {
                if (StringUtils.isBlank(state.getSupplierInput())) {
                    state.setStep("IDLE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.purchaseDraftSupport.buildIncompletePurchaseReply(state);
                }
                owner.resolveSupplierByInput(state, state.getSupplierInput());
                owner.conversationLifecycleSupport.saveState(state);
                if ("WAIT_SUPPLIER_CANDIDATE".equals(state.getStep())
                        || "WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())
                        || "WAIT_SUPPLIER_CREATE_CONFIRM".equals(state.getStep())) {
                    return owner.buildSupplierFollowup(state);
                }
                if (state.getSupplierId() == null) {
                    state.setStep("WAIT_SUPPLIER_INPUT");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.conversationLifecycleSupport.buildReply(state,
                            "请先提供供应商信息。你可以直接输入供应商名称，我会智能匹配并在有歧义时让你选择。",
                            owner.supplierTemplateCard(),
                            owner.conversationLifecycleSupport.quickActions("云腾科技", "小米科技", "以上都不是"));
                }
            }
        }

        // 所有供应商解析逻辑都未匹配到时，回落到默认供应商
        if (state.getSupplierId() == null) {
            WmsSupplier defaultSupplier = owner.supplierService.getDefault();
            if (defaultSupplier != null) {
                state.setSupplierId(defaultSupplier.getId());
                state.setSupplierName(defaultSupplier.getSupplierName());
                state.setSupplierInput(defaultSupplier.getSupplierName());
            }
        }

        List<LineDraft> finalLines = owner.compactPreparedLines(resolvedLines);
        state.setPreparedLines(finalLines);
        state.setPreparedPoItems(DOMAIN_PO.equals(state.getDomainType()) ? owner.buildPurchaseOrderItemsFromLines(finalLines) : new ArrayList<>());
        state.setPreparedPiItems(DOMAIN_PI.equals(state.getDomainType()) ? owner.buildPurchaseInItemsFromLines(finalLines) : new ArrayList<>());
        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);

        String confirmSubject = DOMAIN_PI.equals(state.getDomainType())
                ? "采购入库信息"
                : (DOMAIN_PR.equals(state.getDomainType()) ? "采购退货信息" : "采购信息");

        return owner.conversationLifecycleSupport.buildReply(state,
            owner.buildConfirmPrompt(confirmSubject),
            owner.buildDirectPurchasePreviewCard(state, finalLines),
            owner.confirmCancelQuickActions());
    }

    Map<String, Object> handlePurchaseInModeChoice(PcState state, String input) {
        if (!DOMAIN_PI.equals(state.getDomainType())) {
            return owner.continueCreateFlow(state);
        }
        String normalized = StringUtils.defaultString(input).trim();
        boolean directMode = "1".equals(normalized) || normalized.contains("直接入库") || normalized.contains("直接");
        boolean linkedMode = "2".equals(normalized) || normalized.contains("采购订单") || normalized.contains("关联") || normalized.contains("根据");

        if (!directMode && !linkedMode) {
            state.setStep("WAIT_PI_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请回复“1”直接入库，或回复“2”根据采购订单入库。",
                    purchaseInModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

        if (directMode) {
            state.purchaseInMode = "DIRECT";
            state.linkOrderNo = null;
            state.linkOrderId = null;
            state.linkOrderType = null;
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueCreateFlow(state);
        }

        state.purchaseInMode = "LINKED";
        state.linkOrderNo = null;
        state.linkOrderId = null;
        state.linkOrderType = null;
        state.linkOrderCandidates = loadApprovedPurchaseOrderCandidates();
        state.linkOrderCandidatePage = 1;
        if (state.linkOrderCandidates.isEmpty()) {
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "当前没有可用的已审核采购订单，你可以先直接入库，或稍后再试。",
                    owner.linkOrderTemplateCard(state),
                    owner.conversationLifecycleSupport.quickActions("1", "取消")
            );
        }
        state.setStep("WAIT_LINK_ORDER_CANDIDATE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(
                state,
                "请选择要入库的已审核采购订单（单选，支持分页）。",
                owner.linkOrderCandidateCard(state),
                owner.conversationLifecycleSupport.quickActions("上一页", "下一页")
        );
    }

    Map<String, Object> handlePurchaseReturnModeChoice(PcState state, String input) {
        if (!DOMAIN_PR.equals(state.getDomainType())) {
            return owner.continueCreateFlow(state);
        }
        String normalized = StringUtils.defaultString(input).trim();
        boolean directMode = "1".equals(normalized) || normalized.contains("直接退货") || normalized.contains("直接");
        boolean linkedMode = "2".equals(normalized) || normalized.contains("入库单") || normalized.contains("根据") || normalized.contains("关联");

        if (!directMode && !linkedMode) {
            state.setStep("WAIT_PR_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请回复“1”直接退货，或回复“2”根据采购入库单退货。",
                    purchaseReturnModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

        if (directMode) {
            state.purchaseInMode = "DIRECT";
            state.linkOrderNo = null;
            state.linkOrderId = null;
            state.linkOrderType = null;
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueCreateFlow(state);
        }

        // 关联退货模式：返回来源单候选卡片（空列表），由用户在卡片内输入单号远程搜索
        state.purchaseInMode = "LINKED";
        state.linkOrderNo = null;
        state.linkOrderId = null;
        state.linkOrderType = null;
        state.linkOrderCandidates = new ArrayList<>();
        state.linkOrderCandidatePage = 1;
        state.setStep("WAIT_LINK_ORDER_CANDIDATE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(
                state,
                "请选择来源单，可按单号筛选并查看详情。",
                owner.linkOrderCandidateCard(state),
                owner.conversationLifecycleSupport.quickActions("直接退货", "取消")
        );
    }

    private boolean shouldAskPurchaseInModeChoice(PcState state) {
        if (!DOMAIN_PI.equals(state.getDomainType())) {
            return false;
        }
        if (StringUtils.isNotBlank(state.purchaseInMode)
                || StringUtils.isNotBlank(state.linkOrderNo)
                || state.linkOrderId != null
                || (state.itemDrafts != null && !state.itemDrafts.isEmpty())
                || StringUtils.isNotBlank(state.itemsInput)) {
            return false;
        }
        String intent = StringUtils.defaultString(state.lastIntentText).trim();
        return "采购入库".equals(intent)
                || "创建采购入库".equals(intent)
                || "新增采购入库".equals(intent)
                || "入库".equals(intent);
    }

    private boolean shouldAskPurchaseReturnModeChoice(PcState state) {
        if (!DOMAIN_PR.equals(state.getDomainType())) {
            return false;
        }
        if (StringUtils.isNotBlank(state.purchaseInMode)
                || StringUtils.isNotBlank(state.linkOrderNo)
                || state.linkOrderId != null
                || (state.itemDrafts != null && !state.itemDrafts.isEmpty())
                || StringUtils.isNotBlank(state.itemsInput)) {
            return false;
        }
        String intent = StringUtils.defaultString(state.lastIntentText).trim();
        return "采购退货".equals(intent)
                || "创建采购退货".equals(intent)
                || "新增采购退货".equals(intent)
                || "退货".equals(intent);
    }



    private List<IdNameOption> loadApprovedPurchaseOrderCandidates() {
        List<WmsPurchaseOrderRespVO> records = owner.purchaseOrderService.listAvailableForIn();
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<IdNameOption> result = new ArrayList<>();
        for (WmsPurchaseOrderRespVO record : records) {
            if (record == null || record.getId() == null || StringUtils.isBlank(record.getOrderNo())) {
                continue;
            }
            result.add(new IdNameOption(record.getId(), record.getOrderNo() + " / " + StringUtils.defaultIfBlank(record.getSupplierName(), "未知供应商")));
        }
        return result;
    }

    private List<IdNameOption> loadAvailablePurchaseInCandidates() {
        List<com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO> records = owner.purchaseInService.listAvailableForReturn();
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        List<IdNameOption> result = new ArrayList<>();
        for (com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO record : records) {
            if (record == null || record.getId() == null || StringUtils.isBlank(record.getInNo())) {
                continue;
            }
            result.add(new IdNameOption(record.getId(), record.getInNo() + " / " + StringUtils.defaultIfBlank(record.getSupplierName(), "未知供应商")));
        }
        return result;
    }

    private Map<String, Object> purchaseInModeChoiceCard() {
        Map<String, Object> card = new java.util.HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "采购入库方式选择");
        card.put("selectionMode", "single-list");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("1. 直接入库", "1"));
        options.add(option("2. 根据采购订单入库", "2"));
        card.put("options", options);
        return card;
    }

    private Map<String, Object> purchaseReturnModeChoiceCard() {
        Map<String, Object> card = new java.util.HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "采购退货方式选择");
        card.put("selectionMode", "single-list");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("1. 直接退货", "1"));
        options.add(option("2. 根据采购入库单退货", "2"));
        card.put("options", options);
        return card;
    }

    private Map<String, Object> option(String label, String value) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("label", label);
        map.put("value", value);
        return map;
    }
}

