package com.ruoyi.wms.service.ai.pc.vo;

import com.ruoyi.wms.domain.vo.ai.analysis.NluIntentResult;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemSaveReqVO;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PcState implements Serializable {
    private static final long serialVersionUID = 1L;

    // 保持与原逻辑兼容：当前流程中存在大量直接字段读写。
    public String conversationId;
    public Long userId;
    public String action = "NONE";
    public String step = "IDLE";
    public String domainType;
    public String lastIntentText;

    public Long supplierId;
    public String supplierName;
    public String supplierInput;

    public Long customerId;
    public String customerName;
    public String customerInput;

    public Long warehouseId;
    public String warehouseName;
    public String warehouseInput;

    public Long outWarehouseId;
    public String outWarehouseName;
    public String outWarehouseInput;

    public Long inWarehouseId;
    public String inWarehouseName;
    public String inWarehouseInput;

    public String reason;
    public String checkType;
    public String purchaseInMode;
    public String salesOutMode;
    public String salesReturnMode;
    public String linkOrderNo;
    public Long linkOrderId;
    public String linkOrderType;
    public List<IdNameOption> linkOrderCandidates = new ArrayList<>();
    public int linkOrderCandidatePage = 1;

    public String itemsInput;
    public List<ItemDraft> itemDrafts = new ArrayList<>();
    public List<WmsPurchaseOrderItemSaveReqVO> preparedPoItems = new ArrayList<>();
    public List<WmsPurchaseInItemSaveReqVO> preparedPiItems = new ArrayList<>();
    public List<LineDraft> preparedLines = new ArrayList<>();

    public List<IdNameOption> supplierCandidates = new ArrayList<>();
    public int supplierCandidatePage = 1;
    public List<IdNameOption> customerCandidates = new ArrayList<>();
    public int customerCandidatePage = 1;
    public List<IdNameOption> warehouseCandidates = new ArrayList<>();

    public String pendingGoodsName;
    public String pendingGoodsUnit;
    public List<IdNameOption> pendingGoodsCandidates = new ArrayList<>();
    public int pendingGoodsCandidatePage = 1;
    public Integer pendingGoodsDraftIndex = -1;
    public PendingGoodsDraft pendingGoodsDraft;
    public List<Integer> pendingUnmatchedDraftIndexes = new ArrayList<>();

    public Long lastCreatedOrderId;
    public String lastCreatedOrderNo;
    public String lastCreatedDomainType;

    /** 经营分析对话上下文（NLU意图历史） */
    public List<NluIntentResult> analysisContextHistory;

    public void resetFlow() {
        this.action = "NONE";
        this.step = "IDLE";
        this.domainType = null;
        this.lastIntentText = null;
        this.supplierId = null;
        this.supplierName = null;
        this.supplierInput = null;
        this.customerId = null;
        this.customerName = null;
        this.customerInput = null;
        this.warehouseId = null;
        this.warehouseName = null;
        this.warehouseInput = null;
        this.outWarehouseId = null;
        this.outWarehouseName = null;
        this.outWarehouseInput = null;
        this.inWarehouseId = null;
        this.inWarehouseName = null;
        this.inWarehouseInput = null;
        this.reason = null;
        this.checkType = null;
        this.purchaseInMode = null;
        this.salesOutMode = null;
        this.salesReturnMode = null;
        this.linkOrderNo = null;
        this.linkOrderId = null;
        this.linkOrderType = null;
        this.linkOrderCandidates = new ArrayList<>();
        this.linkOrderCandidatePage = 1;
        this.itemsInput = null;
        this.itemDrafts = new ArrayList<>();
        this.preparedPoItems = new ArrayList<>();
        this.preparedPiItems = new ArrayList<>();
        this.preparedLines = new ArrayList<>();
        this.supplierCandidates = new ArrayList<>();
        this.supplierCandidatePage = 1;
        this.customerCandidates = new ArrayList<>();
        this.customerCandidatePage = 1;
        this.warehouseCandidates = new ArrayList<>();
        this.pendingGoodsName = null;
        this.pendingGoodsUnit = null;
        this.pendingGoodsCandidates = new ArrayList<>();
        this.pendingGoodsCandidatePage = 1;
        this.pendingGoodsDraftIndex = -1;
        this.pendingGoodsDraft = null;
        this.pendingUnmatchedDraftIndexes = new ArrayList<>();
        this.analysisContextHistory = null;
    }
}
