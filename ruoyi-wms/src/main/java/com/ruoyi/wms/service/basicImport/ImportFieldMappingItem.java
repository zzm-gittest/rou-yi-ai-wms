package com.ruoyi.wms.service.basicImport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 前端提交的字段映射项
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportFieldMappingItem {

    private String targetField;
    private String sourceHeader;
    private String ruleType;
    private String fixedValue;

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getSourceHeader() {
        return sourceHeader;
    }

    public void setSourceHeader(String sourceHeader) {
        this.sourceHeader = sourceHeader;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }
}
