package com.ruoyi.web.controller.wms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.domain.WmsBillApprovePolicy;
import com.ruoyi.wms.domain.vo.approvePolicy.WmsBillApprovePolicyPageReqVO;
import com.ruoyi.wms.domain.vo.approvePolicy.WmsBillApprovePolicySaveReqVO;
import com.ruoyi.wms.service.WmsBillApprovePolicyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户单据审核策略配置 Controller
 *
 * 超级管理员：可查看/配置所有租户的审核策略
 * 普通租户：只能查看/配置自己租户的审核策略
 */
@Api("租户单据审核策略配置")
@RestController
@RequestMapping("/wms/approvePolicy")
@Validated
public class WmsBillApprovePolicyController extends BaseController {

    private final WmsBillApprovePolicyService billApprovePolicyService;

    public WmsBillApprovePolicyController(WmsBillApprovePolicyService billApprovePolicyService) {
        this.billApprovePolicyService = billApprovePolicyService;
    }

    /**
     * 查询租户单据审核策略分页列表
     * 超级管理员可按租户筛选；普通租户只能看自己的策略
     */
    @ApiOperation("查询租户单据审核策略分页列表")
    @PreAuthorize("@ss.hasPermi('wms:approvePolicy:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsBillApprovePolicyPageReqVO reqVO) {
        if (!SecurityUtils.isAdmin()) {
            reqVO.setTenantId(SecurityUtils.getTenantId());
        }
        IPage<WmsBillApprovePolicy> page = billApprovePolicyService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取租户单据审核策略详情
     * 普通租户只能查看自己租户的策略
     */
    @ApiOperation("获取租户单据审核策略详情")
    @PreAuthorize("@ss.hasPermi('wms:approvePolicy:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        WmsBillApprovePolicy policy = billApprovePolicyService.getById(id);
        if (policy != null && !SecurityUtils.isAdmin()) {
            if (!SecurityUtils.getTenantId().equals(policy.getTenantId())) {
                throw new ServiceException("\u65e0\u6743\u67e5\u770b\u5176\u4ed6\u79df\u6237\u7684\u7b56\u7565");
            }
        }
        return success(policy);
    }

    /**
     * 保存租户单据审核策略配置
     * 普通租户只能配置自己租户的策略
     */
    @ApiOperation("保存租户单据审核策略配置")
    @PreAuthorize("@ss.hasPermi('wms:approvePolicy:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsBillApprovePolicySaveReqVO reqVO) {
        if (!SecurityUtils.isAdmin()) {
            reqVO.setTenantId(SecurityUtils.getTenantId());
        }
        Long id = billApprovePolicyService.saveOrUpdatePolicy(reqVO);
        return success(id);
    }

    /**
     * 查询当前租户指定单据类型是否需要审核
     */
    @ApiOperation("查询当前租户指定单据类型是否需要审核")
    @GetMapping("/needApprove/{billType}")
    public AjaxResult needApprove(@PathVariable String billType) {
        Long tenantId = SecurityUtils.getTenantId();
        boolean needApprove = billApprovePolicyService.needApprove(tenantId, billType);
        return success(needApprove);
    }

    /**
     * 查询当前租户指定单据类型的完整策略（含是否自动打印）
     */
    @ApiOperation("查询当前租户指定单据类型的完整策略")
    @GetMapping("/policy/{billType}")
    public AjaxResult getPolicy(@PathVariable String billType) {
        Long tenantId = SecurityUtils.getTenantId();
        WmsBillApprovePolicy policy = billApprovePolicyService.findEnabledPolicy(tenantId, billType);
        if (policy == null) {
            // 返回默认策略：需要审核，不自动打印
            java.util.Map<String, Object> def = new java.util.HashMap<>();
            def.put("needApprove", true);
            def.put("autoPrint", false);
            def.put("billType", billType);
            return success(def);
        }
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("needApprove", policy.getNeedApprove() == null || policy.getNeedApprove() == 1);
        result.put("autoPrint", Integer.valueOf(1).equals(policy.getAutoPrint()) && (policy.getNeedApprove() != null && policy.getNeedApprove() == 0));
        result.put("billType", billType);
        return success(result);
    }
}
