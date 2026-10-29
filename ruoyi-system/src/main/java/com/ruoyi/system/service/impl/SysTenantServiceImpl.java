package com.ruoyi.system.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysRoleDept;
import com.ruoyi.system.domain.SysRoleMenu;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysRoleDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysRoleMenuMapper;
import com.ruoyi.system.mapper.SysTenantMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ISysTenantService;

/**
 * 租户 服务层处理
 *
 * @author ruoyi
 */
@Service
public class SysTenantServiceImpl implements ISysTenantService
{
    @Autowired
    private SysTenantMapper tenantMapper;

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询租户信息集合
     *
     * @param tenant 租户信息
     * @return 租户信息集合
     */
    @Override
    public List<SysTenant> selectTenantList(SysTenant tenant)
    {
        return tenantMapper.selectTenantList(tenant);
    }

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param tenant 租户信息
     * @return 租户信息分页结果
     */
    @Override
    public IPage<SysTenant> selectTenantListPage(Page<SysTenant> page, SysTenant tenant)
    {
        return tenantMapper.selectTenantListPage(page, tenant);
    }

    /**
     * 查询所有启用的租户
     *
     * @return 租户列表
     */
    @Override
    public List<SysTenant> selectTenantEnableList()
    {
        return tenantMapper.selectTenantEnableList();
    }

    /**
     * 通过租户ID查询租户信息
     *
     * @param tenantId 租户ID
     * @return 租户对象信息
     */
    @Override
    public SysTenant selectTenantById(Long tenantId)
    {
        return tenantMapper.selectTenantById(tenantId);
    }

    /**
     * 查询租户打印资料
     *
     * @param tenantId 租户ID
     * @return 租户打印资料
     */
    @Override
    public SysTenant selectTenantPrintProfile(Long tenantId)
    {
        return tenantMapper.selectTenantPrintProfile(tenantId);
    }

    /**
     * 根据租户名称查询租户信息
     *
     * @param tenantName 租户名称
     * @return 租户对象信息
     */
    @Override
    public SysTenant selectTenantByName(String tenantName)
    {
        return tenantMapper.selectTenantByName(tenantName);
    }

    /**
     * 校验租户名称是否唯一
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public boolean checkTenantNameUnique(SysTenant tenant)
    {
        Long tenantId = StringUtils.isNull(tenant.getTenantId()) ? -1L : tenant.getTenantId();
        SysTenant info = tenantMapper.checkTenantNameUnique(tenant.getTenantName());
        if (StringUtils.isNotNull(info) && info.getTenantId().longValue() != tenantId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增保存租户信息
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public int insertTenant(SysTenant tenant)
    {
        return tenantMapper.insertTenant(tenant);
    }

    /**
     * 修改保存租户信息
     * <p>
     * 当 edition 变更时，自动同步配额字段
     *
     * @param tenant 租户信息
     * @return 结果
     */
    @Override
    public int updateTenant(SysTenant tenant)
    {
        syncQuotaByEdition(tenant);
        return tenantMapper.updateTenant(tenant);
    }

    /**
     * 根据版本同步配额字段
     */
    private void syncQuotaByEdition(SysTenant tenant)
    {
        String edition = tenant.getEdition();
        if (StringUtils.isEmpty(edition))
        {
            return;
        }
        switch (edition)
        {
            case "free":
                tenant.setAiEnabled(0);
                tenant.setVoiceEnabled(0);
                tenant.setMonthlyQuotaText(0);
                tenant.setMonthlyQuotaVoice(0);
                tenant.setAnnualQuotaText(0);
                tenant.setAnnualQuotaVoice(0);
                break;
            case "basic":
                tenant.setAiEnabled(1);
                tenant.setVoiceEnabled(1);
                tenant.setMonthlyQuotaText(1500);
                tenant.setMonthlyQuotaVoice(60);
                tenant.setAnnualQuotaText(18000);
                tenant.setAnnualQuotaVoice(720);
                break;
            case "pro":
            case "professional":
            case "enterprise":
                tenant.setAiEnabled(1);
                tenant.setVoiceEnabled(1);
                tenant.setMonthlyQuotaText(1500);
                tenant.setMonthlyQuotaVoice(60);
                tenant.setAnnualQuotaText(15000);
                tenant.setAnnualQuotaVoice(600);
                break;
            default:
                break;
        }
    }

