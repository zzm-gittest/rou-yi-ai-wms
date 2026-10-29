package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierPageReqVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;

import java.util.List;

/**
 * 供应商表 Service接口
 *
 * @author ruoyi
 */
public interface WmsSupplierService extends IService<WmsSupplier> {

    /**
     * 分页查询供应商列表
     * <p>

 * 该方法用于根据查询条件分页获取供应商信息
 * </p>
 *
     * @param reqVO 查询条件封装对象，包含分页参数和其他查询条件
     * @return 分页结果
     */
    IPage<WmsSupplierRespVO> page(WmsSupplierPageReqVO reqVO);

    /**
     * 查询供应商列表（不分页，仅正常状态）
     *
     * @return 供应商列表
     */
    List<WmsSupplierRespVO> selectList();

    /**
     * 获取供应商详情
     *
     * @param id 供应商ID
     * @return 供应商详情
     */
    WmsSupplierRespVO get(Long id);

    /**
     * 新增供应商
     *
     * @param reqVO 供应商信息
     * @return 供应商ID
     */
    Long create(WmsSupplierSaveReqVO reqVO);

    /**
     * 修改供应商
     *
     * @param reqVO 供应商信息
     */
    void update(WmsSupplierSaveReqVO reqVO);

    /**
     * 删除供应商
     *
     * @param id 供应商ID
     */
    void delete(Long id);

    /**
     * 根据名称查找供应商ID（用于AI匹配）
     *
     * @param name 供应商名称
     * @return 供应商ID，未找到返回null
     */
    Long findIdByName(String name);

    /**
     * 获取默认供应商（is_default=1）
     *
     * @return 默认供应商，未找到返回null
     */
    WmsSupplier getDefault();
}
