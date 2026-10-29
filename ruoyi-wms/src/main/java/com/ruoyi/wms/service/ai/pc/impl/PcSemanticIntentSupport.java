package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.ai.VoiceParseResultVO;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class PcSemanticIntentSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private final PcMultiBillChatServiceImpl owner;

    PcSemanticIntentSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> handleSemanticCreateIntent(PcState state, String input, String action) {
        VoiceParseResultVO parsed = null;
        try {
            parsed = owner.voiceParseService.parseText(input);
        } catch (Exception ignored) {
            // 语义服务异常时按未解析处理，由后续补槽流程接管
        }

        String domain = null;
        if ("CREATE_PO".equals(action)) {
            domain = DOMAIN_PO;
        } else if ("CREATE_PI".equals(action)) {
            domain = DOMAIN_PI;
        } else if ("CREATE_PR".equals(action)) {
            domain = DOMAIN_PR;
        } else if ("CREATE_SO".equals(action)) {
            domain = DOMAIN_SO;
        } else if ("CREATE_SOUT".equals(action)) {
            domain = DOMAIN_SOUT;
        } else if ("CREATE_SR".equals(action)) {
            domain = DOMAIN_SR;
        } else if ("CREATE_SC".equals(action)) {
            domain = DOMAIN_SC;
        } else if ("CREATE_ST".equals(action)) {
            domain = DOMAIN_ST;
        } else {
            domain = detectDomainByText(input);
        }

        // 保留已确定的出库/入库/退货模式，避免 resetFlow 清除后丢失"直接/关联"状态
        String savedSalesOutMode = state.salesOutMode;
        String savedPurchaseInMode = state.purchaseInMode;
        String savedSalesReturnMode = state.salesReturnMode;

        state.resetFlow();

        if (DOMAIN_SOUT.equals(domain) && StringUtils.isNotBlank(savedSalesOutMode)) {
            state.salesOutMode = savedSalesOutMode;
        }
        if ((DOMAIN_PI.equals(domain) || DOMAIN_PR.equals(domain)) && StringUtils.isNotBlank(savedPurchaseInMode)) {
            state.purchaseInMode = savedPurchaseInMode;
        }
        if (DOMAIN_SR.equals(domain) && StringUtils.isNotBlank(savedSalesReturnMode)) {
            state.salesReturnMode = savedSalesReturnMode;
        }

        if (domain == null) {
            state.setStep("WAIT_DOMAIN_CHOICE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(state,
                    "我理解到你在说采购，但还不能确定是“采购订单”还是“采购入库”。",
                    owner.domainChoiceCard(),
                    owner.conversationLifecycleSupport.quickActions("采购订单", "采购入库"));
        }

        state.setDomainType(domain);
        state.setAction("CREATE");
        state.lastIntentText = input;

        fillSlotsFromSemanticResult(state, input, parsed);

        if (owner.isPurchaseDomain(domain) && StringUtils.isBlank(state.getSupplierInput())) {
            String supplierHint = owner.itemParseSupport.extractSupplierName(input);
            if (StringUtils.isNotBlank(supplierHint)) {
                state.setSupplierInput(supplierHint);
            }
        }

        if (owner.supportsLinkedSource(domain) && StringUtils.isBlank(state.linkOrderNo)
                && owner.itemParseSupport.hasLinkedSourceIntent(input)) {
            state.linkOrderNo = owner.itemParseSupport.extractLinkOrderNo(input);
            if ((DOMAIN_PI.equals(domain) || DOMAIN_PR.equals(domain)) && StringUtils.isNotBlank(state.linkOrderNo)) {
                state.purchaseInMode = "LINKED";
            }
        }

        if ((DOMAIN_PI.equals(domain) || DOMAIN_PR.equals(domain)) && StringUtils.isBlank(state.getWarehouseInput())) {
            String warehouseHint = owner.itemParseSupport.extractWarehouseHint(input);
            if (StringUtils.isNotBlank(warehouseHint)) {
                state.setWarehouseInput(warehouseHint);
            }
        }

        if (DOMAIN_PR.equals(domain) && StringUtils.isBlank(state.reason)) {
            state.reason = owner.itemParseSupport.extractReason(input);
        }

        if (DOMAIN_PI.equals(domain) && state.getWarehouseId() == null && StringUtils.isNotBlank(state.getWarehouseInput())) {
            owner.resolveWarehouseByInput(state, state.getWarehouseInput());
            if ("WAIT_WAREHOUSE_CANDIDATE".equals(state.getStep()) || "WAIT_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
                owner.conversationLifecycleSupport.saveState(state);
                return owner.buildWarehouseFollowup(state);
            }
        }

        if (owner.isPurchaseDomain(domain)) {
            owner.backfillPurchaseDraftsFromRawInput(state, input);
        }

        boolean requireDirectPrice = false;
        if ((state.getItemDrafts().isEmpty() || owner.itemParseSupport.hasIncompleteItemDrafts(state.getItemDrafts(), requireDirectPrice))
                && StringUtils.isNotBlank(state.getItemsInput())) {
            List<ItemDraft> drafts = requireDirectPrice ? owner.itemParseSupport.parseItems(state.getItemsInput()) : owner.itemParseSupport.parseItemsAllowPriceOptional(state.getItemsInput());
            if (!drafts.isEmpty()) {
                state.setItemDrafts(drafts);
                if (DOMAIN_PI.equals(domain) || DOMAIN_PR.equals(domain)) {
                    state.purchaseInMode = "DIRECT";
                }
            }
        }

        if (!owner.isPurchaseDomain(domain)) {
            owner.log().info("=== handleSemanticCreateIntent 非采购域, 当前 state.reason: {}", state.reason);
            if (StringUtils.isBlank(state.reason)) {
                String extractedReason = owner.itemParseSupport.extractReason(input);
                owner.log().info("=== extractReason 从输入提取: {}", extractedReason);
                state.reason = extractedReason;
            }
            if (StringUtils.isBlank(state.checkType)) {
                state.checkType = owner.itemParseSupport.extractCheckType(input);
            }
            if (StringUtils.isBlank(state.linkOrderNo)) {
                state.linkOrderNo = owner.itemParseSupport.extractLinkOrderNo(input);
            }
            if (DOMAIN_ST.equals(domain)) {
                owner.fillTransferWarehousesFromText(state, input);
            } else if ((DOMAIN_PR.equals(domain) || DOMAIN_SOUT.equals(domain) || DOMAIN_SR.equals(domain) || DOMAIN_SC.equals(domain))
                    && StringUtils.isBlank(state.getWarehouseInput())) {
                String warehouseHint = owner.itemParseSupport.extractWarehouseHint(input);
                if (StringUtils.isNotBlank(warehouseHint)) {
                    state.setWarehouseInput(warehouseHint);
                }
            }
            // 语义解析失败时，尝试从原始输入中提取商品信息（与采购域 backfill 逻辑对齐）
            if (state.getItemDrafts().isEmpty() && StringUtils.isBlank(state.getItemsInput())) {
                List<ItemDraft> rawDrafts = DOMAIN_SC.equals(domain)
                        ? owner.itemParseSupport.parseNameOnlyItems(input)
                        : owner.itemParseSupport.parseItemsAllowPriceOptional(input);
                if (!rawDrafts.isEmpty()) {
                    state.setItemDrafts(rawDrafts);
                    state.setItemsInput(owner.purchaseDraftSupport.buildItemsInputFromDrafts(rawDrafts));
                }
            }
            if (state.getItemDrafts().isEmpty() && StringUtils.isNotBlank(state.getItemsInput())) {
                List<ItemDraft> drafts = DOMAIN_SC.equals(domain) ? owner.itemParseSupport.parseNameOnlyItems(state.getItemsInput()) : owner.itemParseSupport.parseItemsAllowPriceOptional(state.getItemsInput());
                if (!drafts.isEmpty()) {
                    state.setItemDrafts(drafts);
                }
            }
            if (DOMAIN_SC.equals(domain) && state.getItemDrafts().isEmpty() && owner.containsAllGoodsKeyword(state.getItemsInput())) {
                state.preparedLines = new ArrayList<>();
            }
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueExtendedFlow(state);
        }

        if (DOMAIN_PO.equals(domain) && !owner.purchaseDraftSupport.hasCompletePurchaseSentence(state)) {
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.purchaseDraftSupport.buildIncompletePurchaseReply(state);
        }

        owner.conversationLifecycleSupport.saveState(state);
        return owner.continueCreateFlow(state);
    }

    String detectAction(String input) {
        String text = input.toLowerCase(Locale.ROOT);
        if (text.contains("经营") || text.contains("经营分析") || text.contains("经营简报")
                || text.contains("销售报告") || text.contains("今日简报") || text.contains("营业概况")
                || text.contains("经营概况") || text.contains("业务概览")) {
            return "BUSINESS_ANALYSIS";
        }
        if ((text.contains("查") || text.contains("查询"))
                && (text.contains("单据") || text.contains("订单") || text.contains("入库") || text.contains("出库")
                || text.contains("退货") || text.contains("盘点") || text.contains("调拨")
                || text.contains("单号") || text.contains("单据号") || text.contains("订单号") || text.contains("全部"))) {
            return "QUERY_BILL_CENTER";
        }
        if (owner.containsRecentOrderQuery(text)) {
            return "QUERY_PO";
        }
        if (text.contains("查询采购订单") || (text.contains("查询") && text.contains("采购订单"))) {
            return "QUERY_PO";
        }
        if ((text.contains("订单") || text.contains("开单"))
                && !text.contains("采购") && !text.contains("供应商") && !text.contains("买")
                && !text.contains("销售") && !text.contains("客户") && !text.contains("卖")) {
            return "AMBIGUOUS_ORDER";
        }
        if ((text.contains("退货") || text.contains("退回"))
                && !text.contains("客户") && !text.contains("出库单") && !text.contains("销售")
                && !text.contains("供应商") && !text.contains("入库单") && !text.contains("采购")
                && !text.contains("退给") && !text.contains("退回给")) {
            return "AMBIGUOUS_RETURN";
        }
        if (text.contains("盘点")) {
            return "CREATE_SC";
        }
        if (text.contains("调拨") || text.contains("调货")
                || (text.contains("从") && text.contains("调") && text.contains("到") && (text.contains("仓") || text.contains("仓库")))) {
            return "CREATE_ST";
        }
        if ((text.contains("退货") || text.contains("退回")) && (text.contains("客户") || text.contains("出库单") || text.contains("销售"))) {
            return "CREATE_SR";
        }
        if ((text.contains("退货") || text.contains("退回") || text.contains("退给") || text.contains("退回给"))
                && (text.contains("供应商") || text.contains("入库单") || text.contains("采购") || text.contains("退给") || text.contains("退回给"))) {
            return "CREATE_PR";
        }
        if (text.contains("销售订单") || text.contains("卖给") || (text.contains("卖") && !text.contains("出库") && !text.contains("退货") && !text.contains("退回"))) {
            return "CREATE_SO";
        }
        if (text.contains("销售出库") || text.contains("发货") || text.contains("出货") || (text.contains("出库") && !text.contains("采购入库"))) {
            return "CREATE_SOUT";
        }
        if (text.contains("创建采购订单") || text.contains("新增采购订单")) {
            return "CREATE_PO";
        }
        if (text.contains("创建采购入库") || text.contains("新增采购入库") || text.contains("入库")) {
            return "CREATE_PI";
        }
        if (text.contains("采购") || text.contains("买")) {
            return "PURCHASE_INTENT";
        }
        return "UNKNOWN";
    }

    String resolveContextualCreateAction(PcState state, String input, String detectedAction) {
        if (state == null || !"IDLE".equals(state.getStep()) || StringUtils.isBlank(state.getDomainType())) {
            return null;
        }
        if (!"UNKNOWN".equals(detectedAction) && !"AMBIGUOUS_ORDER".equals(detectedAction)) {
            return null;
        }
        if (!DOMAIN_PO.equals(state.getDomainType())
                && !DOMAIN_PI.equals(state.getDomainType())
                && !DOMAIN_PR.equals(state.getDomainType())
                && !"SALES_ORDER".equals(state.getDomainType())
                && !DOMAIN_SOUT.equals(state.getDomainType())
                && !DOMAIN_SR.equals(state.getDomainType())) {
            return null;
        }
        if (!looksLikeCommodityInput(input)) {
            return null;
        }
        if (DOMAIN_PI.equals(state.getDomainType())) {
            return "CREATE_PI";
        }
        if (DOMAIN_PR.equals(state.getDomainType())) {
            return "CREATE_PR";
        }
        if ("SALES_ORDER".equals(state.getDomainType())) {
            return "CREATE_SO";
        }
        if (DOMAIN_SOUT.equals(state.getDomainType())) {
            return "CREATE_SOUT";
        }
        if (DOMAIN_SR.equals(state.getDomainType())) {
            return "CREATE_SR";
        }
        return "CREATE_PO";
    }

    /** 判断输入是否像商品+数量的简短录入（用于已确定单据类型后的上下文路由） */
    boolean looksLikeCommodityInput(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return input.matches(".*\\d+.*")
                || input.contains("，")
                || input.contains(",")
                || input.contains("台")
                || input.contains("箱")
                || input.contains("包")
                || input.contains("个")
                || input.contains("件")
                || input.contains("单价")
                || input.contains("元");
    }

    String detectDomainByText(String input) {
        if ((input.contains("退货") || input.contains("退回"))
                && !input.contains("客户") && !input.contains("出库单") && !input.contains("销售")
                && !input.contains("供应商") && !input.contains("入库单") && !input.contains("采购")) {
            return null;
        }
        if (input.contains("盘点")) {
            return DOMAIN_SC;
        }
        if (input.contains("调拨") || input.contains("调货")) {
            return DOMAIN_ST;
        }
        if ((input.contains("退货") || input.contains("退回")) && (input.contains("客户") || input.contains("出库单") || input.contains("销售"))) {
            return DOMAIN_SR;
        }
        if ((input.contains("退货") || input.contains("退回")) && (input.contains("供应商") || input.contains("入库单") || input.contains("采购"))) {
            return DOMAIN_PR;
        }
        if (input.contains("销售订单") || input.contains("卖给") || (input.contains("卖") && !input.contains("出库") && !input.contains("退货") && !input.contains("退回"))) {
            return DOMAIN_SO;
        }
        if (input.contains("销售出库") || input.contains("发货") || input.contains("出货") || (input.contains("出库") && !input.contains("采购入库"))) {
            return DOMAIN_SOUT;
        }
        if (input.contains("入库")) {
            return DOMAIN_PI;
        }
        if (input.contains("采购订单") || input.contains("下单") || input.contains("采购单")
                || input.contains("采购") || input.contains("买") || input.contains("进货")) {
            return DOMAIN_PO;
        }
        return null;
    }

    void fillSlotsFromSemanticResult(PcState state, String input, VoiceParseResultVO parsed) {
        owner.log().info("=== fillSlotsFromSemanticResult 开始, parsed success: {}, remark: {}",
                parsed != null ? parsed.getSuccess() : null,
                parsed != null ? parsed.getRemark() : null);
        if (parsed != null && Boolean.TRUE.equals(parsed.getSuccess())) {
            if (parsed.getSupplier() != null && StringUtils.isNotBlank(parsed.getSupplier().getName())) {
                state.setSupplierInput(parsed.getSupplier().getName().trim());
            }
            if (parsed.getCustomer() != null && StringUtils.isNotBlank(parsed.getCustomer().getName())) {
                state.customerInput = parsed.getCustomer().getName().trim();
            }
            if (StringUtils.isNotBlank(parsed.getWarehouse())) {
                state.setWarehouseInput(parsed.getWarehouse().trim());
            }
            if (StringUtils.isNotBlank(parsed.getOutWarehouse())) {
                state.outWarehouseInput = parsed.getOutWarehouse().trim();
            }
            if (StringUtils.isNotBlank(parsed.getInWarehouse())) {
                state.inWarehouseInput = parsed.getInWarehouse().trim();
            }
            if (StringUtils.isNotBlank(parsed.getRemark())) {
                owner.log().info("=== 从语义结果设置 reason: {}", parsed.getRemark().trim());
                state.reason = parsed.getRemark().trim();
            }
            if (parsed.getGoods() != null && !parsed.getGoods().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                List<ItemDraft> parsedDrafts = new ArrayList<>();
                for (VoiceParseResultVO.GoodsItem item : parsed.getGoods()) {
                    if (item == null || StringUtils.isBlank(item.getName())) {
                        continue;
                    }
                    boolean stockCheckDomain = DOMAIN_SC.equals(state.getDomainType());
                    boolean requirePrice = DOMAIN_SO.equals(state.getDomainType()) || DOMAIN_PI.equals(state.getDomainType());
                    if (stockCheckDomain) {
                        if (owner.containsAllGoodsKeyword(input)) {
                            continue;
                        }
                        if (sb.length() > 0) {
                            sb.append(";");
                        }
                        sb.append(item.getName());
                        continue;
                    }
                    if (item.getQuantity() == null) {
                        continue;
                    }
                    if (requirePrice && item.getPrice() == null) {
                        continue;
                    }
                    String unit = item.getUnit();
                    if (StringUtils.isBlank(unit)) {
                        unit = owner.itemParseSupport.inferUnitByGoodsName(input, item.getName());
                    }

                    if (sb.length() > 0) {
                        sb.append(";");
                    }
                    sb.append(item.getName()).append(",").append(item.getQuantity());
                    if (StringUtils.isNotBlank(unit)) {
                        sb.append(unit);
                    }
                    if (item.getPrice() != null) {
                        sb.append(",").append(item.getPrice());
                    }

                    ItemDraft draft = new ItemDraft();
                    draft.name = item.getName();
                    draft.unit = unit;
                    draft.quantity = item.getQuantity();
                    draft.price = item.getPrice();
                    parsedDrafts.add(draft);
                }
                if (sb.length() > 0) {
                    state.setItemsInput(sb.toString());
                }
                if (!parsedDrafts.isEmpty()) {
                    state.setItemDrafts(parsedDrafts);
                }
            }
        }
        owner.log().info("=== fillSlotsFromSemanticResult 结束, state.reason: {}", state.reason);
    }
}

