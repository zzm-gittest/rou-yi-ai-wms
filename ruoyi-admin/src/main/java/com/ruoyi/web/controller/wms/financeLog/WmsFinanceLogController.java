package com.ruoyi.web.controller.wms.financeLog;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.financeLog.WmsFinanceLogPageReqVO;
import com.ruoyi.wms.domain.vo.financeLog.WmsFinanceLogRespVO;
import com.ruoyi.wms.service.WmsFinanceLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 财务日志 Controller
 *
 * @author ruoyi
 */
@Api(tags = "财务日志管理")
@RestController
@RequestMapping("/wms/financeLog")
public class WmsFinanceLogController extends BaseController {

    @Autowired
    private WmsFinanceLogService financeLogService;

    /**
     * 查询财务日志分页列表
     */
    @ApiOperation("查询财务日志分页列表")
    @PreAuthorize("@ss.hasPermi('wms:financeLog:list')")
    @GetMapping("/page")
    public TableDataInfo page(WmsFinanceLogPageReqVO reqVO) {
        IPage<WmsFinanceLogRespVO> page = financeLogService.page(reqVO);
        return getDataTable(page);
    }

    /**
     * 获取财务日志详细信息
     */
    @ApiOperation("获取财务日志详细信息")
    @PreAuthorize("@ss.hasPermi('wms:financeLog:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        WmsFinanceLogRespVO vo = financeLogService.get(id);
        return AjaxResult.success(vo);
    }
}
