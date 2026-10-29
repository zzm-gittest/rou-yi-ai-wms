package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.vo.account.WmsAccountPageReqVO;
import com.ruoyi.wms.domain.vo.account.WmsAccountRespVO;
import com.ruoyi.wms.domain.vo.account.WmsAccountSaveReqVO;

import java.util.List;

/**
 * 收款账户表 Service接口
 *
 * @author ruoyi
 */
public interface WmsAccountService extends IService<WmsAccount> {

    /**
     * 分页查询结算账户列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsAccountRespVO> page(WmsAccountPageReqVO reqVO);

    /**
     * 查询启用的结算账户列表
     *
     * @return 账户列表
     */
    List<WmsAccountRespVO> listEnabled();

    /**
     * 获取结算账户详情
     *
     * @param id 账户ID
     * @return 账户详情
     */
    WmsAccountRespVO get(Long id);

    /**
     * 新增结算账户
     *
     * @param reqVO 账户信息
     * @return 账户ID
     */
    Long create(WmsAccountSaveReqVO reqVO);

    /**
     * 修改结算账户
     *
     * @param reqVO 账户信息
     */
    void update(WmsAccountSaveReqVO reqVO);

    /**
     * 删除结算账户
     *
     * @param id 账户ID
     */
    void delete(Long id);
}
