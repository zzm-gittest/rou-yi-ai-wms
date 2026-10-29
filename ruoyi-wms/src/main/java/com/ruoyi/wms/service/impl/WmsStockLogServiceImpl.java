package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogExportVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogPageReqVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogRespVO;
import com.ruoyi.wms.mapper.WmsStockLogMapper;
import com.ruoyi.wms.service.WmsStockLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存日志表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsStockLogServiceImpl extends ServiceImpl<WmsStockLogMapper, WmsStockLog> implements WmsStockLogService {

    @Override
    public IPage<WmsStockLogRespVO> page(WmsStockLogPageReqVO reqVO) {
        Page<WmsStockLogPageReqVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        return baseMapper.selectStockLogPage(page, reqVO);
    }

    @Override
    public List<WmsStockLogExportVO> exportList(WmsStockLogPageReqVO reqVO) {
        return baseMapper.selectStockLogExportList(reqVO);
    }
}
