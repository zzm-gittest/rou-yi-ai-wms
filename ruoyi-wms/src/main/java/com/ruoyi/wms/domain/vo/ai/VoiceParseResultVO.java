package com.ruoyi.wms.domain.vo.ai;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * 语音解析结果VO
 *
 * @author ruoyi
 */
@ApiModel("语音解析结果")
public class VoiceParseResultVO {

    @ApiModelProperty("是否成功")
    private Boolean success;

    @ApiModelProperty("错误信息")
    private String errorMessage;

    @ApiModelProperty("识别的文本（ASR结果）")
    private String recognizedText;

    @ApiModelProperty("操作类型：purchase-采购，sales-销售，stock-库存，goods-商品")
    private String operationType;

    @ApiModelProperty("解析出的商品列表")
    private List<GoodsItem> goods;

    @ApiModelProperty("供应商信息")
    private PartnerInfo supplier;

    @ApiModelProperty("客户信息")
    private PartnerInfo customer;

    @ApiModelProperty("仓库信息")
    private String warehouse;

    @ApiModelProperty("调出仓库")
    private String outWarehouse;

    @ApiModelProperty("调入仓库")
    private String inWarehouse;

    @ApiModelProperty("备注信息")
    private String remark;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public List<GoodsItem> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsItem> goods) {
        this.goods = goods;
    }

    public PartnerInfo getSupplier() {
        return supplier;
    }

    public void setSupplier(PartnerInfo supplier) {
        this.supplier = supplier;
    }

    public PartnerInfo getCustomer() {
        return customer;
    }

    public void setCustomer(PartnerInfo customer) {
        this.customer = customer;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getOutWarehouse() {
        return outWarehouse;
    }

    public void setOutWarehouse(String outWarehouse) {
        this.outWarehouse = outWarehouse;
    }

    public String getInWarehouse() {
        return inWarehouse;
    }

    public void setInWarehouse(String inWarehouse) {
        this.inWarehouse = inWarehouse;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 商品项
     */
    public static class GoodsItem {
        @ApiModelProperty("商品名称")
        private String name;

        @ApiModelProperty("商品条码")
        private String barcode;

        @ApiModelProperty("规格")
        private String spec;

        @ApiModelProperty("数量")
        private BigDecimal quantity;

        @ApiModelProperty("单位")
        private String unit;

        @ApiModelProperty("单价")
        private BigDecimal price;

        @ApiModelProperty("金额")
        private BigDecimal amount;

        @ApiModelProperty("匹配到的SKU ID（如果能匹配到）")
        private Long skuId;

        @ApiModelProperty("是否新商品（需要创建）")
        private Boolean isNew;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getSpec() {
            return spec;
        }

        public void setSpec(String spec) {
            this.spec = spec;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public Boolean getIsNew() {
            return isNew;
        }

        public void setIsNew(Boolean isNew) {
            this.isNew = isNew;
        }
    }

    /**
     * 伙伴信息（供应商/客户）
     */
    public static class PartnerInfo {
        @ApiModelProperty("名称")
        private String name;

        @ApiModelProperty("匹配到的ID（如果能匹配到）")
        private Long id;

        @ApiModelProperty("是否新伙伴（需要创建）")
        private Boolean isNew;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Boolean getIsNew() {
            return isNew;
        }

        public void setIsNew(Boolean isNew) {
            this.isNew = isNew;
        }
    }
}
