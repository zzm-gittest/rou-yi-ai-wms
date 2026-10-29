package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.AiPointsService;
import com.ruoyi.system.service.ISysTenantService;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 租户信息操作处理
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/tenant")
public class SysTenantController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(SysTenantController.class);

    @Autowired
    private ISysTenantService tenantService;

    @Autowired
    private AiPointsService aiPointsService;

    /**
     * 获取租户列表
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysTenant tenant)
    {
        Page<SysTenant> page = PageUtils.getPage();
        IPage<SysTenant> result = tenantService.selectTenantListPage(page, tenant);
        return getDataTable(result);
    }

    /**
     * 获取启用的租户列表（登录时使用）
     */
    @GetMapping("/listEnable")
    public AjaxResult listEnable()
    {
        List<SysTenant> list = tenantService.selectTenantEnableList();
        return success(list);
    }

    /**
     * 导出租户数据
     */
    @Log(title = "租户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:tenant:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysTenant tenant)
    {
        List<SysTenant> list = tenantService.selectTenantList(tenant);
        ExcelUtil<SysTenant> util = new ExcelUtil<SysTenant>(SysTenant.class);
        util.exportExcel(response, list, "租户数据");
    }

    /**
     * 根据租户编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    @GetMapping(value = "/{tenantId}")
    public AjaxResult getInfo(@PathVariable Long tenantId)
    {
        return success(tenantService.selectTenantById(tenantId));
    }

    /**
     * 获取当前租户打印资料
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:printProfile')")
    @GetMapping("/printProfile")
    public AjaxResult getPrintProfile()
    {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            return error("未获取到租户信息");
        }

        SysTenant tenant = tenantService.selectTenantPrintProfile(tenantId);
        if (tenant == null)
        {
            return error("租户不存在");
        }
        return success(buildPrintProfileResp(tenant));
    }

    /**
     * 保存当前租户打印资料
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:printProfile')")
    @Log(title = "租户打印资料", businessType = BusinessType.UPDATE)
    @PutMapping("/printProfile")
    public AjaxResult updatePrintProfile(@RequestBody SysTenant profile)
    {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            return error("未获取到租户信息");
        }

        SysTenant tenant = new SysTenant();
        tenant.setTenantId(tenantId);
        tenant.setPrintCompanyName(profile.getPrintCompanyName());
        tenant.setPrintCompanyAddress(profile.getPrintCompanyAddress());
        tenant.setPrintCompanyPhone(profile.getPrintCompanyPhone());
        tenant.setPrintLogoPath(profile.getPrintLogoPath());
        tenant.setPrintFontScale(profile.getPrintFontScale());
        tenant.setPrintPageSize(profile.getPrintPageSize());
        tenant.setAiApiKey(profile.getAiApiKey());
        tenant.setAiModel(profile.getAiModel());
        tenant.setAiApiUrl(profile.getAiApiUrl());
        tenant.setAiVisionModel(profile.getAiVisionModel());
        tenant.setAiVoiceModel(profile.getAiVoiceModel());
        tenant.setUpdateBy(getUsername());
        return toAjax(tenantService.updateTenantPrintProfile(tenant));
    }

    /**
     * 新增租户
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:add')")
    @Log(title = "租户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysTenant tenant)
    {
        if (!tenantService.checkTenantNameUnique(tenant))
        {
            return error("新增租户'" + tenant.getTenantName() + "'失败，租户名称已存在");
        }
        tenant.setCreateBy(getUsername());
        return toAjax(tenantService.insertTenant(tenant));
    }

    /**
     * 修改租户
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    @Log(title = "租户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysTenant tenant)
    {
        if (!tenantService.checkTenantNameUnique(tenant))
        {
            return error("修改租户'" + tenant.getTenantName() + "'失败，租户名称已存在");
        }
        tenant.setUpdateBy(getUsername());
        return toAjax(tenantService.updateTenant(tenant));
    }

    /**
     * 删除租户
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:remove')")
    @Log(title = "租户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tenantIds}")
    public AjaxResult remove(@PathVariable Long[] tenantIds)
    {
        return toAjax(tenantService.deleteTenantByIds(tenantIds));
    }

    /**
     * 租户注册
     */
    @Log(title = "租户注册", businessType = BusinessType.INSERT)
    @PostMapping("/register")
    public AjaxResult register(@RequestBody Map<String, String> params)
    {
        try
        {
            String username = params.get("username");
            String phone = params.get("phone");
            String password = params.get("password");

            tenantService.registerTenant(username, phone, password);
            return success("注册成功");
        }
        catch (Exception e)
        {
            log.error("租户注册失败, username={}, phone={}", params.get("username"), params.get("phone"), e);
            return error(e.getMessage());
        }
    }

    /**
     * 获取当前租户的功能配置
     * 移动端调用此接口获取租户版本和功能开关信息
     */
    @GetMapping("/feature")
    public AjaxResult getFeatureConfig()
    {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null)
        {
            return error("未获取到租户信息");
        }
        SysTenant tenant = tenantService.selectTenantById(tenantId);
        if (tenant == null)
        {
            return error("租户不存在");
        }

        boolean subscriptionActive = tenant.getSubscriptionType() != null && tenant.isSubscriptionValid();
        String edition = tenant.getEdition() != null ? tenant.getEdition() : "free";
        if (subscriptionActive)
        {
            edition = "yearly".equals(tenant.getSubscriptionType()) ? "pro" : "basic";
        }

        return AjaxResult.success()
            .put("edition", edition)
            .put("aiEnabled", subscriptionActive || (tenant.getAiEnabled() != null && tenant.getAiEnabled() == 1))
            .put("voiceEnabled", subscriptionActive || (tenant.getVoiceEnabled() != null && tenant.getVoiceEnabled() == 1))
            .put("ocrEnabled", subscriptionActive || (tenant.getOcrEnabled() != null && tenant.getOcrEnabled() == 1))
            .put("scanEnabled", subscriptionActive || (tenant.getScanEnabled() != null && tenant.getScanEnabled() == 1));
    }

    /**
     * 管理员手动给租户充值AI点数
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    @Log(title = "AI点数充值", businessType = BusinessType.UPDATE)
    @PostMapping("/rechargePoints")
    public AjaxResult rechargePoints(@RequestBody Map<String, Object> params)
    {
        Long tenantId = params.get("tenantId") != null ? Long.valueOf(params.get("tenantId").toString()) : null;
        Integer amount = params.get("amount") != null ? Integer.valueOf(params.get("amount").toString()) : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";

        if (tenantId == null || amount == null || amount <= 0)
        {
            return error("参数错误：租户ID和充值点数不能为空，且点数必须大于0");
        }
        if (amount > 100000)
        {
            return error("单次充值不能超过100000点");
        }

        String bizId = "ADMIN_" + System.currentTimeMillis();
        String fullRemark = "管理员[" + getUsername() + "]手动充值" + (remark.isEmpty() ? "" : "：" + remark);
        boolean success = aiPointsService.chargePoints(tenantId, amount, bizId, fullRemark);
        if (!success)
        {
            return error("充值失败，租户不存在或已删除");
        }
        Map<String, Object> result = aiPointsService.getPointsInfo(tenantId);
        return success(result);
    }

    /**
     * 查询租户AI点数信息（管理端）
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    @GetMapping("/pointsInfo/{tenantId}")
    public AjaxResult getPointsInfo(@PathVariable Long tenantId)
    {
        return success(aiPointsService.getPointsInfo(tenantId));
    }

    /**
     * 查询租户AI点数流水（管理端）
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    @GetMapping("/pointsLog/{tenantId}")
    public AjaxResult getPointsLog(@PathVariable Long tenantId)
    {
        return success(aiPointsService.getPointsLog(tenantId, 50));
    }

    /**
     * 管理员手动设置租户订阅（用于安卓用户线下付费开通）
     *
     * @param params { tenantId: 租户ID, type: monthly/yearly, remark: 备注 }
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:edit')")
    @Log(title = "手动开通订阅", businessType = BusinessType.UPDATE)
    @PostMapping("/setSubscription")
    public AjaxResult setSubscription(@RequestBody Map<String, Object> params)
    {
        Long tenantId = params.get("tenantId") != null ? Long.valueOf(params.get("tenantId").toString()) : null;
        String type = params.get("type") != null ? params.get("type").toString() : null;
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";

        if (tenantId == null || type == null)
        {
            return error("参数错误：租户ID和订阅类型不能为空");
        }
        if (!"monthly".equals(type) && !"yearly".equals(type))
        {
            return error("订阅类型必须为 monthly 或 yearly");
        }

        // 计算到期时间
        Calendar cal = Calendar.getInstance();
        if ("monthly".equals(type))
        {
            cal.add(Calendar.MONTH, 1);
        }
        else
        {
            cal.add(Calendar.YEAR, 1);
        }
        Date expireTime = cal.getTime();

        String bizId = "ADMIN_SUB_" + System.currentTimeMillis();
        String fullRemark = "管理员[" + getUsername() + "]手动开通" + ("monthly".equals(type) ? "月付" : "年付")
                + "订阅" + (remark.isEmpty() ? "" : "\uff1a" + remark);

        boolean success = aiPointsService.updateSubscription(tenantId, type, expireTime, bizId);
        if (!success)
        {
            return error("开通订阅失败，租户不存在或已删除");
        }

        // 记录流水
        aiPointsService.logSubscriptionChange(tenantId, type, expireTime, fullRemark);

        Map<String, Object> result = aiPointsService.getSubscriptionInfo(tenantId);
        log.info("管理员[{}]为租户[{}]手动开通{}订阅，到期时间: {}", getUsername(), tenantId, type, expireTime);
        return success(result);
    }

    /**
     * 查询租户订阅状态（管理端）
     */
    @PreAuthorize("@ss.hasPermi('system:tenant:query')")
    @GetMapping("/subscriptionInfo/{tenantId}")
    public AjaxResult getSubscriptionInfo(@PathVariable Long tenantId)
    {
        return success(aiPointsService.getSubscriptionInfo(tenantId));
    }

    private SysTenant buildPrintProfileResp(SysTenant tenant)
    {
        SysTenant resp = new SysTenant();
        resp.setTenantId(tenant.getTenantId());
        resp.setPrintCompanyName(tenant.getPrintCompanyName());
        resp.setPrintCompanyAddress(tenant.getPrintCompanyAddress());
        resp.setPrintCompanyPhone(tenant.getPrintCompanyPhone());
        resp.setPrintLogoPath(tenant.getPrintLogoPath());
        resp.setPrintFontScale(tenant.getPrintFontScale());
        resp.setPrintPageSize(tenant.getPrintPageSize());
        resp.setAiApiKey(tenant.getAiApiKey());
        resp.setAiModel(tenant.getAiModel());
        resp.setAiApiUrl(tenant.getAiApiUrl());
        resp.setAiVisionModel(tenant.getAiVisionModel());
        resp.setAiVoiceModel(tenant.getAiVoiceModel());
        return resp;
    }
}
