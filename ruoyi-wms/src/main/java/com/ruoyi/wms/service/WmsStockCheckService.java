package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsStockCheck;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckExportVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckPageReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckSaveReqVO;
import java.util.List;

/**
 * 库存盘点单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsStockCheckService extends IService<WmsStockCheck> {

    /**
     * 分页查询库存盘点单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsStockCheckRespVO> page(WmsStockCheckPageReqVO reqVO);

    /**
     * 获取库存盘点单详情
     *
     * @param id 盘点单ID
     * @return 盘点单详情
     */
    WmsStockCheckRespVO get(Long id);

    /**
     * 新增库存盘点单
     *
     * @param reqVO 盘点单信息
     * @return 盘点单ID
     */
    Long create(WmsStockCheckSaveReqVO reqVO);

    /**
     * 修改库存盘点单
     *
     * @param reqVO 盘点单信息
     */
    void update(WmsStockCheckSaveReqVO reqVO);

    /**
     * 删除库存盘点单
     *
     * @param id 盘点单ID
     */
    void delete(Long id);

    /**
     * 审核库存盘点单
     *
     * @param id 盘点单ID
     */
    void approve(Long id);

    /**
     * 导出库存盘点列表（按明细行展开）
     */
    List<WmsStockCheckExportVO> exportList(WmsStockCheckPageReqVO reqVO);
}
