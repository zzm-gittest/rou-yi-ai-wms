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
import com.ruoyi.wms.domain.vo.unit.WmsUnitPageReqVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitRespVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;
import com.ruoyi.wms.service.WmsUnitService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 计量单位管理Controller
 *
 * @author ruoyi
 */
@Api("计量单位管理")
@RestController
@RequestMapping("/wms/unit")
@Validated
public class WmsUnitController extends BaseController {

    private final WmsUnitService unitService;

    public WmsUnitController(WmsUnitService unitService) {
        this.unitService = unitService;
    }

    /**
     * 查询单位分页列表
     */
    @ApiOperation("查询单位分页列表")
    @PreAuthorize("@ss.hasPermi('wms:unit:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsUnitPageReqVO reqVO) {
        IPage<WmsUnitRespVO> page = unitService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 查询单位列表（不分页）
     */
    @ApiOperation("查询单位列表")
    @PreAuthorize("@ss.hasPermi('wms:unit:query')")
    @GetMapping("/list")
    public AjaxResult list() {
        List<WmsUnitRespVO> list = unitService.selectList();
        return success(list);
    }

    /**
     * 获取单位详情
     */
    @ApiOperation("获取单位详情")
    @PreAuthorize("@ss.hasPermi('wms:unit:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(unitService.get(id));
    }

    /**
     * 新增单位
     */
    @ApiOperation("新增单位")
    @PreAuthorize("@ss.hasPermi('wms:unit:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsUnitSaveReqVO reqVO) {
        Long id = unitService.create(reqVO);
        return success(id);
    }

    /**
     * 修改单位
     */
    @ApiOperation("修改单位")
    @PreAuthorize("@ss.hasPermi('wms:unit:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsUnitSaveReqVO reqVO) {
        unitService.update(reqVO);
        return success();
    }

    /**
     * 删除单位
     */
    @ApiOperation("删除单位")
    @PreAuthorize("@ss.hasPermi('wms:unit:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        unitService.delete(id);
        return success();
    }
}