    /**
     * 更新租户打印资料
     *
     * @param tenant 租户打印资料
     * @return 结果
     */
    @Override
    public int updateTenantPrintProfile(SysTenant tenant)
    {
        return tenantMapper.updateTenantPrintProfile(tenant);
    }

    /**
     * 删除租户信息
     *
     * @param tenantId 租户ID
     * @return 结果
     */
    @Override
    public int deleteTenantById(Long tenantId)
    {
        return tenantMapper.deleteTenantById(tenantId);
    }

    /**
     * 批量删除租户信息
     *
     * @param tenantIds 需要删除的租户ID
     * @return 结果
     */
    @Override
    public int deleteTenantByIds(Long[] tenantIds)
    {
        return tenantMapper.deleteTenantByIds(tenantIds);
    }

    /**
     * 租户注册（包含创建管理员账号）
     *
     * @param username 账号
     * @param phone 手机号码
     * @param password 密码
     */
    @Override
    @Transactional
    public void registerTenant(String username, String phone, String password)
    {
        // 校验账号是否已存在
        SysUser checkUser = userMapper.checkUserNameUnique(username);
        if (StringUtils.isNotNull(checkUser))
        {
            throw new ServiceException("账号已存在");
        }

        // 校验手机号是否已存在
        if (StringUtils.isNotEmpty(phone))
        {
            SysUser checkPhone = userMapper.checkPhoneUnique(phone);
            if (StringUtils.isNotNull(checkPhone))
            {
                throw new ServiceException("手机号已被注册");
            }
        }

        // 生成唯一租户标识（使用时间戳）
        String tenantKey = "tenant_" + System.currentTimeMillis();

        // 创建租户
        SysTenant tenant = new SysTenant();
        tenant.setTenantName(tenantKey); // 唯一标识
        tenant.setContactUser(username); // 账号名称存储在联系人字段
        tenant.setContactPhone(phone);
        tenant.setAccountCount(1); // 初始账号数为1（管理员）
        tenant.setStatus("0"); // 默认状态正常
        tenant.setEdition("basic"); // 统一基础版，AI按点数使用
        tenant.setAiEnabled(1); // 默认开通AI助手入口
        tenant.setVoiceEnabled(1);
        tenant.setOcrEnabled(1);
        tenant.setScanEnabled(1);
        tenant.setAiPoints(50); // 新注册赠送50点体验点
        tenant.setAiPointsTotalCharged(50);
        tenant.setAiPointsTotalUsed(0);

        // 新注册用户：7天免费试用期
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, 7);
        tenant.setExpireTime(cal.getTime());

        tenantMapper.insertTenant(tenant);
        initDefaultApprovePolicy(tenant.getTenantId());

        // 创建默认部门
        SysDept defaultDept = new SysDept();
        defaultDept.setTenantId(tenant.getTenantId());
        defaultDept.setParentId(0L);
        defaultDept.setAncestors("0");
        defaultDept.setDeptName("总公司");
        defaultDept.setOrderNum(0);
        defaultDept.setLeader(username);
        defaultDept.setPhone(phone);
        defaultDept.setStatus("0");
        defaultDept.setDelFlag("0");
        defaultDept.setCreateBy("system");
        defaultDept.setCreateTime(new Date());
        // 使用 SysDeptMapper 的 insertDept 方法
        com.ruoyi.system.mapper.SysDeptMapper deptMapper = com.ruoyi.common.utils.spring.SpringUtils.getBean(com.ruoyi.system.mapper.SysDeptMapper.class);
        deptMapper.insertDept(defaultDept);

