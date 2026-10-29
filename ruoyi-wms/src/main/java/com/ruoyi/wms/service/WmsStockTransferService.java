package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsStockTransfer;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferExportVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferPageReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferSaveReqVO;
import java.util.List;

/**
 * 库存调拨单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsStockTransferService extends IService<WmsStockTransfer> {

    /**
     * 分页查询库存调拨单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsStockTransferRespVO> page(WmsStockTransferPageReqVO reqVO);

    /**
     * 获取库存调拨单详情
     *
     * @param id 调拨单ID
     * @return 调拨单详情
     */
    WmsStockTransferRespVO get(Long id);

    /**
     * 新增库存调拨单
     *
     * @param reqVO 调拨单信息
     * @return 调拨单ID
     */
    Long create(WmsStockTransferSaveReqVO reqVO);

    /**
     * 修改库存调拨单
     *
     * @param reqVO 调拨单信息
     */
    void update(WmsStockTransferSaveReqVO reqVO);

    /**
     * 删除库存调拨单
     *
     * @param id 调拨单ID
     */
    void delete(Long id);

    /**
     * 审核库存调拨单
     *
     * @param id 调拨单ID
     */
    void approve(Long id);

    /**
     * 导出库存调拨列表（按明细行展开）
     */
    List<WmsStockTransferExportVO> exportList(WmsStockTransferPageReqVO reqVO);
}
