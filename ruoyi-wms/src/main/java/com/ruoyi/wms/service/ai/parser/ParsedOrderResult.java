package com.ruoyi.wms.service.ai.parser;

import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 解析后的订单结果
 *
 * @author ruoyi
 */
public class ParsedOrderResult {

    /**
     * 意图类型
     */
    private IntentType intentType;

    /**
     * 是否是完整订单（包含商品+数量+价格）
     */
    private boolean complete;

    /**
     * 操作类型代码
     */
    private String operationType;

    /**
     * 匹配到的商品列表（模糊搜索返回最新3条）
     */
    private List<WmsGoodsSkuRespVO> matchedGoodsList;

    /**
     * 匹配到的供应商列表（模糊搜索返回最新3条）
     */
    private List<WmsSupplier> matchedSupplierList;

    /**
     * 匹配到的客户列表（模糊搜索返回最新3条）
     */
    private List<WmsCustomer> matchedCustomerList;

    /**
     * 商品列表
     */
    private List<GoodsItem> goods = new ArrayList<>();

    /**
     * 供应商名称（原始输入）
     */
    private String supplier;

    /**
     * 供应商ID（匹配后）
     */
    private Long supplierId;

    /**
     * 供应商名称（匹配后）
     */
    private String supplierName;

    /**
     * 是否是新供应商
     */
    private boolean newSupplier;

    /**
     * 客户名称（原始输入）
     */
    private String customer;

    /**
     * 客户ID（匹配后）
     */
    private Long customerId;

    /**
     * 客户名称（匹配后）
     */
    private String customerName;

    /**
     * 是否是新客户
     */
    private boolean newCustomer;

    /**
     * 仓库
     */
    private String warehouse;

    /**
     * 备注
     */
    private String remark;

    /**
     * 查询关键词（用于商品查询/库存查询）
     */
    private String queryKeyword;

    /**
     * 原始消息
     */
    private String originalMessage;

    /**
     * 缺少的必填字段
     */
    private List<String> missingFields = new ArrayList<>();

    public ParsedOrderResult() {
    }

    public ParsedOrderResult(IntentType intentType, boolean complete, String operationType,
                             List<WmsGoodsSkuRespVO> matchedGoodsList, List<WmsSupplier> matchedSupplierList,
                             List<WmsCustomer> matchedCustomerList, List<GoodsItem> goods, String supplier,
                             Long supplierId, String supplierName, boolean newSupplier, String customer,
                             Long customerId, String customerName, boolean newCustomer, String warehouse,
                             String remark, String queryKeyword, String originalMessage, List<String> missingFields) {
        this.intentType = intentType;
        this.complete = complete;
        this.operationType = operationType;
        this.matchedGoodsList = matchedGoodsList;
        this.matchedSupplierList = matchedSupplierList;
        this.matchedCustomerList = matchedCustomerList;
        this.goods = goods == null ? new ArrayList<>() : goods;
        this.supplier = supplier;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.newSupplier = newSupplier;
        this.customer = customer;
        this.customerId = customerId;
        this.customerName = customerName;
        this.newCustomer = newCustomer;
        this.warehouse = warehouse;
        this.remark = remark;
        this.queryKeyword = queryKeyword;
        this.originalMessage = originalMessage;
        this.missingFields = missingFields == null ? new ArrayList<>() : missingFields;
    }

    public static Builder builder() {
        return new Builder();
    }

    public IntentType getIntentType() { return intentType; }
    public void setIntentType(IntentType intentType) { this.intentType = intentType; }
    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public List<WmsGoodsSkuRespVO> getMatchedGoodsList() { return matchedGoodsList; }
    public void setMatchedGoodsList(List<WmsGoodsSkuRespVO> matchedGoodsList) { this.matchedGoodsList = matchedGoodsList; }
    public List<WmsSupplier> getMatchedSupplierList() { return matchedSupplierList; }
    public void setMatchedSupplierList(List<WmsSupplier> matchedSupplierList) { this.matchedSupplierList = matchedSupplierList; }
    public List<WmsCustomer> getMatchedCustomerList() { return matchedCustomerList; }
    public void setMatchedCustomerList(List<WmsCustomer> matchedCustomerList) { this.matchedCustomerList = matchedCustomerList; }
    public List<GoodsItem> getGoods() { return goods; }
    public void setGoods(List<GoodsItem> goods) { this.goods = goods; }
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public boolean isNewSupplier() { return newSupplier; }
    public void setNewSupplier(boolean newSupplier) { this.newSupplier = newSupplier; }
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public boolean isNewCustomer() { return newCustomer; }
    public void setNewCustomer(boolean newCustomer) { this.newCustomer = newCustomer; }
    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getQueryKeyword() { return queryKeyword; }
    public void setQueryKeyword(String queryKeyword) { this.queryKeyword = queryKeyword; }
    public String getOriginalMessage() { return originalMessage; }
    public void setOriginalMessage(String originalMessage) { this.originalMessage = originalMessage; }
    public List<String> getMissingFields() { return missingFields; }
    public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

