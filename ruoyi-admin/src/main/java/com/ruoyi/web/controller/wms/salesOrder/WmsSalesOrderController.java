package com.ruoyi.web.controller.wms.salesOrder;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import java.util.List;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderExportVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderPageReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderSaveReqVO;
import com.ruoyi.wms.service.WmsSalesOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.wms.service.print.WmsBillPdfService;

/**
 * 销售订单 Controller
 *
 * @author ruoyi
 */
@Api(tags = "销售订单管理")
@RestController
@RequestMapping("/wms/salesOrder")
public class WmsSalesOrderController extends BaseController {

    @Autowired
    private WmsSalesOrderService salesOrderService;

    @Autowired
    private WmsBillPdfService billPdfService;


    /**
     * 查询销售订单分页列表
     */
    @ApiOperation("查询销售订单分页列表")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsSalesOrderPageReqVO reqVO) {
        IPage<WmsSalesOrderRespVO> page = salesOrderService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出销售订单
     */
    @ApiOperation("导出销售订单")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:list')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsSalesOrderPageReqVO reqVO) {
        List<WmsSalesOrderExportVO> list = salesOrderService.exportList(reqVO);
        ExcelUtil<WmsSalesOrderExportVO> util = new ExcelUtil<>(WmsSalesOrderExportVO.class);
        util.exportExcel(response, list, "销售订单数据");
    }

    /**
     * 获取销售订单详细信息
     */
    @ApiOperation("获取销售订单详细信息")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsSalesOrderRespVO vo = salesOrderService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 获取销售订单打印数据
     */
    @ApiOperation("获取销售订单打印数据")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable("id") Long id) {
        WmsSalesOrderRespVO vo = salesOrderService.get(id);
        WmsBillPrintRespVO printData = billPdfService.buildPrintData("销售订单", vo);
        return AjaxResult.success(printData);
    }

    /**
     * 新增销售订单
     */
    @ApiOperation("新增销售订单")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsSalesOrderSaveReqVO reqVO) {
        Long id = salesOrderService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改销售订单
     */
    @ApiOperation("修改销售订单")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsSalesOrderSaveReqVO reqVO) {
        salesOrderService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除销售订单
     */
    @ApiOperation("删除销售订单")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        salesOrderService.delete(id);
        return AjaxResult.success();
    }

    /**
     * 审核销售订单
     */
    @ApiOperation("审核销售订单")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        salesOrderService.approve(id);
        return AjaxResult.success();
    }

    /**
     * 关闭销售订单
     */
    @ApiOperation("关闭销售订单")
    @PreAuthorize("@ss.hasPermi('wms:salesOrder:close')")
    @PostMapping("/close/{id}")
    public AjaxResult close(@PathVariable Long id) {
        salesOrderService.close(id);
        return AjaxResult.success();
    }

    /**
     * 查询已审核的销售订单列表
     */
    @ApiOperation("查询已审核的销售订单列表")
    @GetMapping("/approved")
    public AjaxResult listApproved() {
        return AjaxResult.success(salesOrderService.listApproved());
    }

    /**
     * 按关键字搜索可出库的已审核销售订单（订单号/客户名称模糊匹配）
     */
    @ApiOperation("搜索可出库的已审核销售订单")
    @GetMapping("/availableForOut")
    public AjaxResult availableForOut(@RequestParam(required = false) String keyword) {
        return AjaxResult.success(salesOrderService.searchAvailableForOut(keyword));
    }

    /**
     * 获取销售订单明细
     */
    @ApiOperation("获取销售订单明细")
    @GetMapping("/items/{id}")
    public AjaxResult getItems(@PathVariable Long id) {
        return AjaxResult.success(salesOrderService.getItems(id));
    }
}
