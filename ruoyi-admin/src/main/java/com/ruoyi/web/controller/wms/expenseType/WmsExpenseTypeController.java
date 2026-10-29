package com.ruoyi.web.controller.wms.expenseType;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypePageReqVO;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypeRespVO;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypeSaveReqVO;
import com.ruoyi.wms.service.WmsExpenseTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 费用类型 Controller
 *
 * @author ruoyi
 */
@Api(tags = "费用类型管理")
@RestController
@RequestMapping("/wms/expenseType")
public class WmsExpenseTypeController extends BaseController {

    @Autowired
    private WmsExpenseTypeService expenseTypeService;

    /**
     * 查询费用类型分页列表
     */
    @ApiOperation("查询费用类型分页列表")
    @PreAuthorize("@ss.hasPermi('wms:expenseType:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsExpenseTypePageReqVO reqVO) {
        IPage<WmsExpenseTypeRespVO> page = expenseTypeService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询费用类型列表
     */
    @ApiOperation("查询费用类型列表")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsExpenseTypeRespVO> list = expenseTypeService.listEnabled();
        return AjaxResult.success(list);
    }

    /**
     * 获取费用类型详细信息
     */
    @ApiOperation("获取费用类型详细信息")
    @PreAuthorize("@ss.hasPermi('wms:expenseType:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsExpenseTypeRespVO vo = expenseTypeService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 新增费用类型
     */
    @ApiOperation("新增费用类型")
    @PreAuthorize("@ss.hasPermi('wms:expenseType:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsExpenseTypeSaveReqVO reqVO) {
        Long id = expenseTypeService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改费用类型
     */
    @ApiOperation("修改费用类型")
    @PreAuthorize("@ss.hasPermi('wms:expenseType:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsExpenseTypeSaveReqVO reqVO) {
        expenseTypeService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除费用类型
     */
    @ApiOperation("删除费用类型")
    @PreAuthorize("@ss.hasPermi('wms:expenseType:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        expenseTypeService.delete(id);
        return AjaxResult.success();
    }
}
