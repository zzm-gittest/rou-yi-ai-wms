package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.IdNameOption;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PcPartyFlowSupport {

    private static final int CANDIDATE_PAGE_SIZE = 3;
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";

    private final PcMultiBillChatServiceImpl owner;

    PcPartyFlowSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    void resolveSupplierByInput(PcState state, String input) {
        List<WmsSupplierRespVO> list = owner.supplierService.selectList();
        List<WmsSupplierRespVO> candidates = list.stream()
                .filter(item -> containsIgnoreCase(item.getSupplierName(), input))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            state.setStep("WAIT_SUPPLIER_CREATE_CONFIRM");
            return;
        }
        WmsSupplierRespVO exact = candidates.stream()
                .filter(item -> equalsIgnoreCase(item.getSupplierName(), input))
                .findFirst()
                .orElse(null);
        if (exact != null || candidates.size() == 1) {
            WmsSupplierRespVO chosen = exact != null ? exact : candidates.get(0);
            state.setSupplierId(chosen.getId());
            state.setSupplierName(chosen.getSupplierName());
            state.setSupplierInput(chosen.getSupplierName());
            state.setStep("IDLE");
            return;
        }
        state.setStep("WAIT_SUPPLIER_CANDIDATE");
        state.setSupplierCandidatePage(1);
        state.setSupplierCandidates(candidates.stream().map(this::toSupplierOption).collect(Collectors.toList()));
    }

    void resolveWarehouseByInput(PcState state, String input) {
        String cleaned = input.replaceAll("^[从到在去往]", "").trim();
        if (cleaned.isEmpty()) cleaned = input;
        final String keyword = cleaned;
        List<WmsWarehouseRespVO> list = owner.warehouseService.selectList();
        List<WmsWarehouseRespVO> candidates = list.stream()
                .filter(item -> containsIgnoreCase(item.getWarehouseName(), keyword) || containsIgnoreCase(keyword, item.getWarehouseName()))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            state.setStep("WAIT_WAREHOUSE_CUSTOM_FORM");
            return;
        }
        if (candidates.size() == 1) {
            state.setWarehouseId(candidates.get(0).getId());
            state.setWarehouseName(candidates.get(0).getWarehouseName());
            state.setStep("IDLE");
            return;
        }
        state.setStep("WAIT_WAREHOUSE_CANDIDATE");
        state.setWarehouseCandidates(candidates.stream().map(this::toWarehouseOption).collect(Collectors.toList()));
    }

    void resolveCustomerByInput(PcState state, String input, boolean allowCreate) {
        String keyword = StringUtils.defaultIfBlank(owner.itemParseSupport.extractCustomerName(input), input);
        List<WmsCustomerRespVO> list = owner.customerService.selectList();
        List<WmsCustomerRespVO> candidates = list.stream()
                .filter(item -> containsIgnoreCase(item.getCustomerName(), keyword) || containsIgnoreCase(keyword, item.getCustomerName()))
                .collect(Collectors.toList());
        if (candidates.isEmpty()) {
            state.step = allowCreate ? "WAIT_CUSTOMER_CUSTOM_FORM" : "WAIT_CUSTOMER_INPUT";
            state.customerCandidates = new ArrayList<>();
            state.customerInput = keyword;
            return;
        }
        if (candidates.size() == 1) {
            state.customerId = candidates.get(0).getId();
            state.customerName = candidates.get(0).getCustomerName();
            state.customerInput = candidates.get(0).getCustomerName();
            state.step = "IDLE";
            return;
        }
        state.customerInput = keyword;
        state.step = "WAIT_CUSTOMER_CANDIDATE";
        state.customerCandidatePage = 1;
        state.customerCandidates = candidates.stream()
                .map(item -> new IdNameOption(item.getId(), item.getCustomerName()))
                .collect(Collectors.toList());
    }

    Map<String, Object> buildSupplierFollowup(PcState state) {
        if ("WAIT_SUPPLIER_CREATE_CONFIRM".equals(state.getStep())) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "未找到供应商【" + state.getSupplierInput() + "】，是否创建该供应商？请点击【确认】创建，或点击【取消】放弃。",
                    null,
                    owner.confirmCancelQuickActions());
        }
        if ("WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "没有匹配到供应商。你可以在聊天内直接补录供应商信息（不跳转页面）。",
                    supplierFormCard(state),
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }
        return owner.conversationLifecycleSupport.buildReply(state, buildSupplierCandidateText(state), supplierCandidateCard(state), supplierCandidateActions(state));
    }

    Map<String, Object> buildWarehouseFollowup(PcState state) {
        if ("WAIT_WAREHOUSE_CUSTOM_FORM".equals(state.getStep())) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    "没有匹配到仓库。你可以在聊天内直接补录仓库信息（不跳转页面）。",
                    warehouseFormCard(state),
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }
        return owner.conversationLifecycleSupport.buildReply(state, "请确认仓库。", warehouseCandidateCard(state), owner.conversationLifecycleSupport.quickActions("以上都不是"));
    }

    Map<String, Object> buildCustomerFollowup(PcState state, boolean allowCreate) {
        if ("WAIT_CUSTOMER_CUSTOM_FORM".equals(state.step)) {
            return owner.conversationLifecycleSupport.buildReply(state,
                    allowCreate ? "没有匹配到客户。你可以在聊天内直接补录客户信息。" : "未找到可用客户，请更换关键词重试或先到基础资料维护客户。",
                    allowCreate ? customerFormCard(state) : customerTemplateCard(state.getDomainType(), false),
                    owner.conversationLifecycleSupport.quickActions("取消"));
        }
        if ("WAIT_CUSTOMER_CANDIDATE".equals(state.step)) {
            return owner.conversationLifecycleSupport.buildReply(state, buildCustomerCandidateText(state), customerCandidateCard(state, allowCreate), owner.conversationLifecycleSupport.quickActions());
        }
        return owner.conversationLifecycleSupport.buildReply(state,
                allowCreate ? "请提供客户名称，我会自动匹配或引导新增。" : "请提供客户名称，我会从现有客户中匹配。",
                customerTemplateCard(state.getDomainType(), allowCreate),
                owner.conversationLifecycleSupport.quickActions("取消"));
    }

    String buildSupplierCandidateText(PcState state) {
        List<IdNameOption> options = currentPageOptions(state.getSupplierCandidates(), state.getSupplierCandidatePage());
        StringBuilder sb = new StringBuilder();
        sb.append("找到多个匹配供应商，请选择：\n");
        int offset = (state.getSupplierCandidatePage() - 1) * CANDIDATE_PAGE_SIZE;
        for (int index = 0; index < options.size(); index++) {
            sb.append(offset + index + 1).append(". ").append(options.get(index).getName()).append("\n");
        }
        if (hasNextPage(state.getSupplierCandidates(), state.getSupplierCandidatePage())) {
            sb.append("...\n");
        }
        sb.append("请在列表中勾选后点击确定，也可以翻页或输入关键词继续筛选。\n");
        sb.append("当前第 ").append(state.getSupplierCandidatePage()).append("/").append(totalPages(state.getSupplierCandidates())).append(" 页");
        return sb.toString();
    }

    List<String> supplierCandidateActions(PcState state) {
        return new ArrayList<>();
    }

    Map<String, Object> supplierCandidateCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "供应商有多个候选，请选择");
        card.put("selectionMode", "single-list");
        card.put("page", state.getSupplierCandidatePage());
        card.put("totalPages", totalPages(state.getSupplierCandidates()));
        card.put("totalCount", state.getSupplierCandidates() == null ? 0 : state.getSupplierCandidates().size());
        card.put("hasPrev", state.getSupplierCandidatePage() > 1);
        card.put("hasNext", hasNextPage(state.getSupplierCandidates(), state.getSupplierCandidatePage()));
        List<Map<String, Object>> options = new ArrayList<>();
        for (IdNameOption option : currentPageOptions(state.getSupplierCandidates(), state.getSupplierCandidatePage())) {
            options.add(option(option.getName(), "__SELECT__SUPPLIER:" + option.getId()));
        }
        card.put("options", options);
        card.put("otherOption", option("以上都不是", "__SELECT__SUPPLIER:OTHER"));
        return card;
    }

    Map<String, Object> warehouseCandidateCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "仓库有多个候选，请选择");
        card.put("selectionMode", "single-list");
        card.put("page", 1);
        card.put("totalPages", 1);
        card.put("totalCount", state.getWarehouseCandidates() == null ? 0 : state.getWarehouseCandidates().size());
        card.put("hasPrev", false);
        card.put("hasNext", false);
        List<Map<String, Object>> options = new ArrayList<>();
        for (IdNameOption option : state.getWarehouseCandidates()) {
            options.add(option(option.getName(), "__SELECT__WAREHOUSE:" + option.getId()));
        }
        card.put("options", options);
        card.put("otherOption", option("以上都不是", "__SELECT__WAREHOUSE:OTHER"));
        return card;
    }

    Map<String, Object> supplierFormCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "REQUIRED_FORM");
        card.put("title", "供应商必填信息（聊天内补录）");
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(formField("supplierName", "供应商名称", "请输入供应商名称", true, state == null ? null : state.getSupplierInput()));
        fields.add(formField("contact", "联系人", "请输入联系人", false));
        fields.add(formField("phone", "联系电话", "请输入联系电话", false));
        card.put("fields", fields);
        card.put("cancelAction", "__ACTION__:CANCEL_SLOT");
        card.put("cancelLabel", "返回当前流程");
        return card;
    }

    Map<String, Object> warehouseFormCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "REQUIRED_FORM");
        card.put("title", "仓库必填信息（聊天内补录）");
        List<Map<String, Object>> fields = new ArrayList<>();
        String warehouseName = state == null ? null : StringUtils.defaultIfBlank(state.getWarehouseInput(), state.getWarehouseName());
        fields.add(formField("warehouseName", "仓库名称", "请输入仓库名称", true, warehouseName));
        fields.add(formField("contact", "联系人", "请输入联系人", false));
        fields.add(formField("phone", "联系电话", "请输入联系电话", false));
        card.put("fields", fields);
        card.put("cancelAction", "__ACTION__:CANCEL_SLOT");
        card.put("cancelLabel", "返回当前流程");
        return card;
    }

    Map<String, Object> customerTemplateCard(String domainType, boolean allowCreate) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "TEMPLATE");
        card.put("title", allowCreate ? "客户补全模板" : "客户选择提示");
        List<Map<String, Object>> templates = new ArrayList<>();
        if (allowCreate) {
            if (DOMAIN_SOUT.equals(domainType)) {
                templates.add(template("给客户A从一号仓库发苹果笔记本10台", routeValue(DOMAIN_SOUT, "给客户A从一号仓库发苹果笔记本10台")));
                templates.add(template("按销售单 SO202603190001 从一号仓库给客户A出库", routeValue(DOMAIN_SOUT, "按销售单 SO202603190001 从一号仓库给客户A出库")));
            } else if (DOMAIN_SR.equals(domainType)) {
                templates.add(template("客户A退回苹果笔记本2台，退回一号仓库，原因是拒收", routeValue(DOMAIN_SR, "客户A退回苹果笔记本2台，退回一号仓库，原因是拒收")));
                templates.add(template("按出库单 SOUT202603190001 客户A退回苹果笔记本2台", routeValue(DOMAIN_SR, "按出库单 SOUT202603190001 客户A退回苹果笔记本2台，退回一号仓库，原因是拒收")));
            } else {
                templates.add(template("卖给客户A苹果笔记本10台，单价9000元", routeValue(DOMAIN_SO, "卖给客户A苹果笔记本10台，单价9000元")));
                templates.add(template("客户是华东旗舰店，商品苹果笔记本，数量10，单价9000", routeValue(DOMAIN_SO, "客户是华东旗舰店，商品苹果笔记本，数量10，单价9000")));
            }
        } else {
            templates.add(template("请提供现有客户名称，例如：客户A", routeValue(StringUtils.defaultIfBlank(domainType, DOMAIN_SO), "客户A")));
            templates.add(template("未找到时请更换关键词重试，或先到基础资料维护客户", routeValue(StringUtils.defaultIfBlank(domainType, DOMAIN_SO), "客户A")));
        }
        card.put("templates", templates);
        return card;
    }

    Map<String, Object> customerFormCard(PcState state) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "REQUIRED_FORM");
        card.put("title", "新增客户");
        List<Map<String, Object>> fields = new ArrayList<>();
        String customerName = state == null ? null : StringUtils.defaultIfBlank(state.customerInput, state.customerName);
        fields.add(formField("customerName", "客户名称", "请输入客户名称", true, customerName));
        fields.add(formField("contact", "联系人", "请输入联系人", false));
        fields.add(formField("phone", "联系电话", "请输入联系电话", false));
        fields.add(formField("address", "地址", "请输入地址", false));
        card.put("fields", fields);
        card.put("formModel", new HashMap<>());
        card.put("cancelAction", "__ACTION__:CANCEL_SLOT");
        card.put("cancelLabel", "返回当前流程");
        return card;
    }

    String buildCustomerCandidateText(PcState state) {
        List<IdNameOption> options = currentPageOptions(state.customerCandidates, state.customerCandidatePage);
        StringBuilder sb = new StringBuilder();
        sb.append("找到多个匹配客户，请选择：\n");
        int offset = (state.customerCandidatePage - 1) * CANDIDATE_PAGE_SIZE;
        for (int index = 0; index < options.size(); index++) {
            sb.append(offset + index + 1).append(". ").append(options.get(index).getName()).append("\n");
        }
        sb.append("当前第 ").append(state.customerCandidatePage).append("/").append(totalPages(state.customerCandidates)).append(" 页");
        return sb.toString();
    }

    Map<String, Object> customerCandidateCard(PcState state, boolean allowCreate) {
        Map<String, Object> card = new HashMap<>();
        card.put("type", "CANDIDATE");
        card.put("title", "客户有多个候选，请选择");
        card.put("selectionMode", "single-list");
        card.put("page", state.customerCandidatePage);
        card.put("totalPages", totalPages(state.customerCandidates));
        card.put("totalCount", state.customerCandidates == null ? 0 : state.customerCandidates.size());
        card.put("hasPrev", state.customerCandidatePage > 1);
        card.put("hasNext", hasNextPage(state.customerCandidates, state.customerCandidatePage));
        List<Map<String, Object>> options = new ArrayList<>();
        for (IdNameOption option : currentPageOptions(state.customerCandidates, state.customerCandidatePage)) {
            options.add(option(option.getName(), "__SELECT__CUSTOMER:" + option.getId()));
        }
        card.put("options", options);
        if (allowCreate) {
            card.put("otherOption", option("以上都不是", "__SELECT__CUSTOMER:OTHER"));
        }
        return card;
    }

    Map<String, Object> handleCustomerCandidateText(PcState state, String input, boolean allowCreate) {
        if ("上一页".equals(input)) {
            if (state.customerCandidatePage > 1) {
                state.customerCandidatePage = state.customerCandidatePage - 1;
                owner.conversationLifecycleSupport.saveState(state);
            }
            return buildCustomerFollowup(state, allowCreate);
        }
        if ("下一页".equals(input)) {
            if (hasNextPage(state.customerCandidates, state.customerCandidatePage)) {
                state.customerCandidatePage = state.customerCandidatePage + 1;
                owner.conversationLifecycleSupport.saveState(state);
            }
            return buildCustomerFollowup(state, allowCreate);
        }
        if (allowCreate && isOtherChoice(input)) {
            state.step = "WAIT_CUSTOMER_CUSTOM_FORM";
            owner.conversationLifecycleSupport.saveState(state);
            return buildCustomerFollowup(state, allowCreate);
        }
        IdNameOption selected = resolveOption(state.customerCandidates, input);
        if (selected == null) {
            return owner.conversationLifecycleSupport.buildReply(state, "请输入客户序号或名称。", customerCandidateCard(state, allowCreate), owner.conversationLifecycleSupport.quickActions());
        }
        WmsCustomerRespVO customer = findCustomerById(selected.getId());
        if (customer == null) {
            throw new ServiceException("客户不存在");
        }
        state.customerId = customer.getId();
        state.customerName = customer.getCustomerName();
        state.customerInput = customer.getCustomerName();
        state.step = "IDLE";
        owner.conversationLifecycleSupport.saveState(state);
        return owner.continueExtendedFlow(state);
    }

    Map<String, Object> handleCustomerCustomText(PcState state, String input) {
        String name = input.trim();
        if (StringUtils.isBlank(name)) {
            return owner.conversationLifecycleSupport.buildReply(state, "请补充客户名称，或者直接提交下方表单。", customerFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        WmsCustomerSaveReqVO req = new WmsCustomerSaveReqVO();
        req.setCustomerName(name);
        Long id = owner.customerService.create(req);
        state.customerId = id;
        state.customerName = name;
        state.step = "IDLE";
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleSupplierCandidateText(PcState state, String input) {
        if ("上一页".equals(input)) {
            if (state.getSupplierCandidatePage() > 1) {
                state.setSupplierCandidatePage(state.getSupplierCandidatePage() - 1);
                owner.conversationLifecycleSupport.saveState(state);
                return buildSupplierFollowup(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, "已经是第一页了。", supplierCandidateCard(state), supplierCandidateActions(state));
        }
        if ("下一页".equals(input)) {
            if (hasNextPage(state.getSupplierCandidates(), state.getSupplierCandidatePage())) {
                state.setSupplierCandidatePage(state.getSupplierCandidatePage() + 1);
                owner.conversationLifecycleSupport.saveState(state);
                return buildSupplierFollowup(state);
            }
            return owner.conversationLifecycleSupport.buildReply(state, "已经是最后一页了。你也可以直接输入关键词继续缩小范围。", supplierCandidateCard(state), supplierCandidateActions(state));
        }
        if (isOtherChoice(input)) {
            state.setStep("WAIT_SUPPLIER_CUSTOM_FORM");
            owner.conversationLifecycleSupport.saveState(state);
            return buildSupplierFollowup(state);
        }
        IdNameOption selected = resolveOption(state.getSupplierCandidates(), input);
        if (selected == null) {
            state.setSupplierInput(input);
            resolveSupplierByInput(state, input);
            owner.conversationLifecycleSupport.saveState(state);
            if ("WAIT_SUPPLIER_CANDIDATE".equals(state.getStep()) || "WAIT_SUPPLIER_CREATE_CONFIRM".equals(state.getStep()) || "WAIT_SUPPLIER_CUSTOM_FORM".equals(state.getStep())) {
                return buildSupplierFollowup(state);
            }
            return owner.resumeCurrentFlow(state);
        }
        WmsSupplierRespVO supplier = findSupplierById(selected.getId());
        if (supplier == null) {
            throw new ServiceException("供应商不存在");
        }
        state.setSupplierId(supplier.getId());
        state.setSupplierName(supplier.getSupplierName());
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleSupplierCustomText(PcState state, String input) {
        String name = input.trim();
        if (StringUtils.isBlank(name)) {
            return owner.conversationLifecycleSupport.buildReply(state, "请补充供应商名称，或者直接提交下方表单。", supplierFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        WmsSupplierSaveReqVO req = new WmsSupplierSaveReqVO();
        req.setSupplierName(name);
        Long id = owner.supplierService.create(req);
        state.setSupplierId(id);
        state.setSupplierName(name);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleWarehouseCandidateText(PcState state, String input) {
        if (isOtherChoice(input)) {
            state.setStep("WAIT_WAREHOUSE_CUSTOM_FORM");
            owner.conversationLifecycleSupport.saveState(state);
            return buildWarehouseFollowup(state);
        }
        IdNameOption selected = resolveOption(state.getWarehouseCandidates(), input);
        if (selected == null) {
            return owner.conversationLifecycleSupport.buildReply(state, "请输入仓库序号或名称；如果都不对，回复“以上都不是”。", warehouseCandidateCard(state), owner.conversationLifecycleSupport.quickActions("以上都不是"));
        }
        WmsWarehouseRespVO warehouse = findWarehouseById(selected.getId());
        if (warehouse == null) {
            throw new ServiceException("仓库不存在");
        }
        state.setWarehouseId(warehouse.getId());
        state.setWarehouseName(warehouse.getWarehouseName());
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleWarehouseCustomText(PcState state, String input) {
        String name = input.trim();
        if (StringUtils.isBlank(name)) {
            return owner.conversationLifecycleSupport.buildReply(state, "请补充仓库名称，或者直接提交下方表单。", warehouseFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        WmsWarehouseSaveReqVO req = new WmsWarehouseSaveReqVO();
        req.setWarehouseName(name);
        req.setSort(0);
        Long id = owner.warehouseService.create(req);
        state.setWarehouseId(id);
        state.setWarehouseName(name);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleSupplierFormSubmit(PcState state, Map<String, Object> formData) {
        String name = asString(formData.get("supplierName"));
        if (StringUtils.isBlank(name)) {
            return owner.conversationLifecycleSupport.buildReply(state, "供应商名称不能为空。", supplierFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        WmsSupplierSaveReqVO req = new WmsSupplierSaveReqVO();
        req.setSupplierName(name);
        req.setContact(asString(formData.get("contact")));
        req.setPhone(asString(formData.get("phone")));
        Long id = owner.supplierService.create(req);
        state.setSupplierId(id);
        state.setSupplierName(name);
        state.setSupplierInput(name);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleWarehouseFormSubmit(PcState state, Map<String, Object> formData) {
        String name = asString(formData.get("warehouseName"));
        if (StringUtils.isBlank(name)) {
            return owner.conversationLifecycleSupport.buildReply(state, "仓库名称不能为空。", warehouseFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        WmsWarehouseSaveReqVO req = new WmsWarehouseSaveReqVO();
        req.setWarehouseName(name);
        req.setContact(asString(formData.get("contact")));
        req.setPhone(asString(formData.get("phone")));
        req.setSort(0);
        Long id = owner.warehouseService.create(req);
        state.setWarehouseId(id);
        state.setWarehouseName(name);
        state.setWarehouseInput(name);
        state.setStep("IDLE");
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    Map<String, Object> handleCustomerFormSubmit(PcState state, Map<String, Object> formData) {
        String name = asString(formData.get("customerName"));
        if (StringUtils.isBlank(name)) {
            return owner.conversationLifecycleSupport.buildReply(state, "客户名称不能为空。", customerFormCard(state), owner.conversationLifecycleSupport.quickActions("取消"));
        }
        WmsCustomerSaveReqVO req = new WmsCustomerSaveReqVO();
        req.setCustomerName(name);
        req.setContact(asString(formData.get("contact")));
        req.setPhone(asString(formData.get("phone")));
        req.setAddress(asString(formData.get("address")));
        Long id = owner.customerService.create(req);
        state.customerId = id;
        state.customerName = name;
        state.customerInput = name;
        state.step = "IDLE";
        owner.conversationLifecycleSupport.saveState(state);
        return owner.resumeCurrentFlow(state);
    }

    private WmsSupplierRespVO findSupplierById(Long id) {
        return owner.supplierService.selectList().stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
    }

    private WmsCustomerRespVO findCustomerById(Long id) {
        return owner.customerService.selectList().stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
    }

    private WmsWarehouseRespVO findWarehouseById(Long id) {
        return owner.warehouseService.selectList().stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
    }

    private IdNameOption toSupplierOption(WmsSupplierRespVO vo) {
        return new IdNameOption(vo.getId(), vo.getSupplierName());
    }

    private IdNameOption toWarehouseOption(WmsWarehouseRespVO vo) {
        return new IdNameOption(vo.getId(), vo.getWarehouseName());
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

    private boolean isOtherChoice(String input) {
        String normalized = input == null ? "" : input.trim();
        return "以上都不是".equals(normalized) || "都不是".equals(normalized) || "其他".equals(normalized);
    }

    private boolean containsIgnoreCase(String text, String search) {
        if (text == null || search == null) {
            return false;
        }
        return text.toLowerCase().contains(search.toLowerCase());
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

    private Map<String, Object> template(String label, String value) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("value", value);
        return item;
    }

    private String routeValue(String domainType, String text) {
        return "__ROUTE__" + domainType + "::" + text;
    }

    private Map<String, Object> formField(String key, String label, String placeholder, boolean required) {
        return formField(key, label, placeholder, required, null);
    }

    private Map<String, Object> formField(String key, String label, String placeholder, boolean required, Object defaultValue) {
        Map<String, Object> field = new HashMap<>();
        field.put("key", key);
        field.put("label", label);
        field.put("placeholder", placeholder);
        field.put("required", required);
        field.put("defaultValue", defaultValue);
        return field;
    }

    private String asString(Object obj) {
        return obj == null ? null : String.valueOf(obj).trim();
    }
}