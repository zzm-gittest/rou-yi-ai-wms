package com.ruoyi.web.controller.wms.accountsReceivable;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableCollectReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableRespVO;
import com.ruoyi.wms.service.WmsAccountsReceivableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 应收账款Controller
 *
 * @author ruoyi
 */
@Api("应收账款")
@RestController
@RequestMapping("/wms/accountsReceivable")
@Validated
public class WmsAccountsReceivableController extends BaseController {

    private final WmsAccountsReceivableService arService;

    public WmsAccountsReceivableController(WmsAccountsReceivableService arService) {
        this.arService = arService;
    }

    /**
     * 查询应收账款分页列表
     */
    @ApiOperation("查询应收账款分页列表")
    @PreAuthorize("@ss.hasPermi('wms:accountsReceivable:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsAccountsReceivablePageReqVO reqVO) {
        IPage<WmsAccountsReceivableRespVO> page = arService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取应收账款详情
     */
    @ApiOperation("获取应收账款详情")
    @PreAuthorize("@ss.hasPermi('wms:accountsReceivable:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(arService.get(id));
    }

    /**
     * 收款
     */
    @ApiOperation("收款")
    @PreAuthorize("@ss.hasPermi('wms:accountsReceivable:collect')")
    @PostMapping("/collect")
    public AjaxResult collect(@Valid @RequestBody WmsAccountsReceivableCollectReqVO reqVO) {
        arService.collect(reqVO);
        return success();
    }
}
