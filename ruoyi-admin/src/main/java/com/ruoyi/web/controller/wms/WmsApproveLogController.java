package com.ruoyi.web.controller.wms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.domain.WmsApproveLog;
import com.ruoyi.wms.domain.vo.approveLog.WmsApproveLogPageReqVO;
import com.ruoyi.wms.service.WmsApproveLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 审核日志查询 Controller
 */
@Api("审核日志查询")
@RestController
@RequestMapping("/wms/approveLog")
@Validated
public class WmsApproveLogController extends BaseController {

    private final WmsApproveLogService approveLogService;

    public WmsApproveLogController(WmsApproveLogService approveLogService) {
        this.approveLogService = approveLogService;
    }

    /**
     * 查询审核日志分页列表
     */
    @ApiOperation("查询审核日志分页列表")
    @PreAuthorize("@ss.hasPermi('wms:approveLog:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsApproveLogPageReqVO reqVO) {
        checkSuperAdmin();
        IPage<WmsApproveLog> page = approveLogService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取审核日志详情
     */
    @ApiOperation("获取审核日志详情")
    @PreAuthorize("@ss.hasPermi('wms:approveLog:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        checkSuperAdmin();
        return success(approveLogService.getById(id));
    }

    private void checkSuperAdmin() {
        if (!SecurityUtils.isAdmin()) {
            throw new ServiceException("仅超级管理员可操作");
        }
    }
}
