package com.ruoyi.web.controller.wms;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.integration.IntegrationAnalysisAnswerVO;
import com.ruoyi.wms.service.ai.query.IntegrationBusinessAnalysisFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * IM/网关 · 经营分析（订单查询之外的常用分析项）
 */
@Api(tags = "WMS集成-经营分析")
@RestController
@RequestMapping("/wms/integration/business-analysis")
public class WmsIntegrationBusinessAnalysisController extends BaseController {

    private final IntegrationBusinessAnalysisFacade analysisFacade;

    public WmsIntegrationBusinessAnalysisController(IntegrationBusinessAnalysisFacade analysisFacade) {
        this.analysisFacade = analysisFacade;
    }

    @ApiOperation("经营分析问答（自然语言/模版句）")
    @PostMapping("/ask")
    public AjaxResult ask(@RequestBody Map<String, String> body) {
        String message = body != null ? body.get("message") : null;
        if (StringUtils.isBlank(message)) {
            return error("message 不能为空");
        }
        IntegrationAnalysisAnswerVO result = analysisFacade.answer(message.trim());
        return success(result);
    }
}
