package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsExpenseType;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypePageReqVO;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypeRespVO;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypeSaveReqVO;

import java.util.List;

/**
 * 费用类型表 Service接口
 *
 * @author ruoyi
 */
public interface WmsExpenseTypeService extends IService<WmsExpenseType> {

    /**
     * 分页查询费用类型列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsExpenseTypeRespVO> page(WmsExpenseTypePageReqVO reqVO);

    /**
     * 查询启用的费用类型列表
     *
     * @return 费用类型列表
     */
    List<WmsExpenseTypeRespVO> listEnabled();

    /**
     * 获取费用类型详情
     *
     * @param id 费用类型ID
     * @return 费用类型详情
     */
    WmsExpenseTypeRespVO get(Long id);

    /**
     * 新增费用类型
     *
     * @param reqVO 费用类型信息
     * @return 费用类型ID
     */
    Long create(WmsExpenseTypeSaveReqVO reqVO);

    /**
     * 修改费用类型
     *
     * @param reqVO 费用类型信息
     */
    void update(WmsExpenseTypeSaveReqVO reqVO);

    /**
     * 删除费用类型
     *
     * @param id 费用类型ID
     */
    void delete(Long id);
}
