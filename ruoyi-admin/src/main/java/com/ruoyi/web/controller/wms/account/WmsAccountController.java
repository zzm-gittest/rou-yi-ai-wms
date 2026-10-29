package com.ruoyi.web.controller.wms.account;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.account.WmsAccountPageReqVO;
import com.ruoyi.wms.domain.vo.account.WmsAccountRespVO;
import com.ruoyi.wms.domain.vo.account.WmsAccountSaveReqVO;
import com.ruoyi.wms.service.WmsAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 结算账户 Controller
 *
 * @author ruoyi
 */
@Api(tags = "结算账户管理")
@RestController
@RequestMapping("/wms/account")
public class WmsAccountController extends BaseController {

    @Autowired
    private WmsAccountService accountService;

    /**
     * 查询结算账户分页列表
     */
    @ApiOperation("查询结算账户分页列表")
    @PreAuthorize("@ss.hasPermi('wms:account:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsAccountPageReqVO reqVO) {
        IPage<WmsAccountRespVO> page = accountService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询结算账户列表
     */
    @ApiOperation("查询结算账户列表")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsAccountRespVO> list = accountService.listEnabled();
        return AjaxResult.success(list);
    }

    /**
     * 获取结算账户详细信息
     */
    @ApiOperation("获取结算账户详细信息")
    @PreAuthorize("@ss.hasPermi('wms:account:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsAccountRespVO vo = accountService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 新增结算账户
     */
    @ApiOperation("新增结算账户")
    @PreAuthorize("@ss.hasPermi('wms:account:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsAccountSaveReqVO reqVO) {
        Long id = accountService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改结算账户
     */
    @ApiOperation("修改结算账户")
    @PreAuthorize("@ss.hasPermi('wms:account:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsAccountSaveReqVO reqVO) {
        accountService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除结算账户
     */
    @ApiOperation("删除结算账户")
    @PreAuthorize("@ss.hasPermi('wms:account:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        accountService.delete(id);
        return AjaxResult.success();
    }
}
