package com.ruoyi.wms.service.ai.pc.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderPageReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutPageReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.service.ai.pc.vo.IdNameOption;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

class PcLinkedSourceSupport {

    private static final int CANDIDATE_PAGE_SIZE = 3;
    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";

    private final PcMultiBillChatServiceImpl owner;

    PcLinkedSourceSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    boolean supportsLinkedSource(String domainType) {
        return DOMAIN_PI.equals(domainType) || DOMAIN_PR.equals(domainType) || DOMAIN_SOUT.equals(domainType) || DOMAIN_SR.equals(domainType);
    }

    boolean usesLinkedSource(PcState state) {
        return owner.hasLinkedSource(state) || shouldAskLinkOrder(state) || StringUtils.isNotBlank(state.linkOrderNo);
    }

    boolean shouldAskLinkOrder(PcState state) {
        return supportsLinkedSource(state.getDomainType()) && StringUtils.isBlank(state.linkOrderNo) && hasLinkedSourceIntent(state.itemsInput);
    }

    boolean hasLinkedSourceIntent(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return input.contains("关联")
                || input.contains("根据")
                || input.contains("来源单")
                || input.contains("按采购单")
                || input.contains("按订单")
                || input.contains("按入库单")
                || input.contains("按出库单")
                || input.contains("从采购单")
                || input.contains("从销售单")
                || input.contains("从入库单")
                || input.contains("从出库单");
    }

    String linkOrderExample(String domainType) {
        if (DOMAIN_PI.equals(domainType)) {
            return "PO202603190001";
        }
        if (DOMAIN_PR.equals(domainType)) {
            return "PI202603190001";
        }
        if (DOMAIN_SOUT.equals(domainType)) {
            return "SO202603190001";
        }
        if (DOMAIN_SR.equals(domainType)) {
            return "SOUT202603190001";
        }
        return "BILL202603190001";
    }

