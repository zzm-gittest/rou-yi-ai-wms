package com.ruoyi.web.controller.wms.salesOut;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutExportVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutPageReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO;
import com.ruoyi.wms.service.WmsSalesOutService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 销售出库 Controller
 *
 * @author ruoyi
 */
@Api(tags = "销售出库管理")
@RestController
@RequestMapping("/wms/salesOut")
public class WmsSalesOutController extends BaseController {

    @Autowired
    private WmsSalesOutService salesOutService;



    /**
     * 查询销售出库分页列表
     */
    @ApiOperation("查询销售出库分页列表")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsSalesOutPageReqVO reqVO) {
        IPage<WmsSalesOutRespVO> page = salesOutService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出销售出库
     */
    @ApiOperation("导出销售出库")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:list')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsSalesOutPageReqVO reqVO) {
        List<WmsSalesOutExportVO> list = salesOutService.exportList(reqVO);
        ExcelUtil<WmsSalesOutExportVO> util = new ExcelUtil<>(WmsSalesOutExportVO.class);
        util.exportExcel(response, list, "销售出库数据");
    }

    /**
     * 获取销售出库详细信息
     */
    @ApiOperation("获取销售出库详细信息")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsSalesOutRespVO vo = salesOutService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 获取销售出库打印数据
     */
    @ApiOperation("获取销售出库打印数据")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable("id") Long id) {
        WmsBillPrintRespVO vo = salesOutService.getPrintData(id);
        return AjaxResult.success(vo);
    }

    /**
     * 新增销售出库
     */
    @ApiOperation("新增销售出库")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsSalesOutSaveReqVO reqVO) {
        Long id = salesOutService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改销售出库
     */
    @ApiOperation("修改销售出库")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsSalesOutSaveReqVO reqVO) {
        salesOutService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除销售出库
     */
    @ApiOperation("删除销售出库")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        salesOutService.delete(id);
        return AjaxResult.success();
    }

    /**
     * 审核销售出库
     */
    @ApiOperation("审核销售出库")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        salesOutService.approve(id);
        return AjaxResult.success();
    }

    /**
     * 反审核销售出库
     */
    @ApiOperation("反审核销售出库")
    @PreAuthorize("@ss.hasPermi('wms:salesOut:approve')")
    @PostMapping("/cancelApprove/{id}")
    public AjaxResult cancelApprove(@PathVariable Long id) {
        salesOutService.cancelApprove(id);
        return AjaxResult.success();
    }

    /**
     * 查询已审核的销售出库列表
     */
    @ApiOperation("查询已审核的销售出库列表")
    @GetMapping("/approved")
    public AjaxResult listApproved() {
        return AjaxResult.success(salesOutService.listApproved());
    }

    /**
     * 获取销售出库明细
     */
    @ApiOperation("获取销售出库明细")
    @GetMapping("/items/{id}")
    public AjaxResult getItems(@PathVariable Long id) {
        return AjaxResult.success(salesOutService.getItems(id));
    }

    /**
     * 获取可退货的销售出库单列表（已审核且未全部退货）
     */
    @ApiOperation("获取可退货的销售出库单列表")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:add')")
    @GetMapping("/availableForReturn")
    public AjaxResult getAvailableForReturn(@RequestParam(required = false) String keyword) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(keyword)) {
            return AjaxResult.success(salesOutService.searchAvailableForReturn(keyword.trim()));
        }
        return AjaxResult.success(salesOutService.listApproved());
    }
}
