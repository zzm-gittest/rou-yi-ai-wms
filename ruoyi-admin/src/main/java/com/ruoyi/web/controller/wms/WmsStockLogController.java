package com.ruoyi.web.controller.wms;

import java.util.List;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogExportVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogPageReqVO;
import com.ruoyi.wms.domain.vo.stockLog.WmsStockLogRespVO;
import com.ruoyi.wms.service.WmsStockLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 库存变动日志Controller
 *
 * @author ruoyi
 */
@Api("库存变动日志")
@RestController
@RequestMapping("/wms/stockLog")
@Validated
public class WmsStockLogController extends BaseController {

    private final WmsStockLogService stockLogService;

    public WmsStockLogController(WmsStockLogService stockLogService) {
        this.stockLogService = stockLogService;
    }

    /**
     * 查询库存变动日志分页列表
     */
    @ApiOperation("查询库存变动日志分页列表")
    @PreAuthorize("@ss.hasPermi('wms:stockLog:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid WmsStockLogPageReqVO reqVO) {
        if (reqVO.getPageNum() != null) {
            reqVO.setPageNo(reqVO.getPageNum());
        }
        IPage<WmsStockLogRespVO> page = stockLogService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取库存变动日志详情
     */
    @ApiOperation("获取库存变动日志详情")
    @PreAuthorize("@ss.hasPermi('wms:stockLog:query')")
    @GetMapping("/{id}")
    public AjaxResult get(Long id) {
        return success(stockLogService.getById(id));
    }

    /**
     * 导出库存变动日志
     */
    @ApiOperation("导出库存变动日志")
    @PreAuthorize("@ss.hasPermi('wms:stockLog:query')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, @Valid WmsStockLogPageReqVO reqVO) {
        List<WmsStockLogExportVO> list = stockLogService.exportList(reqVO);
        ExcelUtil<WmsStockLogExportVO> util = new ExcelUtil<>(WmsStockLogExportVO.class);
        util.exportExcel(response, list, "库存变动日志数据");
    }
}
