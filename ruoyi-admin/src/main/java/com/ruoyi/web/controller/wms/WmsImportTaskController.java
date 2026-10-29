package com.ruoyi.web.controller.wms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.wms.domain.vo.importTask.CreateImportTaskReqVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskPageReqVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskReportRespVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskRespVO;
import com.ruoyi.wms.service.WmsImportTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 导入任务 Controller
 */
@Api("导入任务")
@RestController
@RequestMapping("/wms/import/task")
@Validated
public class WmsImportTaskController extends BaseController {

    private final WmsImportTaskService importTaskService;

    public WmsImportTaskController(WmsImportTaskService importTaskService) {
        this.importTaskService = importTaskService;
    }

    @ApiOperation("创建导入任务")
    @PreAuthorize("@ss.hasPermi('wms:basicImport:query')")
    @PostMapping("/create")
    public AjaxResult create(@Valid @RequestBody CreateImportTaskReqVO reqVO) {
        ImportTaskRespVO task = importTaskService.createTask(reqVO);
        return success(task);
    }

    @ApiOperation("导入任务分页查询")
    @PreAuthorize("@ss.hasPermi('wms:basicImport:query')")
    @GetMapping("/page")
    public TableDataInfo page(@Valid ImportTaskPageReqVO reqVO) {
        IPage<ImportTaskRespVO> page = importTaskService.page(reqVO);
        return getDataTable(page);
    }

    @ApiOperation("文件预览")
    @PreAuthorize("@ss.hasPermi('wms:goods:import')")
    @PostMapping("/{id}/preview")
    public AjaxResult preview(@PathVariable("id") Long taskId,
                              @RequestParam("file") MultipartFile file,
                              @RequestParam(value = "goodsTemplateType", required = false, defaultValue = "simple") String goodsTemplateType,
                              @RequestParam(value = "mappingJson", required = false) String mappingJson) throws Exception {
        ImportTaskReportRespVO report = importTaskService.preview(taskId, file, goodsTemplateType, mappingJson);
        return success(report);
    }

    @ApiOperation("执行导入")
    @PreAuthorize("@ss.hasPermi('wms:goods:import')")
    @PostMapping("/{id}/execute")
    public AjaxResult execute(@PathVariable("id") Long taskId) throws Exception {
        ImportTaskReportRespVO report = importTaskService.execute(taskId);
        return success(report);
    }

    @ApiOperation("失败重跑")
    @PreAuthorize("@ss.hasPermi('wms:goods:import')")
    @PostMapping("/{id}/retry")
    public AjaxResult retry(@PathVariable("id") Long taskId,
                            @RequestParam("file") MultipartFile file,
                            @RequestParam(value = "goodsTemplateType", required = false, defaultValue = "simple") String goodsTemplateType,
                            @RequestParam(value = "mappingJson", required = false) String mappingJson) throws Exception {
        ImportTaskReportRespVO report = importTaskService.retry(taskId, file, goodsTemplateType, mappingJson);
        return success(report);
    }

    @ApiOperation("解析文件表头")
    @PreAuthorize("@ss.hasPermi('wms:goods:import')")
    @PostMapping("/headers")
    public AjaxResult parseHeaders(@RequestParam("file") MultipartFile file) throws Exception {
        return success(importTaskService.parseHeaders(file));
    }

    @ApiOperation("导入报告")
    @PreAuthorize("@ss.hasPermi('wms:basicImport:query')")
    @GetMapping("/{id}/report")
    public AjaxResult report(@PathVariable("id") Long taskId) {
        return success(importTaskService.report(taskId));
    }
}
