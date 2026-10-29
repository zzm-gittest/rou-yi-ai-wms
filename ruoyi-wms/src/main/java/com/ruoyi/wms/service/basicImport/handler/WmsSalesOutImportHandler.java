package com.ruoyi.wms.service.basicImport.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.goods.ImportError;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsGoodsMapper;
import com.ruoyi.wms.mapper.WmsGoodsSkuMapper;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.service.WmsSalesOutService;
import com.ruoyi.wms.service.basicImport.ImportFieldMappingItem;
import com.ruoyi.wms.service.basicImport.WmsBasicDataImportHandler;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class WmsSalesOutImportHandler implements WmsBasicDataImportHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final WmsSalesOutService salesOutService;
    private final WmsCustomerMapper customerMapper;
    private final WmsWarehouseMapper warehouseMapper;
    private final WmsGoodsSkuMapper goodsSkuMapper;
    private final WmsGoodsMapper goodsMapper;

    public WmsSalesOutImportHandler(WmsSalesOutService salesOutService,
                                    WmsCustomerMapper customerMapper,
                                    WmsWarehouseMapper warehouseMapper,
                                    WmsGoodsSkuMapper goodsSkuMapper,
                                    WmsGoodsMapper goodsMapper) {
        this.salesOutService = salesOutService;
        this.customerMapper = customerMapper;
        this.warehouseMapper = warehouseMapper;
        this.goodsSkuMapper = goodsSkuMapper;
        this.goodsMapper = goodsMapper;
    }

    @Override
    public String getType() {
        return "sales_out";
    }

    @Override
    public ImportResult importData(MultipartFile file) throws Exception {
        return importData(file, null);
    }

    @Override
    public ImportResult importData(MultipartFile file, String mappingJson) throws Exception {
        ImportResult result = new ImportResult();
        List<ImportError> errors = new ArrayList<>();
        Map<String, BillAggregate> billMap = new LinkedHashMap<>();

        ImportConfig importConfig = parseImportConfig(mappingJson);
        int total = 0;

        Map<String, MatchResult<WmsCustomer>> customerCodeCache = new HashMap<>();
        Map<String, MatchResult<WmsCustomer>> customerNameCache = new HashMap<>();
        Map<String, MatchResult<WmsWarehouse>> warehouseCodeCache = new HashMap<>();
        Map<String, MatchResult<WmsWarehouse>> warehouseNameCache = new HashMap<>();
        Map<String, MatchResult<WmsGoodsSku>> skuCodeCache = new HashMap<>();
        Map<String, MatchResult<WmsGoodsSku>> skuGoodsNameCache = new HashMap<>();

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
            Map<String, FieldReadConfig> fieldConfig = buildFieldConfig(headers, importConfig.mappings);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isRowEmpty(row)) {
                    continue;
                }
                if (isTemplateNoteRow(row)) {
                    continue;
                }

                total++;
                int excelRowNum = rowIndex + 1;

                try {
                    ParsedRow parsed = parseRow(row, fieldConfig);
                    String validateMsg = validateRow(parsed,
                            importConfig.strategies,
                            customerCodeCache,
                            customerNameCache,
                            warehouseCodeCache,
                            warehouseNameCache,
                            skuCodeCache,
                            skuGoodsNameCache);
                    if (StringUtils.isNotEmpty(validateMsg)) {
                        addError(errors, excelRowNum, validateMsg, extractRowData(row));
                        continue;
                    }

                    BillAggregate aggregate = billMap.computeIfAbsent(parsed.billNo, key -> BillAggregate.of(parsed));
                    String headConflict = aggregate.checkHeadConflict(parsed);
                    if (StringUtils.isNotEmpty(headConflict)) {
                        addError(errors, excelRowNum, headConflict, extractRowData(row));
                        continue;
                    }

                    aggregate.items.add(BillItem.of(excelRowNum, parsed.skuId, parsed.quantity, parsed.price, parsed.itemRemark, extractRowData(row)));
                } catch (Exception ex) {
                    addError(errors, excelRowNum, StringUtils.defaultIfEmpty(ex.getMessage(), "销售出库导入失败"), extractRowData(row));
                }
            }
        }

        for (BillAggregate aggregate : billMap.values()) {
            if (aggregate.items.isEmpty()) {
                continue;
            }

            try {
                String existingOutNo = salesOutService.findOutNoByImportSourceBillNo(aggregate.billNo);
                if (StringUtils.isNotEmpty(existingOutNo)) {
                    result.getBillOutNoMap().put(aggregate.billNo, existingOutNo);
                    continue;
                }

                WmsSalesOutSaveReqVO reqVO = new WmsSalesOutSaveReqVO();
                reqVO.setSourceType(1);
                reqVO.setSourceBillNo(aggregate.billNo);
                reqVO.setCustomerId(aggregate.customerId);
                reqVO.setWarehouseId(aggregate.warehouseId);
                reqVO.setOutDate(aggregate.outDate);
                reqVO.setRemark(buildBillRemark(aggregate));

                List<WmsSalesOutItemSaveReqVO> itemReqList = new ArrayList<>();
                for (BillItem item : aggregate.items) {
                    WmsSalesOutItemSaveReqVO itemReq = new WmsSalesOutItemSaveReqVO();
                    itemReq.setSkuId(item.skuId);
                    itemReq.setQuantity(item.quantity);
                    itemReq.setPrice(item.price);
                    itemReq.setRemark(item.remark);
                    itemReqList.add(itemReq);
                }
                reqVO.setItems(itemReqList);

                Long outId = salesOutService.create(reqVO);
                if (outId != null) {
                    var created = salesOutService.get(outId);
                    if (created != null && StringUtils.isNotEmpty(created.getOutNo())) {
                        result.getBillOutNoMap().put(aggregate.billNo, created.getOutNo());
                    }
                }
            } catch (Exception ex) {
                String msg = StringUtils.defaultIfEmpty(ex.getMessage(), "销售出库单导入失败");
                for (BillItem item : aggregate.items) {
                    addError(errors, item.rowNum, "单据[" + aggregate.billNo + "]导入失败: " + msg, item.rawData);
                }
            }
        }

        result.setTotal(total);
        result.setFailCount(errors.size());
        result.setSuccessCount(Math.max(total - errors.size(), 0));
        result.setErrors(errors);
        return result;
    }

    @Override
    public void writeTemplate(OutputStream outputStream) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("销售出库导入模板");

            String[] headers = {
                    "单据号*", "单据日期*", "客户编码", "客户名称", "仓库编码", "仓库名称", "SKU编码", "SKU名称", "商品名称", "数量*", "单价*", "金额", "备注"
            };
            String[] sample = {
                    "SO20260314001", "2026-03-14", "CUS001", "华南门店", "WH001", "华南主仓", "SKU001", "苹果-红富士", "苹果", "3", "8.00", "24.00", "首批导入"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row sampleRow = sheet.createRow(1);
            for (int i = 0; i < sample.length; i++) {
                sampleRow.createCell(i).setCellValue(sample[i]);
            }

            Row noteRow = sheet.createRow(2);
            noteRow.createCell(0).setCellValue("【说明】客户/仓库/SKU支持编码优先匹配；SKU编码缺失时需同时填写商品名称+SKU名称进行联合匹配；同单据号自动聚合，金额可空");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    @Override
    public String templateFileName() {
        return "sales_out_import_template.xlsx";
    }

    private String buildBillRemark(BillAggregate aggregate) {
        return StringUtils.isEmpty(aggregate.billRemark) ? null : aggregate.billRemark.trim();
    }

    private ParsedRow parseRow(Row row, Map<String, FieldReadConfig> configMap) {
        ParsedRow parsed = new ParsedRow();
        parsed.billNo = readString(row, configMap, "billNo");
        parsed.outDate = parseDate(readString(row, configMap, "outDate"));
        parsed.customerCode = readString(row, configMap, "customerCode");
        parsed.customerName = readString(row, configMap, "customerName");
        parsed.warehouseCode = readString(row, configMap, "warehouseCode");
        parsed.warehouseName = readString(row, configMap, "warehouseName");
        parsed.skuCode = readString(row, configMap, "skuCode");
        parsed.skuName = readString(row, configMap, "skuName");
        parsed.goodsName = readString(row, configMap, "goodsName");
        parsed.quantity = parseDecimal(readString(row, configMap, "quantity"));
        parsed.price = parseDecimal(readString(row, configMap, "price"));
        parsed.amount = parseDecimal(readString(row, configMap, "amount"));
        parsed.itemRemark = readString(row, configMap, "remark");
        return parsed;
    }

    private String validateRow(ParsedRow parsed,
                               ImportStrategies strategies,
                               Map<String, MatchResult<WmsCustomer>> customerCodeCache,
                               Map<String, MatchResult<WmsCustomer>> customerNameCache,
                               Map<String, MatchResult<WmsWarehouse>> warehouseCodeCache,
                               Map<String, MatchResult<WmsWarehouse>> warehouseNameCache,
                               Map<String, MatchResult<WmsGoodsSku>> skuCodeCache,
                               Map<String, MatchResult<WmsGoodsSku>> skuGoodsNameCache) {
        if (StringUtils.isEmpty(parsed.billNo)) {
            return "单据号不能为空";
        }
        if (parsed.outDate == null) {
            return "单据日期格式错误，支持 yyyy-MM-dd";
        }
        if (StringUtils.isEmpty(parsed.customerCode) && StringUtils.isEmpty(parsed.customerName)) {
            return "客户编码/名称至少填写一个";
        }
        if (StringUtils.isEmpty(parsed.warehouseCode) && StringUtils.isEmpty(parsed.warehouseName)) {
            return "仓库编码/名称至少填写一个";
        }
        if (StringUtils.isEmpty(parsed.skuCode)
                && (StringUtils.isEmpty(parsed.skuName) || StringUtils.isEmpty(parsed.goodsName))) {
            return "SKU编码缺失时，必须同时填写SKU名称和商品名称";
        }
        if (parsed.quantity == null || parsed.quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return "数量必须大于0";
        }
        if (parsed.price == null || parsed.price.compareTo(BigDecimal.ZERO) <= 0) {
            return "单价必须大于0";
        }

        MatchResult<WmsCustomer> customerResult = resolveCustomer(parsed, customerCodeCache, customerNameCache);
        if (!customerResult.isSuccess()) {
            return customerResult.message;
        }
        parsed.customerId = customerResult.data.getId();

        MatchResult<WmsWarehouse> warehouseResult = resolveWarehouse(parsed, warehouseCodeCache, warehouseNameCache);
        if (!warehouseResult.isSuccess()) {
            return warehouseResult.message;
        }
        parsed.warehouseId = warehouseResult.data.getId();

        MatchResult<WmsGoodsSku> skuResult = resolveSku(parsed, skuCodeCache, skuGoodsNameCache);
        if (!skuResult.isSuccess()) {
            return skuResult.message;
        }
        parsed.skuId = skuResult.data.getId();

        BigDecimal calcAmount = parsed.quantity.multiply(parsed.price).setScale(2, RoundingMode.HALF_UP);
        if (parsed.amount != null && parsed.amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal excelAmount = parsed.amount.setScale(2, RoundingMode.HALF_UP);
            if ("strict".equalsIgnoreCase(strategies.amountStrategy) && calcAmount.compareTo(excelAmount) != 0) {
                return "金额不一致(数量*单价!=金额)";
            }
        }

        return null;
    }

    private MatchResult<WmsCustomer> resolveCustomer(ParsedRow parsed,
                                                     Map<String, MatchResult<WmsCustomer>> customerCodeCache,
                                                     Map<String, MatchResult<WmsCustomer>> customerNameCache) {
        if (StringUtils.isNotEmpty(parsed.customerCode)) {
            MatchResult<WmsCustomer> byCode = customerCodeCache.computeIfAbsent(parsed.customerCode,
                    code -> matchByCode(() -> customerMapper.selectOne(new LambdaQueryWrapper<WmsCustomer>()
                            .eq(WmsCustomer::getCustomerCode, code)
                            .eq(WmsCustomer::getStatus, "0")
                            .last("limit 1")), "客户编码不存在或已停用: " + code));
            if (!byCode.isSuccess()) {
                return byCode;
            }
            if (StringUtils.isNotEmpty(parsed.customerName)
                    && !parsed.customerName.equals(StringUtils.trimToEmpty(byCode.data.getCustomerName()))) {
                return MatchResult.fail("客户编码与客户名称不匹配: " + parsed.customerCode + " / " + parsed.customerName);
            }
            return byCode;
        }
        return customerNameCache.computeIfAbsent(parsed.customerName,
                name -> matchSingleByName(() -> customerMapper.selectList(new LambdaQueryWrapper<WmsCustomer>()
                                .eq(WmsCustomer::getCustomerName, name)
                                .eq(WmsCustomer::getStatus, "0")
                                .last("limit 2")),
                        "客户名称不存在或已停用: " + name,
                        "客户名称匹配到多条，请提供客户编码: " + name));
    }

    private MatchResult<WmsWarehouse> resolveWarehouse(ParsedRow parsed,
                                                       Map<String, MatchResult<WmsWarehouse>> warehouseCodeCache,
                                                       Map<String, MatchResult<WmsWarehouse>> warehouseNameCache) {
        if (StringUtils.isNotEmpty(parsed.warehouseCode)) {
            MatchResult<WmsWarehouse> byCode = warehouseCodeCache.computeIfAbsent(parsed.warehouseCode,
                    code -> matchByCode(() -> warehouseMapper.selectOne(new LambdaQueryWrapper<WmsWarehouse>()
                            .eq(WmsWarehouse::getWarehouseCode, code)
                            .eq(WmsWarehouse::getStatus, "0")
                            .last("limit 1")), "仓库编码不存在或已停用: " + code));
            if (!byCode.isSuccess()) {
                return byCode;
            }
            if (StringUtils.isNotEmpty(parsed.warehouseName)
                    && !parsed.warehouseName.equals(StringUtils.trimToEmpty(byCode.data.getWarehouseName()))) {
                return MatchResult.fail("仓库编码与仓库名称不匹配: " + parsed.warehouseCode + " / " + parsed.warehouseName);
            }
            return byCode;
        }
        return warehouseNameCache.computeIfAbsent(parsed.warehouseName,
                name -> matchSingleByName(() -> warehouseMapper.selectList(new LambdaQueryWrapper<WmsWarehouse>()
                                .eq(WmsWarehouse::getWarehouseName, name)
                                .eq(WmsWarehouse::getStatus, "0")
                                .last("limit 2")),
                        "仓库名称不存在或已停用: " + name,
                        "仓库名称匹配到多条，请提供仓库编码: " + name));
    }

    private MatchResult<WmsGoodsSku> resolveSku(ParsedRow parsed,
                                                Map<String, MatchResult<WmsGoodsSku>> skuCodeCache,
                                                Map<String, MatchResult<WmsGoodsSku>> skuGoodsNameCache) {
        if (StringUtils.isNotEmpty(parsed.skuCode)) {
            MatchResult<WmsGoodsSku> byCode = skuCodeCache.computeIfAbsent(parsed.skuCode,
                    code -> matchByCode(() -> goodsSkuMapper.selectOne(new LambdaQueryWrapper<WmsGoodsSku>()
                            .eq(WmsGoodsSku::getSkuCode, code)
                            .eq(WmsGoodsSku::getStatus, "0")
                            .last("limit 1")), "SKU编码不存在或已停用: " + code));
            if (!byCode.isSuccess()) {
                return byCode;
            }
            if (StringUtils.isNotEmpty(parsed.skuName)
                    && !parsed.skuName.equals(StringUtils.trimToEmpty(byCode.data.getSkuName()))) {
                return MatchResult.fail("SKU编码与SKU名称不匹配: " + parsed.skuCode + " / " + parsed.skuName);
            }
            if (StringUtils.isNotEmpty(parsed.goodsName)) {
                WmsGoods goods = goodsMapper.selectById(byCode.data.getGoodsId());
                String skuGoodsName = goods == null ? "" : StringUtils.trimToEmpty(goods.getGoodsName());
                if (StringUtils.isNotEmpty(skuGoodsName)
                        && !parsed.goodsName.equals(skuGoodsName)) {
                    return MatchResult.fail("SKU编码与商品名称不匹配: " + parsed.skuCode + " / " + parsed.goodsName);
                }
            }
            return byCode;
        }

        List<WmsGoods> goodsList = goodsMapper.selectList(new LambdaQueryWrapper<WmsGoods>()
                .eq(WmsGoods::getGoodsName, parsed.goodsName)
                .eq(WmsGoods::getStatus, "0")
                .eq(WmsGoods::getDelFlag, false)
                .last("limit 5"));
        if (goodsList == null || goodsList.isEmpty()) {
            return MatchResult.fail("商品名称不存在或已停用: " + parsed.goodsName);
        }

        Set<Long> goodsIds = new LinkedHashSet<>();
        for (WmsGoods goods : goodsList) {
            if (goods != null && goods.getId() != null) {
                goodsIds.add(goods.getId());
            }
        }
        if (goodsIds.isEmpty()) {
            return MatchResult.fail("商品名称不存在或已停用: " + parsed.goodsName);
        }

        String comboKey = parsed.goodsName + "||" + parsed.skuName;
        return skuGoodsNameCache.computeIfAbsent(comboKey,
                key -> {
                    List<WmsGoodsSku> skuList = goodsSkuMapper.selectList(new LambdaQueryWrapper<WmsGoodsSku>()
                            .in(WmsGoodsSku::getGoodsId, goodsIds)
                            .eq(WmsGoodsSku::getSkuName, parsed.skuName)
                            .eq(WmsGoodsSku::getStatus, "0")
                            .last("limit 2"));
                    if (skuList == null || skuList.isEmpty()) {
                        return MatchResult.fail("商品名称+SKU名称未匹配到SKU: " + parsed.goodsName + " / " + parsed.skuName);
                    }
                    if (skuList.size() > 1) {
                        return MatchResult.fail("商品名称+SKU名称匹配到多条SKU，请提供SKU编码: " + parsed.goodsName + " / " + parsed.skuName);
                    }
                    return MatchResult.ok(skuList.get(0));
                });
    }

    private <T> MatchResult<T> matchByCode(EntitySupplier<T> loader, String notFoundMessage) {
        T entity = loader.get();
        if (entity == null) {
            return MatchResult.fail(notFoundMessage);
        }
        return MatchResult.ok(entity);
    }

    private <T> MatchResult<T> matchSingleByName(ListSupplier<T> loader, String notFoundMessage, String duplicateMessage) {
        List<T> list = loader.get();
        if (list == null || list.isEmpty()) {
            return MatchResult.fail(notFoundMessage);
        }
        if (list.size() > 1) {
            return MatchResult.fail(duplicateMessage);
        }
        return MatchResult.ok(list.get(0));
    }

    private ImportConfig parseImportConfig(String mappingJson) {
        ImportConfig config = new ImportConfig();
        config.mappings = Collections.emptyMap();
        config.strategies = new ImportStrategies();

        if (StringUtils.isEmpty(mappingJson)) {
            return config;
        }

        try {
            JsonNode root = OBJECT_MAPPER.readTree(mappingJson);
            if (root == null || root.isNull()) {
                return config;
            }

            JsonNode mappingsNode = root;
            if (root.isObject()) {
                JsonNode explicit = root.get("mappings");
                if (explicit != null) {
                    mappingsNode = explicit;
                }

                JsonNode strategiesNode = root.get("strategies");
                if (strategiesNode != null && strategiesNode.isObject()) {
                    String amount = textOf(strategiesNode.get("amountStrategy"));
                    if (StringUtils.isNotEmpty(amount)) {
                        config.strategies.amountStrategy = amount;
                    }
                }
            }

            List<ImportFieldMappingItem> mappingItems = new ArrayList<>();
            if (mappingsNode != null && mappingsNode.isArray()) {
                mappingItems.addAll(OBJECT_MAPPER.convertValue(mappingsNode, new TypeReference<List<ImportFieldMappingItem>>() {
                }));
            }

            Map<String, ImportFieldMappingItem> mappingMap = new HashMap<>();
            for (ImportFieldMappingItem item : mappingItems) {
                if (item == null || StringUtils.isEmpty(item.getTargetField())) {
                    continue;
                }
                mappingMap.put(item.getTargetField().trim(), item);
            }
            config.mappings = mappingMap;
            return config;
        } catch (Exception ex) {
            throw new ServiceException("字段映射配置格式错误");
        }
    }

    private Map<String, FieldReadConfig> buildFieldConfig(List<String> headers,
                                                           Map<String, ImportFieldMappingItem> explicitMapping) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String normalized = normalizeHeader(headers.get(i));
            if (StringUtils.isNotEmpty(normalized) && !headerIndexMap.containsKey(normalized)) {
                headerIndexMap.put(normalized, i);
            }
        }

        List<FieldMeta> metas = fieldMetas();
        Map<String, FieldReadConfig> configMap = new HashMap<>();
        for (FieldMeta meta : metas) {
            FieldReadConfig config = new FieldReadConfig();
            config.fieldKey = meta.fieldKey;
            config.ruleType = "direct";

            ImportFieldMappingItem item = explicitMapping.get(meta.fieldKey);
            if (item != null) {
                String ruleType = StringUtils.trimToEmpty(item.getRuleType()).toLowerCase();
                if ("fixed".equals(ruleType) && StringUtils.isNotEmpty(item.getFixedValue())) {
                    config.ruleType = "fixed";
                    config.fixedValue = item.getFixedValue();
                } else if (StringUtils.isNotEmpty(item.getSourceHeader())) {
                    Integer idx = headerIndexMap.get(normalizeHeader(item.getSourceHeader()));
                    if (idx != null) {
                        config.columnIndex = idx;
                    }
                }
            }

            if (config.columnIndex == null && !"fixed".equals(config.ruleType)) {
                config.columnIndex = autoMatchIndex(meta, headerIndexMap);
            }

            if (meta.required && !config.available()) {
                throw new ServiceException("字段映射缺失: " + meta.systemFieldName);
            }
            configMap.put(meta.fieldKey, config);
        }
        return configMap;
    }

    private Integer autoMatchIndex(FieldMeta meta, Map<String, Integer> headerIndexMap) {
        Integer idx = headerIndexMap.get(normalizeHeader(meta.systemFieldName));
        if (idx != null) {
            return idx;
        }
        for (String alias : meta.aliases) {
            idx = headerIndexMap.get(normalizeHeader(alias));
            if (idx != null) {
                return idx;
            }
        }
        return null;
    }

    private List<FieldMeta> fieldMetas() {
        List<FieldMeta> metas = new ArrayList<>();
        metas.add(FieldMeta.of("billNo", "单据号", true, "单据号*"));
        metas.add(FieldMeta.of("outDate", "单据日期", true, "单据日期*", "出库日期"));
        metas.add(FieldMeta.of("customerCode", "客户编码", false, "客户编码*"));
        metas.add(FieldMeta.of("customerName", "客户名称", false, "客户名称*", "客户"));
        metas.add(FieldMeta.of("warehouseCode", "仓库编码", false, "仓库编码*"));
        metas.add(FieldMeta.of("warehouseName", "仓库名称", false, "仓库名称*", "仓库"));
        metas.add(FieldMeta.of("skuCode", "SKU编码", false, "SKU编码*", "sku编码"));
        metas.add(FieldMeta.of("skuName", "SKU名称", false, "SKU名称*"));
        metas.add(FieldMeta.of("goodsName", "商品名称", false, "商品", "货品名称", "品名"));
        metas.add(FieldMeta.of("quantity", "数量", true, "数量*"));
        metas.add(FieldMeta.of("price", "单价", true, "单价*"));
        metas.add(FieldMeta.of("amount", "金额", false));
        metas.add(FieldMeta.of("remark", "备注", false));
        return metas;
    }

    private String readString(Row row, Map<String, FieldReadConfig> configMap, String fieldKey) {
        FieldReadConfig config = configMap.get(fieldKey);
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

    private String cellString(Row row, int index) {
        if (row == null || index < 0) {
            return "";
        }
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate().toString();
            }
            double value = cell.getNumericCellValue();
            if (value == (long) value) {
                return String.valueOf((long) value);
            }
            return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return StringUtils.trimToEmpty(cell.toString());
    }

    private LocalDate parseDate(String value) {
        String text = StringUtils.trimToEmpty(value);
        if (StringUtils.isEmpty(text)) {
            return null;
        }

        List<DateTimeFormatter> fmts = new ArrayList<>();
        fmts.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        fmts.add(DateTimeFormatter.ofPattern("yyyy/M/d"));
        fmts.add(DateTimeFormatter.ofPattern("yyyy/M/dd"));
        fmts.add(DateTimeFormatter.ofPattern("yyyy.MM.dd"));

        for (DateTimeFormatter fmt : fmts) {
            try {
                return LocalDate.parse(text, fmt);
            } catch (DateTimeParseException ignore) {
                // try next formatter
            }
        }
        return null;
    }

    private BigDecimal parseDecimal(String value) {
        String text = StringUtils.trimToEmpty(value);
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            return new BigDecimal(text);
        } catch (Exception ex) {
            return null;
        }
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

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            if (StringUtils.isNotEmpty(cellString(row, i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTemplateNoteRow(Row row) {
        String first = cellString(row, 0);
        return StringUtils.isNotEmpty(first)
                && (first.startsWith("【说明】") || first.startsWith("说明"));
    }

    private String extractRowData(Row row) {
        if (row == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            parts.add(cellString(row, i));
        }
        return String.join(" | ", parts);
    }

    private void addError(List<ImportError> errors, int rowNum, String msg, String rawData) {
        ImportError error = new ImportError();
        error.setRowNumber(rowNum);
        error.setErrorMessage(msg);
        error.setRawData(rawData);
        errors.add(error);
    }

    private String normalizeHeader(String header) {
        String text = StringUtils.trimToEmpty(header);
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        return text.replace("*", "")
                .replace("（", "(")
                .replace("）", ")")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    private String textOf(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        return StringUtils.trimToEmpty(node.asText());
    }

    private static class ImportConfig {
        private Map<String, ImportFieldMappingItem> mappings;
        private ImportStrategies strategies;
    }

    private static class ImportStrategies {
        private String amountStrategy = "strict";
    }

    private static class ParsedRow {
        private String billNo;
        private LocalDate outDate;
        private String customerCode;
        private String customerName;
        private Long customerId;
        private String warehouseCode;
        private String warehouseName;
        private Long warehouseId;
        private String skuCode;
        private String skuName;
        private String goodsName;
        private Long skuId;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal amount;
        private String itemRemark;
    }

    private static class BillAggregate {
        private String billNo;
        private Long customerId;
        private Long warehouseId;
        private LocalDate outDate;
        private String billRemark;
        private List<BillItem> items = new ArrayList<>();

        private static BillAggregate of(ParsedRow row) {
            BillAggregate aggregate = new BillAggregate();
            aggregate.billNo = row.billNo;
            aggregate.customerId = row.customerId;
            aggregate.warehouseId = row.warehouseId;
            aggregate.outDate = row.outDate;
            aggregate.billRemark = row.itemRemark;
            return aggregate;
        }

        private String checkHeadConflict(ParsedRow row) {
            if (!customerId.equals(row.customerId)) {
                return "同单据号的客户不一致: " + billNo;
            }
            if (!warehouseId.equals(row.warehouseId)) {
                return "同单据号的仓库不一致: " + billNo;
            }
            if (!outDate.equals(row.outDate)) {
                return "同单据号的单据日期不一致: " + billNo;
            }
            return null;
        }
    }

    private static class BillItem {
        private int rowNum;
        private Long skuId;
        private BigDecimal quantity;
        private BigDecimal price;
        private String remark;
        private String rawData;

        private static BillItem of(int rowNum, Long skuId, BigDecimal quantity, BigDecimal price, String remark, String rawData) {
            BillItem item = new BillItem();
            item.rowNum = rowNum;
            item.skuId = skuId;
            item.quantity = quantity;
            item.price = price;
            item.remark = remark;
            item.rawData = rawData;
            return item;
        }
    }

    private static class FieldMeta {
        private String fieldKey;
        private String systemFieldName;
        private boolean required;
        private List<String> aliases;

        private static FieldMeta of(String fieldKey, String systemFieldName, boolean required, String... aliases) {
            FieldMeta meta = new FieldMeta();
            meta.fieldKey = fieldKey;
            meta.systemFieldName = systemFieldName;
            meta.required = required;
            List<String> aliasList = new ArrayList<>();
            if (aliases != null) {
                Collections.addAll(aliasList, aliases);
            }
            meta.aliases = aliasList;
            return meta;
        }
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

    @FunctionalInterface
    private interface EntitySupplier<T> {
        T get();
    }

    @FunctionalInterface
    private interface ListSupplier<T> {
        List<T> get();
    }

    private static class MatchResult<T> {
        private final T data;
        private final String message;

        private MatchResult(T data, String message) {
            this.data = data;
            this.message = message;
        }

        private boolean isSuccess() {
            return data != null;
        }

        private static <T> MatchResult<T> ok(T data) {
            return new MatchResult<>(data, null);
        }

        private static <T> MatchResult<T> fail(String message) {
            return new MatchResult<>(null, message);
        }
    }
}