    public static class Builder {
        private IntentType intentType;
        private boolean complete;
        private String operationType;
        private List<WmsGoodsSkuRespVO> matchedGoodsList;
        private List<WmsSupplier> matchedSupplierList;
        private List<WmsCustomer> matchedCustomerList;
        private List<GoodsItem> goods = new ArrayList<>();
        private String supplier;
        private Long supplierId;
        private String supplierName;
        private boolean newSupplier;
        private String customer;
        private Long customerId;
        private String customerName;
        private boolean newCustomer;
        private String warehouse;
        private String remark;
        private String queryKeyword;
        private String originalMessage;
        private List<String> missingFields = new ArrayList<>();

        public Builder intentType(IntentType intentType) { this.intentType = intentType; return this; }
        public Builder complete(boolean complete) { this.complete = complete; return this; }
        public Builder operationType(String operationType) { this.operationType = operationType; return this; }
        public Builder matchedGoodsList(List<WmsGoodsSkuRespVO> matchedGoodsList) { this.matchedGoodsList = matchedGoodsList; return this; }
        public Builder matchedSupplierList(List<WmsSupplier> matchedSupplierList) { this.matchedSupplierList = matchedSupplierList; return this; }
        public Builder matchedCustomerList(List<WmsCustomer> matchedCustomerList) { this.matchedCustomerList = matchedCustomerList; return this; }
        public Builder goods(List<GoodsItem> goods) { this.goods = goods; return this; }
        public Builder supplier(String supplier) { this.supplier = supplier; return this; }
        public Builder supplierId(Long supplierId) { this.supplierId = supplierId; return this; }
        public Builder supplierName(String supplierName) { this.supplierName = supplierName; return this; }
        public Builder newSupplier(boolean newSupplier) { this.newSupplier = newSupplier; return this; }
        public Builder customer(String customer) { this.customer = customer; return this; }
        public Builder customerId(Long customerId) { this.customerId = customerId; return this; }
        public Builder customerName(String customerName) { this.customerName = customerName; return this; }
        public Builder newCustomer(boolean newCustomer) { this.newCustomer = newCustomer; return this; }
        public Builder warehouse(String warehouse) { this.warehouse = warehouse; return this; }
        public Builder remark(String remark) { this.remark = remark; return this; }
        public Builder queryKeyword(String queryKeyword) { this.queryKeyword = queryKeyword; return this; }
        public Builder originalMessage(String originalMessage) { this.originalMessage = originalMessage; return this; }
        public Builder missingFields(List<String> missingFields) { this.missingFields = missingFields; return this; }

        public ParsedOrderResult build() {
            return new ParsedOrderResult(intentType, complete, operationType, matchedGoodsList, matchedSupplierList,
                    matchedCustomerList, goods, supplier, supplierId, supplierName, newSupplier, customer,
                    customerId, customerName, newCustomer, warehouse, remark, queryKeyword, originalMessage,
                    missingFields);
        }
    }

    /**
     * 商品项
     */
    public static class GoodsItem {
        /**
         * 商品名称（原始输入）
         */
        private String name;

        /**
         * SKU ID（匹配后）
         */
        private Long skuId;

        /**
         * 商品名称（匹配后）
         */
        private String matchedName;

        /**
         * 是否是新商品
         */
        private boolean newGoods;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 单位（原始输入）
         */
        private String unit;

        /**
         * 单位ID（匹配后）
         */
        private Long unitId;

        /**
         * 单位名称（匹配后）
         */
        private String unitName;

        /**
         * 是否是新单位
         */
        private boolean newUnit;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 金额
         */
        private BigDecimal amount;

        /**
         * 规格
         */
        private String spec;

        public GoodsItem() {
        }

        public GoodsItem(String name, Long skuId, String matchedName, boolean newGoods, Integer quantity,
                         String unit, Long unitId, String unitName, boolean newUnit, BigDecimal price,
                         BigDecimal amount, String spec) {
            this.name = name;
            this.skuId = skuId;
            this.matchedName = matchedName;
            this.newGoods = newGoods;
            this.quantity = quantity;
            this.unit = unit;
            this.unitId = unitId;
            this.unitName = unitName;
            this.newUnit = newUnit;
            this.price = price;
            this.amount = amount;
            this.spec = spec;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Long getSkuId() { return skuId; }
        public void setSkuId(Long skuId) { this.skuId = skuId; }
        public String getMatchedName() { return matchedName; }
        public void setMatchedName(String matchedName) { this.matchedName = matchedName; }
        public boolean isNewGoods() { return newGoods; }
        public void setNewGoods(boolean newGoods) { this.newGoods = newGoods; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public Long getUnitId() { return unitId; }
        public void setUnitId(Long unitId) { this.unitId = unitId; }
        public String getUnitName() { return unitName; }
        public void setUnitName(String unitName) { this.unitName = unitName; }
        public boolean isNewUnit() { return newUnit; }
        public void setNewUnit(boolean newUnit) { this.newUnit = newUnit; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getSpec() { return spec; }
        public void setSpec(String spec) { this.spec = spec; }
    }

    /**
     * 添加商品
     */
    public void addGoods(GoodsItem item) {
        if (this.goods == null) {
            this.goods = new ArrayList<>();
        }
        this.goods.add(item);
    }

    /**
     * 判断是否有商品信息
     */
    public boolean hasGoods() {
        return goods != null && !goods.isEmpty();
    }

    /**
     * 判断是否有供应商信息
     */
    public boolean hasSupplier() {
        return supplier != null && !supplier.isEmpty();
    }

    /**
     * 判断是否有客户信息
     */
    public boolean hasCustomer() {
        return customer != null && !customer.isEmpty();
    }
}
