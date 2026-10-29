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
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferExportVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferPageReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferSaveReqVO;
import com.ruoyi.wms.service.WmsStockTransferService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.wms.service.print.WmsBillPdfService;

/**
 * 库存调拨单管理Controller
 *
 * @author ruoyi
 */
@Api("库存调拨单管理")
@RestController
@RequestMapping("/wms/stockTransfer")
@Validated
public class WmsStockTransferController extends BaseController {

    private final WmsStockTransferService stockTransferService;

    private final WmsBillPdfService billPdfService;

    public WmsStockTransferController(WmsStockTransferService stockTransferService, WmsBillPdfService billPdfService) {
        this.stockTransferService = stockTransferService;
        this.billPdfService = billPdfService;
    }

    /**
     * 查询库存调拨单分页列表
     */
    @ApiOperation("查询库存调拨单分页列表")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsStockTransferPageReqVO reqVO) {
        IPage<WmsStockTransferRespVO> page = stockTransferService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出库存调拨
     */
    @ApiOperation("导出库存调拨")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsStockTransferPageReqVO reqVO) {
        List<WmsStockTransferExportVO> list = stockTransferService.exportList(reqVO);
        ExcelUtil<WmsStockTransferExportVO> util = new ExcelUtil<>(WmsStockTransferExportVO.class);
        util.exportExcel(response, list, "库存调拨数据");
    }

    /**
     * 获取库存调拨单详情
     */
    @ApiOperation("获取库存调拨单详情")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(stockTransferService.get(id));
    }

    /**
     * 获取库存调拨打印数据
     */
    @ApiOperation("获取库存调拨打印数据")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable Long id) {
        WmsStockTransferRespVO data = stockTransferService.get(id);
        WmsBillPrintRespVO printData = billPdfService.buildPrintData("库存调拨", data);
        return success(printData);
    }

    /**
     * 新增库存调拨单
     */
    @ApiOperation("新增库存调拨单")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsStockTransferSaveReqVO reqVO) {
        Long id = stockTransferService.create(reqVO);
        return success(id);
    }

    /**
     * 修改库存调拨单
     */
    @ApiOperation("修改库存调拨单")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsStockTransferSaveReqVO reqVO) {
        stockTransferService.update(reqVO);
        return success();
    }

    /**
     * 删除库存调拨单
     */
    @ApiOperation("删除库存调拨单")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        stockTransferService.delete(id);
        return success();
    }

    /**
     * 审核库存调拨单
     */
    @ApiOperation("审核库存调拨单")
    @PreAuthorize("@ss.hasPermi('wms:stockTransfer:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        stockTransferService.approve(id);
        return success();
    }
}
