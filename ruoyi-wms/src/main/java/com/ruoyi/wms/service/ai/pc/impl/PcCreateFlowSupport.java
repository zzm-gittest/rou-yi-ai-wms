package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PcCreateFlowSupport {

    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private static final String CHECK_TYPE_FULL = "FULL";
    private static final String CHECK_TYPE_SAMPLE = "SAMPLE";

    private final PcMultiBillChatServiceImpl owner;

    PcCreateFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> continueExtendedFlow(PcState state) {
        String domainType = state.getDomainType();
        if (StringUtils.isBlank(domainType)) {
            return owner.conversationLifecycleSupport.buildReply(state, "请先说明要创建哪种单据。", owner.cardBuilderSupport.actionListCard(), new ArrayList<>());
        }

        // 销售出库方式选择引导
        if (DOMAIN_SOUT.equals(domainType) && shouldAskSalesOutModeChoice(state)) {
            state.setStep("WAIT_SOUT_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请选择出库方式：回复\"1\"直接出库，或回复\"2\"根据销售订单出库。",
                    salesOutModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

        // 销售退货方式选择引导
        if (DOMAIN_SR.equals(domainType) && shouldAskSalesReturnModeChoice(state)) {
            state.setStep("WAIT_SR_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请选择销售退货方式：回复\"1\"直接退货，或回复\"2\"根据销售出库单退货。",
                    salesReturnModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

        boolean allGoodsRequested = containsAllGoodsKeyword(state.getItemsInput())
                || containsAllGoodsKeyword(state.lastIntentText);

        if (owner.supportsLinkedSource(domainType) && !DOMAIN_PI.equals(domainType)) {
            // 销售出库关联模式走单独的 linked flow
            if (DOMAIN_SOUT.equals(domainType) && "LINKED".equals(state.salesOutMode)) {
                return owner.continueSalesOutLinkedFlow(state);
            }
            // 销售退货关联模式走单独的 linked flow
            if (DOMAIN_SR.equals(domainType) && "LINKED".equals(state.salesReturnMode)) {
                return owner.continueSalesOutLinkedFlow(state);
            }
            if (state.linkOrderId == null && owner.shouldAskLinkOrder(state)) {
                state.setStep("WAIT_LINK_ORDER_INPUT");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "请提供关联来源单号，我会带出来源单明细。", owner.linkOrderTemplateCard(state), owner.conversationLifecycleSupport.quickActions(owner.linkOrderExample(domainType), "取消"));
            }
            if (state.linkOrderId == null && StringUtils.isNotBlank(state.linkOrderNo)) {
                Map<String, Object> sourceReply = owner.resolveLinkedSource(state);
                if (sourceReply != null) {
                    return sourceReply;
                }
            }
        }

        if (DOMAIN_PR.equals(domainType)) {
            // PR直接模式：供应商可选，留到确认卡片下拉选择；长句提供了供应商名称时仍尝试解析
            if (state.getSupplierId() == null && StringUtils.isNotBlank(state.getSupplierInput())) {
                owner.resolveSupplierByInput(state, state.getSupplierInput());
                owner.conversationLifecycleSupport.saveState(state);
                if (!"IDLE".equals(state.getStep())) {
                    return owner.buildSupplierFollowup(state);
                }
            }
        }

        if (DOMAIN_SO.equals(domainType) || DOMAIN_SOUT.equals(domainType) || DOMAIN_SR.equals(domainType)) {
            if (state.customerId == null) {
                if (StringUtils.isBlank(state.customerInput)) {
                    // 销售订单 和 销售出库直接模式 和 销售退货直接模式 允许不提供客户名称，在确认卡片上补充
                    if (!DOMAIN_SO.equals(domainType)
                            && !(DOMAIN_SOUT.equals(domainType) && "DIRECT".equals(state.salesOutMode))
                            && !(DOMAIN_SR.equals(domainType) && "DIRECT".equals(state.salesReturnMode))) {
                        state.step = "WAIT_CUSTOMER_INPUT";
                        owner.conversationLifecycleSupport.saveState(state);
                        return owner.conversationLifecycleSupport.buildReply(state, "请补充客户名称。", owner.customerTemplateCard(domainType, true), owner.conversationLifecycleSupport.quickActions("客户A", "北京星河商贸"));
                    }
                } else {
                    owner.resolveCustomerByInput(state, state.customerInput, true);
                    owner.conversationLifecycleSupport.saveState(state);
                    if (!"IDLE".equals(state.step)) {
                        return owner.buildCustomerFollowup(state, true);
                    }
                }
            }
        }

        // PR直接模式 和 SOUT直接模式 和 SR直接模式：仓库可选，留到确认卡片下拉选择；长句提供了仓库名称时仍尝试解析
        if ((DOMAIN_PR.equals(domainType) && !owner.hasLinkedSource(state))
                || (DOMAIN_SOUT.equals(domainType) && "DIRECT".equals(state.salesOutMode))
                || (DOMAIN_SR.equals(domainType) && "DIRECT".equals(state.salesReturnMode))) {
            if (state.getWarehouseId() == null && StringUtils.isNotBlank(state.getWarehouseInput())) {
                owner.resolveWarehouseByInput(state, state.getWarehouseInput());
                owner.conversationLifecycleSupport.saveState(state);
                if (!"IDLE".equals(state.getStep())) {
                    return owner.buildWarehouseFollowup(state);
                }
            }
        } else if (DOMAIN_PI.equals(domainType) || DOMAIN_SOUT.equals(domainType)
                || DOMAIN_SR.equals(domainType)) {
            if (state.getWarehouseId() == null) {
                if (StringUtils.isBlank(state.getWarehouseInput())) {
                    state.setStep("WAIT_WAREHOUSE_INPUT");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.conversationLifecycleSupport.buildReply(state, "请补充仓库名称。", owner.warehouseTemplateCard(domainType), owner.conversationLifecycleSupport.quickActions("一号仓库", "二号仓库"));
                }
                owner.resolveWarehouseByInput(state, state.getWarehouseInput());
                owner.conversationLifecycleSupport.saveState(state);
                if (!"IDLE".equals(state.getStep())) {
                    return owner.buildWarehouseFollowup(state);
                }
            }
        }

        if (DOMAIN_ST.equals(domainType)) {
            Map<String, Object> transferWarehouseReply = ensureTransferWarehouses(state);
            if (transferWarehouseReply != null) {
                return transferWarehouseReply;
            }
        }

        if (DOMAIN_SC.equals(domainType) && state.getWarehouseId() == null && StringUtils.isNotBlank(state.getWarehouseInput())) {
            owner.resolveWarehouseByInput(state, state.getWarehouseInput());
            owner.conversationLifecycleSupport.saveState(state);
            if (!"IDLE".equals(state.getStep())) {
                return owner.buildWarehouseFollowup(state);
            }
        }

        if (DOMAIN_SC.equals(domainType) && state.getWarehouseId() == null && StringUtils.isBlank(state.getWarehouseInput())) {
            return startStockCheckGuidedFlow(state);
        }

        if (DOMAIN_SC.equals(domainType)
                && state.getWarehouseId() != null
                && (state.getItemDrafts() == null || state.getItemDrafts().isEmpty())
                && (state.preparedLines == null || state.preparedLines.isEmpty())) {
            return showStockCheckGoodsSelection(state);
        }

        if (owner.hasLinkedSource(state)) {
            if (state.preparedLines == null || state.preparedLines.isEmpty()) {
                throw new ServiceException("关联来源单没有可用明细");
            }
            state.setStep("WAIT_CONFIRM_EXEC");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("单据信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
        }

        // 销售订单不强制要求输入时提供单价，可在确认卡片上补充
        boolean requirePrice = DOMAIN_PI.equals(domainType) && !owner.hasLinkedSource(state);
        if (!allGoodsRequested && (state.getItemDrafts() == null || state.getItemDrafts().isEmpty())) {
            state.setStep("WAIT_ITEMS_INPUT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, "请补充商品明细。", owner.goodsTemplateCard(domainType), owner.conversationLifecycleSupport.quickActions(defaultGoodsExample(domainType)));
        }

        Map<String, Object> lineResolution = resolveExtendedLines(state, requirePrice);
        if (lineResolution != null) {
            return lineResolution;
        }

        if (DOMAIN_PR.equals(domainType)) {
            state.setPreparedPiItems(owner.buildPurchaseInItemsFromLines(state.preparedLines));
        }

        // 销售类单据：用户未指定客户时，回落到默认客户
        if ((DOMAIN_SO.equals(domainType) || DOMAIN_SOUT.equals(domainType) || DOMAIN_SR.equals(domainType))
                && state.getCustomerId() == null
                && StringUtils.isBlank(state.customerName)
                && StringUtils.isBlank(state.customerInput)) {
            com.ruoyi.wms.domain.WmsCustomer defaultCustomer = owner.customerService.getDefault();
            if (defaultCustomer != null) {
                state.setCustomerId(defaultCustomer.getId());
                state.customerName = defaultCustomer.getCustomerName();
                state.customerInput = defaultCustomer.getCustomerName();
            }
        }

        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state, owner.buildConfirmPrompt("单据信息"), owner.buildExtendedPreviewCard(state), owner.confirmCancelQuickActions());
    }

    void fillTransferWarehousesFromText(PcState state, String input) {
        if (StringUtils.isBlank(input)) {
            return;
        }
        Matcher matcher = Pattern.compile("从\\s*(.+?仓(?:库)?)\\s*(?:调拨|调|出库)?.*?到\\s*(.+?仓(?:库)?)").matcher(input.replace("，", ","));
        if (matcher.find()) {
            if (StringUtils.isBlank(state.outWarehouseInput)) {
                state.outWarehouseInput = matcher.group(1).trim();
            }
            if (StringUtils.isBlank(state.inWarehouseInput)) {
                state.inWarehouseInput = matcher.group(2).trim();
            }
        }
    }

    boolean containsAllGoodsKeyword(String input) {
        return StringUtils.isNotBlank(input)
                && (input.contains("所有商品") || input.contains("全部商品") || input.contains("全仓") || input.contains("全部库存"));
    }

    Map<String, Object> ensureTransferWarehouses(PcState state) {
        if ((StringUtils.isBlank(state.outWarehouseInput) || StringUtils.isBlank(state.inWarehouseInput)) && StringUtils.isNotBlank(state.lastIntentText)) {
            fillTransferWarehousesFromText(state, state.lastIntentText);
        }
        if (state.outWarehouseId == null) {
            if (StringUtils.isBlank(state.outWarehouseInput)) {
                state.step = "WAIT_OUT_WAREHOUSE_INPUT";
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "请补充调出仓库名称。", owner.warehouseTemplateCard(), owner.conversationLifecycleSupport.quickActions("一号仓库", "原料仓"));
            }
            resolveWarehouseForSlot(state, state.outWarehouseInput, "OUT");
            owner.conversationLifecycleSupport.saveState(state);
            if (!"IDLE".equals(state.step)) {
                return buildTransferWarehouseFollowup(state, "OUT");
            }
        }
        if (state.inWarehouseId == null) {
            if (StringUtils.isBlank(state.inWarehouseInput)) {
                state.step = "WAIT_IN_WAREHOUSE_INPUT";
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state, "请补充调入仓库名称。", owner.warehouseTemplateCard(), owner.conversationLifecycleSupport.quickActions("二号仓库", "成品仓"));
            }
            resolveWarehouseForSlot(state, state.inWarehouseInput, "IN");
            owner.conversationLifecycleSupport.saveState(state);
            if (!"IDLE".equals(state.step)) {
                return buildTransferWarehouseFollowup(state, "IN");
            }
        }
        if (state.outWarehouseId != null && state.outWarehouseId.equals(state.inWarehouseId)) {
            state.inWarehouseId = null;
            state.inWarehouseName = null;
            state.step = "WAIT_IN_WAREHOUSE_INPUT";
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state, "调出仓库与调入仓库不能相同，请重新输入调入仓库。", owner.warehouseTemplateCard(), owner.conversationLifecycleSupport.quickActions("二号仓库", "成品仓"));
        }
        return null;
    }

    void resolveWarehouseForSlot(PcState state, String input, String slot) {
        List<WmsWarehouseRespVO> candidates = owner.warehouseService.selectList().stream()
                .filter(item -> containsIgnoreCase(item.getWarehouseName(), input))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            state.step = "OUT".equals(slot) ? "WAIT_OUT_WAREHOUSE_CUSTOM_FORM" : "WAIT_IN_WAREHOUSE_CUSTOM_FORM";
            return;
        }
        WmsWarehouseRespVO exact = null;
        for (WmsWarehouseRespVO candidate : candidates) {
            if (equalsIgnoreCase(candidate.getWarehouseName(), input)) {
                exact = candidate;
                break;
            }
        }
        if (exact != null || candidates.size() == 1) {
            owner.applyWarehouseToSlot(state, exact != null ? exact : candidates.get(0), slot);
            state.step = "IDLE";
            return;
        }
        state.setWarehouseCandidates(candidates.stream().map(owner::toWarehouseOption).collect(Collectors.toList()));
        state.step = "OUT".equals(slot) ? "WAIT_OUT_WAREHOUSE_CANDIDATE" : "WAIT_IN_WAREHOUSE_CANDIDATE";
    }

    Map<String, Object> buildTransferWarehouseFollowup(PcState state, String slot) {
        String warehouseLabel = "OUT".equals(slot) ? "调出仓库" : "调入仓库";
        if (state.step.endsWith("CUSTOM_FORM")) {
            return owner.conversationLifecycleSupport.buildReply(state, "未找到匹配的" + warehouseLabel + "，请补充新增信息。", owner.warehouseFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        return owner.conversationLifecycleSupport.buildReply(state, "找到多个匹配的" + warehouseLabel + "，请确认。", owner.warehouseCandidateCard(state), owner.conversationLifecycleSupport.quickActions("以上都不是"));
    }

    Map<String, Object> resolveExtendedLines(PcState state, boolean requirePrice) {
        List<LineDraft> lines = new ArrayList<>();
        if (DOMAIN_SC.equals(state.getDomainType())
                && (containsAllGoodsKeyword(state.getItemsInput()) || containsAllGoodsKeyword(state.lastIntentText))) {
            List<WmsStock> stocks = owner.stockService.list().stream()
                    .filter(stock -> state.getWarehouseId().equals(stock.getWarehouseId()))
                    .collect(Collectors.toList());
            if (stocks.isEmpty()) {
                return owner.conversationLifecycleSupport.buildReply(state, "该仓库暂无库存，无法生成盘点明细。", null, owner.conversationLifecycleSupport.quickActions("取消"));
            }
            for (WmsStock stock : stocks) {
                WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(stock.getSkuId());
                if (sku == null) {
                    continue;
                }
                LineDraft line = new LineDraft();
                line.skuId = sku.getId();
                line.goodsName = sku.getGoodsName();
                line.skuName = sku.getSkuName();
                line.unitName = sku.getUnitName();
                line.beforeQty = stock.getQuantity();
                line.quantity = stock.getQuantity();
                line.price = stock.getCostPrice();
                lines.add(line);
            }
            state.preparedLines = lines;
            return null;
        }

        for (int index = 0; index < state.getItemDrafts().size(); index++) {
            ItemDraft draft = state.getItemDrafts().get(index);
            if (draft == null || StringUtils.isBlank(draft.name)) {
                return owner.conversationLifecycleSupport.buildReply(state, "商品明细不完整，请重新输入。", owner.goodsTemplateCard(state.getDomainType()), owner.conversationLifecycleSupport.quickActions(defaultGoodsExample(state.getDomainType())));
            }
            if (draft.quantity == null && !DOMAIN_SC.equals(state.getDomainType())) {
                return owner.conversationLifecycleSupport.buildReply(state, "商品【" + draft.name + "】缺少数量，请补充后再试。", owner.goodsTemplateCard(state.getDomainType()), owner.conversationLifecycleSupport.quickActions(defaultGoodsExample(state.getDomainType())));
            }
            if (requirePrice && draft.price == null) {
                return owner.conversationLifecycleSupport.buildReply(state, "商品【" + draft.name + "】缺少单价，请按模板补充。", owner.goodsTemplateCard(state.getDomainType()), owner.conversationLifecycleSupport.quickActions(defaultGoodsExample(state.getDomainType())));
            }

            WmsGoodsSkuRespVO chosen = draft.forceSkuId == null ? null : owner.goodsSkuService.get(draft.forceSkuId);
            if (chosen == null) {
                List<WmsGoodsSkuRespVO> candidates = owner.goodsSkuService.searchByName(draft.name, 10);
                if (candidates == null || candidates.isEmpty()) {
                    owner.preparePendingGoods(state, draft, index, new ArrayList<>());
                    state.setStep("WAIT_GOODS_BASE_FORM");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.conversationLifecycleSupport.buildReply(state, "商品【" + draft.name + "】不存在，请先补充商品资料。", owner.goodsFlowSupport.goodsCreateFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
                }
                // 候选中恰好 1 个且命名完全相同 → 直接自动选中；
                // 其余情况（含恰好 1 个但名字不完全相同）统一进候选卡（带「以上都不是」）。
                WmsGoodsSkuRespVO exactNamed = owner.chooseExactlyNamedSingle(candidates, draft.name);
                if (exactNamed != null) {
                    chosen = exactNamed;
                } else {
                    owner.preparePendingGoods(state, draft, index, candidates.stream().map(owner::toGoodsOption).collect(Collectors.toList()));
                    state.setStep("WAIT_GOODS_CANDIDATE");
                    owner.conversationLifecycleSupport.saveState(state);
                    return owner.goodsFlowSupport.buildGoodsCandidateReply(state);
                }
            }

            LineDraft line = new LineDraft();
            line.skuId = chosen.getId();
            line.goodsName = StringUtils.defaultIfBlank(chosen.getGoodsName(), draft.name);
            line.skuName = chosen.getSkuName();
            line.unitName = chosen.getUnitName();
            line.quantity = draft.quantity;
            line.price = draft.price;
            if (DOMAIN_SC.equals(state.getDomainType())) {
                final Long chosenSkuId = chosen.getId();
                WmsStock stock = owner.stockService.list().stream()
                        .filter(item -> state.getWarehouseId().equals(item.getWarehouseId()) && chosenSkuId.equals(item.getSkuId()))
                        .findFirst().orElse(null);
                line.beforeQty = stock == null ? BigDecimal.ZERO : stock.getQuantity();
                line.quantity = draft.quantity == null ? line.beforeQty : draft.quantity;
                line.price = stock == null ? BigDecimal.ZERO : stock.getCostPrice();
            }
            lines.add(line);
        }
        state.preparedLines = lines;
        return null;
    }

    String defaultGoodsExample(String domainType) {
        if (DOMAIN_SO.equals(domainType)) {
            return "苹果笔记本,10,9000";
        }
        if (DOMAIN_SC.equals(domainType)) {
            return "苹果笔记本;无线鼠标";
        }
        return "苹果笔记本,10";
    }

    private static boolean containsIgnoreCase(String text, String search) {
        if (text == null || search == null) {
            return false;
        }
        return text.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT));
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    // ---- 销售退货方式选择 ----

    /**
     * 判断是否需要展示销售退货方式选择卡片
     */
    boolean shouldAskSalesReturnModeChoice(PcState state) {
        if (!DOMAIN_SR.equals(state.getDomainType())) {
            return false;
        }
        if (StringUtils.isNotBlank(state.salesReturnMode)
                || StringUtils.isNotBlank(state.linkOrderNo)
                || state.linkOrderId != null
                || (state.itemDrafts != null && !state.itemDrafts.isEmpty())
                || StringUtils.isNotBlank(state.itemsInput)) {
            return false;
        }
        return true;
    }

    /**
     * 处理销售退货方式选择输入
     */
    Map<String, Object> handleSalesReturnModeChoice(PcState state, String input) {
        if (!DOMAIN_SR.equals(state.getDomainType())) {
            return owner.continueExtendedFlow(state);
        }
        String normalized = StringUtils.defaultString(input).trim();
        boolean directMode = "1".equals(normalized) || normalized.contains("直接退货") || normalized.contains("直接");
        boolean linkedMode = "2".equals(normalized) || normalized.contains("出库单") || normalized.contains("根据") || normalized.contains("关联");

        if (!directMode && !linkedMode) {
            state.setStep("WAIT_SR_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请回复\"1\"直接退货，或回复\"2\"根据销售出库单退货。",
                    salesReturnModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

        if (directMode) {
            state.salesReturnMode = "DIRECT";
            state.linkOrderNo = null;
            state.linkOrderId = null;
            state.linkOrderType = null;
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildDomainStartGuide(state);
        }

        // 关联退货模式：返回来源单候选卡片（空列表），由用户在卡片内输入单号远程搜索
        state.salesReturnMode = "LINKED";
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

    private Map<String, Object> salesReturnModeChoiceCard() {
        Map<String, Object> card = new java.util.HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "销售退货方式选择");
        card.put("selectionMode", "single-list");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(modeOption("1. 直接退货", "1"));
        options.add(modeOption("2. 根据销售出库单退货", "2"));
        card.put("options", options);
        return card;
    }

    // ---- 销售出库方式选择 ----

    boolean shouldAskSalesOutModeChoice(PcState state) {
        if (!DOMAIN_SOUT.equals(state.getDomainType())) {
            return false;
        }
        // 已经选过模式、或语义解析已提取商品/关联单号 → 不再询问
        if (StringUtils.isNotBlank(state.salesOutMode)
                || StringUtils.isNotBlank(state.linkOrderNo)
                || state.linkOrderId != null
                || (state.itemDrafts != null && !state.itemDrafts.isEmpty())
                || StringUtils.isNotBlank(state.itemsInput)) {
            return false;
        }
        return true;
    }

    Map<String, Object> handleSalesOutModeChoice(PcState state, String input) {
        if (!DOMAIN_SOUT.equals(state.getDomainType())) {
            return owner.continueExtendedFlow(state);
        }
        String normalized = StringUtils.defaultString(input).trim();
        boolean directMode = "1".equals(normalized) || normalized.contains("直接出库") || normalized.contains("直接");
        boolean linkedMode = "2".equals(normalized) || normalized.contains("销售订单") || normalized.contains("关联") || normalized.contains("根据");

        if (!directMode && !linkedMode) {
            state.setStep("WAIT_SOUT_MODE_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请回复\"1\"直接出库，或回复\"2\"根据销售订单出库。",
                    salesOutModeChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("1", "2")
            );
        }

        if (directMode) {
            state.salesOutMode = "DIRECT";
            state.linkOrderNo = null;
            state.linkOrderId = null;
            state.linkOrderType = null;
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildDomainStartGuide(state);
        }

        // 关联出库模式：返回来源单候选卡片（空列表），由用户在卡片内输入单号远程搜索
        state.salesOutMode = "LINKED";
        state.linkOrderNo = null;
        state.linkOrderId = null;
        state.linkOrderType = null;
        state.linkOrderCandidates = new ArrayList<>();
        state.linkOrderCandidatePage = 1;
        state.setStep("WAIT_LINK_ORDER_CANDIDATE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(
                state,
                "请搜索选择要出库的销售订单。",
                owner.linkOrderCandidateCard(state),
                owner.conversationLifecycleSupport.quickActions("直接出库", "取消")
        );
    }

    private Map<String, Object> salesOutModeChoiceCard() {
        Map<String, Object> card = new java.util.HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "销售出库方式选择");
        card.put("selectionMode", "single-list");
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(modeOption("1. 直接出库", "1"));
        options.add(modeOption("2. 根据销售订单出库", "2"));
        card.put("options", options);
        return card;
    }

    private Map<String, Object> modeOption(String label, String value) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("label", label);
        map.put("value", value);
        return map;
    }

    // ---- 库存盘点引导式流程 ----

    Map<String, Object> startStockCheckGuidedFlow(PcState state) {
        List<WmsWarehouseRespVO> warehouses = owner.warehouseService.selectList();
        if (warehouses == null || warehouses.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "暂无可用仓库，请先创建仓库。", null,
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }
        state.setWarehouseCandidates(warehouses.stream()
                .map(owner::toWarehouseOption).collect(Collectors.toList()));
        state.setStep("WAIT_SC_WAREHOUSE_SELECT");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                "请选择盘点仓库：",
                owner.warehouseCandidateCard(state),
                owner.conversationLifecycleSupport.quickActions("取消"));
    }

    Map<String, Object> showStockCheckGoodsSelection(PcState state) {
        Long warehouseId = state.getWarehouseId();
        List<WmsStock> stocks = owner.stockService.lambdaQuery()
                .eq(WmsStock::getWarehouseId, warehouseId)
                .gt(WmsStock::getQuantity, BigDecimal.ZERO)
                .list();

        if (stocks == null || stocks.isEmpty()) {
            state.setWarehouseId(null);
            state.setWarehouseName(null);
            state.setWarehouseInput(null);
            state.setStep("WAIT_SC_WAREHOUSE_SELECT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state,
                    "该仓库暂无库存商品，请选择其他盘点仓库。",
                    owner.warehouseCandidateCard(state),
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }

        List<Map<String, Object>> options = new ArrayList<>();
        for (WmsStock stock : stocks) {
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(stock.getSkuId());
            if (sku == null) {
                continue;
            }
            Map<String, Object> opt = new HashMap<>();
            opt.put("id", sku.getId());
            opt.put("skuId", sku.getId());
            opt.put("value", "__SELECT__GOODS:" + sku.getId());
            opt.put("goodsName", StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            opt.put("skuName", sku.getSkuName());
            opt.put("unitName", sku.getUnitName());
            opt.put("specInfo", sku.getSpecInfo());
            opt.put("stockQty", stock.getQuantity());
            opt.put("costPrice", stock.getCostPrice());
            opt.put("label", StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName())
                    + (StringUtils.isNotBlank(sku.getSkuName()) ? " / " + sku.getSkuName() : "")
                    + " | 规格: " + StringUtils.defaultIfBlank(sku.getSpecInfo(), "无规格")
                    + " | 库存: " + stock.getQuantity() + StringUtils.defaultIfBlank(sku.getUnitName(), "")
                    + " | 成本价: " + (stock.getCostPrice() == null ? BigDecimal.ZERO : stock.getCostPrice()));
            options.add(opt);
        }

        if (options.isEmpty()) {
            state.setWarehouseId(null);
            state.setWarehouseName(null);
            state.setWarehouseInput(null);
            state.setStep("WAIT_SC_WAREHOUSE_SELECT");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state,
                    "该仓库暂无可盘点商品，请选择其他盘点仓库。",
                    owner.warehouseCandidateCard(state),
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }

        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "请选择要盘点的商品（可多选）");
        card.put("selectionMode", "multi-list");
        card.put("options", options);
        state.setStep("WAIT_SC_GOODS_SELECT");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                "盘点仓库：" + state.getWarehouseName() + "，请选择库存大于0的商品：",
                card,
                owner.conversationLifecycleSupport.quickActions("取消"));
    }

    Map<String, Object> handleStockCheckGoodsSelect(PcState state, List<Long> skuIds) {
        Long warehouseId = state.getWarehouseId();
        List<LineDraft> lines = new ArrayList<>();
        for (Long skuId : skuIds) {
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                continue;
            }
            WmsStock stock = owner.stockService.lambdaQuery()
                    .eq(WmsStock::getWarehouseId, warehouseId)
                    .eq(WmsStock::getSkuId, skuId)
                    .one();
            if (stock == null || stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            LineDraft line = new LineDraft();
            line.setSkuId(sku.getId());
            line.setGoodsName(StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            line.setSkuName(sku.getSkuName());
            line.setUnitName(sku.getUnitName());
            line.setBeforeQty(stock.getQuantity());
            line.setQuantity(stock.getQuantity());
            line.setPrice(stock.getCostPrice());
            lines.add(line);
        }

        if (lines.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "所选商品在该仓库中无可用库存，请重新选择。",
                    null,
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }

        state.setPreparedLines(lines);
        if (StringUtils.isBlank(state.getCheckType())) {
            Long availableCount = owner.stockService.lambdaQuery()
                    .eq(WmsStock::getWarehouseId, warehouseId)
                    .gt(WmsStock::getQuantity, BigDecimal.ZERO)
                    .count();
            boolean isPartialSelection = availableCount != null && availableCount > lines.size();
            state.setCheckType(isPartialSelection ? CHECK_TYPE_SAMPLE : CHECK_TYPE_FULL);
        }
        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                owner.buildConfirmPrompt("盘点单信息"),
                owner.buildExtendedPreviewCard(state),
                owner.confirmCancelQuickActions());
    }

    // ---- 库存调拨引导式流程 ----

    /**
     * 启动库存调拨引导流程：展示调出仓库候选列表
     */
    Map<String, Object> startStockTransferGuidedFlow(PcState state) {
        List<WmsWarehouseRespVO> warehouses = owner.warehouseService.selectList();
        if (warehouses == null || warehouses.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "暂无可用仓库，请先创建仓库。", null,
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }
        state.setWarehouseCandidates(warehouses.stream()
                .map(owner::toWarehouseOption).collect(Collectors.toList()));
        state.setStep("WAIT_ST_OUT_WAREHOUSE_SELECT");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                "请选择调出仓库：",
                owner.warehouseCandidateCard(state),
                owner.conversationLifecycleSupport.quickActions("取消"));
    }

    /**
     * 调出仓库选定后，查询该仓库有库存的商品并展示多选列表
     */
    Map<String, Object> showStockTransferGoodsSelection(PcState state) {
        Long outWarehouseId = state.outWarehouseId;
        List<WmsStock> stocks = owner.stockService.lambdaQuery()
                .eq(WmsStock::getWarehouseId, outWarehouseId)
                .gt(WmsStock::getQuantity, BigDecimal.ZERO)
                .list();
        if (stocks == null || stocks.isEmpty()) {
            state.outWarehouseId = null;
            state.outWarehouseName = null;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state,
                    "该仓库暂无库存商品，请重新选择调出仓库。", null,
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }

        List<Map<String, Object>> options = new ArrayList<>();
        for (WmsStock stock : stocks) {
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(stock.getSkuId());
            if (sku == null) {
                continue;
            }
            Map<String, Object> opt = new HashMap<>();
            opt.put("id", sku.getId());
            opt.put("skuId", sku.getId());
            opt.put("value", "__SELECT__GOODS:" + sku.getId());
            opt.put("goodsName", StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName()));
            opt.put("skuName", sku.getSkuName());
            opt.put("unitName", sku.getUnitName());
            opt.put("specInfo", sku.getSpecInfo());
            opt.put("stockQty", stock.getQuantity());
            opt.put("costPrice", stock.getCostPrice());
            opt.put("label", StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName())
                    + (StringUtils.isNotBlank(sku.getSkuName()) ? " / " + sku.getSkuName() : "")
                    + " | 库存: " + stock.getQuantity()
                    + (sku.getUnitName() != null ? sku.getUnitName() : ""));
            options.add(opt);
        }
        if (options.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "该仓库暂无可调拨的商品。", null,
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }

        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "请选择要调拨的商品（可多选）");
        card.put("selectionMode", "multi-list");
        card.put("options", options);
        state.setStep("WAIT_ST_GOODS_SELECT");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                "调出仓库：" + state.outWarehouseName + "，请选择要调拨的商品：",
                card,
                owner.conversationLifecycleSupport.quickActions("取消"));
    }

    /**
     * 用户选择商品后，构建 LineDraft 并展示确认卡片
     */
    Map<String, Object> handleStockTransferGoodsSelect(PcState state, List<Long> skuIds) {
        Long outWarehouseId = state.outWarehouseId;
        List<LineDraft> lines = new ArrayList<>();
        for (Long skuId : skuIds) {
            WmsGoodsSkuRespVO sku = owner.goodsSkuService.get(skuId);
            if (sku == null) {
                continue;
            }
            WmsStock stock = owner.stockService.lambdaQuery()
                    .eq(WmsStock::getWarehouseId, outWarehouseId)
                    .eq(WmsStock::getSkuId, skuId)
                    .one();
            if (stock == null || stock.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            LineDraft line = new LineDraft();
            line.skuId = sku.getId();
            line.goodsName = StringUtils.defaultIfBlank(sku.getGoodsName(), sku.getSkuName());
            line.skuName = sku.getSkuName();
            line.unitName = sku.getUnitName();
            line.quantity = stock.getQuantity();
            line.price = stock.getCostPrice();
            line.maxQty = stock.getQuantity();
            lines.add(line);
        }
        if (lines.isEmpty()) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "所选商品在调出仓库中无可用库存。", null,
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }
        state.preparedLines = lines;
        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                owner.buildConfirmPrompt("调拨单信息"),
                owner.buildExtendedPreviewCard(state),
                owner.confirmCancelQuickActions());
    }
}

