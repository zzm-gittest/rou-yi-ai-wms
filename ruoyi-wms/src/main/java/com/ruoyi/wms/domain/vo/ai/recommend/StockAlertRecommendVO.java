package com.ruoyi.wms.domain.vo.ai.recommend;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存预警推荐结果VO
 *
 * @author ruoyi
 */
@Data
public class StockAlertRecommendVO {

    /**
     * 预警商品列表
     */
    private List<StockAlertItem> alertItems;

    /**
     * 库存预警项
     */
    @Data
    public static class StockAlertItem {

        /**
         * SKU ID
         */
        private Long skuId;

        /**
         * SKU名称
         */
        private String skuName;

        /**
         * 当前库存
         */
        private BigDecimal currentStock;

        /**
         * 安全库存
         */
        private BigDecimal safeStock;

        /**
         * 缺货数量
         */
        private BigDecimal shortage;

        /**
         * 紧急程度：高/中/低
         */
        private String urgentLevel;
    }
}