        // 创建租户管理员
        SysUser admin = new SysUser();
        admin.setTenantId(tenant.getTenantId());
        admin.setUserName(username);
        admin.setNickName(username);
        admin.setPhonenumber(phone);
        admin.setPassword(SecurityUtils.encryptPassword(password));
        admin.setStatus("0"); // 默认状态正常
        admin.setDeptId(defaultDept.getDeptId()); // 设置默认部门
        admin.setCreateBy("system");
        admin.setCreateTime(new Date());

        // 插入用户
        userMapper.insertUser(admin);

        // 创建租户专用角色
        SysRole tenantRole = new SysRole();
        tenantRole.setTenantId(tenant.getTenantId());
        tenantRole.setRoleName(username + "管理员");
        tenantRole.setRoleKey("tenant_admin_" + tenant.getTenantId());
        tenantRole.setRoleSort(1);
        tenantRole.setDataScope("1"); // 所有数据权限
        tenantRole.setStatus("0"); // 正常状态
        tenantRole.setDelFlag("0");
        tenantRole.setCreateBy("system");
        tenantRole.setCreateTime(new Date());
        tenantRole.setRemark("租户默认管理员角色");
        roleMapper.insertRole(tenantRole);

        // 为管理员分配租户角色
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(admin.getUserId());
        userRole.setRoleId(tenantRole.getRoleId());
        userRoleMapper.batchUserRole(java.util.Collections.singletonList(userRole));

        // 为租户角色分配菜单权限
        List<SysRoleMenu> roleMenuList = new ArrayList<>();
        Set<Long> assignedMenuIds = new LinkedHashSet<>();

        // 系统管理 -> 仅开放打印资料设置（perms = system:tenant:printProfile），其余菜单不分配
        List<Long> printProfileMenuIds = getMenuIdsByPerms("system:tenant:printProfile");
        for (Long menuId : printProfileMenuIds)
        {
            addRoleMenuIfAbsent(roleMenuList, assignedMenuIds, tenantRole.getRoleId(), menuId, tenant.getTenantId());
        }

        // 新租户默认开放WMS全量业务菜单（含AI菜单）；语音实际可用性由租户版本字段控制
        List<Long> wmsMenuIds = getMenuIdsByPerms("wms:%");
        for (Long menuId : wmsMenuIds)
        {
            addRoleMenuIfAbsent(roleMenuList, assignedMenuIds, tenantRole.getRoleId(), menuId, tenant.getTenantId());
        }

        // 新租户默认开放PC端AI助手菜单（ai:assistantPc:query 等）
        List<Long> aiMenuIds = getMenuIdsByPerms("ai:%");
        for (Long menuId : aiMenuIds)
        {
            addRoleMenuIfAbsent(roleMenuList, assignedMenuIds, tenantRole.getRoleId(), menuId, tenant.getTenantId());
        }

        roleMenuMapper.batchRoleMenu(roleMenuList);

        // 初始化角色部门权限：为角色分配租户默认部门权限
        List<SysRoleDept> roleDeptList = new ArrayList<>();
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setRoleId(tenantRole.getRoleId());
        roleDept.setDeptId(defaultDept.getDeptId());
        roleDept.setTenantId(tenant.getTenantId());
        roleDeptList.add(roleDept);
        roleDeptMapper.batchRoleDept(roleDeptList);

