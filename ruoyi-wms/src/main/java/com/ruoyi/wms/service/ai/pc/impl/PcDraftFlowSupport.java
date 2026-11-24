package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

class PcDraftFlowSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";

    private final PcMultiBillChatServiceImpl owner;

    PcDraftFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    void tryAutoSelectPurchaseSupplier(PcState state) {
        if (state.getSupplierId() != null || StringUtils.isBlank(state.getSupplierInput())) {
            return;
        }
        List<WmsSupplierRespVO> list = owner.supplierService.selectList();
        List<WmsSupplierRespVO> candidates = list.stream()
                .filter(item -> containsIgnoreCase(item.getSupplierName(), state.getSupplierInput()))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            return;
        }
        WmsSupplierRespVO exact = candidates.stream()
                .filter(item -> equalsIgnoreCase(item.getSupplierName(), state.getSupplierInput()))
                .findFirst()
                .orElse(null);
        if (exact != null || candidates.size() == 1) {
            WmsSupplierRespVO chosen = exact != null ? exact : candidates.get(0);
            state.setSupplierId(chosen.getId());
            state.setSupplierName(chosen.getSupplierName());
            state.setSupplierInput(chosen.getSupplierName());
        }
    }

    LineDraft buildDirectPurchaseLine(ItemDraft draft, WmsGoodsSkuRespVO sku) {
        LineDraft line = new LineDraft();
        line.setSkuId(sku.getId());
        line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), draft.name));
        line.setSkuName(sku.getSkuName());
        line.setUnitName(StringUtils.defaultIfBlank(draft.unit, sku.getUnitName()));
        line.setQuantity(draft.quantity);
        line.setPrice(draft.price);
        return line;
    }

    List<LineDraft> compactPreparedLines(List<LineDraft> lines) {
        List<LineDraft> compacted = new ArrayList<>();
        if (lines == null) {
            return compacted;
        }
        for (LineDraft line : lines) {
            if (line != null) {
                compacted.add(line);
            }
        }
        return compacted;
    }

    Map<String, Object> handleSupplierCreateConfirm(PcState state, String input) {
        if (owner.isConfirmInput(input)) {
            WmsSupplierSaveReqVO req = new WmsSupplierSaveReqVO();
            req.setSupplierName(state.getSupplierInput());
            Long id = owner.supplierService.create(req);
            state.setSupplierId(id);
            state.setSupplierName(state.getSupplierInput());
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.resumeCurrentFlow(state);
        }
        if (owner.isCancelInput(input)) {
            state.resetFlow();
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, "采购流程已取消，请重新输入正确的供应商信息。", owner.cardBuilderSupport.actionListCard(), owner.conversationLifecycleSupport.quickActions("向云腾科技有限公司采购苹果笔记本50台，单价是8000元"));
        }
        return owner.conversationLifecycleSupport.buildReply(state,
                "未找到供应商【" + state.getSupplierInput() + "】，是否创建该供应商？请点击【确认】创建，或点击【取消】放弃。",
                null,
                owner.confirmCancelQuickActions());
    }

    Map<String, Object> handleItemsText(PcState state, String input) {
        // 采购订单和销售订单支持不带单价的简短输入
        boolean priceOptional = DOMAIN_PO.equals(state.getDomainType()) || "SALES_ORDER".equals(state.getDomainType());
        List<ItemDraft> drafts = priceOptional ? owner.itemParseSupport.parseItemsAllowPriceOptional(input) : owner.itemParseSupport.parseItems(input);
        if (drafts.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "商品明细格式不正确，请按“商品名,数量,单价；商品名,数量,单价”输入。",
                    owner.goodsTemplateCard(state.getDomainType()),
                    owner.conversationLifecycleSupport.quickActions("苹果笔记本,2,6800", "无线鼠标,10,59"));
        }
        state.setItemsInput(input);
        state.setItemDrafts(drafts);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    // 商品候选默认不自动选中：统一进候选卡，用户可从列表选择或点「以上都不是」逃逸到
    // 新增商品流程。唯一例外：候选中恰好 1 个且命名完全相同（忽略空格/大小写）时
    // 直接自动选中，无需候选卡。
    WmsGoodsSkuRespVO chooseExactlyNamedSingle(List<WmsGoodsSkuRespVO> list, String name) {
        if (list == null || list.isEmpty() || name == null) {
            return null;
        }
        List<WmsGoodsSkuRespVO> exact = list.stream()
                .filter(vo -> equalsNameIgnoreSpace(vo.getSkuName(), name) || equalsNameIgnoreSpace(vo.getGoodsName(), name))
                .collect(Collectors.toList());
        return exact.size() == 1 ? exact.get(0) : null;
    }

    private boolean equalsNameIgnoreSpace(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.replaceAll("\\s+", "").equalsIgnoreCase(b.replaceAll("\\s+", ""));
    }

    private boolean containsIgnoreCase(String text, String search) {
        if (text == null || search == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}

