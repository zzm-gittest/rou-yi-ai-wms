package com.ruoyi.web.controller.wms.salesReturn;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import java.util.List;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnExportVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnPageReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnSaveReqVO;
import com.ruoyi.wms.service.WmsSalesReturnService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.wms.service.print.WmsBillPdfService;

/**
 * 销售退货 Controller
 *
 * @author ruoyi
 */
@Api(tags = "销售退货管理")
@RestController
@RequestMapping("/wms/salesReturn")
public class WmsSalesReturnController extends BaseController {

    @Autowired
    private WmsSalesReturnService salesReturnService;

    @Autowired
    private WmsBillPdfService billPdfService;


    /**
     * 查询销售退货分页列表
     */
    @ApiOperation("查询销售退货分页列表")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsSalesReturnPageReqVO reqVO) {
        IPage<WmsSalesReturnRespVO> page = salesReturnService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出销售退货
     */
    @ApiOperation("导出销售退货")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:list')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsSalesReturnPageReqVO reqVO) {
        List<WmsSalesReturnExportVO> list = salesReturnService.exportList(reqVO);
        ExcelUtil<WmsSalesReturnExportVO> util = new ExcelUtil<>(WmsSalesReturnExportVO.class);
        util.exportExcel(response, list, "销售退货数据");
    }

    /**
     * 获取销售退货详细信息
     */
    @ApiOperation("获取销售退货详细信息")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsSalesReturnRespVO vo = salesReturnService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 获取销售退货打印数据
     */
    @ApiOperation("获取销售退货打印数据")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable("id") Long id) {
        WmsSalesReturnRespVO vo = salesReturnService.get(id);
        WmsBillPrintRespVO printData = billPdfService.buildPrintData("销售退货", vo);
        return AjaxResult.success(printData);
    }

    /**
     * 新增销售退货
     */
    @ApiOperation("新增销售退货")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsSalesReturnSaveReqVO reqVO) {
        Long id = salesReturnService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改销售退货
     */
    @ApiOperation("修改销售退货")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsSalesReturnSaveReqVO reqVO) {
        salesReturnService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除销售退货
     */
    @ApiOperation("删除销售退货")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        salesReturnService.delete(id);
        return AjaxResult.success();
    }

    /**
     * 审核销售退货
     */
    @ApiOperation("审核销售退货")
    @PreAuthorize("@ss.hasPermi('wms:salesReturn:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        salesReturnService.approve(id);
        return AjaxResult.success();
    }
}
