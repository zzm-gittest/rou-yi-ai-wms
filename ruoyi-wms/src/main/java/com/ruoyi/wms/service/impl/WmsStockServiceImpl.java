package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.vo.stock.WmsStockExportVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockPageReqVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;
import com.ruoyi.wms.mapper.WmsStockMapper;
import com.ruoyi.wms.service.WmsStockService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsStockServiceImpl extends ServiceImpl<WmsStockMapper, WmsStock> implements WmsStockService {

    @Override
    public IPage<WmsStockRespVO> page(WmsStockPageReqVO reqVO) {
        Page<WmsStockPageReqVO> page = PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsStockRespVO> resultPage = baseMapper.selectStockPage(page, reqVO);
        return resultPage;
    }

    @Override
    public WmsStockRespVO get(Long id) {
        return baseMapper.selectStockById(id);
    }

    @Override
    public WmsStockRespVO getByWarehouseAndSku(Long warehouseId, Long skuId) {
        WmsStock stock = lambdaQuery()
                .eq(WmsStock::getWarehouseId, warehouseId)
                .eq(WmsStock::getSkuId, skuId)
                .one();
        if (stock == null) {
            return null;
        }
        return baseMapper.selectStockById(stock.getId());
    }

    @Override
    public List<WmsStockExportVO> exportList(WmsStockPageReqVO reqVO) {
        return baseMapper.selectStockExportList(reqVO);
    }
}
