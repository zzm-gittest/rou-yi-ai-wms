package com.ruoyi.wms.domain.vo.ai.recommend;

import lombok.Data;

import java.util.List;

/**
 * 采购订单推荐结果VO
 *
 * @author ruoyi
 */
@Data
public class PurchaseOrderRecommendVO {

    /**
     * 推荐供应商列表
     */
    private List<SupplierRecommendItem> recommendedSuppliers;

    /**
     * 推荐商品列表
     */
    private List<GoodsRecommendItem> recommendedGoods;

    /**
     * 供应商推荐项
     */
    @Data
    public static class SupplierRecommendItem {

        /**
         * 供应商ID
         */
        private Long supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 联系人
         */
        private String contact;

        /**
         * 联系电话
         */
        private String phone;
    }

    /**
     * 商品推荐项
     */
    @Data
    public static class GoodsRecommendItem {

        /**
         * SKU ID
         */
        private Long skuId;

        /**
         * SKU名称
         */
        private String skuName;

        /**
         * 规格信息
         */
        private String specInfo;

        /**
         * 单位名称
         */
        private String unitName;

        /**
         * 当前库存
         */
        private Integer stockQty;

        /**
         * 库存状态
         */
        private String stockStatus;

        /**
         * 采购价
         */
        private String purchasePrice;
    }
}
