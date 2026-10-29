package com.ruoyi.web.controller.wms;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.vo.stock.WmsStockExportVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockPageReqVO;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;
import com.ruoyi.wms.service.WmsStockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 库存管理Controller
 *
 * @author ruoyi
 */
@Api("库存管理")
@RestController
@RequestMapping("/wms/stock")
@Validated
public class WmsStockController extends BaseController {

    private final WmsStockService stockService;

    public WmsStockController(WmsStockService stockService) {
        this.stockService = stockService;
    }

    /**
     * 查询库存分页列表
     */
    @ApiOperation("查询库存分页列表")
    @PreAuthorize("@ss.hasPermi('wms:stock:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsStockPageReqVO reqVO) {
        IPage<WmsStockRespVO> page = stockService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取库存详情
     */
    @ApiOperation("获取库存详情")
    @PreAuthorize("@ss.hasPermi('wms:stock:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(stockService.get(id));
    }

    /**
     * 根据仓库和SKU查询库存
     */
    @ApiOperation("根据仓库和SKU查询库存")
    @GetMapping("/byWarehouseAndSku")
    public AjaxResult getByWarehouseAndSku(@RequestParam Long warehouseId, @RequestParam Long skuId) {
        WmsStockRespVO stock = stockService.getByWarehouseAndSku(warehouseId, skuId);
        return success(stock);
    }

    /**
     * 导出库存
     */
    @ApiOperation("导出库存")
    @PreAuthorize("@ss.hasPermi('wms:stock:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsStockPageReqVO reqVO) {
        List<WmsStockExportVO> list = stockService.exportList(reqVO);
        ExcelUtil<WmsStockExportVO> util = new ExcelUtil<>(WmsStockExportVO.class);
        util.exportExcel(response, list, "库存数据");
    }
}
