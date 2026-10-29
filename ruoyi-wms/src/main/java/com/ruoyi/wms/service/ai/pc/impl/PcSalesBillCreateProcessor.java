package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnSaveReqVO;
import com.ruoyi.wms.service.ai.pc.vo.PcState;

import java.time.LocalDate;
import java.util.Map;

class PcSalesBillCreateProcessor {

    private static final String DOMAIN_SO = "SALES_ORDER";
    private static final String DOMAIN_SOUT = "SALES_OUT";
    private static final String DOMAIN_SR = "SALES_RETURN";

    private final PcMultiBillChatServiceImpl owner;

    PcSalesBillCreateProcessor(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    Map<String, Object> execute(PcState state) {
        if (DOMAIN_SO.equals(state.getDomainType())) {
            return executeCreateSalesOrder(state);
        }
        if (DOMAIN_SOUT.equals(state.getDomainType())) {
            return executeCreateSalesOut(state);
        }
        if (DOMAIN_SR.equals(state.getDomainType())) {
            return executeCreateSalesReturn(state);
        }
        throw new ServiceException("未识别销售域单据类型");
    }

    private Map<String, Object> executeCreateSalesOrder(PcState state) {
        // 销售订单创建前校验客户已选择（可从确认卡片表单提交或之前流程解析）
        if (state.getCustomerId() == null) {
            Map<String, Object> customerReply = owner.purchaseDraftSupport.ensureCustomerReadyForCreate(state);
            if (customerReply != null) {
                return customerReply;
            }
        }
        String customerName = state.getCustomerName();
        WmsSalesOrderSaveReqVO req = new WmsSalesOrderSaveReqVO();
        req.setCustomerId(state.getCustomerId());
        req.setOrderDate(LocalDate.now());
        req.setDeliveryDate(LocalDate.now().plusDays(3));
        req.setRemark(StringUtils.defaultIfBlank(state.getReason(), "PC-AI助手语义建单"));
        req.setItems(owner.buildSalesOrderItems(state));
        Long id = owner.salesOrderService.create(req);
        WmsSalesOrderRespVO resp = owner.salesOrderService.get(id);
        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(resp == null ? null : resp.getOrderNo());
        state.setLastCreatedDomainType(DOMAIN_SO);
        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                resp == null ? "销售订单创建成功，ID=" + id : "销售订单创建成功，单号：" + resp.getOrderNo(),
                owner.cardResultSupport.buildSalesOrderResultCard(resp, id, customerName),
                owner.conversationLifecycleSupport.quickActions("再来一单销售订单", "查看刚创建的订单"));
    }

    private Map<String, Object> executeCreateSalesOut(PcState state) {
        Map<String, Object> customerReply = owner.purchaseDraftSupport.ensureCustomerReadyForCreate(state);
        if (customerReply != null) {
            return customerReply;
        }
        String customerName = state.getCustomerName();
        WmsSalesOutSaveReqVO req = new WmsSalesOutSaveReqVO();
        req.setSourceType(owner.hasLinkedSource(state) ? 2 : 1);
        req.setOrderId(DOMAIN_SO.equals(state.getLinkOrderType()) ? state.getLinkOrderId() : null);
        req.setCustomerId(state.getCustomerId());
        req.setWarehouseId(state.getWarehouseId());
        req.setOutDate(LocalDate.now());
        req.setRemark(StringUtils.defaultIfBlank(state.getReason(), "PC-AI助手语义建单"));
        req.setItems(owner.buildSalesOutItems(state));
        Long id = owner.salesOutService.create(req);
        WmsSalesOutRespVO resp = owner.salesOutService.get(id);
        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(resp == null ? null : resp.getOutNo());
        state.setLastCreatedDomainType(DOMAIN_SOUT);
        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                resp == null ? "销售出库创建成功，ID=" + id : "销售出库创建成功，单号：" + resp.getOutNo(),
                owner.cardResultSupport.buildSalesOutResultCard(resp, id, customerName),
                owner.conversationLifecycleSupport.quickActions("再来一单销售出库", "查看刚创建的订单"));
    }

    private Map<String, Object> executeCreateSalesReturn(PcState state) {
        Map<String, Object> customerReply = owner.purchaseDraftSupport.ensureCustomerReadyForCreate(state);
        if (customerReply != null) {
            return customerReply;
        }
        String customerName = state.getCustomerName();
        WmsSalesReturnSaveReqVO req = new WmsSalesReturnSaveReqVO();
        req.setSourceType(owner.hasLinkedSource(state) ? 2 : 1);
        req.setOutId(DOMAIN_SOUT.equals(state.getLinkOrderType()) ? state.getLinkOrderId() : null);
        req.setCustomerId(state.getCustomerId());
        req.setWarehouseId(state.getWarehouseId());
        req.setReturnDate(LocalDate.now());
        req.setRemark(StringUtils.defaultIfBlank(state.getReason(), "PC-AI助手语义建单"));
        req.setItems(owner.buildSalesReturnItems(state));
        Long id = owner.salesReturnService.create(req);
        WmsSalesReturnRespVO resp = owner.salesReturnService.get(id);
        state.setLastCreatedOrderId(id);
        state.setLastCreatedOrderNo(resp == null ? null : resp.getReturnNo());
        state.setLastCreatedDomainType(DOMAIN_SR);
        state.resetFlow();
        owner.conversationLifecycleSupport.saveState(state);
        return owner.conversationLifecycleSupport.buildReply(state,
                resp == null ? "销售退货创建成功，ID=" + id : "销售退货创建成功，单号：" + resp.getReturnNo(),
                owner.cardResultSupport.buildSalesReturnResultCard(resp, id, customerName),
                owner.conversationLifecycleSupport.quickActions("再来一单销售退货", "查看刚创建的订单"));
    }
}

