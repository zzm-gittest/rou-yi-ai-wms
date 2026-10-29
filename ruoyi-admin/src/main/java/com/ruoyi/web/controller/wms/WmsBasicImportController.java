package com.ruoyi.web.controller.wms;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.service.WmsBasicDataImportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;

@Api("基础数据导入")
@RestController
@RequestMapping("/wms/basicImport")
public class WmsBasicImportController extends BaseController {

    private final WmsBasicDataImportService basicDataImportService;

    public WmsBasicImportController(WmsBasicDataImportService basicDataImportService) {
        this.basicDataImportService = basicDataImportService;
    }

    @ApiOperation("按类型导入基础数据")
    @PreAuthorize("@ss.hasPermi('wms:goods:import')")
    @PostMapping("/import/{type}")
    public AjaxResult importByType(@PathVariable String type, @RequestParam("file") MultipartFile file) throws Exception {
        ImportResult result = basicDataImportService.importByType(type, file, null);
        return success(result);
    }

    @ApiOperation("下载基础数据模板")
    @PreAuthorize("@ss.hasPermi('wms:goods:importTemplate')")
    @GetMapping("/template/{type}")
    public void downloadTemplate(@PathVariable String type,
                                 @RequestParam(value = "goodsTemplateType", required = false, defaultValue = "simple") String goodsTemplateType,
                                 HttpServletResponse response) throws Exception {
        basicDataImportService.downloadTemplate(type, goodsTemplateType, response);
    }

    @ApiOperation("下载基础数据模板包")
    @PreAuthorize("@ss.hasPermi('wms:goods:importTemplate')")
    @GetMapping("/template/package")
    public void downloadTemplatePackage(HttpServletResponse response) throws Exception {
        basicDataImportService.downloadTemplatePackage(response);
    }
}
