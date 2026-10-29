package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.*;
import com.ruoyi.wms.domain.vo.goods.*;
import com.ruoyi.wms.domain.vo.goodsSku.*;
import com.ruoyi.wms.mapper.*;
import com.ruoyi.wms.service.*;
import com.ruoyi.wms.service.basicImport.ImportFieldMappingItem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;

/**
 * 商品导入服务实现
 *
 * @author ruoyi
 */
@Service
public class WmsGoodsImportServiceImpl implements WmsGoodsImportService {

    private static final Logger log = LoggerFactory.getLogger(WmsGoodsImportServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int GOODS_CODE_MAX_LENGTH = 50;
    private static final int GOODS_NAME_MAX_LENGTH = 100;
    private static final int BRAND_MAX_LENGTH = 50;
    private static final int PLACE_MAX_LENGTH = 50;
    private static final int BARCODE_MAX_LENGTH = 50;

    @Autowired
    private WmsGoodsService goodsService;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsGoodsCategoryService categoryService;

    @Autowired
    private WmsUnitService unitService;

    @Autowired
    private WmsGoodsMapper goodsMapper;

    @Autowired
    private WmsGoodsCategoryMapper categoryMapper;

    @Autowired
    private WmsUnitMapper unitMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importGoods(MultipartFile file) throws Exception {
        return doImportGoods(file, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importGoods(MultipartFile file, String mappingJson) throws Exception {
        return doImportGoods(file, mappingJson);
    }

    private ImportResult doImportGoods(MultipartFile file, String mappingJson) throws Exception {
        ImportResult result = new ImportResult();
        List<ImportError> errors = new ArrayList<>();

        // 1. 读取Excel文件
        List<WmsGoodsImportVo> dataList = readExcel(file, mappingJson);
        result.setTotal(dataList.size());

        if (dataList.isEmpty()) {
            ImportError error = new ImportError();
            error.setRowNumber(0);
            error.setErrorMessage("Excel文件为空或格式不正确");
            error.setRawData("");
            errors.add(error);
            result.setFailCount(1);
            result.setErrors(errors);
            return result;
        }

        // 2. 分组：按商品名称分组
        Map<String, ImportGoodsData> goodsMap = new LinkedHashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            WmsGoodsImportVo vo = dataList.get(i);
            vo.setRowNumber(i + 2); // Excel行号（从2开始，1是表头）

            // 检查商品名称是否为空
            if (StringUtils.isEmpty(vo.getGoodsName())) {
                ImportError error = new ImportError();
                error.setRowNumber(vo.getRowNumber());
                error.setErrorMessage("商品名称不能为空");
                error.setRawData(toJsonString(vo));
                errors.add(error);
                continue;
            }

            // 使用商品名称作为key，合并同一商品的SKU
            String goodsKey = vo.getGoodsName().trim();
            ImportGoodsData data = goodsMap.computeIfAbsent(goodsKey, k -> new ImportGoodsData());
            data.setGoods(vo);

            // 如果有规格信息，添加到SKU列表
            if (vo.hasSpec()) {
                data.addSku(vo);
            }
        }

        // 3. 先全量校验（有任何错误都不入库）
        for (ImportGoodsData data : goodsMap.values()) {
            validateGoodsData(data, errors);
        }

        if (!errors.isEmpty()) {
            result.setSuccessCount(0);
            result.setFailCount(errors.size());
            result.setErrors(errors);
            return result;
        }

        // 4. 校验通过后统一入库，保证同一事务内全成全败
        try {
            for (ImportGoodsData data : goodsMap.values()) {
                saveGoodsData(data);
            }
            result.setSuccessCount(goodsMap.size());
            result.setFailCount(0);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            ImportError error = new ImportError();
            error.setRowNumber(0);
            error.setErrorMessage(buildImportFailMessage(e));
            error.setRawData("");
            errors.add(error);
            result.setSuccessCount(0);
            result.setFailCount(goodsMap.size());
        }

        result.setErrors(errors);
        return result;
    }

    /**
     * 校验单个商品分组
     */
    private void validateGoodsData(ImportGoodsData data, List<ImportError> errors) {
        WmsGoodsImportVo goodsVo = data.getGoods();

        // 库存阈值校验：最大库存必须大于最小库存
        appendStockRangeError(goodsVo, errors);
        appendTextLengthError(goodsVo.getRowNumber(), "商品编码", goodsVo.getGoodsCode(), GOODS_CODE_MAX_LENGTH, goodsVo, errors);
        appendTextLengthError(goodsVo.getRowNumber(), "商品条码", goodsVo.getBarcode(), BARCODE_MAX_LENGTH, goodsVo, errors);
        appendTextLengthError(goodsVo.getRowNumber(), "商品名称", goodsVo.getGoodsName(), GOODS_NAME_MAX_LENGTH, goodsVo, errors);
        appendTextLengthError(goodsVo.getRowNumber(), "品牌", goodsVo.getBrand(), BRAND_MAX_LENGTH, goodsVo, errors);
        appendTextLengthError(goodsVo.getRowNumber(), "产地", goodsVo.getPlace(), PLACE_MAX_LENGTH, goodsVo, errors);
        if (data.getSkuList() != null && !data.getSkuList().isEmpty()) {
            for (WmsGoodsImportVo skuVo : data.getSkuList()) {
                appendStockRangeError(skuVo, errors);
            }
        }

        // 查询分类ID
        Long categoryId = getCategoryidByName(goodsVo.getCategoryName());
        if (categoryId == null) {
            ImportError error = new ImportError();
            error.setRowNumber(goodsVo.getRowNumber());
            error.setErrorMessage("分类不存在: " + goodsVo.getCategoryName());
            error.setRawData(toJsonString(goodsVo));
            errors.add(error);
        }

        // 查询单位ID
        Long unitId = getUnitIdByName(goodsVo.getUnitName());
        if (unitId == null) {
            ImportError error = new ImportError();
            error.setRowNumber(goodsVo.getRowNumber());
            error.setErrorMessage("单位不存在: " + goodsVo.getUnitName());
            error.setRawData(toJsonString(goodsVo));
            errors.add(error);
        }

        // 检查商品编码是否重复（只检查未删除的商品）
        if (StringUtils.isNotEmpty(goodsVo.getGoodsCode())) {
            LambdaQueryWrapper<WmsGoods> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(WmsGoods::getGoodsCode, goodsVo.getGoodsCode())
                    .eq(WmsGoods::getDelFlag, false);
            long count = goodsMapper.selectCount(wrapper);
            if (count > 0) {
                ImportError error = new ImportError();
                error.setRowNumber(goodsVo.getRowNumber());
                error.setErrorMessage("商品编码已存在: " + goodsVo.getGoodsCode());
                error.setRawData(toJsonString(goodsVo));
                errors.add(error);
            }
        }
    }

    /**
     * 保存单个商品分组
     */
    private void saveGoodsData(ImportGoodsData data) {
        WmsGoodsImportVo goodsVo = data.getGoods();

        Long categoryId = getCategoryidByName(goodsVo.getCategoryName());
        Long unitId = getUnitIdByName(goodsVo.getUnitName());
        if (categoryId == null) {
            throw new ServiceException("分类不存在: " + goodsVo.getCategoryName());
        }
        if (unitId == null) {
            throw new ServiceException("单位不存在: " + goodsVo.getUnitName());
        }

        // 判断是否有规格
        boolean hasSpec = data.hasSpec();

        // 保存商品
        WmsGoodsSaveReqVO goodsReq = new WmsGoodsSaveReqVO();
        goodsReq.setGoodsCode(goodsVo.getGoodsCode());
        goodsReq.setGoodsName(goodsVo.getGoodsName());
        goodsReq.setCategoryId(String.valueOf(categoryId));
        goodsReq.setUnitId(unitId);
        goodsReq.setBrand(goodsVo.getBrand());
        goodsReq.setPlace(goodsVo.getPlace());
        goodsReq.setHasSpec(hasSpec ? "1" : "0");
        // 状态转换：正常->0，停用->1
        goodsReq.setStatus("停用".equals(goodsVo.getStatus()) ? "1" : "0");
        goodsReq.setRemark(goodsVo.getRemark());
        // 设置商品主表的最小库存和最大库存（与SKU保持一致）
        goodsReq.setMinStock(goodsVo.getMinStock() != null ? goodsVo.getMinStock() : 0);
        goodsReq.setMaxStock(goodsVo.getMaxStock() != null ? goodsVo.getMaxStock() : 999999);

        Long goodsId = goodsService.create(goodsReq);

        // 保存SKU
        if (hasSpec && !data.getSkuList().isEmpty()) {
            // 有规格商品，保存多个SKU
            for (WmsGoodsImportVo skuVo : data.getSkuList()) {
                saveSku(goodsId, goodsVo.getGoodsName(), unitId, skuVo, true);
            }
        } else {
            // 无规格商品，自动生成一条默认SKU
            if (StringUtils.isEmpty(goodsVo.getSkuName())) {
                goodsVo.setSkuName(goodsVo.getGoodsName());
            }
            saveSku(goodsId, goodsVo.getGoodsName(), unitId, goodsVo, false);
        }
    }

    /**
     * 库存阈值校验：最大库存必须大于最小库存
     */
    private void appendStockRangeError(WmsGoodsImportVo vo, List<ImportError> errors) {
        if (vo == null || vo.getMaxStock() == null || vo.getMinStock() == null) {
            return;
        }
        if (vo.getMaxStock() <= vo.getMinStock()) {
            ImportError error = new ImportError();
            error.setRowNumber(vo.getRowNumber());
            error.setErrorMessage("商品【" + vo.getGoodsName() + "】库存填写有误：最大库存必须大于最小库存");
            error.setRawData(toJsonString(vo));
            errors.add(error);
        }
    }

    private void appendTextLengthError(Integer rowNumber, String fieldName, String value, int maxLength,
                                       WmsGoodsImportVo vo, List<ImportError> errors) {
        if (StringUtils.isEmpty(value) || value.length() <= maxLength) {
            return;
        }

        ImportError error = new ImportError();
        error.setRowNumber(rowNumber);
        error.setErrorMessage(fieldName + "长度超限，最多" + maxLength + "个字符");
        error.setRawData(toJsonString(vo));
        errors.add(error);
    }

    /**
     * 保存SKU
     */
    private void saveSku(Long goodsId, String goodsName, Long unitId, WmsGoodsImportVo skuVo, boolean hasSpec) {
        WmsGoodsSkuSaveReqVO skuReq = new WmsGoodsSkuSaveReqVO();
        skuReq.setGoodsId(goodsId);
        skuReq.setUnitId(unitId);

        if (hasSpec) {
            // 有规格：使用导入的SKU信息
            skuReq.setBarcode(StringUtils.isNotEmpty(skuVo.getBarcode()) ? skuVo.getBarcode() : null);
            // SKU名称：商品名 + 规格值
            String skuName = goodsName + " " + skuVo.buildSkuName();
            skuReq.setSkuName(skuName.trim());
            skuReq.setSpecInfo(skuVo.buildSpecInfo());
        } else {
            // 无规格：生成默认SKU
            skuReq.setBarcode(StringUtils.isNotEmpty(skuVo.getBarcode()) ? skuVo.getBarcode() : null);
            skuReq.setSkuName(StringUtils.isNotEmpty(skuVo.getSkuName()) ? skuVo.getSkuName().trim() : goodsName);
            skuReq.setSpecInfo(null);
        }

        skuReq.setCostPrice(skuVo.getCostPrice());
        skuReq.setSalePrice(skuVo.getSalePrice());
        skuReq.setMaxStock(skuVo.getMaxStock() != null ? skuVo.getMaxStock() : 999999);
        skuReq.setMinStock(skuVo.getMinStock() != null ? skuVo.getMinStock() : 0);

        goodsSkuService.create(skuReq);
    }

    /**
     * 读取Excel文件（自动识别模板类型）
     */
    private List<WmsGoodsImportVo> readExcel(MultipartFile file, String mappingJson) throws IOException {
        List<WmsGoodsImportVo> dataList = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return dataList;
            }

            // 读取表头判断模板类型
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return dataList;
            }

            int columnCount = headerRow.getLastCellNum();
            // 简易模板：12列（商品编码、商品名称、SKU名称、分类名称、单位名称、品牌、产地、状态、成本价、销售价、最小库存、最大库存）
            // 完整模板：24列（商品编码、商品名称、分类名称、单位名称、品牌、产地、状态、备注 + SKU编码、SKU名称 + 5组规格名/值 + 成本价、销售价、库存、预警库存）

            boolean isSimpleTemplate = columnCount <= 12;
            Map<String, Integer> mappedSimpleIndex = resolveSimpleTemplateMapping(headerRow, mappingJson);

            // 读取数据行（跳过表头）
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                WmsGoodsImportVo vo = new WmsGoodsImportVo();
                vo.setRowNumber(rowIndex + 1);

                if (isSimpleTemplate) {
                    // 简易模板格式
                    // A: 商品条码, B: 商品名称, C: SKU名称(选填), D: 分类名称, E: 单位名称, F: 品牌, G: 产地, H: 状态, I: 成本价, J: 销售价, K: 最小库存, L: 最大库存
                    vo.setBarcode(getMappedString(row, mappedSimpleIndex, "barcode", 0));
                    vo.setGoodsName(getMappedString(row, mappedSimpleIndex, "goodsName", 1));
                    vo.setSkuName(getMappedString(row, mappedSimpleIndex, "skuName", -1));
                    vo.setCategoryName(getMappedString(row, mappedSimpleIndex, "categoryName", 2));
                    vo.setUnitName(getMappedString(row, mappedSimpleIndex, "unitName", 3));
                    vo.setBrand(getMappedString(row, mappedSimpleIndex, "brand", 4));
                    vo.setPlace(getMappedString(row, mappedSimpleIndex, "place", 5));
                    vo.setStatus(getMappedString(row, mappedSimpleIndex, "status", 6));
                    vo.setCostPrice(getMappedDecimal(row, mappedSimpleIndex, "costPrice", 7));
                    vo.setSalePrice(getMappedDecimal(row, mappedSimpleIndex, "salePrice", 8));
                    vo.setMinStock(getMappedInteger(row, mappedSimpleIndex, "minStock", 9));
                    vo.setMaxStock(getMappedInteger(row, mappedSimpleIndex, "maxStock", 10));
                } else {
                    // 完整模板格式
                    // A: 商品编码, B: 商品名称, C: 分类名称, D: 单位名称, E: 品牌, F: 产地, G: 状态, H: 备注
                    // I: 商品条码, J: SKU名称
                    // K: 规格名1, L: 规格值1, M: 规格名2, N: 规格值2, O: 规格名3, P: 规格值3, Q: 规格名4, R: 规格值4, S: 规格名5, T: 规格值5
                    // U: 成本价, V: 销售价, W: 最小库存, X: 最大库存
                    vo.setGoodsCode(getCellValueAsString(row, 0));
                    vo.setGoodsName(getCellValueAsString(row, 1));
                    vo.setCategoryName(getCellValueAsString(row, 2));
                    vo.setUnitName(getCellValueAsString(row, 3));
                    vo.setBrand(getCellValueAsString(row, 4));
                    vo.setPlace(getCellValueAsString(row, 5));
                    vo.setStatus(getCellValueAsString(row, 6));
                    vo.setRemark(getCellValueAsString(row, 7));
                    vo.setBarcode(getCellValueAsString(row, 8));
                    vo.setSkuName(getCellValueAsString(row, 9));
                    vo.setSpecName1(getCellValueAsString(row, 10));
                    vo.setSpecValue1(getCellValueAsString(row, 11));
                    vo.setSpecName2(getCellValueAsString(row, 12));
                    vo.setSpecValue2(getCellValueAsString(row, 13));
                    vo.setSpecName3(getCellValueAsString(row, 14));
                    vo.setSpecValue3(getCellValueAsString(row, 15));
                    vo.setSpecName4(getCellValueAsString(row, 16));
                    vo.setSpecValue4(getCellValueAsString(row, 17));
                    vo.setSpecName5(getCellValueAsString(row, 18));
                    vo.setSpecValue5(getCellValueAsString(row, 19));
                    vo.setCostPrice(getBigDecimalValue(row, 20));
                    vo.setSalePrice(getBigDecimalValue(row, 21));
                    vo.setMinStock(getIntegerValue(row, 22));
                    vo.setMaxStock(getIntegerValue(row, 23));
                }

                if (isTemplateNoteRow(vo)) {
                    continue;
                }

                dataList.add(vo);
            }
        }

        return dataList;
    }

    private Map<String, Integer> resolveSimpleTemplateMapping(Row headerRow, String mappingJson) {
        if (headerRow == null) {
            return Collections.emptyMap();
        }

        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            String key = normalizeHeader(getCellValueAsString(headerRow, i));
            if (StringUtils.isNotEmpty(key) && !headerIndexMap.containsKey(key)) {
                headerIndexMap.put(key, i);
            }
        }

        Map<String, ImportFieldMappingItem> explicitMapping = parseMappingJson(mappingJson);
        Map<String, Integer> mapped = new HashMap<>();
        for (GoodsSimpleFieldMeta meta : simpleFieldMetas()) {
            ImportFieldMappingItem item = explicitMapping.get(meta.fieldKey);
            Integer index = null;

            if (item != null) {
                if (StringUtils.isNotEmpty(item.getSourceHeader())) {
                    index = headerIndexMap.get(normalizeHeader(item.getSourceHeader()));
                }
                if (index != null) {
                    mapped.put(meta.fieldKey, index);
                    continue;
                }
                if (meta.required) {
                    throw new ServiceException("字段映射缺失: " + meta.systemFieldName);
                }
                mapped.put(meta.fieldKey, -1);
                continue;
            }

            index = headerIndexMap.get(normalizeHeader(meta.systemFieldName));
            if (index == null) {
                for (String alias : meta.aliases) {
                    index = headerIndexMap.get(normalizeHeader(alias));
                    if (index != null) {
                        break;
                    }
                }
            }

            if (meta.required && index == null) {
                throw new ServiceException("字段映射缺失: " + meta.systemFieldName);
            }
            if (index != null) {
                mapped.put(meta.fieldKey, index);
            }
        }

        return mapped;
    }

    private List<GoodsSimpleFieldMeta> simpleFieldMetas() {
        return Arrays.asList(
                GoodsSimpleFieldMeta.of("barcode", "商品条码", false, "条形码", "条码", "商品条形码"),
                GoodsSimpleFieldMeta.of("goodsName", "商品名称", true, "品名", "货品名称"),
                GoodsSimpleFieldMeta.of("skuName", "SKU名称", false, "SKU", "规格名称", "SKU名称(选填)"),
                GoodsSimpleFieldMeta.of("categoryName", "分类名称", true, "分类", "商品分类"),
                GoodsSimpleFieldMeta.of("unitName", "单位名称", true, "单位", "计量单位"),
                GoodsSimpleFieldMeta.of("brand", "品牌", false),
                GoodsSimpleFieldMeta.of("place", "产地", false, "产地/厂家", "厂家"),
                GoodsSimpleFieldMeta.of("status", "状态", false, "启用状态"),
                GoodsSimpleFieldMeta.of("costPrice", "成本价", false, "进货价"),
                GoodsSimpleFieldMeta.of("salePrice", "销售价", false, "售价", "零售价"),
                GoodsSimpleFieldMeta.of("minStock", "最小库存", false, "库存下限"),
                GoodsSimpleFieldMeta.of("maxStock", "最大库存", false, "库存上限")
        );
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

    private int getMappedIndex(Map<String, Integer> mappedIndex, String fieldKey, int defaultIndex) {
        if (mappedIndex == null || mappedIndex.isEmpty()) {
            return defaultIndex;
        }
        Integer mapped = mappedIndex.get(fieldKey);
        return mapped == null ? -1 : mapped;
    }

    private String getMappedString(Row row, Map<String, Integer> mappedIndex, String fieldKey, int defaultIndex) {
        return getCellValueAsString(row, getMappedIndex(mappedIndex, fieldKey, defaultIndex));
    }

    private BigDecimal getMappedDecimal(Row row, Map<String, Integer> mappedIndex, String fieldKey, int defaultIndex) {
        return getBigDecimalValue(row, getMappedIndex(mappedIndex, fieldKey, defaultIndex));
    }

    private Integer getMappedInteger(Row row, Map<String, Integer> mappedIndex, String fieldKey, int defaultIndex) {
        return getIntegerValue(row, getMappedIndex(mappedIndex, fieldKey, defaultIndex));
    }

    private static class GoodsSimpleFieldMeta {
        private final String fieldKey;
        private final String systemFieldName;
        private final boolean required;
        private final List<String> aliases;

        private GoodsSimpleFieldMeta(String fieldKey, String systemFieldName, boolean required, List<String> aliases) {
            this.fieldKey = fieldKey;
            this.systemFieldName = systemFieldName;
            this.required = required;
            this.aliases = aliases;
        }

        private static GoodsSimpleFieldMeta of(String fieldKey, String systemFieldName, boolean required, String... aliases) {
            return new GoodsSimpleFieldMeta(fieldKey, systemFieldName, required, Arrays.asList(aliases));
        }
    }

    /**
     * 判断行是否为空
     */
    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(row, i);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 模板中会包含一行说明文本，导入时应忽略该行。
     */
    private boolean isTemplateNoteRow(WmsGoodsImportVo vo) {
        if (vo == null) {
            return false;
        }

        String firstCol = StringUtils.trimToEmpty(vo.getGoodsCode());
        if (StringUtils.isEmpty(firstCol)) {
            firstCol = StringUtils.trimToEmpty(vo.getBarcode());
        }
        if (!(firstCol.startsWith("【说明】") || firstCol.startsWith("说明"))) {
            return false;
        }

        return StringUtils.isEmpty(vo.getGoodsName())
                && StringUtils.isEmpty(vo.getCategoryName())
                && StringUtils.isEmpty(vo.getUnitName());
    }

    /**
     * 获取单元格值（字符串）
     */
    private String getCellValueAsString(Row row, int columnIndex) {
        if (columnIndex < 0) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numValue = cell.getNumericCellValue();
                if (numValue == (long) numValue) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /**
     * 获取单元格值（BigDecimal）
     */
    private BigDecimal getBigDecimalValue(Row row, int columnIndex) {
        if (columnIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return new BigDecimal(String.valueOf(cell.getNumericCellValue()));
        } else if (cell.getCellType() == CellType.STRING) {
            String strValue = cell.getStringCellValue().trim();
            if (!strValue.isEmpty()) {
                try {
                    return new BigDecimal(strValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 获取单元格值（Integer）
     */
    private Integer getIntegerValue(Row row, int columnIndex) {
        if (columnIndex < 0) {
            return null;
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            String strValue = cell.getStringCellValue().trim();
            if (!strValue.isEmpty()) {
                try {
                    return Integer.parseInt(strValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * 根据分类名称获取分类ID
     */
    private Long getCategoryidByName(String categoryName) {
        if (StringUtils.isEmpty(categoryName)) {
            return null;
        }

        WmsGoodsCategory category = categoryMapper.selectOne(
                new LambdaQueryWrapper<WmsGoodsCategory>()
                        .eq(WmsGoodsCategory::getCategoryName, categoryName.trim())
        );
        return category != null ? category.getId() : null;
    }

    /**
     * 根据单位名称获取单位ID
     */
    private Long getUnitIdByName(String unitName) {
        if (StringUtils.isEmpty(unitName)) {
            return null;
        }

        WmsUnit unit = unitMapper.selectOne(
                new LambdaQueryWrapper<WmsUnit>()
                        .eq(WmsUnit::getUnitName, unitName.trim())
        );
        return unit != null ? unit.getId() : null;
    }

    /**
     * 对象转JSON字符串
     */
    private String toJsonString(Object obj) {
        try {
            return com.alibaba.fastjson2.JSON.toJSONString(obj);
        } catch (Exception e) {
            return "";
        }
    }

    private String buildImportFailMessage(Exception ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        String detail = StringUtils.defaultIfEmpty(root.getMessage(), ex.getMessage());
        if (StringUtils.isEmpty(detail)) {
            detail = ex.getClass().getSimpleName();
        }

        if (detail.length() > 300) {
            detail = detail.substring(0, 300) + "...";
        }

        return "导入失败，已回滚全部数据：" + detail;
    }

    @Override
    public void generateImportTemplate(HttpServletResponse response, String templateType) throws IOException {
        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook()) {

            if ("simple".equals(templateType)) {
                // 生成简易模板（无规格）
                generateSimpleTemplate(workbook);
            } else {
                // 生成完整模板（有规格）
                generateFullTemplate(workbook);
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "simple".equals(templateType) ? "商品导入模板_简易版" : "商品导入模板_完整版";
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

            // 写入输出流
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 生成简易模板（无规格）
     */
    private void generateSimpleTemplate(Workbook workbook) {
        Sheet sheet = workbook.createSheet("商品导入模板_简易版");

        // 查询所有分类和单位用于下拉列表
        List<String> categoryNames = getAllCategoryNames();
        List<String> unitNames = getAllUnitNames();

        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook, true);
        CellStyle contentStyle = createContentStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle integerStyle = createIntegerStyle(workbook);

        // 表头（必填项用红色背景）
        String[] headers = {
            "商品条码", "商品名称*", "SKU名称", "分类名称*", "单位名称*", "品牌", "产地", "状态",
            "成本价", "销售价", "最小库存", "最大库存"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 设置列宽
        int[] widths = {15, 20, 20, 15, 12, 12, 12, 10, 12, 12, 10, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }

        // 创建下拉列表数据验证
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // 分类名称下拉列表（D列，第2行到第1000行）
        if (!categoryNames.isEmpty()) {
            DataValidationConstraint categoryConstraint = validationHelper.createExplicitListConstraint(categoryNames.toArray(new String[0]));
            CellRangeAddressList categoryRange = new CellRangeAddressList(1, 1000, 3, 3);
            DataValidation categoryValidation = validationHelper.createValidation(categoryConstraint, categoryRange);
            categoryValidation.setShowErrorBox(true);
            categoryValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            categoryValidation.createErrorBox("输入错误", "请从下拉列表中选择分类名称");
            sheet.addValidationData(categoryValidation);
        }

        // 单位名称下拉列表（E列，第2行到第1000行）
        if (!unitNames.isEmpty()) {
            DataValidationConstraint unitConstraint = validationHelper.createExplicitListConstraint(unitNames.toArray(new String[0]));
            CellRangeAddressList unitRange = new CellRangeAddressList(1, 1000, 4, 4);
            DataValidation unitValidation = validationHelper.createValidation(unitConstraint, unitRange);
            unitValidation.setShowErrorBox(true);
            unitValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            unitValidation.createErrorBox("输入错误", "请从下拉列表中选择单位名称");
            sheet.addValidationData(unitValidation);
        }

        // 状态下拉列表（H列，第2行到第1000行）
        String[] statusOptions = {"正常", "停用"};
        DataValidationConstraint statusConstraint = validationHelper.createExplicitListConstraint(statusOptions);
        CellRangeAddressList statusRange = new CellRangeAddressList(1, 1000, 7, 7);
        DataValidation statusValidation = validationHelper.createValidation(statusConstraint, statusRange);
        statusValidation.setShowErrorBox(true);
        statusValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        statusValidation.createErrorBox("输入错误", "请从下拉列表中选择状态：正常/停用");
        sheet.addValidationData(statusValidation);

        // 示例数据
        String templateCategoryName = categoryNames.isEmpty() ? "请先添加分类" : categoryNames.get(0);
        String templateUnitName = unitNames.isEmpty() ? "请先添加单位" : unitNames.get(0);

        Object[][] examples = {
            {"6901234567890", "iPhone 15", "iPhone 15", templateCategoryName, templateUnitName, "Apple", "中国", "正常", 5999.00, 6999.00, 10, 100},
            {"6901234567891", "小米14", "小米14", templateCategoryName, templateUnitName, "小米", "中国", "正常", 3499.00, 3999.00, 20, 200},
            {"", "华为Mate60", "华为Mate60", templateCategoryName, templateUnitName, "华为", "中国", "停用", 4999.00, 5999.00, 5, 0}
        };

        for (int i = 0; i < examples.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < examples[i].length; j++) {
                Cell cell = row.createCell(j);
                Object value = examples[i][j];
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                    // 成本价、销售价使用数字格式，库存使用整数格式
                    if (j == 8 || j == 9) {
                        cell.setCellStyle(numberStyle);
                    } else if (j == 10 || j == 11) {
                        cell.setCellStyle(integerStyle);
                    } else {
                        cell.setCellStyle(contentStyle);
                    }
                } else {
                    cell.setCellValue((String) value);
                    cell.setCellStyle(contentStyle);
                }
            }
        }

        // 添加说明
        Row noteRow = sheet.createRow(examples.length + 2);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("【说明】带*号为必填项。商品编码由系统自动生成，无需填写。商品条码为选填，填写后会导入到SKU条码字段。SKU名称为选填；不填时默认使用商品名称。分类、单位、状态请从下拉列表选择。库存列：最小库存（选填，默认0）、最大库存（选填，默认9999）。此模板适用于无规格商品。");
        sheet.addMergedRegion(new CellRangeAddress(examples.length + 2, examples.length + 2, 0, headers.length - 1));
    }

    /**
     * 生成完整模板（有规格）
     */
    private void generateFullTemplate(Workbook workbook) {
        Sheet sheet = workbook.createSheet("商品导入模板_完整版");

        // 查询所有分类和单位用于下拉列表
        List<String> categoryNames = getAllCategoryNames();
        List<String> unitNames = getAllUnitNames();

        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook, true);
        CellStyle contentStyle = createContentStyle(workbook);
        CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle integerStyle = createIntegerStyle(workbook);

        // 表头
        String[] headers = {
                "商品编码", "商品名称*", "分类名称*", "单位名称*", "品牌", "产地", "状态", "备注",
                "商品条码", "SKU名称",
                "规格名1", "规格值1", "规格名2", "规格值2", "规格名3", "规格值3", "规格名4", "规格值4", "规格名5", "规格值5",
                "成本价", "销售价", "最小库存", "最大库存"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 设置列宽
        int[] widths = {12, 15, 12, 10, 10, 10, 8, 15, 12, 20, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }

        // 创建下拉列表数据验证
        DataValidationHelper validationHelper = sheet.getDataValidationHelper();

        // 分类名称下拉列表（C列，第2行到第1000行）
        if (!categoryNames.isEmpty()) {
            DataValidationConstraint categoryConstraint = validationHelper.createExplicitListConstraint(categoryNames.toArray(new String[0]));
            CellRangeAddressList categoryRange = new CellRangeAddressList(1, 1000, 2, 2);
            DataValidation categoryValidation = validationHelper.createValidation(categoryConstraint, categoryRange);
            categoryValidation.setShowErrorBox(true);
            categoryValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            categoryValidation.createErrorBox("输入错误", "请从下拉列表中选择分类名称");
            sheet.addValidationData(categoryValidation);
        }

        // 单位名称下拉列表（D列，第2行到第1000行）
        if (!unitNames.isEmpty()) {
            DataValidationConstraint unitConstraint = validationHelper.createExplicitListConstraint(unitNames.toArray(new String[0]));
            CellRangeAddressList unitRange = new CellRangeAddressList(1, 1000, 3, 3);
            DataValidation unitValidation = validationHelper.createValidation(unitConstraint, unitRange);
            unitValidation.setShowErrorBox(true);
            unitValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
            unitValidation.createErrorBox("输入错误", "请从下拉列表中选择单位名称");
            sheet.addValidationData(unitValidation);
        }

        // 状态下拉列表（G列，第2行到第1000行）
        String[] statusOptions = {"正常", "停用"};
        DataValidationConstraint statusConstraint = validationHelper.createExplicitListConstraint(statusOptions);
        CellRangeAddressList statusRange = new CellRangeAddressList(1, 1000, 6, 6);
        DataValidation statusValidation = validationHelper.createValidation(statusConstraint, statusRange);
        statusValidation.setShowErrorBox(true);
        statusValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        statusValidation.createErrorBox("输入错误", "请从下拉列表中选择状态：正常/停用");
        sheet.addValidationData(statusValidation);

        // 示例数据
        String templateCategoryName = categoryNames.isEmpty() ? "请先添加分类" : categoryNames.get(0);
        String templateUnitName = unitNames.isEmpty() ? "请先添加单位" : unitNames.get(0);

        Object[][] examples = {
            {"", "iPhone 15", templateCategoryName, templateUnitName, "Apple", "中国", "正常", "热销商品", "6901234567890", "", "颜色", "黑色", "容量", "128G", "", "", "", "", "", "", 5999.00, 6999.00, 10, 100},
            {"", "iPhone 15", templateCategoryName, templateUnitName, "Apple", "中国", "正常", "", "6901234567891", "", "颜色", "黑色", "容量", "256G", "", "", "", "", "", "", 6499.00, 7499.00, 5, 50},
            {"", "iPhone 15", templateCategoryName, templateUnitName, "Apple", "中国", "正常", "", "6901234567892", "", "颜色", "白色", "容量", "128G", "", "", "", "", "", "", 5999.00, 6999.00, 10, 80},
            {"", "小米14", templateCategoryName, templateUnitName, "小米", "中国", "正常", "", "6901234567893", "", "颜色", "黑色", "容量", "256G", "", "", "", "", "", "", 3499.00, 3999.00, 20, 200},
            {"", "小米14", templateCategoryName, templateUnitName, "小米", "中国", "正常", "", "6901234567894", "", "颜色", "白色", "容量", "512G", "", "", "", "", "", "", 3999.00, 4499.00, 15, 150}
        };

        for (int i = 0; i < examples.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < examples[i].length; j++) {
                Cell cell = row.createCell(j);
                Object value = examples[i][j];
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                    // 成本价、销售价使用数字格式，库存使用整数格式
                    if (j == 20 || j == 21) {
                        cell.setCellStyle(numberStyle);
                    } else if (j == 22 || j == 23) {
                        cell.setCellStyle(integerStyle);
                    } else {
                        cell.setCellStyle(contentStyle);
                    }
                } else {
                    cell.setCellValue((String) value);
                    cell.setCellStyle(contentStyle);
                }
            }
        }

        // 添加说明
        Row noteRow = sheet.createRow(examples.length + 2);
        Cell noteCell = noteRow.createCell(0);
        noteCell.setCellValue("【说明】带*号为必填项。商品编码由系统自动生成，无需填写。同一商品的多行数据使用相同商品名称。商品条码为选填，填写后会导入到SKU条码字段。规格名/规格值最多5组，按需填写。分类、单位、状态请从下拉列表选择。多规格商品的库存列：最小库存（选填，默认0）、最大库存（选填，默认9999）。");
        sheet.addMergedRegion(new CellRangeAddress(examples.length + 2, examples.length + 2, 0, headers.length - 1));
    }

    /**
     * 获取所有分类名称列表
     */
    private List<String> getAllCategoryNames() {
        List<WmsGoodsCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<WmsGoodsCategory>()
                        .eq(WmsGoodsCategory::getStatus, "0")
                        .orderByAsc(WmsGoodsCategory::getOrderNum)
        );
        return categories.stream()
                .map(WmsGoodsCategory::getCategoryName)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有单位名称列表
     */
    private List<String> getAllUnitNames() {
        List<WmsUnit> units = unitMapper.selectList(
                new LambdaQueryWrapper<WmsUnit>()
                        .eq(WmsUnit::getStatus, "0")
                        .orderByDesc(WmsUnit::getIsDefault)
                        .orderByAsc(WmsUnit::getId)
        );
        return units.stream()
                .map(WmsUnit::getUnitName)
                .collect(Collectors.toList());
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook, boolean required) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        if (required) {
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
        } else {
            style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建内容样式
     */
    private CellStyle createContentStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 创建数字格式样式（保留两位小数）
     */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    /**
     * 创建整数格式样式
     */
    private CellStyle createIntegerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }

    private String resolveTemplateCategoryName() {
        WmsGoodsCategory category = categoryMapper.selectOne(
                new LambdaQueryWrapper<WmsGoodsCategory>()
                        .eq(WmsGoodsCategory::getStatus, "0")
                        .orderByAsc(WmsGoodsCategory::getOrderNum)
                        .last("LIMIT 1")
        );
        return category != null ? category.getCategoryName() : "请填写已存在分类";
    }

    private String resolveTemplateUnitName() {
        WmsUnit unit = unitMapper.selectOne(
                new LambdaQueryWrapper<WmsUnit>()
                        .eq(WmsUnit::getStatus, "0")
                        .orderByDesc(WmsUnit::getIsDefault)
                        .orderByAsc(WmsUnit::getId)
                        .last("LIMIT 1")
        );
        return unit != null ? unit.getUnitName() : "请填写已存在单位";
    }

    @Override
    public List<String> validateImportData(WmsGoodsImportVo importVo) {
        List<String> errors = new ArrayList<>();

        // 商品名称必填
        if (StringUtils.isEmpty(importVo.getGoodsName())) {
            errors.add("商品名称不能为空");
        }

        // 分类名称必填
        if (StringUtils.isEmpty(importVo.getCategoryName())) {
            errors.add("分类名称不能为空");
        }

        // 单位名称必填
        if (StringUtils.isEmpty(importVo.getUnitName())) {
            errors.add("单位名称不能为空");
        }

        // 价格校验
        if (importVo.getCostPrice() != null && importVo.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("成本价不能为负数");
        }
        if (importVo.getSalePrice() != null && importVo.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("销售价不能为负数");
        }

        // 库存校验
        if (importVo.getMaxStock() != null && importVo.getMaxStock() < 0) {
            errors.add("最大库存不能为负数");
        }
        if (importVo.getMinStock() != null && importVo.getMinStock() < 0) {
            errors.add("最小库存不能为负数");
        }

        return errors;
    }
}