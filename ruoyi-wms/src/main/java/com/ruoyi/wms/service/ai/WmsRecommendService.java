package com.ruoyi.wms.service.ai;

import com.ruoyi.wms.domain.vo.ai.recommend.PurchaseOrderRecommendVO;
import com.ruoyi.wms.domain.vo.ai.recommend.SalesOrderRecommendVO;

/**
 * 智能推荐服务接口
 *
 * @author ruoyi
 */
public interface WmsRecommendService {

    /**
     * 获取采购订单推荐信息
     * 包括：常用供应商、常购商品、库存预警商品
     *
     * @return 推荐信息
     */
    PurchaseOrderRecommendVO getPurchaseOrderRecommend();

    /**
     * 获取销售订单推荐信息
     * 包括：常用客户、常购商品、库存预警商品
     *
     * @return 推荐信息
     */
    SalesOrderRecommendVO getSalesOrderRecommend();
}
