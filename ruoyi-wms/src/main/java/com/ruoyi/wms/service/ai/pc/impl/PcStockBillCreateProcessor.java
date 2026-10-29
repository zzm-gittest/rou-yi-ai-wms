package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckSaveReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.LineDraft;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class PcStockBillCreateProcessor {

    private static final String DOMAIN_SC = "STOCK_CHECK";
    private static final String DOMAIN_ST = "STOCK_TRANSFER";
    private static final String CHECK_TYPE_FULL = "FULL";

    private final PcMultiBillChatServiceImpl owner;

    PcStockBillCreateProcessor(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> execute(PcState state) {
        if (DOMAIN_SC.equals(state.getDomainType())) {
            return executeCreateStockCheck(state);
        }
        if (DOMAIN_ST.equals(state.getDomainType())) {
            return executeCreateStockTransfer(state);
        }
        throw new ServiceException("未识别库存域单据类型");
    }

    private Map<String, Object> executeCreateStockCheck(PcState state) {
        String warehouseName = state.getWarehouseName();
        List<LineDraft> previewLines = state.getPreparedLines() == null
            ? new ArrayList<>() : new ArrayList<>(state.getPreparedLines());
        WmsStockCheckSaveReqVO req = new WmsStockCheckSaveReqVO();
        req.setWarehouseId(state.getWarehouseId());
        req.setCheckDate(LocalDate.now());
        req.setCheckType(StringUtils.defaultIfBlank(state.getCheckType(), CHECK_TYPE_FULL));
        req.setRemark(StringUtils.defaultIfBlank(state.getReason(), "PC-AI助手语义建单"));
        req.setItems(owner.buildStockCheckItems(state));
        Long id = owner.stockCheckService.create(req);
        WmsStockCheckRespVO resp = owner.stockCheckService.get(id);
        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(resp == null ? null : resp.getCheckNo());
        state.setLastCreatedDomainType(DOMAIN_SC);
        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        Map<String, Object> card = owner.cardResultSupport.buildStockCheckResultCard(resp, id, warehouseName);
        List<?> items = (List<?>) card.get("items");
        if (items == null || items.isEmpty()) {
            card.put("items", owner.cardResultSupport.buildLineFallbackItems(previewLines, true));
        }
        return owner.conversationLifecycleSupport.buildReply(state,
                resp == null ? "库存盘点创建成功，ID=" + id : "库存盘点创建成功，单号：" + resp.getCheckNo(),
                card,
                owner.conversationLifecycleSupport.quickActions("再来一单库存盘点", "查看刚创建的订单"));
    }

    private Map<String, Object> executeCreateStockTransfer(PcState state) {
        String transferWarehousePair = state.getOutWarehouseName() + " -> " + state.getInWarehouseName();
        WmsStockTransferSaveReqVO req = new WmsStockTransferSaveReqVO();
        req.setOutWarehouseId(state.getOutWarehouseId());
        req.setInWarehouseId(state.getInWarehouseId());
        req.setTransferDate(LocalDate.now());
        req.setRemark(StringUtils.defaultIfBlank(state.getReason(), "PC-AI助手语义建单"));
        req.setItems(owner.buildStockTransferItems(state));
        Long id = owner.stockTransferService.create(req);
        WmsStockTransferRespVO resp = owner.stockTransferService.get(id);
        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(resp == null ? null : resp.getTransferNo());
        state.setLastCreatedDomainType(DOMAIN_ST);
        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                resp == null ? "库存调拨创建成功，ID=" + id : "库存调拨创建成功，单号：" + resp.getTransferNo(),
                owner.cardResultSupport.buildStockTransferResultCard(resp, id, transferWarehousePair, state.getPreparedLines()),
                owner.conversationLifecycleSupport.quickActions("再来一单库存调拨", "查看刚创建的订单"));
    }
}
