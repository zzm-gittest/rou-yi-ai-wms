package com.ruoyi.wms.domain.vo.ai.recommend;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 库存查询结果VO
 *
 * @author ruoyi
 */
@Data
public class StockQueryResultVO {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 查询结果列表
     */
    private List<StockQueryItem> items;

    /**
     * 补货建议
     */
    private List<String> suggestions;

    /**
     * 库存查询项
     */
    @Data
    public static class StockQueryItem {

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
         * 库存数量
         */
        private BigDecimal quantity;

        /**
         * 仓库名称
         */
        private String warehouseName;
    }
}
