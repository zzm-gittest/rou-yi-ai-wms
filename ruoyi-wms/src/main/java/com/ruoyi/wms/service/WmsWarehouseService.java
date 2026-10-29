package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehousePageReqVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseRespVO;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseSaveReqVO;

import java.util.List;

/**
 * 仓库表 Service接口
 *
 * @author ruoyi
 */
public interface WmsWarehouseService extends IService<WmsWarehouse> {

    /**
     * 分页查询仓库列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsWarehouseRespVO> page(WmsWarehousePageReqVO reqVO);

    /**
     * 查询仓库列表（不分页，仅正常状态）
     *
     * @return 仓库列表
     */
    List<WmsWarehouseRespVO> selectList();

    /**
     * 获取仓库详情
     *
     * @param id 仓库ID
     * @return 仓库详情
     */
    WmsWarehouseRespVO get(Long id);

    /**
     * 新增仓库
     *
     * @param reqVO 仓库信息
     * @return 仓库ID
     */
    Long create(WmsWarehouseSaveReqVO reqVO);

    /**
     * 修改仓库
     *
     * @param reqVO 仓库信息
     */
    void update(WmsWarehouseSaveReqVO reqVO);

    /**
     * 删除仓库
     *
     * @param id 仓库ID
     */
    void delete(Long id);
}
