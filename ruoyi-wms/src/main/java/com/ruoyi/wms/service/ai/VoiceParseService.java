package com.ruoyi.wms.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.config.AiProperties;
import com.ruoyi.wms.domain.vo.ai.VoiceParseResultVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsCustomerService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsSupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 语音解析服务 - 整合ASR和LLM
 *
 * @author ruoyi
 */
@Service
public class VoiceParseService {

    private static final Logger log = LoggerFactory.getLogger(VoiceParseService.class);
    private static final Pattern NATURAL_QTY_UNIT_PATTERN = Pattern.compile("(?<qty>\\d+(?:\\.\\d+)?)\\s*(?<unit>个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张)?");
    private static final Pattern NATURAL_PRICE_PATTERN = Pattern.compile("(?:单价|价格|每个|每件|每箱|每盒|每包|每台|每套|每条|每瓶)?\\s*(?<price>\\d+(?:\\.\\d+)?)\\s*元");

    @Autowired
    private DashScopeAsrService asrService;

    @Autowired
    private QwenLlmService llmService;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsSupplierService supplierService;

    @Autowired
    private WmsCustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiProperties aiProperties;

    /**
     * 解析语音文件
     *
     * @param audioFile 音频文件
     * @return 解析结果
     */
    public VoiceParseResultVO parseVoice(MultipartFile audioFile) {
        VoiceParseResultVO result = new VoiceParseResultVO();

        try {
            // 1. ASR语音转文字
            log.info("开始语音识别...");
            String recognizedText = asrService.recognize(audioFile);
            if (recognizedText == null || recognizedText.trim().isEmpty()) {
                result.setSuccess(false);
                result.setErrorMessage("语音识别失败，未能识别到文字");
                return result;
            }

            result.setRecognizedText(recognizedText);
            log.info("语音识别结果: {}", recognizedText);


            // 2. LLM解析语义
            log.info("开始语义解析...");
            String llmResponse = llmService.parseVoiceText(recognizedText);
            log.info("LLM解析结果: {}", llmResponse);

            // 3. 解析JSON结果
            return parseLlmResponse(llmResponse, recognizedText);

        } catch (Exception e) {
            log.error("语音解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage("语音解析失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 解析文本（直接传入文字，跳过ASR）
     *
     * @param text 输入文本
     * @return 解析结果
     */
    public VoiceParseResultVO parseText(String text) {
        VoiceParseResultVO result = new VoiceParseResultVO();

        try {
            result.setRecognizedText(text);

            // 截图识别场景：前置视觉模型已返回结构化 JSON，直接复用，跳过文本模型解析
            VoiceParseResultVO visionResult = tryParseImageRecognition(text);
            if (visionResult != null) {
                return visionResult;
            }


            // 回退LLM解析语义
            String llmResponse = llmService.parseVoiceText(text);
            log.info("LLM解析结果: {}", llmResponse);

            return parseLlmResponse(llmResponse, text);

        } catch (Exception e) {
            log.error("文本解析失败", e);
            result.setSuccess(false);
            result.setErrorMessage("文本解析失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 截图识别结果文本前缀。与 {@code MobileGoodsParseHelper.IMAGE_RECOGNITION_PREFIX} 保持一致。
     */
    public static final String IMAGE_RECOGNITION_PREFIX = "截图识别结果: ";

    /**
     * 尝试将截图识别（视觉模型）结果文本直接转换为语义解析结果，避免再次调用文本模型。
     * <p>视觉模型输出格式：
     * <pre>{"supplier":"...","items":[{"goodsName":"水饺","quantity":100,"unit":"件","price":null}],"remark":"..."}</pre>
     *
     * @return 解析成功返回结果；非截图场景或无法解析时返回 null（由调用方走原有流程）
     */
    private VoiceParseResultVO tryParseImageRecognition(String text) {
        if (text == null || !text.startsWith(IMAGE_RECOGNITION_PREFIX)) {
            return null;
        }
        String json = text.substring(IMAGE_RECOGNITION_PREFIX.length()).trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }
        if (!json.startsWith("{") && !json.startsWith("[")) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("error")) {
                log.warn("视觉模型识别失败: {}", root.path("error").asText(null));
                return null;
            }

            VoiceParseResultVO result = new VoiceParseResultVO();
            result.setRecognizedText(text);

            // 视觉模型用 supplier 字段同时承载供应商/客户名称，这里同时填入供应商与客户，
            // 由下游按单据域（采购取 supplier、销售取 customer）各取所需。
            String partnerName = root.path("supplier").isNull() ? null : root.path("supplier").asText(null);
            if (StringUtils.isNotBlank(partnerName)) {
                VoiceParseResultVO.PartnerInfo supplier = new VoiceParseResultVO.PartnerInfo();
                supplier.setName(partnerName.trim());
                matchSupplier(supplier);
                result.setSupplier(supplier);

                VoiceParseResultVO.PartnerInfo customer = new VoiceParseResultVO.PartnerInfo();
                customer.setName(partnerName.trim());
                matchCustomer(customer);
                result.setCustomer(customer);
            }

            String remark = root.path("remark").isNull() ? null : root.path("remark").asText(null);
            if (StringUtils.isNotBlank(remark)) {
                result.setRemark(remark.trim());
            }

            JsonNode itemsNode = root.has("items") ? root.get("items") : root;
            List<VoiceParseResultVO.GoodsItem> goods = new ArrayList<>();
            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    String name = firstNonBlank(itemNode.path("goodsName").asText(null), itemNode.path("name").asText(null));
                    if (StringUtils.isBlank(name)) {
                        continue;
                    }
                    VoiceParseResultVO.GoodsItem item = new VoiceParseResultVO.GoodsItem();
                    item.setName(name.trim());
                    String qty = firstNonBlank(itemNode.path("quantity").asText(null), itemNode.path("qty").asText(null));
                    if (StringUtils.isNotBlank(qty)) {
                        try {
                            item.setQuantity(new BigDecimal(qty.trim()));
                        } catch (NumberFormatException ignored) {
                            // 数量无法解析则留空，由下游补问
                        }
                    }
                    String unit = itemNode.path("unit").isNull() ? null : itemNode.path("unit").asText(null);
                    if (StringUtils.isNotBlank(unit)) {
                        item.setUnit(unit.trim());
                    }
                    String price = itemNode.path("price").isNull() ? null : itemNode.path("price").asText(null);
                    if (StringUtils.isNotBlank(price)) {
                        try {
                            item.setPrice(new BigDecimal(price.trim()));
                        } catch (NumberFormatException ignored) {
                            // 单价无法解析则留空
                        }
                    }
                    goods.add(item);
                }
            }
            if (goods.isEmpty()) {
                log.warn("截图识别结果未提取到有效商品，回退原有解析流程: {}", json);
                return null;
            }
            result.setGoods(goods);
            result.setSuccess(true);
            log.info("截图识别复用视觉模型结果(PC)，跳过文本模型解析，商品数={}", goods.size());
            return result;
        } catch (Exception e) {
            log.warn("截图识别结果解析失败，回退原有解析流程: {}", json, e);
            return null;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private boolean isIntentConsistentWithText(String text, String operationType) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(operationType)) {
            return true;
        }
        String textOperationType = inferOperationTypeFromText(text);
        return StringUtils.isBlank(textOperationType) || StringUtils.equals(textOperationType, operationType);
    }

    private String inferOperationTypeFromText(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String normalized = text.replace(" ", "");
        if (normalized.contains("采购") || normalized.contains("进货") || normalized.contains("订货") || normalized.contains("补货")) {
            return "purchase";
        }
        if (normalized.contains("销售") || normalized.contains("卖给") || normalized.contains("销给")) {
            return "sales";
        }
        if (normalized.contains("盘点") || normalized.contains("调拨") || normalized.contains("移库")) {
            return "stock";
        }
        return null;
    }

    private void inferOperationType(VoiceParseResultVO result) {
        if (StringUtils.isNotBlank(result.getOperationType())) {
            return;
        }
        if (result.getCustomer() != null) {
            result.setOperationType("sales");
            return;
        }
        if (result.getSupplier() != null) {
            result.setOperationType("purchase");
        }
    }

    private boolean hasStructuredData(VoiceParseResultVO result) {
        return result.getSupplier() != null
                || result.getCustomer() != null
                || StringUtils.isNotBlank(result.getWarehouse())
            || StringUtils.isNotBlank(result.getOutWarehouse())
            || StringUtils.isNotBlank(result.getInWarehouse())
                || StringUtils.isNotBlank(result.getRemark())
                || (result.getGoods() != null && !result.getGoods().isEmpty());
    }

    private boolean isSemanticResultAccepted(VoiceParseResultVO result, String originalText, boolean hasReliableSignal) {
        if (!hasReliableSignal) {
            return false;
        }
        String operationType = result == null ? null : result.getOperationType();
        if ("error".equalsIgnoreCase(operationType)) {
            return false;
        }
        return isIntentConsistentWithText(originalText, operationType);
    }

    /**
     * 解析LLM返回的JSON
     */
    private VoiceParseResultVO parseLlmResponse(String llmResponse, String originalText) {
        VoiceParseResultVO result = new VoiceParseResultVO();
        result.setRecognizedText(originalText);

        // 检查LLM响应是否为空
        if (llmResponse == null || llmResponse.trim().isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("AI服务返回为空，请检查API配置或使用备用解析");
            log.warn("LLM响应为空，原始文本: {}", originalText);
            return result;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(llmResponse);

            if (jsonNode.has("error")) {
                result.setOperationType(jsonNode.path("operationType").asText(null));
                result.setSuccess(false);
                result.setErrorMessage(jsonNode.path("error").asText("LLM调用失败"));
                log.warn("LLM解析返回错误: {}, 原始文本: {}", result.getErrorMessage(), originalText);
                return result;
            }

            // 操作类型
            String operationType = null;
            if (jsonNode.has("operationType")) {
                operationType = jsonNode.get("operationType").asText();
                result.setOperationType(operationType);
            }

            // 商品列表
            if (jsonNode.has("goods")) {
                JsonNode goodsArray = jsonNode.get("goods");
                List<VoiceParseResultVO.GoodsItem> goodsList = new ArrayList<>();

                if (goodsArray.isArray()) {
                    for (JsonNode goodsNode : goodsArray) {
                        VoiceParseResultVO.GoodsItem item = new VoiceParseResultVO.GoodsItem();

                        if (goodsNode.has("name")) {
                            item.setName(goodsNode.get("name").asText());
                            // 尝试匹配已有商品，传入操作类型以确定使用哪个价格
                            matchGoods(item, operationType);
                        }

                        if (goodsNode.has("barcode")) {
                            item.setBarcode(goodsNode.get("barcode").asText());
                        }

                        if (goodsNode.has("spec")) {
                            item.setSpec(goodsNode.get("spec").asText());
                        }

                        if (goodsNode.has("quantity")) {
                            item.setQuantity(parseDecimal(goodsNode, "quantity"));
                        }

                        if (goodsNode.has("unit")) {
                            item.setUnit(goodsNode.get("unit").asText());
                        }

                        if (goodsNode.has("price")) {
                            item.setPrice(parseDecimal(goodsNode, "price"));
                        }

                        if (goodsNode.has("amount")) {
                            item.setAmount(parseDecimal(goodsNode, "amount"));
                        }

                        // 计算金额
                        if (item.getAmount() == null && item.getQuantity() != null && item.getPrice() != null) {
                            item.setAmount(item.getQuantity().multiply(item.getPrice()));
                        }

                        goodsList.add(item);
                    }
                    result.setGoods(goodsList);
                }
            }

            // 供应商
            if (jsonNode.has("supplier")) {
                JsonNode supplierNode = jsonNode.get("supplier");
                if (supplierNode.has("name")) {
                    VoiceParseResultVO.PartnerInfo supplier = new VoiceParseResultVO.PartnerInfo();
                    supplier.setName(supplierNode.get("name").asText());
                    // 尝试匹配已有供应商
                    matchSupplier(supplier);
                    result.setSupplier(supplier);
                }
            }

            // 客户
            if (jsonNode.has("customer")) {
                JsonNode customerNode = jsonNode.get("customer");
                if (customerNode.has("name")) {
                    VoiceParseResultVO.PartnerInfo customer = new VoiceParseResultVO.PartnerInfo();
                    customer.setName(customerNode.get("name").asText());
                    // 尝试匹配已有客户
                    matchCustomer(customer);
                    result.setCustomer(customer);
                }
            }

            // 仓库
            if (jsonNode.has("warehouse")) {
                result.setWarehouse(jsonNode.get("warehouse").asText());
            }

            // 备注
            if (jsonNode.has("remark")) {
                String remark = jsonNode.get("remark").asText();
                log.info("=== LLM解析 remark: {}", remark);
                result.setRemark(remark);
            }

            inferOperationType(result);
            boolean hasStructuredData = hasStructuredData(result);
            boolean pass = isSemanticResultAccepted(
                    result,
                    originalText,
                    hasStructuredData || StringUtils.isNotBlank(result.getOperationType())
            );
            result.setSuccess(pass);
            if (!result.getSuccess()) {
                result.setErrorMessage("LLM未返回可靠结构化结果");
            }
            log.info("=== LLM解析结果 success: {}, remark: {}", result.getSuccess(), result.getRemark());

        } catch (Exception e) {
            log.error("解析LLM响应JSON失败", e);
            result.setSuccess(false);
            result.setErrorMessage("解析结果格式错误: " + e.getMessage());
        }

        return result;
    }

    /**
     * 匹配已有商品
     *
     * @param item 商品项
     * @param operationType 操作类型（purchase=采购用采购价，sales=销售用销售价）
     */
    private void matchGoods(VoiceParseResultVO.GoodsItem item, String operationType) {
        try {
            // 优先通过条码匹配
            if (item.getBarcode() != null) {
                WmsGoodsSkuRespVO sku = goodsSkuService.getByBarcode(item.getBarcode());
                if (sku != null) {
                    item.setSkuId(sku.getId());
                    item.setIsNew(false);
                    // 补充商品信息
                    if (item.getName() == null) {
                        item.setName(sku.getSkuName());
                    }
                    if (item.getSpec() == null) {
                        item.setSpec(sku.getSpecInfo());
                    }
                    if (item.getUnit() == null) {
                        item.setUnit(sku.getUnitName());
                    }
                    // 根据操作类型使用正确的价格
                    if (item.getPrice() == null) {
                        item.setPrice(getPriceByOperationType(sku, operationType));
                    }
                    return;
                }
            }

            // 通过商品名称模糊匹配
            if (item.getName() != null) {
                List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(item.getName(), 20);
                if (skus != null && !skus.isEmpty()) {
                    String inputName = item.getName().trim();
                    log.debug("开始匹配商品: {}", inputName);

                    // 1. 优先精确匹配（完全相同）
                    for (WmsGoodsSkuRespVO sku : skus) {
                        if (inputName.equals(sku.getSkuName()) || inputName.equals(sku.getGoodsName())) {
                            log.info("商品精确匹配成功: {}", inputName);
                            item.setSkuId(sku.getId());
                            item.setIsNew(false);
                            if (item.getSpec() == null) {
                                item.setSpec(sku.getSpecInfo());
                            }
                            if (item.getUnit() == null) {
                                item.setUnit(sku.getUnitName());
                            }
                            if (item.getPrice() == null) {
                                item.setPrice(getPriceByOperationType(sku, operationType));
                            }
                            return;
                        }
                    }

                    // 2. 包含匹配（商品名包含输入或输入包含商品名）
                    for (WmsGoodsSkuRespVO sku : skus) {
                        String skuName = sku.getSkuName();
                        String goodsName = sku.getGoodsName();
                        if (skuName.contains(inputName) || inputName.contains(skuName)
                                || (goodsName != null && (goodsName.contains(inputName) || inputName.contains(goodsName)))) {
                            log.info("商品包含匹配成功: 输入[{}] 匹配到[{}]", inputName, skuName);
                            item.setSkuId(sku.getId());
                            item.setIsNew(false);
                            if (item.getSpec() == null) {
                                item.setSpec(sku.getSpecInfo());
                            }
                            if (item.getUnit() == null) {
                                item.setUnit(sku.getUnitName());
                            }
                            if (item.getPrice() == null) {
                                item.setPrice(getPriceByOperationType(sku, operationType));
                            }
                            return;
                        }
                    }

                    // 3. 相似度匹配（标准化后的模糊匹配）
                    String normalizedInput = normalizeName(inputName);
                    log.debug("标准化输入名称: {} -> {}", inputName, normalizedInput);

                    WmsGoodsSkuRespVO bestMatch = null;
                    double bestSimilarity = 0.0;
                    double threshold = 0.50; // 相似度阈值50%（进一步降低以提高匹配成功率）

                    for (WmsGoodsSkuRespVO sku : skus) {
                        String skuName = normalizeName(sku.getSkuName());
                        String goodsName = normalizeName(sku.getGoodsName());
                        double skuSimilarity = calculateSimilarity(normalizedInput, skuName);
                        double goodsSimilarity = calculateSimilarity(normalizedInput, goodsName);
                        double similarity = Math.max(skuSimilarity, goodsSimilarity);
                        log.debug("比较: {} vs skuName={}({}) goodsName={}({}), 取最高相似度: {}",
                                normalizedInput, skuName, skuSimilarity, goodsName, goodsSimilarity, similarity);

                        if (similarity > bestSimilarity) {
                            bestSimilarity = similarity;
                            bestMatch = sku;
                        }
                    }

                    // 只有相似度达到阈值才使用匹配结果
                    if (bestMatch != null && bestSimilarity >= threshold) {
                        log.info("商品相似度匹配成功: 输入[{}] 匹配到[{}] 相似度={}", item.getName(), bestMatch.getSkuName(), bestSimilarity);
                        item.setSkuId(bestMatch.getId());
                        item.setIsNew(false);
                        // 补充信息
                        if (item.getSpec() == null) {
                            item.setSpec(bestMatch.getSpecInfo());
                        }
                        if (item.getUnit() == null) {
                            item.setUnit(bestMatch.getUnitName());
                        }
                        // 根据操作类型使用正确的价格
                        if (item.getPrice() == null) {
                            item.setPrice(getPriceByOperationType(bestMatch, operationType));
                        }
                        return;
                    } else {
                        log.warn("商品匹配失败: 输入[{}] 最高相似度={} 未达到阈值{}", item.getName(), bestSimilarity, threshold);
                    }
                }
            }

            // 没有匹配到，标记为新商品
            log.info("未找到匹配商品，标记为新商品: {}", item.getName());
            item.setIsNew(true);

        } catch (Exception e) {
            log.warn("匹配商品失败: {}", item.getName(), e);
            item.setIsNew(true);
        }
    }

    /**
     * 标准化商品名称（去除空格、特殊字符，统一大小写）
     *
     * @param name 原始名称
     * @return 标准化后的名称
     */
    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        // 去除所有空格、转小写、去除全角字符
        return name.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[\\p{Punct}\\p{Space}]", "");
    }

