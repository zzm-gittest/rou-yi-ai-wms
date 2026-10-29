package com.ruoyi.web.controller.wms;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.wms.domain.WmsBizOperLog;
import com.ruoyi.wms.service.IWmsBizOperLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS业务操作日志Controller
 */
@Api(tags = "WMS业务操作日志")
@RestController
@RequestMapping("/wms/bizLog")
public class WmsBizOperLogController extends BaseController {

    @Autowired
    private IWmsBizOperLogService wmsBizOperLogService;

    @ApiOperation("分页查询业务操作日志")
    @PreAuthorize("@ss.hasPermi('wms:bizLog:list')")
    @GetMapping("/page")
    public TableDataInfo page(
            @RequestParam(value = "bizModule", required = false) String bizModule,
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "operUserName", required = false) String operUserName,
            @RequestParam(value = "bizNo", required = false) String bizNo,
            @RequestParam(value = "beginTime", required = false) String beginTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        Long tenantId = TenantHolder.getTenantId();
        IPage<WmsBizOperLog> page = wmsBizOperLogService.page(
                tenantId, bizModule, bizType, operUserName, bizNo,
                beginTime, endTime, pageNo, pageSize);
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询成功");
        dataInfo.setRows(page.getRecords());
        dataInfo.setTotal(page.getTotal());
        return dataInfo;
    }
}
