package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInExportVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 采购入库单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsPurchaseInService extends IService<WmsPurchaseIn> {

    /**
     * 分页查询采购入库单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsPurchaseInRespVO> page(WmsPurchaseInPageReqVO reqVO);

    /**
     * 获取采购入库单详情
     *
     * @param id 入库单ID
     * @return 入库单详情
     */
    WmsPurchaseInRespVO get(Long id);

    /**
     * 获取采购入库打印数据
     *
     * @param id 入库单ID
     * @return 打印数据
     */
    WmsBillPrintRespVO getPrintData(Long id);

    /**
     * 新增采购入库单
     *
     * @param reqVO 入库单信息
     * @return 入库单ID
     */
    Long create(WmsPurchaseInSaveReqVO reqVO);

    /**
     * 修改采购入库单
     *
     * @param reqVO 入库单信息
     */
    void update(WmsPurchaseInSaveReqVO reqVO);

    /**
     * 删除采购入库单
     *
     * @param id 入库单ID
     */
    void delete(Long id);

    /**
     * 审核采购入库单
     *
     * @param id 入库单ID
     */
    void approve(Long id);

    /**
     * 反审核采购入库单
     *
     * @param id 入库单ID
     */
    void cancelApprove(Long id);

    /**
     * 根据采购订单ID获取可入库商品明细
     *
     * @param orderId 采购订单ID
     * @return 可入库商品明细列表
     */
    List<WmsPurchaseOrderItemRespVO> getOrderItems(Long orderId);

    /**
     * 获取指定采购订单中待审核入库单的商品数量汇总（skuId → 合计数量）
     *
     * @param orderId 采购订单ID
     * @return skuId到待审核入库数量的映射
     */
    Map<Long, BigDecimal> getPendingInboundQtyByOrder(Long orderId);

    /**
     * 获取已审核的采购入库单列表（用于退货）
     *
     * @return 已审核的入库单列表
     */
    List<WmsPurchaseInRespVO> listApproved();

    /**
     * 获取可退货的采购入库单列表（已审核且未全部退货）
     *
     * @return 可退货的入库单列表
     */
    List<WmsPurchaseInRespVO> listAvailableForReturn();

    /**
     * 根据关键字模糊匹配可退货的采购入库单列表
     *
     * @param keyword 入库单号或供应商名称关键字
     * @return 匹配到的可退货入库单列表
     */
    List<WmsPurchaseInRespVO> listAvailableForReturn(String keyword);

    /**
     * 导出采购入库列表（按明细行展开）
     *
     * @param reqVO 查询条件
     * @return 导出数据列表
     */
    List<WmsPurchaseInExportVO> exportList(WmsPurchaseInPageReqVO reqVO);
}
