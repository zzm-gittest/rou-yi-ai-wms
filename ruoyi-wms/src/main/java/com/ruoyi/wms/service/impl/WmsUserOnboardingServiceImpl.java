package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.WmsUserOnboardingProgress;
import com.ruoyi.wms.mapper.WmsUserOnboardingProgressMapper;
import com.ruoyi.wms.service.WmsUserOnboardingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 新手引导任务服务实现
 */
@Service
public class WmsUserOnboardingServiceImpl implements WmsUserOnboardingService {

    private static final List<String> TASK_CODES = Arrays.asList(
            "add_goods",
            "purchase_in",
            "sales_out",
            "view_report"
    );

    private static final Set<String> TASK_CODE_SET = new HashSet<>(TASK_CODES);

    private final WmsUserOnboardingProgressMapper progressMapper;

    public WmsUserOnboardingServiceImpl(WmsUserOnboardingProgressMapper progressMapper) {
        this.progressMapper = progressMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WmsUserOnboardingProgress> getUserProgress(Long userId) {
        initUserTasks(userId);

        List<WmsUserOnboardingProgress> list = progressMapper.selectList(
                new LambdaQueryWrapper<WmsUserOnboardingProgress>()
                        .eq(WmsUserOnboardingProgress::getUserId, userId)
        );
        if (list == null) {
            return Collections.emptyList();
        }

        list.sort(Comparator.comparingInt(item -> TASK_CODES.indexOf(item.getTaskCode())));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long userId, String taskCode) {
        if (!isValidTaskCode(taskCode)) {
            return;
        }

        List<WmsUserOnboardingProgress> progressList = getUserProgress(userId);
        int currentTaskIndex = TASK_CODES.indexOf(taskCode);
        if (currentTaskIndex < 0) {
            return;
        }

        WmsUserOnboardingProgress currentTask = findTaskProgress(progressList, taskCode);
        if (isTaskCompleted(currentTask)) {
            return;
        }

        String blockedByTaskCode = findFirstUnfinishedPreviousTask(progressList, currentTaskIndex);
        if (blockedByTaskCode != null) {
            throw new ServiceException("请按顺序完成新手任务，请先完成：" + getTaskName(blockedByTaskCode));
        }

        progressMapper.update(
                null,
                new LambdaUpdateWrapper<WmsUserOnboardingProgress>()
                        .eq(WmsUserOnboardingProgress::getUserId, userId)
                        .eq(WmsUserOnboardingProgress::getTaskCode, taskCode)
                        .eq(WmsUserOnboardingProgress::getIsCompleted, 0)
                        .set(WmsUserOnboardingProgress::getIsCompleted, 1)
                        .set(WmsUserOnboardingProgress::getCompletedTime, LocalDateTime.now())
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserTasks(Long userId) {
        List<WmsUserOnboardingProgress> existing = progressMapper.selectList(
                new LambdaQueryWrapper<WmsUserOnboardingProgress>()
                        .eq(WmsUserOnboardingProgress::getUserId, userId)
                        .select(WmsUserOnboardingProgress::getTaskCode)
        );

        Set<String> existingCodes = existing == null ? Collections.emptySet() :
                existing.stream().map(WmsUserOnboardingProgress::getTaskCode).collect(Collectors.toSet());

        List<WmsUserOnboardingProgress> toCreate = new ArrayList<>();
        for (String code : TASK_CODES) {
            if (!existingCodes.contains(code)) {
                WmsUserOnboardingProgress progress = new WmsUserOnboardingProgress();
                progress.setUserId(userId);
                progress.setTaskCode(code);
                progress.setIsCompleted(0);
                toCreate.add(progress);
            }
        }

        for (WmsUserOnboardingProgress progress : toCreate) {
            progressMapper.insert(progress);
        }
    }

    @Override
    public boolean shouldShowOnboarding(Long userId) {
        List<WmsUserOnboardingProgress> progressList = getUserProgress(userId);
        if (progressList.isEmpty()) {
            return true;
        }

        for (WmsUserOnboardingProgress item : progressList) {
            if (item.getIsCompleted() == null || item.getIsCompleted() == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidTaskCode(String taskCode) {
        return TASK_CODE_SET.contains(taskCode);
    }

    private String findFirstUnfinishedPreviousTask(List<WmsUserOnboardingProgress> progressList, int currentTaskIndex) {
        for (int i = 0; i < currentTaskIndex; i++) {
            String previousTaskCode = TASK_CODES.get(i);
            WmsUserOnboardingProgress previousTask = findTaskProgress(progressList, previousTaskCode);
            if (!isTaskCompleted(previousTask)) {
                return previousTaskCode;
            }
        }
        return null;
    }

    private WmsUserOnboardingProgress findTaskProgress(List<WmsUserOnboardingProgress> progressList, String taskCode) {
        if (progressList == null || progressList.isEmpty()) {
            return null;
        }
        for (WmsUserOnboardingProgress progress : progressList) {
            if (taskCode.equals(progress.getTaskCode())) {
                return progress;
            }
        }
        return null;
    }

    private boolean isTaskCompleted(WmsUserOnboardingProgress progress) {
        return progress != null && progress.getIsCompleted() != null && progress.getIsCompleted() == 1;
    }

    private String getTaskName(String taskCode) {
        if ("add_goods".equals(taskCode)) {
            return "添加第一个商品";
        }
        if ("purchase_in".equals(taskCode)) {
            return "创建采购入库单";
        }
        if ("sales_out".equals(taskCode)) {
            return "创建销售出库单";
        }
        if ("view_report".equals(taskCode)) {
            return "查看库存报表";
        }
        return taskCode;
    }
}
