package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsExpense;
import com.ruoyi.wms.domain.vo.expense.WmsExpensePageReqVO;
import com.ruoyi.wms.domain.vo.expense.WmsExpenseRespVO;
import com.ruoyi.wms.domain.vo.expense.WmsExpenseSaveReqVO;

/**
 * 费用表 Service接口
 *
 * @author ruoyi
 */
public interface WmsExpenseService extends IService<WmsExpense> {

    /**
     * 分页查询费用列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsExpenseRespVO> page(WmsExpensePageReqVO reqVO);

    /**
     * 获取费用详情
     *
     * @param id 费用ID
     * @return 费用详情
     */
    WmsExpenseRespVO get(Long id);

    /**
     * 新增费用
     *
     * @param reqVO 费用信息
     * @return 费用ID
     */
    Long create(WmsExpenseSaveReqVO reqVO);

    /**
     * 修改费用
     *
     * @param reqVO 费用信息
     */
    void update(WmsExpenseSaveReqVO reqVO);

    /**
     * 删除费用
     *
     * @param id 费用ID
     */
    void delete(Long id);

    /**
     * 审核费用
     *
     * @param id 费用ID
     */
    void approve(Long id);
}
