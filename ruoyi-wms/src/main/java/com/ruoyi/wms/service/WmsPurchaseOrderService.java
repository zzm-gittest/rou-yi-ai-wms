package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderExportVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderSaveReqVO;
import java.util.List;

/**
 * 采购订单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsPurchaseOrderService extends IService<WmsPurchaseOrder> {

    /**
     * 分页查询采购订单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsPurchaseOrderRespVO> page(WmsPurchaseOrderPageReqVO reqVO);

    /**
     * 获取采购订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    WmsPurchaseOrderRespVO get(Long id);

    /**
     * 新增采购订单
     *
     * @param reqVO 订单信息
     * @return 订单ID
     */
    Long create(WmsPurchaseOrderSaveReqVO reqVO);

    /**
     * 修改采购订单
     *
     * @param reqVO 订单信息
     */
    void update(WmsPurchaseOrderSaveReqVO reqVO);

    /**
     * 删除采购订单
     *
     * @param id 订单ID
     */
    void delete(Long id);

    /**
     * 审核采购订单
     *
     * @param id 订单ID
     */
    void approve(Long id);

    /**
     * 关闭采购订单
     *
     * @param id 订单ID
     */
    void close(Long id);

    /**
     * 获取可入库的采购订单列表（已审核且未全部入库）
     *
     * @return 可入库的订单列表
     */
    java.util.List<WmsPurchaseOrderRespVO> listAvailableForIn();

    /**
     * 导出采购订单列表（按明细行展开）
     */
    List<WmsPurchaseOrderExportVO> exportList(WmsPurchaseOrderPageReqVO reqVO);
}
