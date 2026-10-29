package com.ruoyi.web.controller.wms.purchaseReturn;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnExportVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.service.WmsPurchaseReturnService;
import com.ruoyi.wms.service.WmsPurchaseInService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import com.ruoyi.wms.service.print.WmsBillPdfService;

/**
 * 采购退货 Controller
 *
 * @author ruoyi
 */
@Api(tags = "采购退货管理")
@RestController
@RequestMapping("/wms/purchaseReturn")
public class WmsPurchaseReturnController extends BaseController {

    @Autowired
    private WmsPurchaseReturnService purchaseReturnService;

    @Autowired
    private WmsPurchaseInService purchaseInService;

    @Autowired
    private WmsBillPdfService billPdfService;


    /**
     * 查询采购退货分页列表
     */
    @ApiOperation("查询采购退货分页列表")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsPurchaseReturnPageReqVO reqVO) {
        IPage<WmsPurchaseReturnRespVO> page = purchaseReturnService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 导出采购退货
     */
    @ApiOperation("导出采购退货")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:list')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsPurchaseReturnPageReqVO reqVO) {
        List<WmsPurchaseReturnExportVO> list = purchaseReturnService.exportList(reqVO);
        ExcelUtil<WmsPurchaseReturnExportVO> util = new ExcelUtil<>(WmsPurchaseReturnExportVO.class);
        util.exportExcel(response, list, "采购退货数据");
    }

    /**
     * 获取采购退货详细信息
     */
    @ApiOperation("获取采购退货详细信息")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsPurchaseReturnRespVO vo = purchaseReturnService.get(id);
        return AjaxResult.success(vo);
    }

    /**
     * 获取采购退货打印数据
     */
    @ApiOperation("获取采购退货打印数据")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:query')")
    @GetMapping("/print/{id}")
    public AjaxResult print(@PathVariable("id") Long id) {
        WmsPurchaseReturnRespVO vo = purchaseReturnService.get(id);
        WmsBillPrintRespVO printData = billPdfService.buildPrintData("采购退货", vo);
        return AjaxResult.success(printData);
    }

    /**
     * 新增采购退货
     */
    @ApiOperation("新增采购退货")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsPurchaseReturnSaveReqVO reqVO) {
        Long id = purchaseReturnService.create(reqVO);
        return AjaxResult.success(id);
    }

    /**
     * 修改采购退货
     */
    @ApiOperation("修改采购退货")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsPurchaseReturnSaveReqVO reqVO) {
        purchaseReturnService.update(reqVO);
        return AjaxResult.success();
    }

    /**
     * 删除采购退货
     */
    @ApiOperation("删除采购退货")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:remove')")
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        purchaseReturnService.delete(id);
        return AjaxResult.success();
    }

    /**
     * 审核采购退货
     */
    @ApiOperation("审核采购退货")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:approve')")
    @PostMapping("/approve/{id}")
    public AjaxResult approve(@PathVariable Long id) {
        purchaseReturnService.approve(id);
        return AjaxResult.success();
    }

    /**
     * 获取已审核的采购入库单列表（用于退货）
     */
    @ApiOperation("获取已审核的采购入库单列表")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:list')")
    @GetMapping("/approvedIns")
    public AjaxResult getApprovedPurchaseInList() {
        List<WmsPurchaseInRespVO> list = purchaseInService.listApproved();
        return AjaxResult.success(list);
    }

    /**
     * 获取采购入库单的商品明细（用于退货）
     */
    @ApiOperation("获取采购入库单的商品明细")
    @PreAuthorize("@ss.hasPermi('wms:purchaseReturn:list')")
    @GetMapping("/inItems/{inId}")
    public AjaxResult getInItems(@PathVariable Long inId) {
        List<WmsPurchaseInItemRespVO> items = purchaseReturnService.getInItems(inId);
        return AjaxResult.success(items);
    }
}
