package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.vo.stock.WmsStockExportVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockPageReqVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;

import java.util.List;

/**
 * 库存表 Service接口
 *
 * @author ruoyi
 */
public interface WmsStockService extends IService<WmsStock> {

    /**
     * 分页查询库存列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsStockRespVO> page(WmsStockPageReqVO reqVO);

    /**
     * 获取库存详情
     *
     * @param id 库存ID
     * @return 库存详情
     */
    WmsStockRespVO get(Long id);

    /**
     * 根据仓库和SKU查询库存
     *
     * @param warehouseId 仓库ID
     * @param skuId SKU ID
     * @return 库存详情
     */
    WmsStockRespVO getByWarehouseAndSku(Long warehouseId, Long skuId);

    /**
     * 导出库存列表
     *
     * @param reqVO 查询条件
     * @return 库存导出列表
     */
    List<WmsStockExportVO> exportList(WmsStockPageReqVO reqVO);
}
