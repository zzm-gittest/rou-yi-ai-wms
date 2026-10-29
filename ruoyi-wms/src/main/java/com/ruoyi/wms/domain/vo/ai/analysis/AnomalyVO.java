package com.ruoyi.wms.domain.vo.ai.analysis;

import java.util.List;

/**
 * 异常检测条目 VO
 *
 * @author ruoyi
 */
public class AnomalyVO {

    /** 异常类型 */
    private String type;
    /** 严重程度：RED, ORANGE, YELLOW, BLUE, WHITE */
    private String severity;
    /** 标题 */
    private String title;
    /** 描述 */
    private String description;
    /** 给LLM的上下文提示 */
    private String analysisHint;
    /** 建议操作列表 */
    private List<SuggestedAction> suggestedActions;

    public AnomalyVO() {
    }

    public AnomalyVO(String type, String severity, String title, String description) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAnalysisHint() {
        return analysisHint;
    }

    public void setAnalysisHint(String analysisHint) {
        this.analysisHint = analysisHint;
    }

    public List<SuggestedAction> getSuggestedActions() {
        return suggestedActions;
    }

    public void setSuggestedActions(List<SuggestedAction> suggestedActions) {
        this.suggestedActions = suggestedActions;
    }

    /**
     * 建议操作
     */
    public static class SuggestedAction {
        private String label;
        private String action;
        private String type;

        public SuggestedAction() {
        }

        public SuggestedAction(String label, String action, String type) {
            this.label = label;
            this.action = action;
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
