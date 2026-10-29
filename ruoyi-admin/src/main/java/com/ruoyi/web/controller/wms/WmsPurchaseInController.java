package com.ruoyi.web.controller.wms;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInExportVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.service.WmsPurchaseInService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 采购入库管理Controller
 *
 * @author ruoyi
 */
@Api("采购入库管理")
@RestController
@RequestMapping("/wms/purchaseIn")
@Validated
public class WmsPurchaseInController extends BaseController {

    private final WmsPurchaseInService purchaseInService;

    public WmsPurchaseInController(WmsPurchaseInService purchaseInService) {
        this.purchaseInService = purchaseInService;
    }

    /**
     * 查询采购入库分页列表
     */
    @ApiOperation("查询采购入库分页列表")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsPurchaseInPageReqVO reqVO) {
        IPage<WmsPurchaseInRespVO> page = purchaseInService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出采购入库
     */
    @ApiOperation("导出采购入库")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsPurchaseInPageReqVO reqVO) {
        List<WmsPurchaseInExportVO> list = purchaseInService.exportList(reqVO);
        ExcelUtil<WmsPurchaseInExportVO> util = new ExcelUtil<>(WmsPurchaseInExportVO.class);
        util.exportExcel(response, list, "采购入库数据");
    }

    /**
     * 获取采购入库详情
     */
    @ApiOperation("获取采购入库详情")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:query')")
    @GetMapping("/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return success(purchaseInService.get(id));
    }

    /**
     * 获取采购入库打印数据
     */
    @ApiOperation("获取采购入库打印数据")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable Long id) {
        WmsBillPrintRespVO data = purchaseInService.getPrintData(id);
        return success(data);
    }

    /**
     * 新增采购入库
     */
    @ApiOperation("新增采购入库")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:add')")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody WmsPurchaseInSaveReqVO reqVO) {
        Long id = purchaseInService.create(reqVO);
        return success(id);
    }

    /**
     * 修改采购入库
     */
    @ApiOperation("修改采购入库")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:edit')")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody WmsPurchaseInSaveReqVO reqVO) {
        purchaseInService.update(reqVO);
        return success();
    }

    /**
     * 删除采购入库
     */
    @ApiOperation("删除采购入库")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        purchaseInService.delete(id);
        return success();
    }

    /**
     * 审核采购入库
     */
    @ApiOperation("审核采购入库")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        purchaseInService.approve(id);
        return success();
    }

    /**
     * 反审核采购入库
     */
    @ApiOperation("反审核采购入库")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:approve')")
    @PostMapping("/cancelApprove/{id}")
    public AjaxResult cancelApprove(@PathVariable Long id) {
        purchaseInService.cancelApprove(id);
        return success();
    }

    /**
     * 根据采购订单ID获取可入库商品明细
     */
    @ApiOperation("根据采购订单ID获取可入库商品明细")
    @PreAuthorize("@ss.hasPermi('wms:purchaseIn:query')")
    @GetMapping("/orderItems/{orderId}")
    public AjaxResult getOrderItems(@PathVariable Long orderId) {
        return success(purchaseInService.getOrderItems(orderId));
    }

    /**
     * 获取可退货的采购入库单列表（已审核且未全部退货）
     */
    @ApiOperation("获取可退货的采购入库单列表")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:add')")
    @GetMapping("/availableForReturn")
    public AjaxResult getAvailableForReturn(@RequestParam(required = false) String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            List<WmsPurchaseInRespVO> list = purchaseInService.listAvailableForReturn(keyword.trim());
            return success(list);
        }
        List<WmsPurchaseInRespVO> list = purchaseInService.listAvailableForReturn();
        return success(list);
    }
}
