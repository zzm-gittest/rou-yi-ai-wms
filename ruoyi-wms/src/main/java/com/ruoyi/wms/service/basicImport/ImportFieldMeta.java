package com.ruoyi.wms.service.basicImport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 导入系统字段元信息
 */
public class ImportFieldMeta {

    private final String fieldKey;
    private final String systemFieldName;
    private final boolean required;
    private final List<String> aliases;

    private ImportFieldMeta(String fieldKey, String systemFieldName, boolean required, List<String> aliases) {
        this.fieldKey = fieldKey;
        this.systemFieldName = systemFieldName;
        this.required = required;
        this.aliases = aliases;
    }

    public static ImportFieldMeta of(String fieldKey, String systemFieldName, boolean required, String... aliases) {
        List<String> aliasList = new ArrayList<>();
        if (aliases != null) {
            Collections.addAll(aliasList, aliases);
        }
        return new ImportFieldMeta(fieldKey, systemFieldName, required, aliasList);
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getSystemFieldName() {
        return systemFieldName;
    }

    public boolean isRequired() {
        return required;
    }

    public List<String> getAliases() {
        return aliases;
    }
}
