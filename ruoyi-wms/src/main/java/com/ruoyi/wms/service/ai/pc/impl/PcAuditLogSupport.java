package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.util.HashMap;
import java.util.Map;

class PcAuditLogSupport {

    private static final String DOMAIN_PO = "PURCHASE_ORDER";
    private static final String DOMAIN_PI = "PURCHASE_IN";
    private static final String DOMAIN_PR = "PURCHASE_RETURN";
    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";
    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";

    private final PcMultiBillChatServiceImpl owner;

    PcAuditLogSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    void logConversation(String conversationId, Long userId, String operationType, String inputType,
                         String inputContent, Map<String, Object> response, String resultStatus,
                         String errorMessage, long startTime, PcState state) {
        String responseContent = buildAuditResponseContent(response, state);
        int processTime = (int) (System.currentTimeMillis() - startTime);
        owner.aiChatLogService.logAsync(
                conversationId,
                userId,
                operationType,
                inputType,
                inputContent,
                null,
                responseContent,
                resultStatus,
                errorMessage,
                processTime
        );
    }

    String buildLogOperationType(PcState state) {
        if (state == null) {
            return "PC_ASSISTANT";
        }
        if ("QUERY_BILL_CENTER".equals(state.getAction())) {
            return "PC_QUERY_BILL_CENTER";
        }
        if ("QUERY_PO".equals(state.getAction())) {
            return "PC_QUERY_PO";
        }
        String domain = DOMAIN_PO.equals(state.getDomainType()) ? "PO"
                : DOMAIN_PI.equals(state.getDomainType()) ? "PI"
                : DOMAIN_PR.equals(state.getDomainType()) ? "PR"
                : DOMAIN_SO.equals(state.getDomainType()) ? "SO"
                : DOMAIN_SOUT.equals(state.getDomainType()) ? "SOUT"
                : DOMAIN_SR.equals(state.getDomainType()) ? "SR"
                : DOMAIN_SC.equals(state.getDomainType()) ? "SC"
                : DOMAIN_ST.equals(state.getDomainType()) ? "ST"
                : "UNK";
        return "PC_" + domain + "_" + abbreviateLogStep(state.getStep());
    }

    String abbreviateLogStep(String step) {
        if (StringUtils.isBlank(step)) {
            return "IDLE";
        }
        switch (step) {
            case "WAIT_GOODS_CANDIDATE":
                return "GOODS_CAND";
            case "WAIT_GOODS_BASE_FORM":
                return "GOODS_FORM";
            case "WAIT_GOODS_SPEC_FORM":
                return "GOODS_SPEC";
            case "WAIT_GOODS_SKU_FORM":
                return "GOODS_SKU";
            case "WAIT_SUPPLIER_INPUT":
                return "SUP_INPUT";
            case "WAIT_SUPPLIER_CANDIDATE":
                return "SUP_CAND";
            case "WAIT_SUPPLIER_CUSTOM_FORM":
                return "SUP_FORM";
            case "WAIT_SUPPLIER_CREATE_CONFIRM":
                return "SUP_CONFIRM";
            case "WAIT_WAREHOUSE_INPUT":
                return "WH_INPUT";
            case "WAIT_CONFIRM_EXEC":
                return "CONFIRM";
            default:
                return step.length() > 16 ? step.substring(0, 16) : step;
        }
    }

    String buildAuditResponseContent(Map<String, Object> response, PcState state) {
        Map<String, Object> payload = new HashMap<>();
        if (response != null) {
            payload.put("text", response.get("text"));
            payload.put("card", response.get("card"));
            payload.put("quickActions", response.get("quickActions"));
            payload.put("context", response.get("context"));
        }
        if (state != null) {
            Map<String, Object> stateSnapshot = new HashMap<>();
            stateSnapshot.put("action", state.getAction());
            stateSnapshot.put("step", state.getStep());
            stateSnapshot.put("domainType", state.getDomainType());
            stateSnapshot.put("lastIntentText", state.lastIntentText);
            stateSnapshot.put("supplierId", state.getSupplierId());
            stateSnapshot.put("supplierName", state.getSupplierName());
            stateSnapshot.put("supplierInput", state.getSupplierInput());
            stateSnapshot.put("customerId", state.customerId);
            stateSnapshot.put("customerName", state.customerName);
            stateSnapshot.put("customerInput", state.customerInput);
            stateSnapshot.put("warehouseId", state.getWarehouseId());
            stateSnapshot.put("warehouseName", state.getWarehouseName());
            stateSnapshot.put("warehouseInput", state.getWarehouseInput());
            stateSnapshot.put("itemsInput", state.getItemsInput());
            stateSnapshot.put("itemDraftCount", state.getItemDrafts() == null ? 0 : state.getItemDrafts().size());
            stateSnapshot.put("supplierCandidatePage", state.getSupplierCandidatePage());
            stateSnapshot.put("supplierCandidateCount", state.getSupplierCandidates() == null ? 0 : state.getSupplierCandidates().size());
            stateSnapshot.put("pendingGoodsName", state.getPendingGoodsName());
            stateSnapshot.put("pendingGoodsCandidatePage", state.getPendingGoodsCandidatePage());
            stateSnapshot.put("pendingGoodsCandidateCount", state.getPendingGoodsCandidates() == null ? 0 : state.getPendingGoodsCandidates().size());
            stateSnapshot.put("pendingGoodsDraftIndex", state.getPendingGoodsDraftIndex());
            stateSnapshot.put("pendingGoodsDraft", state.getPendingGoodsDraft());
            stateSnapshot.put("pendingUnmatchedDraftIndexes", state.pendingUnmatchedDraftIndexes);
            stateSnapshot.put("lastCreatedOrderId", state.getLastCreatedOrderId());
            stateSnapshot.put("lastCreatedOrderNo", state.getLastCreatedOrderNo());
            payload.put("stateSnapshot", stateSnapshot);
        }
        try {
            return owner.objectMapper.writeValueAsString(payload);
        } catch (Exception ignored) {
            return response == null ? null : String.valueOf(response.get("text"));
        }
    }
}
