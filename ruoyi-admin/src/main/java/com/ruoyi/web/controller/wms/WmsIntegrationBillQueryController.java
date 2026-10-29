package com.ruoyi.web.controller.wms;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wms.domain.vo.integration.BillDetailVO;
import com.ruoyi.wms.domain.vo.integration.BillQueryRequestVO;
import com.ruoyi.wms.domain.vo.integration.BillQueryResultVO;
import com.ruoyi.wms.service.ai.query.UnifiedBillDetailService;
import com.ruoyi.wms.service.ai.query.UnifiedBillQueryService;
import com.ruoyi.wms.service.ai.query.UnifiedBillQueryTemplateProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * IM/网关统一查单（复用 WMS 分页服务，规则解析意图；不经过 PC 会话与 LLM）
 */
@Api(tags = "WMS集成-统一查单")
@RestController
@RequestMapping("/wms/integration/bill-query")
public class WmsIntegrationBillQueryController extends BaseController {

    private final UnifiedBillQueryService unifiedBillQueryService;
    private final UnifiedBillDetailService unifiedBillDetailService;

    public WmsIntegrationBillQueryController(UnifiedBillQueryService unifiedBillQueryService,
                                             UnifiedBillDetailService unifiedBillDetailService) {
        this.unifiedBillQueryService = unifiedBillQueryService;
        this.unifiedBillDetailService = unifiedBillDetailService;
    }

    @ApiOperation("统一查单（自然语言/模板句）")
    @PostMapping("/query")
    public AjaxResult query(@RequestBody BillQueryRequestVO reqVO) {
        if (reqVO == null || StringUtils.isBlank(reqVO.getMessage())) {
            return error("message 不能为空");
        }
        BillQueryResultVO result = unifiedBillQueryService.queryByMessage(
            reqVO.getMessage(), reqVO.getMaxRows(), reqVO.getPageNo(), reqVO.getPageSize());
        return success(result);
    }

    @ApiOperation("单据详情（IM 只读卡片）")
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam String domainType, @RequestParam Long id) {
        if (StringUtils.isBlank(domainType) || id == null || id <= 0) {
            return error("domainType 与 id 不能为空");
        }
        BillDetailVO detail = unifiedBillDetailService.getDetail(domainType.trim(), id);
        if (detail == null) {
            return error("未找到该单据");
        }
        return success(detail);
    }

    @ApiOperation("查询模板列表（与 PC 统一查询卡槽一致）")
    @GetMapping("/templates")
    public AjaxResult templates() {
        Map<String, Object> data = new HashMap<>(2);
        data.put("templates", UnifiedBillQueryTemplateProvider.defaultTemplates());
        return success(data);
    }
}
