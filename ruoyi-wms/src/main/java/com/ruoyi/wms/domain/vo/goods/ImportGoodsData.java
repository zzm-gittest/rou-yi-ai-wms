package com.ruoyi.wms.domain.vo.goods;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入商品数据（包含SKU）
 *
 * @author ruoyi
 */
@Data
public class ImportGoodsData {

    /**
     * 商品信息
     */
    private WmsGoodsImportVo goods;

    /**
     * SKU列表
     */
    private List<WmsGoodsImportVo> skuList = new ArrayList<>();

    /**
     * 添加SKU
     */
    public void addSku(WmsGoodsImportVo sku) {
        if (sku != null) {
            skuList.add(sku);
        }
    }

    /**
     * 获取第一个SKU（用于无规格商品）
     */
    public WmsGoodsImportVo getFirstSku() {
        return skuList.isEmpty() ? null : skuList.get(0);
    }

    /**
     * 是否有规格
     */
    public boolean hasSpec() {
        return goods != null && goods.hasSpec() && skuList.size() > 1;
    }
}
