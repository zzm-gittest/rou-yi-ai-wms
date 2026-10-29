package com.ruoyi.web.controller.wms;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.domain.WmsUserOnboardingProgress;
import com.ruoyi.wms.service.WmsUserOnboardingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 新手引导 Controller
 */
@Api("新手引导")
@RestController
@RequestMapping("/wms/onboarding")
@Validated
public class WmsUserOnboardingController extends BaseController {

    private final WmsUserOnboardingService onboardingService;

    public WmsUserOnboardingController(WmsUserOnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @ApiOperation("获取当前用户新手任务进度")
    @GetMapping("/progress")
    public AjaxResult getProgress() {
        Long userId = SecurityUtils.getUserId();
        List<WmsUserOnboardingProgress> progressList = onboardingService.getUserProgress(userId);
        return success(progressList);
    }

    @ApiOperation("标记任务完成")
    @PostMapping("/complete/{taskCode}")
    public AjaxResult completeTask(@PathVariable String taskCode) {
        if (!onboardingService.isValidTaskCode(taskCode)) {
            return AjaxResult.error("无效的任务编码");
        }
        Long userId = SecurityUtils.getUserId();
        onboardingService.completeTask(userId, taskCode);
        return success();
    }

    @ApiOperation("检查是否显示新手引导")
    @GetMapping("/should-show")
    public AjaxResult shouldShow() {
        Long userId = SecurityUtils.getUserId();
        boolean shouldShow = onboardingService.shouldShowOnboarding(userId);
        return success(shouldShow);
    }
}
