package com.ruoyi.web.controller.wms.accountsPayable;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePayReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayableRespVO;
import com.ruoyi.wms.service.WmsAccountsPayableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 应付账款Controller
 *
 * @author ruoyi
 */
@Api("应付账款")
@RestController
@RequestMapping("/wms/accountsPayable")
@Validated
public class WmsAccountsPayableController extends BaseController {

    private final WmsAccountsPayableService apService;

    public WmsAccountsPayableController(WmsAccountsPayableService apService) {
        this.apService = apService;
    }

    /**
     * 查询应付账款分页列表
     */
    @ApiOperation("查询应付账款分页列表")
    @PreAuthorize("@ss.hasPermi('wms:accountsPayable:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsAccountsPayablePageReqVO reqVO) {
        IPage<WmsAccountsPayableRespVO> page = apService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取应付账款详情
     */
    @ApiOperation("获取应付账款详情")
    @PreAuthorize("@ss.hasPermi('wms:accountsPayable:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(apService.get(id));
    }

    /**
     * 付款
     */
    @ApiOperation("付款")
    @PreAuthorize("@ss.hasPermi('wms:accountsPayable:pay')")
    @PostMapping("/pay")
    public AjaxResult pay(@Valid @RequestBody WmsAccountsPayablePayReqVO reqVO) {
        apService.pay(reqVO);
        return success();
    }
}
