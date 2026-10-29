package com.ruoyi.wms.service.basicImport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.goods.ImportError;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用简单Excel导入处理器（首行表头，第二行起为数据）
 */
public abstract class AbstractSimpleExcelImportHandler implements WmsBasicDataImportHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private Map<String, FieldReadConfig> fieldConfigMap = Collections.emptyMap();

    @Override
    public ImportResult importData(MultipartFile file) throws Exception {
        return importData(file, null);
    }

    @Override
    public ImportResult importData(MultipartFile file, String mappingJson) throws Exception {
        ImportResult result = new ImportResult();
        List<ImportError> errors = new ArrayList<>();

        int total = 0;
        int success = 0;
        int fail = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                result.setTotal(0);
                result.setSuccessCount(0);
                result.setFailCount(0);
                result.setErrors(errors);
                return result;
            }

            Row headerRow = sheet.getRow(0);
            List<String> headers = readHeaders(headerRow);
            fieldConfigMap = buildFieldConfig(headers, mappingJson);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isRowEmpty(row)) {
                    continue;
                }

                int excelRowNum = rowIndex + 1;
                if (isTemplateNoteRow(row)) {
                    continue;
                }

                total++;
                try {
                    String errorMessage = validateAndSave(row, excelRowNum);
                    if (StringUtils.isNotEmpty(errorMessage)) {
                        fail++;
                        ImportError error = new ImportError();
                        error.setRowNumber(excelRowNum);
                        error.setErrorMessage(errorMessage);
                        error.setRawData(extractRowData(row));
                        errors.add(error);
                    } else {
                        success++;
                    }
                } catch (Exception ex) {
                    fail++;
                    ImportError error = new ImportError();
                    error.setRowNumber(excelRowNum);
                    error.setErrorMessage(ex.getMessage());
                    error.setRawData(extractRowData(row));
                    errors.add(error);
                }
            }
        }

        result.setTotal(total);
        result.setSuccessCount(success);
        result.setFailCount(fail);
        result.setErrors(errors);
        fieldConfigMap = Collections.emptyMap();
        return result;
    }

    @Override
    public void writeTemplate(OutputStream outputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("导入模板");

            String[] headers = headers();
            String[] sample = sampleRow();

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row sampleRow = sheet.createRow(1);
            for (int i = 0; i < sample.length; i++) {
                sampleRow.createCell(i).setCellValue(sample[i]);
            }

            Row noteRow = sheet.createRow(2);
            noteRow.createCell(0).setCellValue("【说明】请按示例填写，状态列可填：正常/停用");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    protected abstract String validateAndSave(Row row, int excelRowNum);

    protected abstract String[] headers();

    protected abstract String[] sampleRow();

    protected List<ImportFieldMeta> fieldMetas() {
        return Collections.emptyList();
    }

    protected String cellString(Row row, int index) {
        if (row == null) {
            return "";
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == (long) val) {
                return String.valueOf((long) val);
            }
            return String.valueOf(val);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return StringUtils.trimToEmpty(cell.toString());
    }

    protected String cellString(Row row, String fieldKey) {
        FieldReadConfig config = fieldConfigMap.get(fieldKey);
        if (config == null) {
            return "";
        }
        if ("fixed".equals(config.ruleType)) {
            return StringUtils.trimToEmpty(config.fixedValue);
        }
        if (config.columnIndex == null || config.columnIndex < 0) {
            return "";
        }
        return cellString(row, config.columnIndex);
    }

    protected Integer cellInteger(Row row, int index, Integer defaultValue) {
        String value = cellString(row, index);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    protected Integer cellInteger(Row row, String fieldKey, Integer defaultValue) {
        String value = cellString(row, fieldKey);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    protected BigDecimal cellBigDecimal(Row row, int index, BigDecimal defaultValue) {
        String value = cellString(row, index);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    protected BigDecimal cellBigDecimal(Row row, String fieldKey, BigDecimal defaultValue) {
        String value = cellString(row, fieldKey);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    protected String normalizeStatus(String statusText) {
        if ("1".equals(statusText) || "停用".equals(StringUtils.trimToEmpty(statusText))) {
            return "1";
        }
        return "0";
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            String value = cellString(row, i);
            if (StringUtils.isNotEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTemplateNoteRow(Row row) {
        String firstCell = cellString(row, 0);
        return StringUtils.isNotEmpty(firstCell)
                && (firstCell.startsWith("【说明】") || firstCell.startsWith("说明"));
    }

    private String extractRowData(Row row) {
        List<String> parts = new ArrayList<>();
        if (row == null) {
            return "";
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            parts.add(cellString(row, i));
        }
        return String.join(" | ", parts);
    }

    private List<String> readHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        if (headerRow == null) {
            return headers;
        }

        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            headers.add(cellString(headerRow, i));
        }
        return headers;
    }

    private Map<String, FieldReadConfig> buildFieldConfig(List<String> headers, String mappingJson) {
        List<ImportFieldMeta> metas = fieldMetas();
        if (metas == null || metas.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String normalized = normalizeHeader(headers.get(i));
            if (StringUtils.isNotEmpty(normalized) && !headerIndexMap.containsKey(normalized)) {
                headerIndexMap.put(normalized, i);
            }
        }

        Map<String, ImportFieldMappingItem> explicitMapping = parseMappingJson(mappingJson);
        Map<String, FieldReadConfig> configMap = new HashMap<>();

        for (ImportFieldMeta meta : metas) {
            FieldReadConfig config = new FieldReadConfig();
            config.fieldKey = meta.getFieldKey();
            config.ruleType = "direct";
            config.columnIndex = null;

            ImportFieldMappingItem item = explicitMapping.get(meta.getFieldKey());
            if (item != null) {
                String ruleType = StringUtils.trimToEmpty(item.getRuleType()).toLowerCase();
                if ("fixed".equals(ruleType) && StringUtils.isNotEmpty(item.getFixedValue())) {
                    config.ruleType = "fixed";
                    config.fixedValue = item.getFixedValue();
                } else if (StringUtils.isNotEmpty(item.getSourceHeader())) {
                    Integer index = headerIndexMap.get(normalizeHeader(item.getSourceHeader()));
                    if (index != null) {
                        config.columnIndex = index;
                    }
                }
            }

            if (config.columnIndex == null && !"fixed".equals(config.ruleType)) {
                config.columnIndex = autoMatchHeaderIndex(meta, headerIndexMap);
            }

            if (meta.isRequired() && !config.available()) {
                throw new ServiceException("字段映射缺失: " + meta.getSystemFieldName());
            }

            configMap.put(meta.getFieldKey(), config);
        }

        return configMap;
    }

    private Integer autoMatchHeaderIndex(ImportFieldMeta meta, Map<String, Integer> headerIndexMap) {
        Integer index = headerIndexMap.get(normalizeHeader(meta.getSystemFieldName()));
        if (index != null) {
            return index;
        }
        for (String alias : meta.getAliases()) {
            index = headerIndexMap.get(normalizeHeader(alias));
            if (index != null) {
                return index;
            }
        }
        return null;
    }

    private Map<String, ImportFieldMappingItem> parseMappingJson(String mappingJson) {
        if (StringUtils.isEmpty(mappingJson)) {
            return Collections.emptyMap();
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(mappingJson);
            final List<ImportFieldMappingItem> items = new ArrayList<>();

            if (root == null || root.isNull()) {
                return Collections.emptyMap();
            }
            if (root.isArray()) {
                items.addAll(OBJECT_MAPPER.convertValue(root, new TypeReference<List<ImportFieldMappingItem>>() {
                }));
            } else if (root.isObject()) {
                JsonNode mappingsNode = root.has("mappings") ? root.get("mappings") : root.get("items");
                if (mappingsNode != null && mappingsNode.isArray()) {
                    items.addAll(OBJECT_MAPPER.convertValue(mappingsNode, new TypeReference<List<ImportFieldMappingItem>>() {
                    }));
                } else {
                    // 兼容对象字典格式：{"fieldKey": {ruleType, sourceHeader...}}
                    root.fields().forEachRemaining(entry -> {
                        JsonNode itemNode = entry.getValue();
                        if (itemNode == null || itemNode.isNull()) {
                            return;
                        }
                        ImportFieldMappingItem item = OBJECT_MAPPER.convertValue(itemNode, ImportFieldMappingItem.class);
                        if (StringUtils.isEmpty(item.getTargetField())) {
                            item.setTargetField(entry.getKey());
                        }
                        items.add(item);
                    });
                }
            }

            Map<String, ImportFieldMappingItem> map = new HashMap<>();
            for (ImportFieldMappingItem item : items) {
                if (item == null || StringUtils.isEmpty(item.getTargetField())) {
                    continue;
                }
                map.put(item.getTargetField().trim(), item);
            }
            return map;
        } catch (Exception ex) {
            throw new ServiceException("字段映射配置格式错误");
        }
    }

    private String normalizeHeader(String header) {
        String value = StringUtils.trimToEmpty(header);
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        return value.replace("*", "")
                .replace("（", "(")
                .replace("）", ")")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private static class FieldReadConfig {
        private String fieldKey;
        private String ruleType;
        private Integer columnIndex;
        private String fixedValue;

        private boolean available() {
            if ("fixed".equals(ruleType)) {
                return StringUtils.isNotEmpty(fixedValue);
            }
            return columnIndex != null && columnIndex >= 0;
        }
    }
}
