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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckExportVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckPageReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckSaveReqVO;
import com.ruoyi.wms.service.WmsStockCheckService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.wms.service.print.WmsBillPdfService;

/**
 * 库存盘点单管理Controller
 *
 * @author ruoyi
 */
@Api("库存盘点单管理")
@RestController
@RequestMapping("/wms/stockCheck")
@Validated
public class WmsStockCheckController extends BaseController {

    private final WmsStockCheckService stockCheckService;

    private final WmsBillPdfService billPdfService;

    public WmsStockCheckController(WmsStockCheckService stockCheckService, WmsBillPdfService billPdfService) {
        this.stockCheckService = stockCheckService;
        this.billPdfService = billPdfService;
    }

    /**
     * 查询库存盘点单分页列表
     */
    @ApiOperation("查询库存盘点单分页列表")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsStockCheckPageReqVO reqVO) {
        IPage<WmsStockCheckRespVO> page = stockCheckService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出库存盘点
     */
    @ApiOperation("导出库存盘点")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsStockCheckPageReqVO reqVO) {
        List<WmsStockCheckExportVO> list = stockCheckService.exportList(reqVO);
        ExcelUtil<WmsStockCheckExportVO> util = new ExcelUtil<>(WmsStockCheckExportVO.class);
        util.exportExcel(response, list, "库存盘点数据");
    }

    /**
     * 获取库存盘点单详情
     */
    @ApiOperation("获取库存盘点单详情")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(stockCheckService.get(id));
    }

    /**
     * 获取库存盘点打印数据
     */
    @ApiOperation("获取库存盘点打印数据")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable Long id) {
        WmsStockCheckRespVO data = stockCheckService.get(id);
        WmsBillPrintRespVO printData = billPdfService.buildPrintData("库存盘点", data);
        return success(printData);
    }

    /**
     * 新增库存盘点单
     */
    @ApiOperation("新增库存盘点单")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsStockCheckSaveReqVO reqVO) {
        Long id = stockCheckService.create(reqVO);
        return success(id);
    }

    /**
     * 修改库存盘点单
     */
    @ApiOperation("修改库存盘点单")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsStockCheckSaveReqVO reqVO) {
        stockCheckService.update(reqVO);
        return success();
    }

    /**
     * 删除库存盘点单
     */
    @ApiOperation("删除库存盘点单")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        stockCheckService.delete(id);
        return success();
    }

    /**
     * 审核库存盘点单
     */
    @ApiOperation("审核库存盘点单")
    @PreAuthorize("@ss.hasPermi('wms:stockCheck:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        stockCheckService.approve(id);
        return success();
    }
}
