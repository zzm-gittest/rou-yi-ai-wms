package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsUnit;
import com.ruoyi.wms.domain.vo.unit.WmsUnitPageReqVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitRespVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;

import java.util.List;

/**
 * 计量单位表 Service接口
 *
 * @author ruoyi
 */
public interface WmsUnitService extends IService<WmsUnit> {

    /**
     * 分页查询单位列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsUnitRespVO> page(WmsUnitPageReqVO reqVO);

    /**
     * 查询单位列表（不分页，仅正常状态）
     *
     * @return 单位列表
     */
    List<WmsUnitRespVO> selectList();

    /**
     * 获取单位详情
     *
     * @param id 单位ID
     * @return 单位详情
     */
    WmsUnitRespVO get(Long id);

    /**
     * 新增单位
     *
     * @param reqVO 单位信息
     * @return 单位ID
     */
    Long create(WmsUnitSaveReqVO reqVO);

    /**
     * 修改单位
     *
     * @param reqVO 单位信息
     */
    void update(WmsUnitSaveReqVO reqVO);

    /**
     * 删除单位
     *
     * @param id 单位ID
     */
    void delete(Long id);
}
