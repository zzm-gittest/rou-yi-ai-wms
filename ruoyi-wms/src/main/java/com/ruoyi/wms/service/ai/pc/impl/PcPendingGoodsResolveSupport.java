package com.ruoyi.wms.service.ai.pc.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;
import com.ruoyi.wms.service.ai.pc.vo.PendingGoodsDraft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PcPendingGoodsResolveSupport {

    private final PcMultiBillChatServiceImpl owner;

    PcPendingGoodsResolveSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    void replacePendingGoodsBySku(PcState state, WmsGoodsSkuRespVO sku) {
        PendingGoodsDraft pendingDraft = state.getPendingGoodsDraft();
        if (state.getPendingGoodsDraftIndex() != null
                && state.getPendingGoodsDraftIndex() >= 0
                && state.getPendingGoodsDraftIndex() < state.getItemDrafts().size()) {
            ItemDraft draft = state.getItemDrafts().get(state.getPendingGoodsDraftIndex());
            draft.forceSkuId = sku.getId();
            draft.name = StringUtils.isNotBlank(sku.getGoodsName()) ? sku.getGoodsName() : sku.getSkuName();
            if (StringUtils.isBlank(draft.unit)) {
                draft.unit = sku.getUnitName();
            }
            if (draft.quantity == null && pendingDraft != null) {
                draft.quantity = pendingDraft.getDefaultQuantity();
            }
            if (draft.price == null && pendingDraft != null) {
                draft.price = pendingDraft.getDefaultPrice();
            }
            owner.clearPendingGoodsState(state);
            return;
        }
        for (ItemDraft draft : state.getItemDrafts()) {
            if (state.getPendingGoodsName() != null && state.getPendingGoodsName().equals(draft.name)) {
                draft.forceSkuId = sku.getId();
                draft.name = StringUtils.isNotBlank(sku.getGoodsName()) ? sku.getGoodsName() : sku.getSkuName();
                if (StringUtils.isBlank(draft.unit)) {
                    draft.unit = sku.getUnitName();
                }
                if (draft.quantity == null && pendingDraft != null) {
                    draft.quantity = pendingDraft.getDefaultQuantity();
                }
                if (draft.price == null && pendingDraft != null) {
                    draft.price = pendingDraft.getDefaultPrice();
                }
                break;
            }
        }
        owner.clearPendingGoodsState(state);
    }

    List<Long> parseSelectedSkuIds(String value) {
        if (StringUtils.isBlank(value)) {
            return new ArrayList<>();
        }
        LinkedHashSet<Long> skuIds = new LinkedHashSet<>();
        Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(item -> skuIds.add(Long.valueOf(item)));
        return new ArrayList<>(skuIds);
    }

    void replacePendingGoodsBySelectedSkus(PcState state, List<Long> skuIds) {
        if (state.getPendingGoodsDraftIndex() == null
                || state.getPendingGoodsDraftIndex() < 0
                || state.getPendingGoodsDraftIndex() >= state.getItemDrafts().size()) {
            throw new ServiceException("待处理商品上下文丢失");
        }
        PendingGoodsDraft pendingDraft = owner.goodsFlowSupport.ensurePendingGoodsDraft(state, owner.goodsFlowSupport.currentPendingDraft(state));
        List<ItemDraft> replacements = new ArrayList<>();
        for (Long skuId : skuIds) {
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                throw new ServiceException("商品不存在: " + skuId);
            }
            ItemDraft itemDraft = new ItemDraft();
            itemDraft.name = StringUtils.isNotBlank(sku.getGoodsName()) ? sku.getGoodsName() : sku.getSkuName();
            itemDraft.unit = StringUtils.isNotBlank(sku.getUnitName()) ? sku.getUnitName() : pendingDraft.getUnitName();
            itemDraft.quantity = pendingDraft.getDefaultQuantity();
            itemDraft.price = pendingDraft.getDefaultPrice();
            itemDraft.forceSkuId = sku.getId();
            replacements.add(itemDraft);
        }
        int index = state.getPendingGoodsDraftIndex();
        state.getItemDrafts().remove(index);
        state.getItemDrafts().addAll(index, replacements);
        owner.clearPendingGoodsState(state);
    }

    void applyStockCheckPreviewItems(PcState state, Object rawItems) {
        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(rawItems, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("盘点预览明细格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("盘点明细至少保留一条商品");
        }
        Map<Long, LineDraft> originalMap = state.preparedLines.stream()
                .filter(line -> line.skuId != null)
                .collect(Collectors.toMap(line -> line.skuId, line -> line, (a, b) -> a));
        List<LineDraft> updated = new ArrayList<>();
        LinkedHashSet<Long> seen = new LinkedHashSet<>();
        for (Map<String, Object> item : items) {
            Long skuId = item.get("skuId") == null ? null : Long.valueOf(String.valueOf(item.get("skuId")));
            if (skuId == null || !originalMap.containsKey(skuId) || !seen.add(skuId)) {
                throw new ServiceException("盘点预览不允许追加原清单外商品");
            }
            LineDraft original = originalMap.get(skuId);
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("商品【" + original.goodsName + "】实际数量不能小于0");
            }
            BigDecimal costPrice = toBigDecimal(item.get("price"));
            if (costPrice == null) {
                costPrice = BigDecimal.ZERO;
            }
            if (costPrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("商品【" + original.goodsName + "】成本价不能小于0");
            }
            LineDraft line = new LineDraft();
            line.skuId = original.skuId;
            line.goodsName = original.goodsName;
            line.skuName = original.skuName;
            line.unitName = original.unitName;
            line.beforeQty = original.beforeQty;
            line.maxQty = original.maxQty;
            line.remark = original.remark;
            line.quantity = quantity;
            line.price = costPrice;
            updated.add(line);
        }
        state.preparedLines = updated;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value).trim());
        } catch (Exception ex) {
            return null;
        }
    }
}

