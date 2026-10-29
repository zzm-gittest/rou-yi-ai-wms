package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerPageReqVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO;

import java.util.List;

/**
 * 客户表 Service接口
 *
 * @author ruoyi
 */
public interface WmsCustomerService extends IService<WmsCustomer> {

    /**
     * 分页查询客户列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsCustomerRespVO> page(WmsCustomerPageReqVO reqVO);

    /**
     * 查询客户列表（不分页，仅正常状态）
     *
     * @return 客户列表
     */
    List<WmsCustomerRespVO> selectList();

    /**
     * 获取客户详情
     *
     * @param id 客户ID
     * @return 客户详情
     */
    WmsCustomerRespVO get(Long id);

    /**
     * 新增客户
     *
     * @param reqVO 客户信息
     * @return 客户ID
     */
    Long create(WmsCustomerSaveReqVO reqVO);

    /**
     * 修改客户
     *
     * @param reqVO 客户信息
     */
    void update(WmsCustomerSaveReqVO reqVO);

    /**
     * 删除客户
     *
     * @param id 客户ID
     */
    void delete(Long id);

    /**
     * 根据名称查找客户ID（用于AI匹配）
     *
     * @param name 客户名称
     * @return 客户ID，未找到返回null
     */
    Long findIdByName(String name);

    /**
     * 获取默认客户（is_default=1）
     *
     * @return 默认客户，未找到返回null
     */
    WmsCustomer getDefault();
}
