package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderExportVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderPageReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderSaveReqVO;

import java.util.List;
import java.util.Map;

/**
 * 销售订单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsSalesOrderService extends IService<WmsSalesOrder> {

    /**
     * 分页查询销售订单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsSalesOrderRespVO> page(WmsSalesOrderPageReqVO reqVO);

    /**
     * 获取销售订单详情
     *
     * @param id 订单ID
     * @return 订单详情
     */
    WmsSalesOrderRespVO get(Long id);

    /**
     * 新增销售订单
     *
     * @param reqVO 订单信息
     * @return 订单ID
     */
    Long create(WmsSalesOrderSaveReqVO reqVO);

    /**
     * 修改销售订单
     *
     * @param reqVO 订单信息
     */
    void update(WmsSalesOrderSaveReqVO reqVO);

    /**
     * 删除销售订单
     *
     * @param id 订单ID
     */
    void delete(Long id);

    /**
     * 审核销售订单
     *
     * @param id 订单ID
     */
    void approve(Long id);

    /**
     * 关闭销售订单
     *
     * @param id 订单ID
     */
    void close(Long id);

    /**
     * 查询已审核的销售订单列表
     *
     * @return 订单列表
     */
    List<WmsSalesOrderRespVO> listApproved();

    /**
     * 获取销售订单明细
     *
     * @param orderId 订单ID
     * @return 明细列表
     */
    List<WmsSalesOrderItemRespVO> getItems(Long orderId);

    /**
     * 按关键字搜索可出库的已审核销售订单
     *
     * @param keyword 关键字（匹配订单号或客户名称）
     * @return 候选记录列表
     */
    List<Map<String, Object>> searchAvailableForOut(String keyword);

    /**
     * 导出销售订单列表（按明细行展开）
     */
    List<WmsSalesOrderExportVO> exportList(WmsSalesOrderPageReqVO reqVO);
}
