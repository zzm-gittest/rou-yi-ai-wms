package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.vo.stock.WmsStockExportVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockPageReqVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 库存表(SKU维度) Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsStockMapper extends BaseMapper<WmsStock> {

    /**
     * 分页查询库存列表
     *
     * @param page 分页参数
     * @param reqVO 查询条件
     * @return 库存列表
     */
    IPage<WmsStockRespVO> selectStockPage(Page<WmsStockPageReqVO> page, @Param("reqVO") WmsStockPageReqVO reqVO);

    /**
     * 查询库存详情
     *
     * @param id 库存ID
     * @return 库存详情
     */
    WmsStockRespVO selectStockById(@Param("id") Long id);

    /**
     * 查询库存导出列表
     *
     * @param reqVO 查询条件
     * @return 库存导出列表
     */
    List<WmsStockExportVO> selectStockExportList(@Param("reqVO") WmsStockPageReqVO reqVO);

    /**
     * 查询库存Top列表（按库存数量降序）
     *
     * @param limit 限制数量
     * @return 库存Top列表
     */
    List<WmsStockRespVO> selectStockTop(@Param("limit") int limit);

    /**
     * 查询库存预警列表（库存低于指定值）
     *
     * @param threshold 阈值
     * @param limit 限制数量
     * @return 库存预警列表
     */
    List<WmsStockRespVO> selectStockWarning(@Param("threshold") java.math.BigDecimal threshold, @Param("limit") int limit);

    /**
     * 按仓库统计库存
     *
     * @return 仓库库存统计列表
     */
    List<WmsStockRespVO> selectWarehouseStockSummary();
}
