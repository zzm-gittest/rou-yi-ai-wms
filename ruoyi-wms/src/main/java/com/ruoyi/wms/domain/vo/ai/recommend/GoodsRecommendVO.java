package com.ruoyi.wms.domain.vo.ai.recommend;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品推荐结果VO
 *
 * @author ruoyi
 */
@Data
public class GoodsRecommendVO {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 推荐商品列表
     */
    private List<GoodsRecommendItem> recommendations;

    /**
     * 推荐商品项
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
         * 条码
         */
        private String barcode;

        /**
         * 规格信息
         */
        private String specInfo;

        /**
         * 单位名称
         */
        private String unitName;

        /**
         * 采购价
         */
        private BigDecimal purchasePrice;

        /**
         * 销售价
         */
        private BigDecimal salePrice;

        /**
         * 当前库存
         */
        private BigDecimal stockQty;

        /**
         * 库存状态
         */
        private String stockStatus;

        /**
         * 推荐分数
         */
        private Double score;
    }
}
