package com.ruoyi.wms.service;

import com.ruoyi.wms.domain.WmsUserOnboardingProgress;

import java.util.List;

/**
 * 新手引导任务服务
 */
public interface WmsUserOnboardingService {

    /**
     * 获取用户新手任务进度，不存在时自动初始化
     */
    List<WmsUserOnboardingProgress> getUserProgress(Long userId);

    /**
     * 标记任务完成
     */
    void completeTask(Long userId, String taskCode);

    /**
     * 初始化用户任务（幂等）
     */
    void initUserTasks(Long userId);

    /**
     * 是否需要显示新手引导
     */
    boolean shouldShowOnboarding(Long userId);

    /**
     * 校验任务编码是否有效
     */
    boolean isValidTaskCode(String taskCode);
}
