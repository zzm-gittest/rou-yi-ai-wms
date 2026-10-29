package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;

import java.util.List;

/**
 * 确认类交互的统一支持：
 * 1) 统一输出“确认/取消”快捷操作；
 * 2) 兼容历史输入“是/否”；
 * 3) 避免多类散落重复判断。
 */
class PcConfirmActionSupport {

    private final PcMultiBillChatServiceImpl owner;

    PcConfirmActionSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    List<String> confirmCancelQuickActions() {
        return owner.conversationLifecycleSupport.quickActions("确认", "取消");
    }

    boolean isConfirmInput(String input) {
        String normalized = normalize(input);
        return "确认".equals(normalized) || "是".equals(normalized);
    }

    boolean isCancelInput(String input) {
        String normalized = normalize(input);
        return "取消".equals(normalized) || "否".equals(normalized) || "返回".equals(normalized);
    }

    String buildConfirmPrompt(String subject) {
        return "请确认" + StringUtils.defaultIfBlank(subject, "单据信息") + "，可在卡片下方点击【确认】或【取消】。";
    }

    String confirmHintText() {
        return "请点击卡片下方【确认】继续执行，或点击【取消】终止本次创建。";
    }

    private String normalize(String input) {
        return StringUtils.defaultString(input).trim();
    }
}