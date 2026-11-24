package com.ruoyi.wms.domain.vo.ai.recommend;

import lombok.Data;

import java.util.List;

/**
 * 销售订单推荐结果VO
 *
 * @author ruoyi
 */
@Data
public class SalesOrderRecommendVO {

    /**
     * 推荐客户列表
     */
    private List<CustomerRecommendItem> recommendedCustomers;

    /**
     * 推荐商品列表
     */
    private List<GoodsRecommendItem> recommendedGoods;

    /**
     * 客户推荐项
     */
    @Data
    public static class CustomerRecommendItem {

        /**
         * 客户ID
         */
        private Long customerId;

        /**
         * 客户名称
         */
        private String customerName;

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
         * 销售价
         */
        private String salePrice;
    }
}