        // 初始化WMS默认基础数据
        initWmsDefaultData(tenant.getTenantId());
    }

    /**
     * 初始化新租户默认免审核策略
     */
    private void initDefaultApprovePolicy(Long tenantId)
    {
        if (tenantId == null)
        {
            return;
        }

        String operator = "register_flow";
        String remark = "Init default approve policy for new tenant";
        String sql = "INSERT IGNORE INTO wms_bill_approve_policy "
                + "(tenant_id, bill_type, need_approve, status, remark, create_by) VALUES (?, ?, 1, '0', ?, ?)";

        String[] billTypes = {
                "PURCHASE_ORDER",
                "PURCHASE_IN",
                "PURCHASE_RETURN",
                "SALES_ORDER",
                "SALES_OUT",
                "SALES_RETURN",
                "STOCK_CHECK",
                "STOCK_TRANSFER"
        };

        for (String billType : billTypes)
        {
            jdbcTemplate.update(sql, tenantId, billType, remark, operator);
        }
    }

    /**
     * 提交账号注销申请并立即停用租户，禁止再次登录。
     */
    @Override
    @Transactional
    public void submitCancelAccountRequest(Long tenantId, Long userId, String username, String reason)
    {
        if (tenantId == null)
        {
            throw new ServiceException("未获取到租户信息");
        }
        if (userId == null)
        {
            throw new ServiceException("未获取到用户信息");
        }

        SysTenant tenant = tenantMapper.selectTenantById(tenantId);
        if (tenant == null)
        {
            throw new ServiceException("租户不存在");
        }
        if (Integer.valueOf(1).equals(tenant.getCancelRequested()))
        {
            throw new ServiceException("账号注销申请已提交，请勿重复操作");
        }

        Date now = DateUtils.getNowDate();
        String normalizedReason = StringUtils.substring(StringUtils.trimToEmpty(reason), 0, 500);

        SysTenant updateTenant = new SysTenant();
        updateTenant.setTenantId(tenantId);
        updateTenant.setStatus("1");
        updateTenant.setCancelRequested(1);
        updateTenant.setCancelRequestedAt(now);
        updateTenant.setCancelReason(normalizedReason);
        updateTenant.setUpdateBy(username);
        updateTenant.setRemark(buildCancelRemark(tenant.getRemark(), username, normalizedReason, now));

        if (tenantMapper.updateTenant(updateTenant) <= 0)
        {
            throw new ServiceException("提交注销申请失败，请稍后重试");
        }

        jdbcTemplate.update(
            "update sys_user set status = ?, update_by = ?, update_time = sysdate() where tenant_id = ? and del_flag = '0'",
            "1", username, tenantId);
    }

    private String buildCancelRemark(String oldRemark, String username, String reason, Date applyTime)
    {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotEmpty(oldRemark))
        {
            builder.append(oldRemark.trim());
            builder.append("\n");
        }
        builder.append("[account-cancel-request] by=");
        builder.append(StringUtils.defaultString(username, "unknown"));
        builder.append(", at=");
        builder.append(DateUtils.parseDateToStr(DateUtils.YYYY_MM_DD_HH_MM_SS, applyTime));
        if (StringUtils.isNotEmpty(reason))
        {
            builder.append(", reason=");
            builder.append(reason);
        }
        return builder.toString();
    }

    /**
     * 初始化租户WMS默认数据
     */
    private void initWmsDefaultData(Long tenantId)
    {
        if (tenantId == null)
        {
            return;
        }

        jdbcTemplate.update("UPDATE wms_warehouse SET is_default = 0 WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update(
            "INSERT INTO wms_warehouse (tenant_id, warehouse_code, warehouse_name, status, sort, is_default, del_flag, create_by, create_time) " +
                "SELECT ?, ?, ?, '0', 0, 1, '0', 'system', NOW() FROM dual WHERE NOT EXISTS (" +
                "SELECT 1 FROM wms_warehouse WHERE tenant_id = ? AND warehouse_name = ?)",
            tenantId, "WH-DEFAULT-" + tenantId, "默认仓库", tenantId, "默认仓库"
        );
        jdbcTemplate.update("UPDATE wms_warehouse SET is_default = 1 WHERE tenant_id = ? AND warehouse_name = ?", tenantId, "默认仓库");

        jdbcTemplate.update("UPDATE wms_supplier SET is_default = 0 WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update(
            "INSERT INTO wms_supplier (tenant_id, supplier_code, supplier_name, status, is_default, del_flag, create_by, create_time) " +
                "SELECT ?, ?, ?, '0', 1, '0', 'system', NOW() FROM dual WHERE NOT EXISTS (" +
                "SELECT 1 FROM wms_supplier WHERE tenant_id = ? AND supplier_name = ?)",
            tenantId, "SUP-DEFAULT-" + tenantId, "默认供应商", tenantId, "默认供应商"
        );
        jdbcTemplate.update("UPDATE wms_supplier SET is_default = 1 WHERE tenant_id = ? AND supplier_name = ?", tenantId, "默认供应商");

        jdbcTemplate.update("UPDATE wms_customer SET is_default = 0 WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update(
            "INSERT INTO wms_customer (tenant_id, customer_code, customer_name, status, customer_type, is_default, del_flag, create_by, create_time) " +
                "SELECT ?, ?, ?, '0', '2', 1, '0', 'system', NOW() FROM dual WHERE NOT EXISTS (" +
                "SELECT 1 FROM wms_customer WHERE tenant_id = ? AND customer_name = ?)",
            tenantId, "CUS-DEFAULT-" + tenantId, "默认客户", tenantId, "默认客户"
        );
        jdbcTemplate.update("UPDATE wms_customer SET is_default = 1 WHERE tenant_id = ? AND customer_name = ?", tenantId, "默认客户");

        jdbcTemplate.update("UPDATE wms_unit SET is_default = 0 WHERE tenant_id = ?", tenantId);
        jdbcTemplate.update(
            "INSERT INTO wms_unit (tenant_id, unit_name, status, is_default, del_flag, create_by, create_time) " +
                "SELECT ?, ?, '0', 1, '0', 'system', NOW() FROM dual WHERE NOT EXISTS (" +
                "SELECT 1 FROM wms_unit WHERE tenant_id = ? AND unit_name = ?)",
            tenantId, "件", tenantId, "件"
        );
        jdbcTemplate.update("UPDATE wms_unit SET is_default = 1 WHERE tenant_id = ? AND unit_name = ?", tenantId, "件");
    }

    /**
     * 创建角色菜单关联对象
     *
     * @param roleId 角色ID
     * @param menuId 菜单ID
     * @param tenantId 租户ID
     * @return 角色菜单关联对象
     */
    private SysRoleMenu createRoleMenu(Long roleId, Long menuId, Long tenantId)
    {
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        roleMenu.setTenantId(tenantId);
        return roleMenu;
    }

    /**
     * 避免角色菜单重复插入，防止批量写入时出现重复键异常。
     */
    private void addRoleMenuIfAbsent(List<SysRoleMenu> roleMenuList, Set<Long> assignedMenuIds,
        Long roleId, Long menuId, Long tenantId)
    {
        if (menuId != null && assignedMenuIds.add(menuId))
        {
            roleMenuList.add(createRoleMenu(roleId, menuId, tenantId));
        }
    }

    /**
     * 根据权限标识模式查询菜单ID，并递归补齐父级菜单ID，确保前端菜单树完整可见。
     */
    private List<Long> getMenuIdsByPerms(String permsPattern)
    {
        Set<Long> menuIdSet = new LinkedHashSet<>(tenantMapper.selectMenuIdsByPerms(permsPattern));
        if (menuIdSet.isEmpty())
        {
            return new ArrayList<>();
        }

        List<Long> pendingMenuIds = new ArrayList<>(menuIdSet);
        while (!pendingMenuIds.isEmpty())
        {
            List<Long> parentMenuIds = tenantMapper.selectParentMenuIdsByMenuIds(pendingMenuIds);
            pendingMenuIds = new ArrayList<>();
            for (Long parentMenuId : parentMenuIds)
            {
                if (parentMenuId != null && menuIdSet.add(parentMenuId))
                {
                    pendingMenuIds.add(parentMenuId);
                }
            }
        }

        return new ArrayList<>(menuIdSet);
    }
}
