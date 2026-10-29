package com.ruoyi.web.controller.wms;

import java.util.List;
import jakarta.validation.Valid;
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
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierPageReqVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;
import com.ruoyi.wms.service.WmsSupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 供应商管理Controller
 *
 * @author ruoyi
 */
@Api("供应商管理")
@RestController
@RequestMapping("/wms/supplier")
@Validated
public class WmsSupplierController extends BaseController {

    private final WmsSupplierService supplierService;

    public WmsSupplierController(WmsSupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * 查询供应商分页列表
     */
    @ApiOperation("查询供应商分页列表")
    @PreAuthorize("@ss.hasPermi('wms:supplier:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsSupplierPageReqVO reqVO) {
        IPage<WmsSupplierRespVO> page = supplierService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询供应商列表（不分页）
     */
    @ApiOperation("查询供应商列表")
    @PreAuthorize("@ss.hasPermi('wms:supplier:query')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsSupplierRespVO> list = supplierService.selectList();
        return success(list);
    }

    /**
     * 获取供应商详情
     */
    @ApiOperation("获取供应商详情")
    @PreAuthorize("@ss.hasPermi('wms:supplier:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(supplierService.get(id));
    }

    /**
     * 新增供应商
     */
    @ApiOperation("新增供应商")
    @PreAuthorize("@ss.hasPermi('wms:supplier:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsSupplierSaveReqVO reqVO) {
        Long id = supplierService.create(reqVO);
        return success(id);
    }

    /**
     * 修改供应商
     */
    @ApiOperation("修改供应商")
    @PreAuthorize("@ss.hasPermi('wms:supplier:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsSupplierSaveReqVO reqVO) {
        supplierService.update(reqVO);
        return success();
    }

    /**
     * 删除供应商
     */
    @ApiOperation("删除供应商")
    @PreAuthorize("@ss.hasPermi('wms:supplier:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        supplierService.delete(id);
        return success();
    }
}
