package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsPurchaseReturn;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnExportVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;

import java.util.List;

/**
 * 采购退货单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsPurchaseReturnService extends IService<WmsPurchaseReturn> {

    /**
     * 分页查询采购退货单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsPurchaseReturnRespVO> page(WmsPurchaseReturnPageReqVO reqVO);

    /**
     * 获取采购退货单详情
     *
     * @param id 退货单ID
     * @return 退货单详情
     */
    WmsPurchaseReturnRespVO get(Long id);

    /**
     * 新增采购退货单
     *
     * @param reqVO 退货单信息
     * @return 退货单ID
     */
    Long create(WmsPurchaseReturnSaveReqVO reqVO);

    /**
     * 修改采购退货单
     *
     * @param reqVO 退货单信息
     */
    void update(WmsPurchaseReturnSaveReqVO reqVO);

    /**
     * 删除采购退货单
     *
     * @param id 退货单ID
     */
    void delete(Long id);

    /**
     * 审核采购退货单
     *
     * @param id 退货单ID
     */
    void approve(Long id);

    /**
     * 获取采购入库单的商品明细（用于退货）
     *
     * @param inId 入库单ID
     * @return 商品明细列表
     */
    List<WmsPurchaseInItemRespVO> getInItems(Long inId);

    /**
     * 导出采购退货列表（按明细行展开）
     */
    List<WmsPurchaseReturnExportVO> exportList(WmsPurchaseReturnPageReqVO reqVO);
}
