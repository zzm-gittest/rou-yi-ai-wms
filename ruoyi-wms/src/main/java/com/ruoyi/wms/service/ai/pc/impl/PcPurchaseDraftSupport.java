package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PcPurchaseDraftSupport {

    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";

    private final PcMultiBillChatServiceImpl owner;

    PcPurchaseDraftSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    boolean hasCompletePurchaseSentence(PcState state) {
        boolean requireSupplier = false;
        boolean requirePrice = false;
        if (requireSupplier && StringUtils.isBlank(state.getSupplierInput())) {
            return false;
        }
        if (state.getItemDrafts() == null || state.getItemDrafts().isEmpty()) {
            return false;
        }
        for (ItemDraft draft : state.getItemDrafts()) {
            if (draft == null || StringUtils.isBlank(draft.name) || draft.quantity == null || (requirePrice && draft.price == null)) {
                return false;
            }
        }
        return true;
    }

    Map<String, Object> buildIncompletePurchaseReply(PcState state) {
        if (DOMAIN_PI.equals(state.getDomainType())) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "采购入库信息还不够完整。你现在只需要先输入商品和数量，供应商、仓库、单价都可以在最后确认表单里选择、新增或补齐；也可以直接输入采购单号走订单入库。",
                    owner.supplierTemplateCard(DOMAIN_PI),
                    owner.conversationLifecycleSupport.quickActions("榴莲100箱，苹果230箱，香蕉200打", "苹果笔记本50台", "按采购单 PO202603190001 入库到一号仓库"));
        }
        if (DOMAIN_PR.equals(state.getDomainType())) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "请按模板信息修改后发送。你可以直接复制长句模板，或先发短句商品列表（如：榴莲100箱，苹果20箱），其余信息可在确认卡片补齐。",
                    owner.supplierTemplateCard(DOMAIN_PR),
                    owner.conversationLifecycleSupport.quickActions("榴莲100箱，苹果20箱", "采购退货，退给云腾科技5台坏的苹果笔记本，从一号仓库退货，每台8000元"));
        }
        return owner.conversationLifecycleSupport.buildReply(state,
                "采购订单信息还不够完整。你至少需要说清商品和数量；供应商、单价都可以在确认卡片里继续补充或修改。",
                owner.supplierTemplateCard(),
                owner.conversationLifecycleSupport.quickActions("采购富士苹果10箱，湖南辣条10包，华诞饼干40箱", "向云腾科技有限公司采购苹果笔记本50台"));
    }

    void backfillPurchaseDraftsFromRawInput(PcState state, String input) {
        if (!owner.isPurchaseDomain(state.getDomainType()) || StringUtils.isBlank(input)) {
            return;
        }
        // 截图识别场景：input 是视觉模型返回的 JSON 文本，不能用正则二次解析（会抓取到 JSON 中的杂散数字导致商品错乱）。
        // 此时商品已由 fillSlotsFromSemanticResult 从视觉结果填好，直接跳过。
        if (input.startsWith(com.ruoyi.wms.service.ai.VoiceParseService.IMAGE_RECOGNITION_PREFIX)) {
            return;
        }
        boolean requireDirectPrice = false;
        List<ItemDraft> rawDrafts = requireDirectPrice ? owner.itemParseSupport.parseItems(input) : owner.itemParseSupport.parseItemsAllowPriceOptional(input);
        if (rawDrafts.isEmpty()) {
            return;
        }
        boolean shouldReplace = state.getItemDrafts() == null
                || state.getItemDrafts().isEmpty()
                || owner.itemParseSupport.hasIncompleteItemDrafts(state.getItemDrafts())
                || rawDrafts.size() > state.getItemDrafts().size();
        if (!shouldReplace) {
            return;
        }
        state.setItemDrafts(rawDrafts);
        state.setItemsInput(buildItemsInputFromDrafts(rawDrafts));
    }

    String buildItemsInputFromDrafts(List<ItemDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ItemDraft draft : drafts) {
            if (draft == null || StringUtils.isBlank(draft.name) || draft.quantity == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(draft.name).append(",").append(draft.quantity.stripTrailingZeros().toPlainString());
            if (StringUtils.isNotBlank(draft.unit)) {
                sb.append(draft.unit);
            }
            if (draft.price != null) {
                sb.append(",").append(draft.price.stripTrailingZeros().toPlainString());
            }
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    void ensureCustomerResolved(PcState state) {
        if (state.customerId != null) {
            return;
        }
        if (StringUtils.isBlank(state.customerInput) && StringUtils.isNotBlank(state.lastIntentText)) {
            state.customerInput = owner.itemParseSupport.extractCustomerName(state.lastIntentText);
        }
        String customerKeyword = StringUtils.defaultIfBlank(owner.itemParseSupport.extractCustomerName(state.customerInput),
                StringUtils.defaultIfBlank(state.customerInput, state.customerName));
        if (StringUtils.isBlank(customerKeyword)) {
            return;
        }
        List<WmsCustomerRespVO> list = owner.customerService.selectList();
        WmsCustomerRespVO exact = list.stream()
                .filter(item -> StringUtils.equalsIgnoreCase(item.getCustomerName(), customerKeyword))
                .findFirst()
                .orElse(null);
        if (exact != null) {
            state.customerId = exact.getId();
            state.customerName = exact.getCustomerName();
            state.customerInput = exact.getCustomerName();
            return;
        }
        List<WmsCustomerRespVO> fuzzy = list.stream()
                .filter(item -> containsIgnoreCase(item.getCustomerName(), customerKeyword)
                        || containsIgnoreCase(customerKeyword, item.getCustomerName()))
                .collect(Collectors.toList());
        if (fuzzy.size() == 1) {
            state.customerId = fuzzy.get(0).getId();
            state.customerName = fuzzy.get(0).getCustomerName();
            state.customerInput = fuzzy.get(0).getCustomerName();
        }
    }

    Map<String, Object> ensureCustomerReadyForCreate(PcState state) {
        if (StringUtils.isBlank(state.customerInput) && StringUtils.isNotBlank(state.lastIntentText)) {
            state.customerInput = owner.itemParseSupport.extractCustomerName(state.lastIntentText);
        }
        ensureCustomerResolved(state);
        if (state.customerId != null) {
            return null;
        }
        if (StringUtils.isNotBlank(state.customerInput)) {
            owner.resolveCustomerByInput(state, state.customerInput, true);
        } else if (StringUtils.isNotBlank(state.customerName)) {
            state.customerInput = state.customerName;
            owner.resolveCustomerByInput(state, state.customerName, true);
        } else {
            // 用户未指定客户，尝试使用默认客户
            WmsCustomer defaultCustomer = owner.customerService.getDefault();
            if (defaultCustomer != null) {
                state.customerId = defaultCustomer.getId();
                state.customerName = defaultCustomer.getCustomerName();
                state.customerInput = defaultCustomer.getCustomerName();
                return null;
            }
            state.step = "WAIT_CUSTOMER_INPUT";
        }
        owner.conversationLifecycleSupport.saveState(state);
        return owner.buildCustomerFollowup(state, true);
    }

    private boolean containsIgnoreCase(String text, String search) {
        if (text == null || search == null) {
            return false;
        }
        return text.toLowerCase().contains(search.toLowerCase());
    }
}
