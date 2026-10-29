package com.ruoyi.system.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.core.domain.entity.SysTenant;
import org.apache.ibatis.annotations.Param;

/**
 * 租户 数据层
 *
 * @author ruoyi
 */
public interface SysTenantMapper extends BaseMapper<SysTenant>
{
    /**
     * 查询租户数据集合
     *
     * @param tenant 租户信息
     * @return 租户数据集合
     */
    public List<SysTenant> selectTenantList(SysTenant tenant);

    /**
     * 分页查询租户数据
     *
     * @param page 分页对象
     * @param tenant 租户信息
     * @return 租户数据分页结果
     */
    public IPage<SysTenant> selectTenantListPage(Page<SysTenant> page, SysTenant tenant);

    /**
     * 查询所有启用租户
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
     * 新增租户信息
     *
     * @param tenant 租户信息
     * @return 结果
     */
    public int insertTenant(SysTenant tenant);

    /**
     * 修改租户信息
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
     * 校验租户名称是否唯一
     *
     * @param tenantName 租户名称
     * @return 结果
     */
    public SysTenant checkTenantNameUnique(String tenantName);

    /**
     * 根据权限标识模式查询菜单ID列表
     *
     * @param permsPattern 权限标识模式（支持SQL LIKE通配符）
     * @return 菜单ID列表
     */
    public List<Long> selectMenuIdsByPerms(@Param("permsPattern") String permsPattern);

    /**
     * 根据菜单ID集合查询其父菜单ID列表
     *
     * @param menuIds 菜单ID集合
     * @return 父菜单ID列表
     */
    public List<Long> selectParentMenuIdsByMenuIds(@Param("menuIds") List<Long> menuIds);

    /**
     * 原子预占文字解析配额
     *
     * @param tenantId 租户ID
     * @return 更新行数（1=成功，0=配额不足或未启用）
     */
    public int tryReserveTextQuota(@Param("tenantId") Long tenantId);

    /**
     * 原子预占语音识别配额
     *
     * @param tenantId 租户ID
     * @return 更新行数（1=成功，0=配额不足或未启用）
     */
    public int tryReserveVoiceQuota(@Param("tenantId") Long tenantId);

    /**
     * 回滚文字解析配额预占
     *
     * @param tenantId 租户ID
     * @return 更新行数
     */
    public int rollbackTextQuota(@Param("tenantId") Long tenantId);

    /**
     * 回滚语音识别配额预占
     *
     * @param tenantId 租户ID
     * @return 更新行数
     */
    public int rollbackVoiceQuota(@Param("tenantId") Long tenantId);

    /**
     * 月度配额重置
     *
     * @return 更新行数
     */
    public int resetMonthlyQuota();

    /**
     * 年度配额重置
     *
     * @return 更新行数
     */
    public int resetAnnualQuota();

    /**
     * 原子扣减AI点数
     *
     * @param tenantId 租户ID
     * @param cost 消耗点数
     * @return 更新行数（1=成功，0=点数不足或未启用）
     */
    public int tryConsumePoints(@Param("tenantId") Long tenantId, @Param("cost") int cost);

    /**
     * 退还AI点数
     *
     * @param tenantId 租户ID
     * @param amount 退还点数
     * @return 更新行数
     */
    public int refundPoints(@Param("tenantId") Long tenantId, @Param("amount") int amount);

    /**
     * 充值AI点数
     *
     * @param tenantId 租户ID
     * @param amount 充值点数
     * @return 更新行数
     */
    public int chargePoints(@Param("tenantId") Long tenantId, @Param("amount") int amount);

    /**
     * 更新订阅信息
     *
     * @param tenantId 租户ID
     * @param subscriptionType 订阅类型（monthly/yearly）
     * @param subscriptionExpireTime 订阅到期时间
     * @param appleOriginalTransactionId Apple原始交易ID
     * @return 更新行数
     */
    public int updateSubscription(@Param("tenantId") Long tenantId,
                                  @Param("subscriptionType") String subscriptionType,
                                  @Param("subscriptionExpireTime") java.util.Date subscriptionExpireTime,
                                  @Param("appleOriginalTransactionId") String appleOriginalTransactionId);

    /**
     * 根据Apple原始交易ID查找租户
     *
     * @param originalTransactionId Apple原始交易ID
     * @return 租户对象
     */
    public SysTenant selectTenantByAppleOriginalTxId(@Param("originalTransactionId") String originalTransactionId);
}
