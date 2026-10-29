package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogExportVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogPageReqVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogRespVO;

import java.util.List;

/**
 * 库存日志表 Service接口
 *
 * @author ruoyi
 */
public interface WmsStockLogService extends IService<WmsStockLog> {

    /**
     * 分页查询库存变动日志
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsStockLogRespVO> page(WmsStockLogPageReqVO reqVO);

    /**
     * 导出库存变动日志列表
     *
     * @param reqVO 查询条件
     * @return 库存变动日志导出列表
     */
    List<WmsStockLogExportVO> exportList(WmsStockLogPageReqVO reqVO);
}
