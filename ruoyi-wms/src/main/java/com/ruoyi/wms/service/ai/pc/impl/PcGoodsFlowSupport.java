package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryRespVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecValueSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitRespVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.IdNameOption;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;
import com.ruoyi.wms.service.ai.pc.vo.PendingGoodsDraft;
import com.ruoyi.wms.service.ai.pc.vo.SkuDraft;
import com.ruoyi.wms.service.ai.pc.vo.SpecDraft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PcGoodsFlowSupport {

    private static final int CANDIDATE_PAGE_SIZE = 3;

    private final PcMultiBillChatServiceImpl owner;

    PcGoodsFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handleGoodsBaseFormSubmit(PcState state, Map<String, Object> formData) {
        PendingGoodsDraft draft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
        draft.setGoodsName(StringUtils.isNotBlank(asString(formData.get("goodsName"))) ? asString(formData.get("goodsName")) : state.getPendingGoodsName());
        draft.setUnitName(resolveDraftUnitName(asString(formData.get("unitName")), state));
        draft.setCategoryId(resolveCategoryId(formData.get("categoryId"), asString(formData.get("categoryName"))));
        draft.setCategoryName(resolveDraftCategoryName(draft.getCategoryId(), asString(formData.get("categoryName"))));
        String action = asString(formData.get("_submitAction"));
        if (StringUtils.isBlank(draft.getGoodsName())) {
            return owner.conversationLifecycleSupport.buildReply(state, "商品名称不能为空。", goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        if (draft.getCategoryId() == null) {
            return owner.conversationLifecycleSupport.buildReply(state, "商品分类不能为空。", goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        if ("GO_MULTI_SPEC".equals(action)) {
            state.setStep("WAIT_GOODS_SPEC_FORM");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, "请补充多规格信息。", goodsSpecFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        return createNoSpecGoodsAndContinue(state, draft);
    }

    Map<String, Object> handleGoodsSpecFormSubmit(PcState state, Map<String, Object> formData) {
        PendingGoodsDraft draft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
        String action = asString(formData.get("_submitAction"));
        if ("BACK_TO_NO_SPEC".equals(action)) {
            return createNoSpecGoodsAndContinue(state, draft);
        }
        List<SpecDraft> specs = parseSpecDrafts(formData.get("specs"));
        if (specs.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state, "请至少录入一组规格和值。", goodsSpecFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        draft.setSpecs(specs);
        draft.setSkuDrafts(buildSkuDraftsFromSpecs(draft));
        state.setStep("WAIT_GOODS_SKU_FORM");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state, "请确认SKU列表。第一条已带入原始数量和采购单价，其余SKU请补全。", goodsSkuFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
    }

    Map<String, Object> handleGoodsSkuFormSubmit(PcState state, Map<String, Object> formData) {
        PendingGoodsDraft draft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
        String action = asString(formData.get("_submitAction"));
        if ("BACK_TO_NO_SPEC".equals(action)) {
            return createNoSpecGoodsAndContinue(state, draft);
        }
        List<SkuDraft> skuDrafts = parseSkuDrafts(formData.get("skuList"), draft);
        if (skuDrafts.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state, "请至少保留一条SKU。", goodsSkuFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        for (SkuDraft skuDraft : skuDrafts) {
            if (StringUtils.isBlank(skuDraft.getSkuName())) {
                return owner.conversationLifecycleSupport.buildReply(state, "SKU名称不能为空。", goodsSkuFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            }
            if (skuDraft.getQuantity() == null || skuDraft.getPrice() == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "请补全每条SKU的数量和采购单价。", goodsSkuFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            }
        }
        draft.setSkuDrafts(skuDrafts);
        return createMultiSpecGoodsAndContinue(state, draft);
    }

    Map<String, Object> handleGoodsCandidateText(PcState state, String input) {
        if ("上一页".equals(input)) {
            if (state.getPendingGoodsCandidatePage() > 1) {
                state.setPendingGoodsCandidatePage(state.getPendingGoodsCandidatePage() - 1);
                owner.conversationLifecycleSupport.saveState(state);
                return buildGoodsCandidateReply(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, "已经是第一页了。", goodsCandidateCard(state), goodsCandidateActions(state));
        }
        if ("下一页".equals(input)) {
            if (hasNextPage(state.getPendingGoodsCandidates(), state.getPendingGoodsCandidatePage())) {
                state.setPendingGoodsCandidatePage(state.getPendingGoodsCandidatePage() + 1);
                owner.conversationLifecycleSupport.saveState(state);
                return buildGoodsCandidateReply(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, "已经是最后一页了。你也可以直接输入关键词继续筛选。", goodsCandidateCard(state), goodsCandidateActions(state));
        }
        if (isOtherChoice(input)) {
            state.setStep("WAIT_GOODS_BASE_FORM");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, "以上候选都不合适，已切换为新增商品。", goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        IdNameOption selected = resolveOption(state.getPendingGoodsCandidates(), input);
        if (selected == null) {
            state.setPendingGoodsName(input);
            state.setPendingGoodsCandidatePage(1);
            List<WmsGoodsSkuRespVO> candidates = owner.goodsSkuService.searchByName(input, 10);
            if (candidates == null || candidates.isEmpty()) {
                PendingGoodsDraft pendingGoodsDraft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
                pendingGoodsDraft.setGoodsName(input);
                state.setStep("WAIT_GOODS_BASE_FORM");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "未找到匹配商品，已切换为新增商品。", goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
            }
            state.setPendingGoodsCandidates(candidates.stream().map(this::toGoodsOption).collect(Collectors.toList()));
            owner.conversationLifecycleSupport.saveState(state);
            return buildGoodsCandidateReply(state);
        }
        WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(selected.getId());
        if (sku == null) {
            throw new ServiceException("商品不存在");
        }
        owner.replacePendingGoodsBySku(state, sku);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> goodsCreateFormCard(PcState state) {
        PendingGoodsDraft draft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
        Map<String, Object> card = new HashMap<>();
        card.put("type", "GOODS_CREATE_FORM");
        card.put("title", "新增商品");
        Map<String, Object> formModel = new HashMap<>();
        formModel.put("goodsName", draft.getGoodsName());
        formModel.put("unitName", draft.getUnitName());
        formModel.put("categoryId", draft.getCategoryId());
        formModel.put("categoryName", draft.getCategoryName());
        card.put("formModel", formModel);
        return card;
    }

    Map<String, Object> goodsSpecFormCard(PcState state) {
        PendingGoodsDraft draft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
        Map<String, Object> card = new HashMap<>();
        card.put("type", "GOODS_SPEC_FORM");
        card.put("title", "多规格设置");
        Map<String, Object> formModel = new HashMap<>();
        formModel.put("goodsName", draft.getGoodsName());
        formModel.put("unitName", draft.getUnitName());
        formModel.put("specs", buildSpecFormData(draft));
        card.put("formModel", formModel);
        return card;
    }

    Map<String, Object> goodsSkuFormCard(PcState state) {
        PendingGoodsDraft draft = ensurePendingGoodsDraft(state, currentPendingDraft(state));
        Map<String, Object> card = new HashMap<>();
        card.put("type", "GOODS_SKU_FORM");
        card.put("title", "SKU列表确认");
        Map<String, Object> formModel = new HashMap<>();
        formModel.put("goodsName", draft.getGoodsName());
        formModel.put("unitName", draft.getUnitName());
        formModel.put("skuList", buildSkuFormData(draft.getSkuDrafts()));
        card.put("formModel", formModel);
        return card;
    }

    Map<String, Object> goodsCandidateCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", totalPages(state.getPendingGoodsCandidates()) == 1 ? "商品候选确认" : "商品有多个候选，请选择");
        card.put("selectionMode", "single-list");
        card.put("page", state.getPendingGoodsCandidatePage());
        card.put("totalPages", totalPages(state.getPendingGoodsCandidates()));
        card.put("totalCount", state.getPendingGoodsCandidates() == null ? 0 : state.getPendingGoodsCandidates().size());
        card.put("hasPrev", state.getPendingGoodsCandidatePage() > 1);
        card.put("hasNext", hasNextPage(state.getPendingGoodsCandidates(), state.getPendingGoodsCandidatePage()));
        List<Map<String, Object>> options = new ArrayList<>();
        for (IdNameOption option : currentPageOptions(state.getPendingGoodsCandidates(), state.getPendingGoodsCandidatePage())) {
            options.add(option(option.getName(), "__SELECT__GOODS:" + option.getId()));
        }
        card.put("options", options);
        card.put("otherOption", option("以上都不是", "__SELECT__GOODS:OTHER"));
        return card;
    }

    Map<String, Object> buildGoodsCandidateReply(PcState state) {
        return owner.conversationLifecycleSupport.buildReply(state, buildGoodsCandidateText(state), goodsCandidateCard(state), goodsCandidateActions(state));
    }

    String buildGoodsCandidateText(PcState state) {
        List<IdNameOption> options = currentPageOptions(state.getPendingGoodsCandidates(), state.getPendingGoodsCandidatePage());
        StringBuilder sb = new StringBuilder();
        boolean single = state.getPendingGoodsCandidates() != null && state.getPendingGoodsCandidates().size() == 1;
        sb.append("商品【").append(state.getPendingGoodsName()).append("】")
                .append(single ? "匹配到 1 个候选，请确认或选择：\n" : "匹配到多个候选，请选择：\n");
        int offset = (state.getPendingGoodsCandidatePage() - 1) * CANDIDATE_PAGE_SIZE;
        for (int index = 0; index < options.size(); index++) {
            sb.append(offset + index + 1).append(". ").append(options.get(index).getName()).append("\n");
        }
        if (hasNextPage(state.getPendingGoodsCandidates(), state.getPendingGoodsCandidatePage())) {
            sb.append("...\n");
        }
        sb.append("请在列表中勾选后点击确定，也可以翻页或选择“以上都不是”。\n");
        sb.append("当前第 ").append(state.getPendingGoodsCandidatePage()).append("/").append(totalPages(state.getPendingGoodsCandidates())).append(" 页");
        return sb.toString();
    }

    List<String> goodsCandidateActions(PcState state) {
        return new ArrayList<>();
    }

    void preparePendingGoods(PcState state, ItemDraft draft, int index, List<IdNameOption> candidates) {
        state.setPendingGoodsName(draft.name);
        state.setPendingGoodsUnit(draft.unit);
        state.setPendingGoodsCandidatePage(1);
        state.setPendingGoodsCandidates(candidates);
        state.setPendingGoodsDraftIndex(index);
        ensurePendingGoodsDraft(state, draft);
    }

    Map<String, Object> buildUnmatchedGoodsReply(PcState state) {
        return owner.conversationLifecycleSupport.buildReply(state, buildUnmatchedGoodsText(state), unmatchedGoodsCard(state), owner.conversationLifecycleSupport.quickActions());
    }

    String buildUnmatchedGoodsText(PcState state) {
        List<String> goodsNames = pendingUnmatchedGoodsNames(state);
        StringBuilder sb = new StringBuilder();
        sb.append("以下商品在系统中不存在，已进入第三步“无法匹配”处理：\n");
        for (int index = 0; index < goodsNames.size(); index++) {
            sb.append(index + 1).append(". ").append(goodsNames.get(index)).append("\n");
        }
        sb.append("你可以选择按原流程逐个新增，也可以直接批量新增为无规格商品。\n");
        sb.append("批量新增后，我会自动回到采购确认卡继续补充供应商、数量和单价。");
        return sb.toString();
    }

    Map<String, Object> unmatchedGoodsCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "未匹配商品处理");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(option("新增商品", "__ACTION__:UNMATCHED_GOODS_CREATE_ONE"));
        options.add(option("批量新增", "__ACTION__:BATCH_CREATE_UNMATCHED_GOODS"));
        card.put("options", options);
        return card;
    }

    List<String> pendingUnmatchedGoodsNames(PcState state) {
        List<String> names = new ArrayList<>();
        if (state.pendingUnmatchedDraftIndexes == null || state.pendingUnmatchedDraftIndexes.isEmpty()) {
            return names;
        }
        for (Integer index : state.pendingUnmatchedDraftIndexes) {
            if (index == null || index < 0 || index >= state.getItemDrafts().size()) {
                continue;
            }
            ItemDraft draft = state.getItemDrafts().get(index);
            if (draft != null && StringUtils.isNotBlank(draft.name)) {
                names.add(draft.name);
            }
        }
        return names;
    }

    Map<String, Object> startSingleUnmatchedGoodsCreate(PcState state) {
        Integer firstIndex = firstPendingUnmatchedIndex(state);
        if (firstIndex == null) {
            state.pendingUnmatchedDraftIndexes = new ArrayList<>();
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }
        ItemDraft draft = state.getItemDrafts().get(firstIndex);
        preparePendingGoods(state, draft, firstIndex, new ArrayList<>());
        state.pendingUnmatchedDraftIndexes.remove(firstIndex);
        state.setStep("WAIT_GOODS_BASE_FORM");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state, "商品【" + draft.name + "】不存在，请按原流程新增商品。", goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
    }

    Map<String, Object> batchCreateUnmatchedGoods(PcState state) {
        List<Integer> indexes = new ArrayList<>();
        if (state.pendingUnmatchedDraftIndexes != null) {
            indexes.addAll(state.pendingUnmatchedDraftIndexes);
        }
        indexes.sort(Collections.reverseOrder());
        for (Integer index : indexes) {
            if (index == null || index < 0 || index >= state.getItemDrafts().size()) {
                continue;
            }
            ItemDraft draft = state.getItemDrafts().get(index);
            if (draft == null || StringUtils.isBlank(draft.name)) {
                continue;
            }
            preparePendingGoods(state, draft, index, new ArrayList<>());
            PendingGoodsDraft pendingDraft = ensurePendingGoodsDraft(state, draft);
            WmsGoodsSkuRespVO createdSku = createNoSpecGoods(state, pendingDraft);
            owner.replacePendingGoodsBySku(state, createdSku);
        }
        state.pendingUnmatchedDraftIndexes = new ArrayList<>();
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Integer firstPendingUnmatchedIndex(PcState state) {
        if (state.pendingUnmatchedDraftIndexes == null || state.pendingUnmatchedDraftIndexes.isEmpty()) {
            return null;
        }
        for (Integer index : state.pendingUnmatchedDraftIndexes) {
            if (index != null && index >= 0 && index < state.getItemDrafts().size()) {
                return index;
            }
        }
        return null;
    }

    ItemDraft currentPendingDraft(PcState state) {
        if (state.getPendingGoodsDraftIndex() != null
                && state.getPendingGoodsDraftIndex() >= 0
                && state.getPendingGoodsDraftIndex() < state.getItemDrafts().size()) {
            return state.getItemDrafts().get(state.getPendingGoodsDraftIndex());
        }
        return null;
    }

    PendingGoodsDraft ensurePendingGoodsDraft(PcState state, ItemDraft itemDraft) {
        if (state.getPendingGoodsDraft() == null) {
            PendingGoodsDraft draft = new PendingGoodsDraft();
            draft.setGoodsName(state.getPendingGoodsName());
            draft.setUnitName(resolveDraftUnitName(state.getPendingGoodsUnit(), state));
            WmsGoodsCategoryRespVO defaultCategory = resolveDefaultCategory();
            if (defaultCategory != null) {
                draft.setCategoryId(defaultCategory.getId());
                draft.setCategoryName(defaultCategory.getCategoryName());
            }
            draft.setDefaultQuantity(resolvePendingQuantity(state));
            draft.setDefaultPrice(resolvePendingPrice(state));
            state.setPendingGoodsDraft(draft);
        }
        if (itemDraft != null) {
            if (StringUtils.isBlank(state.getPendingGoodsDraft().getGoodsName())) {
                state.getPendingGoodsDraft().setGoodsName(itemDraft.name);
            }
            if (StringUtils.isBlank(state.getPendingGoodsDraft().getUnitName())) {
                state.getPendingGoodsDraft().setUnitName(resolveDraftUnitName(itemDraft.unit, state));
            }
            if (state.getPendingGoodsDraft().getDefaultQuantity() == null) {
                state.getPendingGoodsDraft().setDefaultQuantity(itemDraft.quantity);
            }
            if (state.getPendingGoodsDraft().getDefaultPrice() == null) {
                state.getPendingGoodsDraft().setDefaultPrice(itemDraft.price);
            }
        }
        return state.getPendingGoodsDraft();
    }

    String resolveDraftUnitName(String unitName, PcState state) {
        if (StringUtils.isNotBlank(unitName)) {
            return unitName;
        }
        if (StringUtils.isNotBlank(state.getPendingGoodsUnit())) {
            return state.getPendingGoodsUnit();
        }
        return "个";
    }

    BigDecimal resolvePendingPrice(PcState state) {
        for (ItemDraft draft : state.getItemDrafts()) {
            if (state.getPendingGoodsName() != null && state.getPendingGoodsName().equals(draft.name) && draft.price != null) {
                return draft.price;
            }
        }
        return BigDecimal.ZERO;
    }

    BigDecimal resolvePendingQuantity(PcState state) {
        for (ItemDraft draft : state.getItemDrafts()) {
            if (state.getPendingGoodsName() != null && state.getPendingGoodsName().equals(draft.name) && draft.quantity != null) {
                return draft.quantity;
            }
        }
        return null;
    }

    Map<String, Object> createNoSpecGoodsAndContinue(PcState state, PendingGoodsDraft draft) {
        WmsGoodsSkuRespVO createdSku = createNoSpecGoods(state, draft);
        owner.replacePendingGoodsBySku(state, createdSku);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    WmsGoodsSkuRespVO createNoSpecGoods(PcState state, PendingGoodsDraft draft) {
        Long unitId = resolveOrCreateUnit(draft.getUnitName());
        Long categoryId = draft.getCategoryId();
        if (categoryId == null) {
            throw new ServiceException("缺少可用商品分类，无法新增商品");
        }

        WmsGoodsSkuSaveReqVO skuReq = new WmsGoodsSkuSaveReqVO();
        skuReq.setSkuName(draft.getGoodsName());
        skuReq.setUnitId(unitId);
        skuReq.setPurchasePrice(draft.getDefaultPrice() == null ? BigDecimal.ZERO : draft.getDefaultPrice());
        skuReq.setStatus("0");
        skuReq.setMinStock(0);
        skuReq.setMaxStock(999999);

        WmsGoodsSaveReqVO goodsReq = new WmsGoodsSaveReqVO();
        goodsReq.setGoodsName(draft.getGoodsName());
        goodsReq.setCategoryId(String.valueOf(categoryId));
        goodsReq.setUnitId(unitId);
        goodsReq.setHasSpec("0");
        goodsReq.setStatus("0");
        goodsReq.setSyncSkuList(true);
        List<WmsGoodsSkuSaveReqVO> skuList = new ArrayList<>();
        skuList.add(skuReq);
        goodsReq.setSkuList(skuList);

        Long goodsId = owner.goodsService.create(goodsReq);
        WmsGoodsSkuRespVO createdSku = owner.goodsSkuService.listByGoodsId(goodsId).stream().findFirst().orElse(null);
        if (createdSku == null) {
            throw new ServiceException("商品已创建，但未生成SKU");
        }
        return createdSku;
    }

    Map<String, Object> createMultiSpecGoodsAndContinue(PcState state, PendingGoodsDraft draft) {
        Long unitId = resolveOrCreateUnit(draft.getUnitName());
        Long categoryId = draft.getCategoryId();
        if (categoryId == null) {
            throw new ServiceException("缺少可用商品分类，无法新增商品");
        }

        WmsGoodsSaveReqVO goodsReq = new WmsGoodsSaveReqVO();
        goodsReq.setGoodsName(draft.getGoodsName());
        goodsReq.setCategoryId(String.valueOf(categoryId));
        goodsReq.setUnitId(unitId);
        goodsReq.setHasSpec("1");
        goodsReq.setStatus("0");
        goodsReq.setSyncSkuList(true);
        List<WmsGoodsSkuSaveReqVO> skuList = new ArrayList<>();
        for (SkuDraft skuDraft : draft.getSkuDrafts()) {
            WmsGoodsSkuSaveReqVO skuReq = new WmsGoodsSkuSaveReqVO();
            skuReq.setSkuName(skuDraft.getSkuName());
            skuReq.setUnitId(unitId);
            skuReq.setSpecInfo(skuDraft.getSpecInfo());
            skuReq.setPurchasePrice(skuDraft.getPrice());
            skuReq.setStatus("0");
            skuReq.setMinStock(0);
            skuReq.setMaxStock(999999);
            skuList.add(skuReq);
        }
        goodsReq.setSkuList(skuList);

        Long goodsId = owner.goodsService.create(goodsReq);
        persistGoodsSpecs(goodsId, draft.getSpecs());
        List<WmsGoodsSkuRespVO> createdSkus = owner.goodsSkuService.listByGoodsId(goodsId);
        replacePendingGoodsBySkuList(state, createdSkus, draft.getSkuDrafts());
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    void persistGoodsSpecs(Long goodsId, List<SpecDraft> specs) {
        int sort = 0;
        for (SpecDraft spec : specs) {
            WmsGoodsSpecSaveReqVO req = new WmsGoodsSpecSaveReqVO();
            req.setGoodsId(goodsId);
            req.setSpecName(spec.getSpecName());
            req.setSort(sort++);
            List<WmsGoodsSpecValueSaveReqVO> values = new ArrayList<>();
            int valueSort = 0;
            for (String value : spec.getValues()) {
                WmsGoodsSpecValueSaveReqVO valueReq = new WmsGoodsSpecValueSaveReqVO();
                valueReq.setSpecValue(value);
                valueReq.setSort(valueSort++);
                values.add(valueReq);
            }
            req.setValues(values);
            owner.goodsSpecService.create(req);
        }
    }

    void replacePendingGoodsBySkuList(PcState state, List<WmsGoodsSkuRespVO> createdSkus, List<SkuDraft> skuDrafts) {
        if (state.getPendingGoodsDraftIndex() == null || state.getPendingGoodsDraftIndex() < 0 || state.getPendingGoodsDraftIndex() >= state.getItemDrafts().size()) {
            throw new ServiceException("待处理商品上下文丢失");
        }
        List<ItemDraft> replacements = new ArrayList<>();
        for (int index = 0; index < skuDrafts.size(); index++) {
            WmsGoodsSkuRespVO created = index < createdSkus.size() ? createdSkus.get(index) : null;
            if (created == null) {
                throw new ServiceException("多规格商品已创建，但SKU列表不完整");
            }
            SkuDraft skuDraft = skuDrafts.get(index);
            ItemDraft itemDraft = new ItemDraft();
            itemDraft.name = StringUtils.isNotBlank(created.getGoodsName()) ? created.getGoodsName() : created.getSkuName();
            itemDraft.unit = StringUtils.isNotBlank(skuDraft.getUnitName()) ? skuDraft.getUnitName() : created.getUnitName();
            itemDraft.quantity = skuDraft.getQuantity();
            itemDraft.price = skuDraft.getPrice();
            itemDraft.forceSkuId = created.getId();
            replacements.add(itemDraft);
        }
        int index = state.getPendingGoodsDraftIndex();
        state.getItemDrafts().remove((int) index);
        state.getItemDrafts().addAll(index, replacements);
        clearPendingGoodsState(state);
    }

    void clearPendingGoodsState(PcState state) {
        state.setPendingGoodsName(null);
        state.setPendingGoodsUnit(null);
        state.setPendingGoodsCandidates(new ArrayList<>());
        state.setPendingGoodsCandidatePage(1);
        state.setPendingGoodsDraftIndex(-1);
        state.setPendingGoodsDraft(null);
    }

    List<Map<String, Object>> buildSpecFormData(PendingGoodsDraft draft) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (draft.getSpecs() == null || draft.getSpecs().isEmpty()) {
            Map<String, Object> row = new HashMap<>();
            row.put("specName", "");
            List<Map<String, Object>> values = new ArrayList<>();
            values.add(specValueRow(""));
            row.put("values", values);
            rows.add(row);
            return rows;
        }
        for (SpecDraft spec : draft.getSpecs()) {
            Map<String, Object> row = new HashMap<>();
            row.put("specName", spec.getSpecName());
            List<Map<String, Object>> values = new ArrayList<>();
            for (String value : spec.getValues()) {
                values.add(specValueRow(value));
            }
            row.put("values", values);
            rows.add(row);
        }
        return rows;
    }

    Map<String, Object> specValueRow(String value) {
        Map<String, Object> row = new HashMap<>();
        row.put("specValue", value);
        return row;
    }

    List<Map<String, Object>> buildSkuFormData(List<SkuDraft> skuDrafts) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SkuDraft skuDraft : skuDrafts) {
            Map<String, Object> row = new HashMap<>();
            row.put("skuName", skuDraft.getSkuName());
            row.put("specInfo", skuDraft.getSpecInfo());
            row.put("unitName", skuDraft.getUnitName());
            row.put("quantity", skuDraft.getQuantity());
            row.put("price", skuDraft.getPrice());
            rows.add(row);
        }
        return rows;
    }

    List<SpecDraft> parseSpecDrafts(Object raw) {
        List<SpecDraft> specs = new ArrayList<>();
        if (!(raw instanceof List)) {
            return specs;
        }
        for (Object item : (List<?>) raw) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<?, ?> row = (Map<?, ?>) item;
            String specName = asString(row.get("specName"));
            if (StringUtils.isBlank(specName)) {
                continue;
            }
            List<String> values = new ArrayList<>();
            Object valueObject = row.get("values");
            if (valueObject instanceof List) {
                for (Object valueItem : (List<?>) valueObject) {
                    if (valueItem instanceof Map) {
                        String specValue = asString(((Map<?, ?>) valueItem).get("specValue"));
                        if (StringUtils.isNotBlank(specValue)) {
                            values.add(specValue);
                        }
                    }
                }
            }
            if (!values.isEmpty()) {
                SpecDraft specDraft = new SpecDraft();
                specDraft.setSpecName(specName);
                specDraft.setValues(values);
                specs.add(specDraft);
            }
        }
        return specs;
    }

    List<SkuDraft> buildSkuDraftsFromSpecs(PendingGoodsDraft draft) {
        List<SkuDraft> skuDrafts = new ArrayList<>();
        List<Map<String, String>> combinations = buildSpecCombinations(draft.getSpecs(), 0, new HashMap<>(), new ArrayList<>());
        for (int index = 0; index < combinations.size(); index++) {
            Map<String, String> combination = combinations.get(index);
            SkuDraft skuDraft = new SkuDraft();
            skuDraft.setUnitName(draft.getUnitName());
            try {
                skuDraft.setSpecInfo(owner.objectMapper.writeValueAsString(combination));
            } catch (Exception exception) {
                throw new ServiceException("规格组合生成失败");
            }
            String specText = combination.entrySet().stream().map(entry -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(" "));
            skuDraft.setSkuName((draft.getGoodsName() + " " + specText).trim());
            skuDraft.setQuantity(index == 0 ? (draft.getDefaultQuantity() == null ? BigDecimal.ZERO : draft.getDefaultQuantity()) : BigDecimal.ZERO);
            skuDraft.setPrice(index == 0 ? (draft.getDefaultPrice() == null ? BigDecimal.ZERO : draft.getDefaultPrice()) : BigDecimal.ZERO);
            skuDrafts.add(skuDraft);
        }
        return skuDrafts;
    }

    List<Map<String, String>> buildSpecCombinations(List<SpecDraft> specs, int index, Map<String, String> current, List<Map<String, String>> result) {
        if (specs == null || specs.isEmpty()) {
            return result;
        }
        if (index >= specs.size()) {
            result.add(new HashMap<>(current));
            return result;
        }
        SpecDraft spec = specs.get(index);
        for (String value : spec.getValues()) {
            current.put(spec.getSpecName(), value);
            buildSpecCombinations(specs, index + 1, current, result);
            current.remove(spec.getSpecName());
        }
        return result;
    }

    List<SkuDraft> parseSkuDrafts(Object raw, PendingGoodsDraft draft) {
        List<SkuDraft> skuDrafts = new ArrayList<>();
        if (!(raw instanceof List)) {
            return skuDrafts;
        }
        for (Object item : (List<?>) raw) {
            if (!(item instanceof Map)) {
                continue;
            }
            Map<?, ?> row = (Map<?, ?>) item;
            SkuDraft skuDraft = new SkuDraft();
            skuDraft.setSkuName(asString(row.get("skuName")));
            skuDraft.setSpecInfo(asString(row.get("specInfo")));
            skuDraft.setUnitName(StringUtils.isNotBlank(asString(row.get("unitName"))) ? asString(row.get("unitName")) : draft.getUnitName());
            skuDraft.setQuantity(toBigDecimal(row.get("quantity")));
            skuDraft.setPrice(toBigDecimal(row.get("price")));
            skuDrafts.add(skuDraft);
        }
        return skuDrafts;
    }

    Long resolveOrCreateUnit(String unitName) {
        String finalUnitName = StringUtils.isNotBlank(unitName) ? unitName : "个";
        for (WmsUnitRespVO unit : owner.unitService.selectList()) {
            if (equalsIgnoreCase(unit.getUnitName(), finalUnitName)) {
                return unit.getId();
            }
        }
        WmsUnitSaveReqVO req = new WmsUnitSaveReqVO();
        req.setUnitName(finalUnitName);
        return owner.unitService.create(req);
    }

    Long resolveCategoryId(Object rawCategoryId, String categoryName) {
        if (rawCategoryId != null && StringUtils.isNotBlank(String.valueOf(rawCategoryId))) {
            try {
                return Long.valueOf(String.valueOf(rawCategoryId));
            } catch (NumberFormatException ignored) {
            }
        }
        if (StringUtils.isBlank(categoryName)) {
            WmsGoodsCategoryRespVO category = resolveDefaultCategory();
            return category == null ? null : category.getId();
        }
        for (WmsGoodsCategoryRespVO category : owner.goodsCategoryService.selectList()) {
            if (category != null && equalsIgnoreCase(category.getCategoryName(), categoryName)) {
                return category.getId();
            }
        }
        return null;
    }

    String resolveDraftCategoryName(Long categoryId, String categoryName) {
        if (StringUtils.isNotBlank(categoryName)) {
            return categoryName;
        }
        if (categoryId != null) {
            for (WmsGoodsCategoryRespVO category : owner.goodsCategoryService.selectList()) {
                if (category != null && categoryId.equals(category.getId())) {
                    return category.getCategoryName();
                }
            }
        }
        WmsGoodsCategoryRespVO category = resolveDefaultCategory();
        return category == null ? null : category.getCategoryName();
    }

    WmsGoodsCategoryRespVO resolveDefaultCategory() {
        List<WmsGoodsCategoryRespVO> categories = owner.goodsCategoryService.selectList();
        if (categories != null && !categories.isEmpty()) {
            return categories.get(0);
        }
        WmsGoodsCategorySaveReqVO req = new WmsGoodsCategorySaveReqVO();
        req.setCategoryName("默认分类");
        req.setParentId(0L);
        req.setStatus("0");
        req.setOrderNum(0);
        Long id = owner.goodsCategoryService.create(req);
        return owner.goodsCategoryService.get(id);
    }

    IdNameOption toGoodsOption(WmsGoodsSkuRespVO vo) {
        String goodsName = StringUtils.isNotBlank(vo.getGoodsName()) ? vo.getGoodsName() : "未命名商品";
        String skuName = StringUtils.isNotBlank(vo.getSkuName()) ? vo.getSkuName() : "默认SKU";
        return new IdNameOption(vo.getId(), goodsName + " / " + skuName);
    }

    private List<IdNameOption> currentPageOptions(List<IdNameOption> options, int page) {
        if (options == null || options.isEmpty()) {
            return new ArrayList<>();
        }
        int safePage = Math.max(page, 1);
        int fromIndex = (safePage - 1) * CANDIDATE_PAGE_SIZE;
        if (fromIndex >= options.size()) {
            return new ArrayList<>();
        }
        int toIndex = Math.min(fromIndex + CANDIDATE_PAGE_SIZE, options.size());
        return new ArrayList<>(options.subList(fromIndex, toIndex));
    }

    private boolean hasNextPage(List<IdNameOption> options, int page) {
        return page * CANDIDATE_PAGE_SIZE < (options == null ? 0 : options.size());
    }

    private int totalPages(List<IdNameOption> options) {
        int size = options == null ? 0 : options.size();
        return size == 0 ? 1 : (size + CANDIDATE_PAGE_SIZE - 1) / CANDIDATE_PAGE_SIZE;
    }

    private List<String> goodsCandidateActions() {
        return new ArrayList<>();
    }

    private Map<String, Object> option(String label, String value) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private IdNameOption resolveOption(List<IdNameOption> options, String input) {
        if (options == null || options.isEmpty() || StringUtils.isBlank(input)) {
            return null;
        }
        String normalized = input.trim();
        try {
            int index = Integer.parseInt(normalized);
            if (index >= 1 && index <= options.size()) {
                return options.get(index - 1);
            }
        } catch (NumberFormatException ignored) {
        }
        for (IdNameOption option : options) {
            if (equalsIgnoreCase(option.getName(), normalized) || option.getName().contains(normalized)) {
                return option;
            }
        }
        return null;
    }

    private boolean isOtherChoice(String input) {
        String normalized = input == null ? "" : input.trim();
        return "以上都不是".equals(normalized) || "都不是".equals(normalized) || "其他".equals(normalized);
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception exception) {
            return null;
        }
    }

    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj).trim();
    }
}