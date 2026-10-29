package com.ruoyi.system.service;

import java.util.List;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.entity.SysTenant;

/**
 * 租户 服务层
 *
 * @author ruoyi
 */
public interface ISysTenantService
{
    /**
     * 查询租户信息集合
     *
     * @param tenant 租户信息
     * @return 租户列表
     */
    public List<SysTenant> selectTenantList(SysTenant tenant);

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param tenant 租户信息
     * @return 租户分页结果
     */
    public IPage<SysTenant> selectTenantListPage(Page<SysTenant> page, SysTenant tenant);

    /**
     * 查询所有启用的租户
     *
     * @return 租户列表
     */
    public List<SysTenant> selectTenantEnableList();

    /**
     * 通过租户ID查询租户信息
     *
     * @param tenantId 租户ID
     * @return 租户对象信息
     */
    public SysTenant selectTenantById(Long tenantId);

    /**
     * 查询租户打印资料
     *
     * @param tenantId 租户ID
     * @return 租户打印资料
     */
    public SysTenant selectTenantPrintProfile(Long tenantId);

    /**
     * 根据租户名称查询租户信息
     *
     * @param tenantName 租户名称
     * @return 租户对象信息
     */
    public SysTenant selectTenantByName(String tenantName);

    /**
     * 校验租户名称是否唯一
     *
     * @param tenant 租户信息
     * @return 结果
     */
    public boolean checkTenantNameUnique(SysTenant tenant);

    /**
     * 新增保存租户信息
     *
     * @param tenant 租户信息
     * @return 结果
     */
    public int insertTenant(SysTenant tenant);

    /**
     * 修改保存租户信息
     *
     * @param tenant 租户信息
     * @return 结果
     */
    public int updateTenant(SysTenant tenant);

    /**
     * 更新租户打印资料
     *
     * @param tenant 租户打印资料
     * @return 结果
     */
    public int updateTenantPrintProfile(SysTenant tenant);

    /**
     * 删除租户信息
     *
     * @param tenantId 租户ID
     * @return 结果
     */
    public int deleteTenantById(Long tenantId);

    /**
     * 批量删除租户信息
     *
     * @param tenantIds 需要删除的租户ID
     * @return 结果
     */
    public int deleteTenantByIds(Long[] tenantIds);

    /**
     * 租户注册（包含创建管理员账号）
     *
     * @param username 账号
     * @param phone 手机号码
     * @param password 密码
     */
    public void registerTenant(String username, String phone, String password);

    /**
     * 提交当前租户的账号注销申请
     *
     * @param tenantId 租户ID
     * @param userId 当前用户ID
     * @param username 当前用户名
     * @param reason 注销原因
     */
    public void submitCancelAccountRequest(Long tenantId, Long userId, String username, String reason);
}
