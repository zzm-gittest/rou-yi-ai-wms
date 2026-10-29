package com.ruoyi.web.controller.wms.expense;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.expense.WmsExpensePageReqVO;
import com.ruoyi.wms.domain.vo.expense.WmsExpenseRespVO;
import com.ruoyi.wms.domain.vo.expense.WmsExpenseSaveReqVO;
import com.ruoyi.wms.service.WmsExpenseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 费用 Controller
 *
 * @author ruoyi
 */
@Api(tags = "费用管理")
@RestController
@RequestMapping("/wms/expense")
public class WmsExpenseController extends BaseController {

    @Autowired
    private WmsExpenseService expenseService;

    /**
     * 查询费用分页列表
     */
    @ApiOperation("查询费用分页列表")
    @PreAuthorize("@ss.hasPermi('wms:expense:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsExpensePageReqVO reqVO) {
        IPage<WmsExpenseRespVO> page = expenseService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取费用详细信息
     */
    @ApiOperation("获取费用详细信息")
    @PreAuthorize("@ss.hasPermi('wms:expense:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsExpenseRespVO vo = expenseService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 新增费用
     */
    @ApiOperation("新增费用")
    @PreAuthorize("@ss.hasPermi('wms:expense:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsExpenseSaveReqVO reqVO) {
        Long id = expenseService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改费用
     */
    @ApiOperation("修改费用")
    @PreAuthorize("@ss.hasPermi('wms:expense:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsExpenseSaveReqVO reqVO) {
        expenseService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除费用
     */
    @ApiOperation("删除费用")
    @PreAuthorize("@ss.hasPermi('wms:expense:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        expenseService.delete(id);
        return AjaxResult.success();
    }

    /**
     * 审核费用
     */
    @ApiOperation("审核费用")
    @PreAuthorize("@ss.hasPermi('wms:expense:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        expenseService.approve(id);
        return AjaxResult.success();
    }
}