    /**
     * 计算两个字符串的相似度（使用编辑距离算法）
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度 0.0~1.0
     */
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0.0;
        }
        if (str1.equals(str2)) {
            return 1.0;
        }
        if (str1.isEmpty() || str2.isEmpty()) {
            return 0.0;
        }

        int maxLength = Math.max(str1.length(), str2.length());
        int distance = levenshteinDistance(str1, str2);

        // 相似度 = 1 - (编辑距离 / 最大长度)
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * 计算编辑距离（Levenshtein距离）
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 编辑距离
     */
    private int levenshteinDistance(String str1, String str2) {
        int m = str1.length();
        int n = str2.length();

        int[][] dp = new int[m + 1][n + 1];

        // 初始化第一行和第一列
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        // 动态规划计算
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j], dp[i][j - 1]),
                            dp[i - 1][j - 1]
                    ) + 1;
                }
            }
        }

        return dp[m][n];
    }

    /**
     * 根据操作类型获取对应的价格
     *
     * @param sku 商品SKU
     * @param operationType 操作类型
     * @return 对应的价格
     */
    private BigDecimal getPriceByOperationType(WmsGoodsSkuRespVO sku, String operationType) {
        if ("sales".equals(operationType) || "stock_out".equals(operationType)) {
            // 销售出库使用销售价
            return sku.getSalePrice();
        } else {
            // 采购入库使用采购价
            return sku.getPurchasePrice();
        }
    }

    /**
     * 匹配供应商
     */
    private void matchSupplier(VoiceParseResultVO.PartnerInfo supplier) {
        try {
            Long id = supplierService.findIdByName(supplier.getName());
            if (id != null) {
                supplier.setId(id);
                supplier.setIsNew(false);
            } else {
                supplier.setIsNew(true);
            }
        } catch (Exception e) {
            log.warn("匹配供应商失败: {}", supplier.getName(), e);
            supplier.setIsNew(true);
        }
    }

    /**
     * 匹配客户
     */
    private void matchCustomer(VoiceParseResultVO.PartnerInfo customer) {
        try {
            Long id = customerService.findIdByName(customer.getName());
            if (id != null) {
                customer.setId(id);
                customer.setIsNew(false);
            } else {
                customer.setIsNew(true);
            }
        } catch (Exception e) {
            log.warn("匹配客户失败: {}", customer.getName(), e);
            customer.setIsNew(true);
        }
    }

    /**
     * 解析Decimal
     */
    private BigDecimal parseDecimal(JsonNode node, String field) {
        if (!node.has(field)) {
            return null;
        }
        JsonNode fieldNode = node.get(field);
        if (fieldNode.isNumber()) {
            return fieldNode.decimalValue();
        }
        if (fieldNode.isTextual()) {
            try {
                return new BigDecimal(fieldNode.asText());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
