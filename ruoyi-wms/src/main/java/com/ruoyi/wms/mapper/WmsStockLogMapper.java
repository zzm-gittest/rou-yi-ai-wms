package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogExportVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogPageReqVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogRespVO;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 库存变动记录表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsStockLogMapper extends BaseMapper<WmsStockLog> {

    /**
     * 分页查询库存变动日志
     *
     * @param page 分页参数
     * @param reqVO 查询条件
     * @return 库存变动日志列表
     */
    IPage<WmsStockLogRespVO> selectStockLogPage(Page<WmsStockLogPageReqVO> page, @Param("reqVO") WmsStockLogPageReqVO reqVO);

    /**
     * 查询库存变动日志导出列表
     *
     * @param reqVO 查询条件
     * @return 库存变动日志导出列表
     */
    List<WmsStockLogExportVO> selectStockLogExportList(@Param("reqVO") WmsStockLogPageReqVO reqVO);
}
