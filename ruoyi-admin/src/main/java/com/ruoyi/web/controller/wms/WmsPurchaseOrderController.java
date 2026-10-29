package com.ruoyi.web.controller.wms;

import java.util.List;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderExportVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemSaveReqVO;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.wms.service.print.WmsBillPdfService;

/**
 * 采购订单管理Controller
 *
 * @author ruoyi
 */
@Api("采购订单管理")
@RestController
@RequestMapping("/wms/purchaseOrder")
@Validated
public class WmsPurchaseOrderController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(WmsPurchaseOrderController.class);

    private final WmsPurchaseOrderService purchaseOrderService;

    private final WmsBillPdfService billPdfService;

    public WmsPurchaseOrderController(WmsPurchaseOrderService purchaseOrderService, WmsBillPdfService billPdfService) {
        this.purchaseOrderService = purchaseOrderService;
        this.billPdfService = billPdfService;
    }

    /**
     * 查询采购订单分页列表
     */
    @ApiOperation("查询采购订单分页列表")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsPurchaseOrderPageReqVO reqVO) {
        IPage<WmsPurchaseOrderRespVO> page = purchaseOrderService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出采购订单
     */
    @ApiOperation("导出采购订单")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsPurchaseOrderPageReqVO reqVO) {
        List<WmsPurchaseOrderExportVO> list = purchaseOrderService.exportList(reqVO);
        ExcelUtil<WmsPurchaseOrderExportVO> util = new ExcelUtil<>(WmsPurchaseOrderExportVO.class);
        util.exportExcel(response, list, "采购订单数据");
    }

    /**
     * 获取采购订单详情
     */
    @ApiOperation("获取采购订单详情")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(purchaseOrderService.get(id));
    }

    /**
     * 获取采购订单打印数据
     */
    @ApiOperation("获取采购订单打印数据")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable Long id) {
        WmsPurchaseOrderRespVO data = purchaseOrderService.get(id);
        WmsBillPrintRespVO printData = billPdfService.buildPrintData("采购订单", data);
        return success(printData);
    }

    /**
     * 新增采购订单
     */
    @ApiOperation("新增采购订单")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsPurchaseOrderSaveReqVO reqVO) {
        log.info("新增采购订单请求: supplierId={}, itemsCount={}", reqVO.getSupplierId(),
                reqVO.getItems() != null ? reqVO.getItems().size() : 0);
        if (reqVO.getItems() != null) {
            for (int i = 0; i < reqVO.getItems().size(); i++) {
                WmsPurchaseOrderItemSaveReqVO item = reqVO.getItems().get(i);
                log.info("  明细[{}]: skuId={}, goodsName='{}', quantity={}, price={}",
                        i, item.getSkuId(), item.getGoodsName(), item.getQuantity(), item.getPrice());
            }
        }
        Long id = purchaseOrderService.create(reqVO);
        return success(id);
    }

    /**
     * 修改采购订单
     */
    @ApiOperation("修改采购订单")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsPurchaseOrderSaveReqVO reqVO) {
        purchaseOrderService.update(reqVO);
        return success();
    }

    /**
     * 删除采购订单
     */
    @ApiOperation("删除采购订单")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return success();
    }

    /**
     * 审核采购订单
     */
    @ApiOperation("审核采购订单")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        purchaseOrderService.approve(id);
        return success();
    }

    /**
     * 关闭采购订单
     */
    @ApiOperation("关闭采购订单")
    @PreAuthorize("@ss.hasPermi('wms:purchaseOrder:close')")
    @PostMapping("/close/{id}")
    public AjaxResult close(@PathVariable Long id) {
        purchaseOrderService.close(id);
        return success();
    }

    /**
     * 获取可入库的采购订单列表（已审核且未全部入库）
     */
    @ApiOperation("获取可入库的采购订单列表")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:add')")
    @GetMapping("/availableForIn")
    public AjaxResult getAvailableForIn() {
        List<WmsPurchaseOrderRespVO> list = purchaseOrderService.listAvailableForIn();
        return success(list);
    }
}
