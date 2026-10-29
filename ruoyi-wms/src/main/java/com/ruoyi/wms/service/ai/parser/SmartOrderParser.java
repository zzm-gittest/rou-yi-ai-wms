package com.ruoyi.wms.service.ai.parser;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsUnit;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.service.WmsCustomerService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsUnitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 智能订单解析服务
 * 从语音/文本中提取结构化数据，支持意图识别和信息匹配
 *
 * @author ruoyi
 */
@Service
public class SmartOrderParser {

    private static final Logger log = LoggerFactory.getLogger(SmartOrderParser.class);

    @Autowired
    private WmsSupplierService supplierService;

    @Autowired
    private WmsCustomerService customerService;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsUnitService unitService;

    // ==================== 意图识别 ====================

    /**
     * 检测用户意图
     */
    public IntentType detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return IntentType.UNKNOWN;
        }

        String msg = message.toLowerCase().trim();

        // 采购入库关键词
        if (containsAny(msg, "入库", "采购入库", "进货入库", "采购", "进货", "买", "购入")) {
            return IntentType.PURCHASE_ORDER;
        }

        // 销售出库关键词
        if (containsAny(msg, "出库", "销售出库", "发货出库", "出货", "销售", "卖", "卖给")) {
            return IntentType.SALES_ORDER;
        }

        // 库存查询关键词
        if (containsAny(msg, "库存", "查库存", "库存查询", "库存有多少", "还有多少", "库存预警", "低库存")) {
            return IntentType.QUERY_STOCK;
        }

        // 商品查询关键词
        if (containsAny(msg, "商品", "查商品", "查询商品", "找商品", "有没有", "查找", "搜索", "看看有什么", "查一下")) {
            return IntentType.QUERY_GOODS;
        }

        return IntentType.UNKNOWN;
    }

    /**
     * 检测字符串是否包含任意关键词
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 订单解析 ====================

    /**
     * 解析订单信息
     */
    public ParsedOrderResult parseOrder(String message) {
        if (message == null || message.trim().isEmpty()) {
            return ParsedOrderResult.builder()
                    .intentType(IntentType.UNKNOWN)
                    .complete(false)
                    .originalMessage(message)
                    .build();
        }

        IntentType intentType = detectIntent(message);
        String operationType = intentType.getCode();

        ParsedOrderResult result = ParsedOrderResult.builder()
                .intentType(intentType)
                .operationType(operationType)
                .originalMessage(message)
                .goods(new ArrayList<>())
                .missingFields(new ArrayList<>())
                .build();

        // 未知意图直接返回
        if (intentType == IntentType.UNKNOWN) {
            return result;
        }

        // 查询类意图，提取查询关键词
        if (intentType == IntentType.QUERY_GOODS || intentType == IntentType.QUERY_STOCK) {
            result.setQueryKeyword(extractQueryKeyword(message));
            return result;
        }

        // 订单类意图，解析详细信息
        parseOrderDetails(message, result);

        // 判断是否是完整订单
        result.setComplete(isCompleteOrder(result));

        return result;
    }

    /**
     * 解析订单详情
     */
    private void parseOrderDetails(String message, ParsedOrderResult result) {
        String remaining = message;

        // 提取供应商/客户
        if (result.getIntentType() == IntentType.PURCHASE_ORDER) {
            remaining = extractSupplier(remaining, result);
        } else if (result.getIntentType() == IntentType.SALES_ORDER) {
            remaining = extractCustomer(remaining, result);
        }

        // 提取商品信息（支持多个商品）
        extractGoodsItems(remaining, result);
    }

    /**
     * 判断是否是完整订单
     */
    private boolean isCompleteOrder(ParsedOrderResult result) {
        boolean isComplete = true;

        if (!result.hasGoods()) {
            result.getMissingFields().add("商品");
            isComplete = false;
        } else {
            for (ParsedOrderResult.GoodsItem item : result.getGoods()) {
                if (item.getName() == null || item.getName().isEmpty()) {
                    result.getMissingFields().add("商品名称");
                    isComplete = false;
                }
                if (item.getQuantity() == null || item.getQuantity() <= 0) {
                    result.getMissingFields().add("数量");
                    isComplete = false;
                }
                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    result.getMissingFields().add("单价");
                    isComplete = false;
                }
            }
        }

        return isComplete;
    }

    /**
     * 构建缺少字段的提示信息
     */
    public String buildMissingFieldsPrompt(ParsedOrderResult result) {
        if (result.getMissingFields().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ **信息不完整**，请补充以下内容：\n\n");

        for (String field : result.getMissingFields()) {
            sb.append("• ").append(field).append("\n");
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("💡 **您可以直接说一句话包含所有信息**：\n");

        if (result.getIntentType() == IntentType.PURCHASE_ORDER) {
            sb.append("• \"入库 5台iPhone，1000元，一鸣供应商\"\n");
            sb.append("• \"入库 10箱可乐，50元\"\n");
        } else if (result.getIntentType() == IntentType.SALES_ORDER) {
            sb.append("• \"出库 10箱可乐，500元，华联超市\"\n");
            sb.append("• \"出库 5台iPhone，6000元\"\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return sb.toString();
    }

    // ==================== 信息提取 ====================

    /**
     * 提取查询关键词
     */
    public String extractQueryKeyword(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }

        String keyword = message;

        // 移除查询关键词
        keyword = keyword.replaceAll("(查询|查一下|查|找一下|找|看看|有没有|是否有|搜索|检索)", "");
        keyword = keyword.replaceAll("(的商品|商品|产品|物品|库存|的库存)", "");
        keyword = keyword.replaceAll("(全部|所有|预警|不足|低库存)", "");
        keyword = keyword.replaceAll("(入库|采购入库|进货入库|采购|进货|买|出库|销售出库|发货出库|销售|卖|出货)", "");

        return keyword.trim();
    }

    /**
     * 提取供应商
     */
    private String extractSupplier(String message, ParsedOrderResult result) {
        // 尝试匹配 "供应商XXX" 或 "XXX供应商" 格式
        Pattern pattern = Pattern.compile("供应商[是为]?([^，。,\\s]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            result.setSupplier(matcher.group(1).trim());
            return message.replace(matcher.group(), "").trim();
        }

        // 尝试匹配 "，XXX" 或 "，给XXX" 格式（通常在末尾）
        pattern = Pattern.compile("[，,]\\s*([\\u4e00-\\u9fa5a-zA-Z]{2,10})\\s*$");
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            // 排除常见的非供应商词
            if (!name.matches("(元|块|件|箱|个|台|瓶|包|袋|盒|套|条|斤)")) {
                result.setSupplier(name);
                return message.substring(0, matcher.start()).trim();
            }
        }

        return message;
    }

    /**
     * 提取客户
     */
    private String extractCustomer(String message, ParsedOrderResult result) {
        // 尝试匹配 "给XXX" 或 "客户XXX" 格式
        Pattern pattern = Pattern.compile("给\\s*([^，。,\\s]+)");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            result.setCustomer(matcher.group(1).trim());
            return message.replace(matcher.group(), "").trim();
        }

        pattern = Pattern.compile("客户[是为]?([^，。,\\s]+)");
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            result.setCustomer(matcher.group(1).trim());
            return message.replace(matcher.group(), "").trim();
        }

        return message;
    }

    /**
     * 提取商品项（支持多商品）
     */
    private void extractGoodsItems(String message, ParsedOrderResult result) {
        // 先移除意图关键词
        String remaining = message.replaceAll("(入库|采购入库|进货入库|采购|进货|买|出库|销售出库|发货出库|销售|卖|出货)", "");
        remaining = remaining.replaceAll("(供应商|客户)[^，。,\\s]*", "");
        remaining = remaining.trim();

        // 提取数量+单位+商品名+价格 的组合
        // 格式1: "5台iPhone 1000元"
        // 格式2: "iPhone 5台 1000元"
        // 格式3: "5个苹果 50元"

        // 尝试提取商品信息
        ParsedOrderResult.GoodsItem item = new ParsedOrderResult.GoodsItem();

        // 提取价格
        Pattern pricePattern = Pattern.compile("[¥￥]?(\\d+\\.?\\d*)\\s*元?");
        Matcher priceMatcher = pricePattern.matcher(remaining);
        if (priceMatcher.find()) {
            try {
                item.setPrice(new BigDecimal(priceMatcher.group(1)));
                remaining = remaining.replace(priceMatcher.group(), "").trim();
            } catch (NumberFormatException e) {
                log.warn("解析价格失败: {}", priceMatcher.group(1));
            }
        }

        // 提取数量+单位+商品名 组合
        // 格式: 数字+单位+商品名 或 商品名+数字+单位
        Pattern quantityUnitPattern = Pattern.compile("(\\d+)\\s*([\\u4e00-\\u9fa5a-zA-Z]{1,5})\\s*([\\u4e00-\\u9fa5a-zA-Z0-9]+)");
        Matcher matcher = quantityUnitPattern.matcher(remaining);
        if (matcher.find()) {
            item.setQuantity(Integer.parseInt(matcher.group(1)));
            String possibleUnit = matcher.group(2);
            String possibleName = matcher.group(3);

            // 判断第二部分是单位还是商品名的一部分
            if (isUnit(possibleUnit)) {
                item.setUnit(possibleUnit);
                item.setName(possibleName);
            } else {
                // 可能是商品名的一部分
                item.setName(possibleUnit + possibleName);
            }
            remaining = remaining.replace(matcher.group(), "").trim();
        } else {
            // 尝试另一种格式: 商品名 + 数字 + 单位
            Pattern nameQuantityPattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z0-9]+)\\s*(\\d+)\\s*([\\u4e00-\\u9fa5a-zA-Z]{1,5})");
            matcher = nameQuantityPattern.matcher(remaining);
            if (matcher.find()) {
                item.setName(matcher.group(1));
                item.setQuantity(Integer.parseInt(matcher.group(2)));
                String possibleUnit = matcher.group(3);
                if (isUnit(possibleUnit)) {
                    item.setUnit(possibleUnit);
                } else {
                    item.setName(item.getName() + possibleUnit);
                }
                remaining = remaining.replace(matcher.group(), "").trim();
            } else {
                // 只提取商品名
                Pattern namePattern = Pattern.compile("([\\u4e00-\\u9fa5a-zA-Z0-9]{2,20})");
                matcher = namePattern.matcher(remaining);
                if (matcher.find()) {
                    item.setName(matcher.group(1));
                }
            }
        }

        // 计算金额
        if (item.getPrice() != null && item.getQuantity() != null) {
            item.setAmount(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        result.addGoods(item);
    }

    /**
     * 判断是否是单位
     */
    private boolean isUnit(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // 常见单位列表
        String[] units = {"件", "箱", "个", "台", "瓶", "包", "袋", "盒", "套", "条", "斤", "kg", "克", "L", "ml", "支", "把", "张", "本", "册"};
        for (String unit : units) {
            if (text.equalsIgnoreCase(unit)) {
                return true;
            }
        }
        return false;
    }

    // ==================== 数据匹配 ====================

    /**
     * 匹配商品（返回单个最佳匹配）
     */
    public WmsGoodsSkuRespVO matchGoods(String goodsName) {
        if (goodsName == null || goodsName.trim().isEmpty()) {
            return null;
        }

        try {
            List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(goodsName.trim(), 5);
            if (skus != null && !skus.isEmpty()) {
                // 返回第一个匹配的商品
                return skus.get(0);
            }
        } catch (Exception e) {
            log.warn("匹配商品失败: {}", goodsName, e);
        }
        return null;
    }

    /**
     * 匹配商品列表（返回最新3条，用于用户选择）
     */
    public List<WmsGoodsSkuRespVO> matchGoodsList(String goodsName) {
        if (goodsName == null || goodsName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 搜索并返回最新3条
            List<WmsGoodsSkuRespVO> skus = goodsSkuService.searchByName(goodsName.trim(), 3);
            return skus != null ? skus : new ArrayList<>();
        } catch (Exception e) {
            log.warn("匹配商品列表失败: {}", goodsName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 匹配供应商（返回单个最佳匹配）
     */
    public WmsSupplier matchSupplier(String supplierName) {
        if (supplierName == null || supplierName.trim().isEmpty()) {
            return null;
        }

        try {
            List<WmsSupplier> suppliers = supplierService.list(
                    Wrappers.<WmsSupplier>lambdaQuery()
                            .like(WmsSupplier::getSupplierName, supplierName.trim())
                            .last("LIMIT 1")
            );
            if (suppliers != null && !suppliers.isEmpty()) {
                return suppliers.get(0);
            }
        } catch (Exception e) {
            log.warn("匹配供应商失败: {}", supplierName, e);
        }
        return null;
    }

    /**
     * 匹配供应商列表（返回最新3条，用于用户选择）
     */
    public List<WmsSupplier> matchSupplierList(String supplierName) {
        if (supplierName == null || supplierName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 按创建时间倒序，返回最新3条
            List<WmsSupplier> suppliers = supplierService.list(
                    Wrappers.<WmsSupplier>lambdaQuery()
                            .like(WmsSupplier::getSupplierName, supplierName.trim())
                            .orderByDesc(WmsSupplier::getCreateTime)
                            .last("LIMIT 3")
            );
            return suppliers != null ? suppliers : new ArrayList<>();
        } catch (Exception e) {
            log.warn("匹配供应商列表失败: {}", supplierName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 匹配客户（返回单个最佳匹配）
     */
    public WmsCustomer matchCustomer(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return null;
        }

        try {
            List<WmsCustomer> customers = customerService.list(
                    Wrappers.<WmsCustomer>lambdaQuery()
                            .like(WmsCustomer::getCustomerName, customerName.trim())
                            .last("LIMIT 1")
            );
            if (customers != null && !customers.isEmpty()) {
                return customers.get(0);
            }
        } catch (Exception e) {
            log.warn("匹配客户失败: {}", customerName, e);
        }
        return null;
    }

    /**
     * 匹配客户列表（返回最新3条，用于用户选择）
     */
    public List<WmsCustomer> matchCustomerList(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 按创建时间倒序，返回最新3条
            List<WmsCustomer> customers = customerService.list(
                    Wrappers.<WmsCustomer>lambdaQuery()
                            .like(WmsCustomer::getCustomerName, customerName.trim())
                            .orderByDesc(WmsCustomer::getCreateTime)
                            .last("LIMIT 3")
            );
            return customers != null ? customers : new ArrayList<>();
        } catch (Exception e) {
            log.warn("匹配客户列表失败: {}", customerName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 匹配或创建单位（模糊匹配，找不到就自动创建）
     */
    public WmsUnit matchOrAddUnit(String unitName) {
        if (unitName == null || unitName.trim().isEmpty()) {
            return null;
        }

        String name = unitName.trim();

        try {
            // 先模糊匹配
            List<WmsUnit> units = unitService.list(
                    Wrappers.<WmsUnit>lambdaQuery()
                            .eq(WmsUnit::getUnitName, name)
                            .or()
                            .like(WmsUnit::getUnitName, name)
                            .last("LIMIT 1")
            );

            if (units != null && !units.isEmpty()) {
                return units.get(0);
            }

            // 没找到，自动创建
            WmsUnit newUnit = new WmsUnit();
            newUnit.setUnitName(name);
            newUnit.setStatus("0"); // 正常状态
            unitService.save(newUnit);

            log.info("自动创建单位成功: {}", name);
            return newUnit;

        } catch (Exception e) {
            log.error("匹配或创建单位失败: {}", name, e);
            return null;
        }
    }

    // ==================== 引导提示 ====================

    /**
     * 构建引导提示响应
     */
    public String buildGuidanceMessage() {
        return "🤔 抱歉，我没有理解您的意思。\n\n" +
                "您可以这样说或输入：\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                "📦 **创建订单**：\n" +
                "   • \"采购5台iPhone，1000元，一鸣供应商\"\n" +
                "   • \"销售10箱可乐给华联超市，500元\"\n\n" +
                "🔍 **查询商品**：\n" +
                "   • \"商品\" 或 \"查询冰红茶\"\n" +
                "   • \"有没有iPhone\"\n\n" +
                "📊 **查询库存**：\n" +
                "   • \"库存\" 或 \"iPhone库存\"\n" +
                "   • \"冰红茶还有多少\"\n" +
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    }
}