    Map<String, Object> linkOrderTemplateCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", "来源单模板");
        List<String> templates = new ArrayList<>();
        if (DOMAIN_PI.equals(state.getDomainType())) {
            templates.add("按采购单 PO202603190001 入库到一号仓库");
        } else if (DOMAIN_PR.equals(state.getDomainType())) {
            templates.add("按入库单 PI202603190001 退货，原因是破损");
        } else if (DOMAIN_SOUT.equals(state.getDomainType())) {
            templates.add("按销售单 SO202603190001 从一号仓库出库");
        } else if (DOMAIN_SR.equals(state.getDomainType())) {
            templates.add("按出库单 SOUT202603190001 退货回一号仓库");
        }
        card.put("templates", templates);
        return card;
    }

    Map<String, Object> resolveLinkedSource(PcState state) {
        List<IdNameOption> candidates = searchLinkedSourceCandidates(state);
        if (candidates.isEmpty()) {
            state.setStep("WAIT_LINK_ORDER_INPUT");
            owner.conversationLifecycleSupport.saveState(state);
            if (DOMAIN_PI.equals(state.getDomainType())) {
                return owner.conversationLifecycleSupport.buildReply(state,
                        "未找到采购订单 " + StringUtils.defaultIfBlank(state.linkOrderNo, "") + "，请检查单号是否正确。",
                        linkOrderTemplateCard(state),
                        owner.conversationLifecycleSupport.quickActions("重新输入单号", "直接入库", "取消"));
            }
            return owner.conversationLifecycleSupport.buildReply(state,
                    "未找到匹配的来源单，请重新输入来源单号。",
                    linkOrderTemplateCard(state),
                    owner.conversationLifecycleSupport.quickActions(linkOrderExample(state.getDomainType()), "取消"));
        }
        if (candidates.size() == 1) {
            applyLinkedSource(state, candidates.get(0).getId());
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return null;
        }
        state.linkOrderCandidates = candidates;
        state.linkOrderCandidatePage = 1;
        state.setStep("WAIT_LINK_ORDER_CANDIDATE");
        owner.conversationLifecycleSupport.saveState(state);
        return buildLinkOrderFollowup(state);
    }

    List<IdNameOption> searchLinkedSourceCandidates(PcState state) {
        String billNo = normalizeLinkOrderInput(state.linkOrderNo);
        state.linkOrderNo = billNo;
        List<IdNameOption> result = new ArrayList<>();
        if (DOMAIN_PI.equals(state.getDomainType())) {
            List<WmsPurchaseOrderRespVO> records = owner.purchaseOrderService.listAvailableForIn();
            for (WmsPurchaseOrderRespVO record : records) {
                if (StringUtils.isNotBlank(billNo) && !containsIgnoreCase(record.getOrderNo(), billNo)) {
                    continue;
                }
                result.add(new IdNameOption(record.getId(), record.getOrderNo() + " / " + StringUtils.defaultIfBlank(record.getSupplierName(), "未知供应商")));
            }
            return result;
        }
        if (DOMAIN_PR.equals(state.getDomainType())) {
            List<WmsPurchaseInRespVO> records = owner.purchaseInService.listAvailableForReturn();
            for (WmsPurchaseInRespVO record : records) {
                if (StringUtils.isNotBlank(billNo) && !containsIgnoreCase(record.getInNo(), billNo)) {
                    continue;
                }
                result.add(new IdNameOption(record.getId(), record.getInNo() + " / " + StringUtils.defaultIfBlank(record.getSupplierName(), "未知供应商")));
            }
            return result;
        }
        if (DOMAIN_SOUT.equals(state.getDomainType())) {
            // 优先使用关键字搜索（JOIN查询避免N+1）
            if (StringUtils.isNotBlank(billNo)) {
                List<Map<String, Object>> rows = owner.salesOrderService.searchAvailableForOut(billNo);
                for (Map<String, Object> row : rows) {
                    Long id = row.get("id") == null ? null : Long.valueOf(String.valueOf(row.get("id")));
                    String orderNo = String.valueOf(row.get("order_no"));
                    String customerName = row.get("customer_name") == null ? "未知客户" : String.valueOf(row.get("customer_name"));
                    result.add(new IdNameOption(id, orderNo + " / " + customerName));
                }
                return result;
            }
            WmsSalesOrderPageReqVO req = new WmsSalesOrderPageReqVO();
            req.setPageNo(1);
            req.setPageSize(10);
            req.setOrderNo(billNo);
            IPage<WmsSalesOrderRespVO> page = owner.salesOrderService.page(req);
            for (WmsSalesOrderRespVO record : page.getRecords()) {
                result.add(new IdNameOption(record.getId(), record.getOrderNo() + " / " + StringUtils.defaultIfBlank(record.getCustomerName(), "未知客户")));
            }
            return result;
        }
        if (DOMAIN_SR.equals(state.getDomainType())) {
            List<WmsSalesOutRespVO> list = owner.salesOutService.searchAvailableForReturn(billNo);
            for (WmsSalesOutRespVO record : list) {
                result.add(new IdNameOption(record.getId(), record.getOutNo() + " / " + StringUtils.defaultIfBlank(record.getCustomerName(), "未知客户")));
            }
            return result;
        }
        return result;
    }

    Map<String, Object> buildLinkOrderFollowup(PcState state) {
        return owner.conversationLifecycleSupport.buildReply(state, buildLinkOrderCandidateText(state), linkOrderCandidateCard(state), owner.conversationLifecycleSupport.quickActions());
    }

    String buildLinkOrderCandidateText(PcState state) {
        return "请选择来源单，可按单号筛选并查看详情。";
    }

    Map<String, Object> linkOrderCandidateCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "来源单候选");
        card.put("selectionMode", "single-list");
        card.put("page", 1);
        card.put("totalPages", 1);
        card.put("totalCount", state.linkOrderCandidates == null ? 0 : state.linkOrderCandidates.size());
        card.put("hasPrev", false);
        card.put("hasNext", false);
        card.put("allowSearch", true);
        card.put("sourceDomainType", DOMAIN_PI.equals(state.getDomainType()) ? DOMAIN_PO : state.getLinkOrderType());
        // 采购退货、销售出库、销售退货 关联模式：空列表 + 远程搜索
        boolean emptyRemote = (DOMAIN_PR.equals(state.getDomainType()) || DOMAIN_SOUT.equals(state.getDomainType()) || DOMAIN_SR.equals(state.getDomainType()))
                && (state.linkOrderCandidates == null || state.linkOrderCandidates.isEmpty());
        if (emptyRemote) {
            card.put("remoteSearch", true);
            if (DOMAIN_SOUT.equals(state.getDomainType())) {
                card.put("remoteSearchDomain", DOMAIN_SOUT);
                card.put("sourceDomainType", DOMAIN_SO);
            } else if (DOMAIN_SR.equals(state.getDomainType())) {
                card.put("remoteSearchDomain", DOMAIN_SR);
                card.put("sourceDomainType", DOMAIN_SOUT);
            } else {
                card.put("remoteSearchDomain", DOMAIN_PR);
            }
        }
        List<Map<String, Object>> options = new ArrayList<>();
        for (IdNameOption option : state.linkOrderCandidates) {
            Map<String, Object> item = option(option.getName(), "__SELECT__LINK_ORDER:" + option.getId());
            item.put("id", option.getId());
            options.add(item);
        }
        card.put("options", options);
        return card;
    }

    Map<String, Object> handleLinkOrderCandidateText(PcState state, String input) {
        // 支持"直接退货"快捷切换
        if (DOMAIN_PR.equals(state.getDomainType()) && ("直接退货".equals(input) || "放弃关联".equals(input))) {
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
        // 支持"直接出库"快捷切换
        if (DOMAIN_SOUT.equals(state.getDomainType()) && ("直接出库".equals(input) || "放弃关联".equals(input))) {
            state.salesOutMode = "DIRECT";
            state.linkOrderNo = null;
            state.linkOrderId = null;
            state.linkOrderType = null;
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.continueExtendedFlow(state);
        }
        // 支持"直接退货"快捷切换（销售退货）
        if (DOMAIN_SR.equals(state.getDomainType()) && ("直接退货".equals(input) || "放弃关联".equals(input))) {
            state.salesReturnMode = "DIRECT";
            state.linkOrderNo = null;
            state.linkOrderId = null;
            state.linkOrderType = null;
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.customerInput = null;
            state.setStep("IDLE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.buildDomainStartGuide(state);
        }
        if ("上一页".equals(input)) {
            if (state.linkOrderCandidatePage > 1) {
                state.linkOrderCandidatePage = state.linkOrderCandidatePage - 1;
                owner.conversationLifecycleSupport.saveState(state);
            }
            return buildLinkOrderFollowup(state);
        }
        if ("下一页".equals(input)) {
            if (hasNextPage(state.linkOrderCandidates, state.linkOrderCandidatePage)) {
                state.linkOrderCandidatePage = state.linkOrderCandidatePage + 1;
                owner.conversationLifecycleSupport.saveState(state);
            }
            return buildLinkOrderFollowup(state);
        }
        // 支持前端远程搜索后直接通过 __SELECT__LINK_ORDER:id 选择
        if (input != null && input.startsWith("__SELECT__LINK_ORDER:")) {
            try {
                Long sourceId = Long.parseLong(input.substring("__SELECT__LINK_ORDER:".length()).trim());
                applyLinkedSource(state, sourceId);
                state.setStep("IDLE");
                owner.conversationLifecycleSupport.saveState(state);
                return DOMAIN_PI.equals(state.getDomainType()) ? owner.continueCreateFlow(state) : owner.continueExtendedFlow(state);
            } catch (NumberFormatException ignored) {
            }
        }
        IdNameOption selected = resolveOption(state.linkOrderCandidates, input);
        if (selected == null) {
            return owner.conversationLifecycleSupport.buildReply(state, "请输入来源单序号或名称。", linkOrderCandidateCard(state), owner.conversationLifecycleSupport.quickActions());
        }
        applyLinkedSource(state, selected.getId());
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return DOMAIN_PI.equals(state.getDomainType()) ? owner.continueCreateFlow(state) : owner.continueExtendedFlow(state);
    }

    void applyLinkedSource(PcState state, Long sourceId) {
        state.linkOrderId = sourceId;
        if (DOMAIN_PI.equals(state.getDomainType())) {
            WmsPurchaseOrderRespVO order = owner.purchaseOrderService.get(sourceId);
            if (order == null) {
                throw new ServiceException("采购订单不存在");
            }
            if (!"1".equals(order.getOrderStatus()) && !"2".equals(order.getOrderStatus())) {
                throw new ServiceException("仅支持关联已审核的采购订单");
            }
            state.linkOrderType = DOMAIN_PO;
            state.linkOrderNo = order.getOrderNo();
            state.setSupplierId(order.getSupplierId());
            state.setSupplierName(order.getSupplierName());
            state.preparedLines = buildPurchaseInLinkedLines(sourceId);
            return;
        }
        if (DOMAIN_PR.equals(state.getDomainType())) {
            WmsPurchaseInRespVO in = owner.purchaseInService.get(sourceId);
            if (in == null) {
                throw new ServiceException("采购入库单不存在");
            }
            state.linkOrderType = DOMAIN_PI;
            state.linkOrderNo = in.getInNo();
            state.setSupplierId(in.getSupplierId());
            state.setSupplierName(in.getSupplierName());
            state.setWarehouseId(in.getWarehouseId());
            state.setWarehouseName(in.getWarehouseName());
            state.preparedLines = buildPurchaseReturnLinkedLines(sourceId);
            return;
        }
        if (DOMAIN_SOUT.equals(state.getDomainType())) {
            WmsSalesOrderRespVO order = owner.salesOrderService.get(sourceId);
            if (order == null) {
                throw new ServiceException("销售订单不存在");
            }
            state.linkOrderType = DOMAIN_SO;
            state.linkOrderNo = order.getOrderNo();
            state.customerId = order.getCustomerId();
            state.customerName = order.getCustomerName();
            state.preparedLines = buildSalesOutLinkedLines(sourceId);
            return;
        }
        if (DOMAIN_SR.equals(state.getDomainType())) {
            WmsSalesOutRespVO out = owner.salesOutService.get(sourceId);
            if (out == null) {
                throw new ServiceException("销售出库单不存在");
            }
            state.linkOrderType = DOMAIN_SOUT;
            state.linkOrderNo = out.getOutNo();
            state.customerId = out.getCustomerId();
            state.customerName = out.getCustomerName();
            state.setWarehouseId(out.getWarehouseId());
            state.setWarehouseName(out.getWarehouseName());
            state.preparedLines = buildSalesReturnLinkedLines(out);
        }
    }

    List<LineDraft> buildPurchaseInLinkedLines(Long orderId) {
        List<LineDraft> lines = new ArrayList<>();
        for (WmsPurchaseOrderItemRespVO item : owner.purchaseInService.getOrderItems(orderId)) {
            BigDecimal orderedQty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            BigDecimal receivedQty = item.getReceivedQty() == null ? BigDecimal.ZERO : item.getReceivedQty();
            BigDecimal availableQty = orderedQty.subtract(receivedQty).max(BigDecimal.ZERO);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            LineDraft line = new LineDraft();
            line.sourceLineId = item.getId();
            line.skuId = item.getSkuId();
            line.goodsName = item.getGoodsName();
            line.skuName = item.getSkuName();
            line.unitName = item.getUnitName();
            line.orderQuantity = orderedQty;
            line.receivedQty = receivedQty;
            line.quantity = availableQty;
            line.maxQty = availableQty;
            line.price = item.getPrice();
            line.remark = item.getRemark();
            lines.add(line);
        }
        if (lines.isEmpty()) {
            throw new ServiceException("该采购订单已全部入库，暂无可入库明细");
        }
        return lines;
    }

    List<LineDraft> buildPurchaseReturnLinkedLines(Long inId) {
        List<LineDraft> lines = new ArrayList<>();
        for (WmsPurchaseInItemRespVO item : owner.purchaseReturnService.getInItems(inId)) {
            BigDecimal inQty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            BigDecimal returnedQty = item.getReturnedQty() == null ? BigDecimal.ZERO : item.getReturnedQty();
            BigDecimal availableQty = inQty.subtract(returnedQty).max(BigDecimal.ZERO);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            LineDraft line = new LineDraft();
            line.sourceLineId = item.getId();
            line.skuId = item.getSkuId();
            line.goodsName = item.getGoodsName();
            line.skuName = item.getSkuName();
            line.unitName = item.getUnitName();
            line.quantity = availableQty;
            line.maxQty = availableQty;
            line.price = item.getPrice();
            line.remark = item.getRemark();
            lines.add(line);
        }
        if (lines.isEmpty()) {
            throw new ServiceException("该入库单已全部退货，暂无可退明细");
        }
        return lines;
    }

    List<LineDraft> buildSalesOutLinkedLines(Long orderId) {
        List<LineDraft> lines = new ArrayList<>();
        for (WmsSalesOrderItemRespVO item : owner.salesOrderService.getItems(orderId)) {
            BigDecimal orderedQty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            BigDecimal deliveredQty = item.getDeliveredQty() == null ? BigDecimal.ZERO : item.getDeliveredQty();
            BigDecimal availableQty = orderedQty.subtract(deliveredQty).max(BigDecimal.ZERO);
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            LineDraft line = new LineDraft();
            line.sourceLineId = item.getId();
            line.skuId = item.getSkuId();
            line.goodsName = item.getGoodsName();
            line.skuName = item.getSkuName();
            line.unitName = item.getUnitName();
            line.orderQuantity = orderedQty;
            line.receivedQty = deliveredQty;
            line.quantity = availableQty;
            line.maxQty = availableQty;
            line.price = item.getPrice();
            line.remark = item.getRemark();
            lines.add(line);
        }
        if (lines.isEmpty()) {
            throw new ServiceException("该销售订单已全部出库，暂无可出库明细");
        }
        return lines;
    }

    List<LineDraft> buildSalesReturnLinkedLines(WmsSalesOutRespVO out) {
        List<WmsSalesOutItemRespVO> outItems = out.getItems();
        if (outItems == null || outItems.isEmpty()) {
            outItems = owner.salesOutService.getItems(out.getId());
        }
        List<LineDraft> lines = new ArrayList<>();
        for (WmsSalesOutItemRespVO item : outItems) {
            BigDecimal availableQty = item.getAvailableQty();
            if (availableQty == null) {
                BigDecimal outQty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
                BigDecimal returnedQty = item.getReturnedQty() == null ? BigDecimal.ZERO : item.getReturnedQty();
                availableQty = outQty.subtract(returnedQty).max(BigDecimal.ZERO);
            }
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            LineDraft line = new LineDraft();
            line.sourceLineId = item.getId();
            line.skuId = item.getSkuId();
            line.goodsName = item.getGoodsName();
            line.skuName = item.getSkuName();
            line.unitName = item.getUnitName();
            line.quantity = availableQty;
            line.maxQty = availableQty;
            line.price = item.getPrice();
            line.remark = item.getRemark();
            lines.add(line);
        }
        if (lines.isEmpty()) {
            throw new ServiceException("该出库单已全部退货，暂无可退明细");
        }
        return lines;
    }

    void applyLinkedPreviewItems(PcState state, Object rawItems) {
        List<Map<String, Object>> items;
        try {
            items = owner.objectMapper.convertValue(rawItems, new TypeReference<List<Map<String, Object>>>() {});
        } catch (IllegalArgumentException ex) {
            throw new ServiceException("预览明细格式错误");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("关联单模式至少保留一条来源明细");
        }
        Map<Long, LineDraft> originalMap = state.preparedLines.stream()
                .filter(line -> line.sourceLineId != null)
                .collect(Collectors.toMap(line -> line.sourceLineId, line -> line, (a, b) -> a));
        List<LineDraft> updated = new ArrayList<>();
        LinkedHashSet<Long> seen = new LinkedHashSet<>();
        for (Map<String, Object> item : items) {
            Long sourceLineId = item.get("sourceLineId") == null ? null : Long.valueOf(String.valueOf(item.get("sourceLineId")));
            if (sourceLineId == null || !originalMap.containsKey(sourceLineId) || !seen.add(sourceLineId)) {
                throw new ServiceException("关联单模式不允许追加来源单外商品");
            }
            LineDraft original = originalMap.get(sourceLineId);
            BigDecimal quantity = toBigDecimal(item.get("quantity"));
            if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("数量必须大于0");
            }
            if (original.maxQty != null && quantity.compareTo(original.maxQty) > 0) {
                throw new ServiceException("商品【" + original.goodsName + "】数量超过来源单可用数量");
            }
            LineDraft line = new LineDraft();
            line.sourceLineId = original.sourceLineId;
            line.skuId = original.skuId;
            line.goodsName = original.goodsName;
            line.skuName = original.skuName;
            line.unitName = original.unitName;
            line.orderQuantity = original.orderQuantity;
            line.receivedQty = original.receivedQty;
            line.beforeQty = original.beforeQty;
            line.maxQty = original.maxQty;
            line.remark = original.remark;
            line.quantity = quantity;
            line.price = original.price;
            updated.add(line);
        }
        state.preparedLines = updated;
    }

    private boolean containsIgnoreCase(String source, String target) {
        return StringUtils.isNotBlank(source)
                && StringUtils.isNotBlank(target)
                && source.toLowerCase(Locale.ROOT).contains(target.toLowerCase(Locale.ROOT));
    }

    Map<String, Object> continuePurchaseInLinkedFlow(PcState state) {
        if (state.linkOrderId == null) {
            if (StringUtils.isBlank(state.linkOrderNo)) {
                state.setStep("WAIT_LINK_ORDER_INPUT");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请提供采购订单单号，例如 PO202603190001。",
                        linkOrderTemplateCard(state),
                        owner.conversationLifecycleSupport.quickActions("PO202603190001", "直接入库", "取消"));
            }
            Map<String, Object> sourceReply = resolveLinkedSource(state);
            if (sourceReply != null) {
                return sourceReply;
            }
        }

        if (state.preparedLines == null || state.preparedLines.isEmpty()) {
            throw new ServiceException("关联采购订单没有可入库商品");
        }

        state.setPreparedPiItems(buildPurchaseInItemsFromLines(state.preparedLines));
        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
            owner.buildConfirmPrompt("采购入库信息"),
                owner.buildExtendedPreviewCard(state),
            owner.confirmCancelQuickActions());
    }

    Map<String, Object> continuePurchaseReturnLinkedFlow(PcState state) {
        if (state.linkOrderId == null) {
            if (StringUtils.isBlank(state.linkOrderNo)) {
                state.setStep("WAIT_LINK_ORDER_INPUT");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        "请提供采购入库单号，例如 PI202603190001。",
                        linkOrderTemplateCard(state),
                        owner.conversationLifecycleSupport.quickActions("PI202603190001", "取消"));
            }
            Map<String, Object> sourceReply = resolveLinkedSource(state);
            if (sourceReply != null) {
                return sourceReply;
            }
        }

        if (state.getWarehouseId() == null) {
            if (StringUtils.isBlank(state.getWarehouseInput())) {
                state.setStep("WAIT_WAREHOUSE_INPUT");
                owner.conversationLifecycleSupport.saveState(state);
                return owner.conversationLifecycleSupport.buildReply(state,
                        "还缺仓库信息，请输入仓库名称。",
                        owner.warehouseTemplateCard(),
                        owner.conversationLifecycleSupport.quickActions("A仓", "B仓", "以上都不是"));
            }
            owner.resolveWarehouseByInput(state, state.getWarehouseInput());
            owner.conversationLifecycleSupport.saveState(state);
            if (!"IDLE".equals(state.getStep())) {
                return owner.buildWarehouseFollowup(state);
            }
        }

        if (state.preparedLines == null || state.preparedLines.isEmpty()) {
            throw new ServiceException("关联采购入库单没有可退货商品");
        }

        state.setPreparedPiItems(buildPurchaseInItemsFromLines(state.preparedLines));
        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
            owner.buildConfirmPrompt("采购退货信息"),
                owner.buildExtendedPreviewCard(state),
            owner.confirmCancelQuickActions());
    }

    Map<String, Object> continueSalesOutLinkedFlow(PcState state) {
        if (state.linkOrderId == null) {
            // 回到候选卡片让用户搜索选择
            state.linkOrderCandidates = new ArrayList<>();
            state.linkOrderCandidatePage = 1;
            state.setStep("WAIT_LINK_ORDER_CANDIDATE");
            owner.conversationLifecycleSupport.saveState(state);
            return owner.conversationLifecycleSupport.buildReply(
                    state,
                    "请搜索选择要出库的销售订单。",
                    linkOrderCandidateCard(state),
                    owner.conversationLifecycleSupport.quickActions("直接出库", "取消")
            );
        }

        // 仓库在确认卡片中通过下拉框选择（allowWarehouseEdit），无需单独步骤
        if (state.preparedLines == null || state.preparedLines.isEmpty()) {
            throw new ServiceException("关联销售订单没有可出库商品");
        }

        state.setStep("WAIT_CONFIRM_EXEC");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
            owner.buildConfirmPrompt("销售出库确认信息"),
                owner.buildExtendedPreviewCard(state),
            owner.confirmCancelQuickActions());
    }

    private List<WmsPurchaseInItemSaveReqVO> buildPurchaseInItemsFromLines(List<LineDraft> lines) {
        List<WmsPurchaseInItemSaveReqVO> items = new ArrayList<>();
        if (lines == null) {
            return items;
        }
        for (LineDraft line : lines) {
            if (line == null) {
                continue;
            }
            WmsPurchaseInItemSaveReqVO item = new WmsPurchaseInItemSaveReqVO();
            item.setSkuId(line.getSkuId());
            item.setQuantity(line.getQuantity());
            item.setPrice(line.getPrice());
            item.setRemark(line.getRemark());
            items.add(item);
        }
        return items;
    }

    private String normalizeLinkOrderInput(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String extracted = extractLinkOrderNo(input);
        return StringUtils.isNotBlank(extracted) ? extracted : input.trim().toUpperCase(Locale.ROOT);
    }

    private String extractLinkOrderNo(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        Matcher matcher = owner.linkOrderPattern().matcher(input.toUpperCase(Locale.ROOT));
        while (matcher.find()) {
            String value = matcher.group("no");
            if (StringUtils.isNotBlank(value) && value.length() >= 6) {
                return value;
            }
        }
        return null;
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

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private Map<String, Object> option(String label, String value) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }
}
