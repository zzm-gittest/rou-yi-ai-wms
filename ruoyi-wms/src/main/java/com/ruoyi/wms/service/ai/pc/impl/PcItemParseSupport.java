package com.ruoyi.wms.service.ai.pc.impl;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.ai.VoiceParseResultVO;
import com.ruoyi.wms.service.ai.VoiceParseService;
import com.ruoyi.wms.service.ai.pc.vo.ItemDraft;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class PcItemParseSupport {

    private static final Pattern SUPPLIER_AFTER_PREPOSITION = Pattern.compile("(?:向|从)(.+?)(?:采购|买|进货)");
    private static final Pattern SUPPLIER_BY_LABEL = Pattern.compile("供应商(?:是|为)?[:：]?(.+?)(?:[，,。；;\\s]|$)");
    private static final Pattern CUSTOMER_AFTER_SALE = Pattern.compile("(?:卖给|给)(.+?)(?:发货|出库|出售|卖|退回)");
    private static final Pattern CUSTOMER_BY_LABEL = Pattern.compile("客户(?:是|为)?[:：]?(.+?)(?:[，,。；;\\s]|$)");
    private static final Pattern LOCAL_PRICE_PATTERN = Pattern.compile("(?:单价|价格)(?:是|为)?\\s*(?<price>\\d+(?:\\.\\d+)?)\\s*元|(?<price2>\\d+(?:\\.\\d+)?)\\s*元");
    private static final Pattern LOCAL_QTY_PATTERN = Pattern.compile("(?<qty>\\d+(?:\\.\\d+)?)\\s*(?<unit>个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张|打)?");
    private static final Pattern LINK_ORDER_PATTERN = Pattern.compile("(?<no>[A-Z]{2,}[A-Z0-9-]{4,}|[A-Z]{1,}[0-9]{6,})");
    private static final Pattern PRICE_ONLY_SEGMENT = Pattern.compile("^(?:单价|价格)?(?:是|为)?\\s*\\d+(?:\\.\\d+)?\\s*元?$", Pattern.CASE_INSENSITIVE);

    /**
     * 截图识别结果 JSON 解析器（Jackson ObjectMapper 线程安全，可静态复用）
     */
    private static final com.fasterxml.jackson.databind.ObjectMapper IMAGE_JSON_PARSER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    private final PcMultiBillChatServiceImpl owner;

    PcItemParseSupport(PcMultiBillChatServiceImpl owner) {
        this.owner = owner;
    }

    List<ItemDraft> parseItems(String input) {
        if (StringUtils.isNotBlank(input) && input.startsWith(VoiceParseService.IMAGE_RECOGNITION_PREFIX)) {
            return new ArrayList<>();
        }
        String normalized = input.replace("；", ";").replace("，", ",");
        String[] lines = normalized.split(";");
        List<ItemDraft> list = new ArrayList<>();
        for (String line : lines) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String[] arr = line.trim().split(",");
            if (arr.length < 3) {
                continue;
            }
            try {
                ItemDraft item = new ItemDraft();
                item.name = arr[0].trim();
                item.quantity = new BigDecimal(arr[1].trim());
                item.price = new BigDecimal(arr[2].trim());
                list.add(item);
            } catch (Exception ignored) {
                // ignore
            }
        }
        if (!list.isEmpty()) {
            return list;
        }
        list.addAll(parseNaturalItems(input));
        if (!list.isEmpty()) {
            return list;
        }
        try {
            VoiceParseResultVO parsed = owner.voiceParseService.parseText(input);
            if (parsed != null && parsed.getGoods() != null) {
                for (VoiceParseResultVO.GoodsItem goodsItem : parsed.getGoods()) {
                    if (goodsItem == null || StringUtils.isBlank(goodsItem.getName())
                            || goodsItem.getQuantity() == null || goodsItem.getPrice() == null) {
                        continue;
                    }
                    ItemDraft item = new ItemDraft();
                    item.name = goodsItem.getName();
                    item.unit = goodsItem.getUnit();
                    item.quantity = goodsItem.getQuantity();
                    item.price = goodsItem.getPrice();
                    list.add(item);
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return list;
    }

    List<ItemDraft> parseNaturalItems(String input) {
        List<ItemDraft> list = new ArrayList<>();
        if (StringUtils.isBlank(input)) {
            return list;
        }
        String normalized = input.replace("；", ";").replace("，", ";").replace("、", ";").replace("。", "").trim();
        String[] segments = normalized.split(";");
        for (String segment : segments) {
            ItemDraft draft = parseNaturalItem(segment);
            if (draft != null) {
                list.add(draft);
            }
        }
        return list;
    }

    ItemDraft parseNaturalItem(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        Matcher priceMatcher = LOCAL_PRICE_PATTERN.matcher(input);
        if (!priceMatcher.find()) {
            return null;
        }
        String priceText = StringUtils.isNotBlank(priceMatcher.group("price"))
                ? priceMatcher.group("price") : priceMatcher.group("price2");
        if (StringUtils.isBlank(priceText)) {
            return null;
        }

        String beforePrice = input.substring(0, priceMatcher.start());
        Matcher qtyMatcher = LOCAL_QTY_PATTERN.matcher(beforePrice);
        Matcher chosenQtyMatcher = null;
        while (qtyMatcher.find()) {
            chosenQtyMatcher = qtyMatcher;
        }
        if (chosenQtyMatcher == null) {
            return null;
        }

        String goodsName = cleanupNaturalGoodsName(beforePrice.substring(0, chosenQtyMatcher.start()));
        if (shouldIgnoreGoodsSegment(input, goodsName)) {
            return null;
        }

        ItemDraft draft = new ItemDraft();
        draft.name = goodsName;
        draft.unit = chosenQtyMatcher.group("unit");
        draft.quantity = new BigDecimal(chosenQtyMatcher.group("qty"));
        draft.price = new BigDecimal(priceText);
        return draft;
    }

    String cleanupNaturalGoodsName(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String name = raw.trim();
        name = name.replaceAll("(?:向|从|跟)\\s*[^，,。\\d采买进货]{2,30}\\s*(?:采购|买|进货)", "").trim();
        name = name.replaceAll("供应商(?:是|为)?[:：]?\\s*[^，,。\\d]{2,30}", "").trim();
        name = name.replaceAll("采购订单|采购入库|采购单|采购|进货|买|下单|创建|新增|帮我|入库|到货", "").trim();
        name = name.replaceAll("^[，,：:\\s]+|[，,：:\\s]+$", "").trim();
        name = name.replaceAll("\\s+", "");
        return name;
    }

    boolean hasLinkedSourceIntent(String input) {
        if (StringUtils.isBlank(input)) {
            return false;
        }
        return input.contains("关联")
                || input.contains("根据")
                || input.contains("来源单")
                || input.contains("按采购单")
                || input.contains("按订单")
                || input.contains("按入库单")
                || input.contains("按出库单")
                || input.contains("从采购单")
                || input.contains("从销售单")
                || input.contains("从入库单")
                || input.contains("从出库单");
    }

    boolean shouldIgnoreGoodsSegment(String rawSegment, String goodsName) {
        if (StringUtils.isBlank(goodsName)) {
            return true;
        }
        String compactSegment = rawSegment == null
                ? ""
                : rawSegment.replace("，", "").replace(",", "").replace("；", "").replace(";", "").replace("。", "").replaceAll("\\s+", "");
        String compactGoods = goodsName.replaceAll("\\s+", "");
        if (StringUtils.isBlank(compactGoods)) {
            return true;
        }
        if (PRICE_ONLY_SEGMENT.matcher(compactSegment).matches()) {
            return true;
        }
        if (compactSegment.matches("^每(?:个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张)?\\d+(?:\\.\\d+)?元$")) {
            return true;
        }
        if (hasLinkedSourceIntent(rawSegment) && StringUtils.isNotBlank(extractLinkOrderNo(rawSegment))) {
            return true;
        }
        if (compactGoods.matches("^(?:单价|价格).*$")) {
            return true;
        }
        if (compactGoods.contains("仓库") || compactGoods.endsWith("仓")) {
            return true;
        }
        return compactGoods.startsWith("按") && StringUtils.isNotBlank(extractLinkOrderNo(rawSegment));
    }

    String cleanupGoodsName(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String name = cleanupNaturalGoodsName(raw);
        if (StringUtils.isBlank(name)) {
            name = raw.trim();
        }
        name = name.replaceAll("销售订单|销售出库|销售退货|出库单|退货单|盘点单|调拨单|创建|新增|帮我|商品|货品", "").trim();
        name = name.replaceAll("^[，,：:;；\\s]+|[，,：:;；\\s]+$", "").trim();
        name = name.replaceAll("\\s+", "");
        return name;
    }

    boolean hasIncompleteItemDrafts(List<ItemDraft> drafts) {
        return hasIncompleteItemDrafts(drafts, true);
    }

    boolean hasIncompleteItemDrafts(List<ItemDraft> drafts, boolean requirePrice) {
        if (drafts == null || drafts.isEmpty()) {
            return true;
        }
        for (ItemDraft draft : drafts) {
            if (draft == null || StringUtils.isBlank(draft.name) || draft.quantity == null || (requirePrice && draft.price == null)) {
                return true;
            }
        }
        return false;
    }

    String inferUnitByGoodsName(String input, String goodsName) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(goodsName)) {
            return null;
        }
        String normalized = input.replace("，", ",").replace("；", ";").replace("、", ",");
        Pattern pattern = Pattern.compile(Pattern.quote(goodsName) + "\\s*\\d+(?:\\.\\d+)?\\s*(个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张|打)");
        Matcher matcher = pattern.matcher(normalized);
        if (matcher.find()) {
            return matcher.group(1);
        }
        String compact = normalized.replace(" ", "");
        String compactGoods = goodsName.replace(" ", "");
        for (String segment : compact.split("[;,]")) {
            if (StringUtils.isBlank(segment) || !segment.contains(compactGoods)) {
                continue;
            }
            Matcher qtyMatcher = LOCAL_QTY_PATTERN.matcher(segment);
            while (qtyMatcher.find()) {
                String unit = qtyMatcher.group("unit");
                if (StringUtils.isNotBlank(unit)) {
                    return unit;
                }
            }
        }
        return null;
    }

    /**
     * 将截图识别（视觉模型）结果 JSON 直接转换为商品明细。
     *
     * @return 非截图识别输入返回 null（由调用方走原有解析流程）；
     *         返回空列表表示是截图输入但未提取到有效商品（由调用方提示格式错误）
     */
    List<ItemDraft> parseImageRecognitionItems(String input) {
        if (StringUtils.isBlank(input) || !input.startsWith(VoiceParseService.IMAGE_RECOGNITION_PREFIX)) {
            return null;
        }
        String json = input.substring(VoiceParseService.IMAGE_RECOGNITION_PREFIX.length()).trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "").trim();
        }
        List<ItemDraft> list = new ArrayList<>();
        try {
            if (!json.startsWith("{") && !json.startsWith("[")) {
                return list;
            }
            com.fasterxml.jackson.databind.JsonNode root = IMAGE_JSON_PARSER.readTree(json);
            if (root.has("error")) {
                return list;
            }
            com.fasterxml.jackson.databind.JsonNode itemsNode = root.has("items") ? root.get("items") : root;
            if (itemsNode != null && itemsNode.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode itemNode : itemsNode) {
                    String name = firstNonBlank(
                            textOrNull(itemNode, "goodsName"),
                            textOrNull(itemNode, "name"));
                    if (StringUtils.isBlank(name)) {
                        continue;
                    }
                    ItemDraft item = new ItemDraft();
                    item.name = name.trim();
                    item.unit = textOrNull(itemNode, "unit");
                    String qty = firstNonBlank(
                            textOrNull(itemNode, "quantity"),
                            textOrNull(itemNode, "qty"));
                    if (StringUtils.isNotBlank(qty)) {
                        try {
                            item.quantity = new BigDecimal(qty.trim());
                        } catch (NumberFormatException ignored) {
                            // 数量无法解析则留空，由下游补问
                        }
                    }
                    String price = textOrNull(itemNode, "price");
                    if (StringUtils.isNotBlank(price)) {
                        try {
                            item.price = new BigDecimal(price.trim());
                        } catch (NumberFormatException ignored) {
                            // 单价无法解析则留空
                        }
                    }
                    list.add(item);
                }
            }
        } catch (Exception e) {
            // JSON 解析失败，返回空列表让调用方给出格式提示
        }
        return list;
    }

    private static String textOrNull(com.fasterxml.jackson.databind.JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field) == null || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText(null);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a;
        }
        return (b != null && !b.trim().isEmpty()) ? b : null;
    }

    List<ItemDraft> parseItemsAllowPriceOptional(String input) {
        // 截图识别结果直接转商品明细（允许缺单价，由下游补问）
        List<ItemDraft> imageItems = parseImageRecognitionItems(input);
        if (imageItems != null) {
            return imageItems;
        }
        List<ItemDraft> structuredItems = parseStructuredItemsAllowPrice(input);
        if (!structuredItems.isEmpty()) {
            return structuredItems;
        }

        List<ItemDraft> itemsWithPrice = parseItems(input);
        if (!itemsWithPrice.isEmpty()) {
            long chineseSeparators = input == null ? 0 : input.chars().filter(ch -> ch == '，' || ch == '、' || ch == '；').count();
            if (!(chineseSeparators >= 2 && itemsWithPrice.size() == 1)) {
                return itemsWithPrice;
            }
        }
        List<ItemDraft> result = new ArrayList<>();
        if (StringUtils.isBlank(input)) {
            return result;
        }
        String normalized = input.replace("；", ";").replace("，", ";").replace("、", ";").replace("。", "").trim();
        for (String segment : normalized.split(";")) {
            ItemDraft item = parseQtyOnlyItem(segment);
            if (item != null) {
                result.add(item);
            }
        }
        if (result.isEmpty()) {
            return itemsWithPrice;
        }
        Map<String, ItemDraft> pricedByName = itemsWithPrice.stream()
                .filter(draft -> draft != null && StringUtils.isNotBlank(draft.name) && draft.price != null)
                .collect(Collectors.toMap(
                        draft -> draft.name,
                        draft -> draft,
                        (left, right) -> left
                ));
        for (ItemDraft item : result) {
            if (item == null || StringUtils.isBlank(item.name) || item.price != null) {
                continue;
            }
            ItemDraft priced = pricedByName.get(item.name);
            if (priced != null) {
                item.price = priced.price;
            }
        }
        // Extract price from "每台X元" style segments when items have no price
        boolean hasUnpriced = result.stream().anyMatch(d -> d != null && d.price == null);
        if (hasUnpriced) {
            for (String segment : normalized.split(";")) {
                Matcher perUnitPrice = Pattern.compile("每[个件箱盒包台套条瓶克斤升只支米张打]?(\\d+(?:\\.\\d+)?)\\s*元").matcher(segment);
                if (perUnitPrice.find()) {
                    BigDecimal price = new BigDecimal(perUnitPrice.group(1));
                    for (ItemDraft item : result) {
                        if (item != null && item.price == null) {
                            item.price = price;
                        }
                    }
                    break;
                }
            }
        }
        return result;
    }

    List<ItemDraft> parseStructuredItemsAllowPrice(String input) {
        List<ItemDraft> list = new ArrayList<>();
        if (StringUtils.isBlank(input)) {
            return list;
        }
        String normalized = input.replace("；", ";").trim();
        String[] segments = normalized.split(";");
        for (String segment : segments) {
            if (StringUtils.isBlank(segment)) {
                continue;
            }
            String[] arr = segment.trim().split(",");
            if (arr.length < 2) {
                return new ArrayList<>();
            }
            try {
                ItemDraft item = new ItemDraft();
                item.name = arr[0].trim();
                String qtyToken = arr[1].trim();
                Matcher qtyMatcher = LOCAL_QTY_PATTERN.matcher(qtyToken);
                if (!qtyMatcher.matches() || StringUtils.isBlank(qtyMatcher.group("qty"))) {
                    return new ArrayList<>();
                }
                item.quantity = new BigDecimal(qtyMatcher.group("qty"));
                item.unit = qtyMatcher.group("unit");
                if (arr.length >= 3 && StringUtils.isNotBlank(arr[2].trim())) {
                    item.price = new BigDecimal(arr[2].trim());
                }
                if (StringUtils.isBlank(item.name) || item.quantity == null) {
                    return new ArrayList<>();
                }
                list.add(item);
            } catch (Exception ex) {
                return new ArrayList<>();
            }
        }
        return list;
    }

    List<ItemDraft> parseNameOnlyItems(String input) {
        List<ItemDraft> result = new ArrayList<>();
        if (StringUtils.isBlank(input) || input.startsWith(VoiceParseService.IMAGE_RECOGNITION_PREFIX)) {
            return result;
        }
        String normalized = input.replace("盘点", "")
                .replace("所有商品", "")
                .replace("全部商品", "")
                .replace("一号仓库", "")
                .replace("二号仓库", "")
                .replace("华南仓", "")
                .replace("全盘", "")
                .replace("抽盘", "")
                .replace("、", ";")
                .replace("，", ";")
                .replace(",", ";")
                .replace("。", "")
                .trim();
        for (String name : normalized.split(";")) {
            String goodsName = cleanupGoodsName(name);
            if (StringUtils.isBlank(goodsName)) {
                continue;
            }
            ItemDraft draft = new ItemDraft();
            draft.name = goodsName;
            result.add(draft);
        }
        return result;
    }

    ItemDraft parseQtyOnlyItem(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        Matcher qtyMatcher = LOCAL_QTY_PATTERN.matcher(input);
        Integer chosenStart = null;
        Integer chosenEnd = null;
        String qtyText = null;
        String unitText = null;
        while (qtyMatcher.find()) {
            chosenStart = qtyMatcher.start();
            chosenEnd = qtyMatcher.end();
            qtyText = qtyMatcher.group("qty");
            unitText = qtyMatcher.group("unit");
        }
        if (chosenStart == null || chosenEnd == null) {
            return null;
        }
        if (StringUtils.isBlank(qtyText)) {
            return null;
        }
        String raw;
        String beforeQty = input.substring(0, chosenStart);
        String afterQty = input.substring(chosenEnd);
        // When qty is between supplier prefix and goods name (e.g., "退给云腾科技5台坏的苹果笔记本"),
        // removing qty concatenates supplier + goods, causing the supplier strip regex to swallow everything.
        // Detect this pattern and take only the text after qty as the goods name.
        if (beforeQty.matches("(?:.*[，,;；\\s])?(?:采购退货[，,]?\\s*)?(?:退给|退回给|给)[^，,。；;\\d]{2,}")) {
            raw = afterQty.replaceFirst("^[坏好新旧大小]的", "");
        } else {
            raw = beforeQty + afterQty;
            raw = raw.replaceFirst("^(?:采购退货[，,]?\\s*)?(?:退给|退回给|给)\\s*[^，,。；;\\d]{2,30}", "");
        }
        String name = cleanupGoodsName(raw);
        if (shouldIgnoreGoodsSegment(input, name)) {
            return null;
        }
        ItemDraft item = new ItemDraft();
        item.name = name;
        item.unit = unitText;
        item.quantity = new BigDecimal(qtyText);
        return item;
    }

    String cleanupPartnerName(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String value = raw.trim();
        value = value.replaceAll("^[：:，,\\s]+", "");
        value = value.replaceAll("^(?:采购退货|销售退货|采购|销售|客户是|客户为|供应商是|供应商为|卖给|给|退给|退回给)\\s*", "");
        value = value.replaceAll("的?\\d+(?:\\.\\d+)?\\s*(?:个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张).*$", "");
        value = value.replaceAll("(?:全部到货.*|到货.*|入库到.*|退回.*|退货.*|发货.*|发出.*|配送.*|送货.*|出库.*|从.+|到.+)$", "");
        value = value.replaceAll("(?:供应商|客户)$", "");
        value = value.replaceAll("(?:发货|发出|发|出库|送货|拣货|配送)$", "");
        value = value.replaceAll("的$", "");
        value = value.replaceAll("[，,。；;\\s]+$", "");
        if (value.startsWith("向") && value.length() > 1) {
            value = value.substring(1).trim();
        }
        value = value.replaceAll("^[：:，,\\s]+|[：:，,。；;\\s]+$", "").trim();
        return value;
    }

    String extractWarehouseHint(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String normalized = input.replace("，", ",").replace("。", "");
        Matcher matcher = Pattern.compile("(?:入库到|退回|退到|从|到)([\\u4e00-\\u9fa5A-Za-z0-9]{1,20}?(?:仓库|仓))").matcher(normalized);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    String extractSupplierName(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String normalized = input.trim().replace("\n", " ").replace("\r", " ");

        Matcher returnMatcher = Pattern.compile("(?:退给|退回给)(.+?)(?=\\d+(?:\\.\\d+)?\\s*(?:个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张))").matcher(normalized);
        if (returnMatcher.find()) {
            String candidate = cleanupPartnerName(returnMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher possessiveMatcher = Pattern.compile("^(.+?)的(?=\\d+(?:\\.\\d+)?\\s*(?:个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张).*(?:到货|入库|收货|签收))").matcher(normalized);
        if (possessiveMatcher.find()) {
            String candidate = cleanupPartnerName(possessiveMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher labelMatcher = SUPPLIER_BY_LABEL.matcher(normalized);
        if (labelMatcher.find()) {
            String candidate = cleanupPartnerName(labelMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher prepositionMatcher = SUPPLIER_AFTER_PREPOSITION.matcher(normalized);
        if (prepositionMatcher.find()) {
            String candidate = cleanupPartnerName(prepositionMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    String extractCustomerName(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String normalized = input.trim().replace("\n", " ").replace("\r", " ");

        Matcher explicitCustomerMatcher = Pattern.compile("^(客户[\\u4e00-\\u9fa5A-Za-z0-9_-]{1,20})(?=(?:发|出库|退回|退货|购买|采购|下单))").matcher(normalized);
        if (explicitCustomerMatcher.find()) {
            String candidate = cleanupPartnerName(explicitCustomerMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher outboundMatcher = Pattern.compile("(?:卖给|给)(.+?)(?=(?:发货|发出|发|出库|送货|拣货|配送))").matcher(normalized);
        if (outboundMatcher.find()) {
            String candidate = cleanupPartnerName(outboundMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher explicitMatcher = Pattern.compile("(?:卖给|给)(.+?)(?=(?:\\d+(?:\\.\\d+)?|[一二三四五六七八九十百千万]+)\\s*(?:个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张))").matcher(normalized);
        if (explicitMatcher.find()) {
            String candidate = cleanupPartnerName(explicitMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher orderMatcher = Pattern.compile("(?:卖给)(.+?)(?=\\d+(?:\\.\\d+)?\\s*(?:个|件|箱|盒|包|台|套|条|瓶|kg|KG|克|斤|升|只|支|米|张))").matcher(normalized);
        if (orderMatcher.find()) {
            String candidate = cleanupPartnerName(orderMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher outMatcher = Pattern.compile("(?:给)(.+?)(?=(?:发|出库|送货|拣货))").matcher(normalized);
        if (outMatcher.find()) {
            String candidate = cleanupPartnerName(outMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher returnMatcher = Pattern.compile("^(.+?)(?=(?:退回|拒收))").matcher(normalized);
        if (returnMatcher.find()) {
            String candidate = cleanupPartnerName(returnMatcher.group(1));
            if (StringUtils.isNotBlank(candidate) && !candidate.contains("供应商")) {
                return candidate;
            }
        }

        Matcher labelMatcher = CUSTOMER_BY_LABEL.matcher(normalized);
        if (labelMatcher.find()) {
            String candidate = cleanupPartnerName(labelMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }

        Matcher saleMatcher = CUSTOMER_AFTER_SALE.matcher(normalized);
        if (saleMatcher.find()) {
            String candidate = cleanupPartnerName(saleMatcher.group(1));
            if (StringUtils.isNotBlank(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    String extractReason(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String normalized = input.replace("，", ",").replace("。", "").trim();
        String[] keywords = new String[]{"原因", "备注", "因为", "退货原因"};
        for (String keyword : keywords) {
            int index = normalized.indexOf(keyword);
            if (index >= 0) {
                String value = normalized.substring(index + keyword.length()).replaceFirst("^[是为:：,，\\s]+", "").trim();
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        }
        if (normalized.contains("坏") || normalized.contains("破损") || normalized.contains("拒收") || normalized.contains("错货")) {
            return normalized;
        }
        return null;
    }

    String extractCheckType(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        if (input.contains("抽盘")) {
            return "SAMPLE";
        }
        if (input.contains("全盘")) {
            return "FULL";
        }
        return null;
    }

    String extractLinkOrderNo(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        Matcher matcher = LINK_ORDER_PATTERN.matcher(input.toUpperCase(Locale.ROOT));
        while (matcher.find()) {
            String value = matcher.group("no");
            if (StringUtils.isNotBlank(value) && value.length() >= 6) {
                return value;
            }
        }
        return null;
    }

    String normalizeLinkOrderInput(String input) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        String extracted = extractLinkOrderNo(input);
        return StringUtils.isNotBlank(extracted) ? extracted : input.trim().toUpperCase(Locale.ROOT);
    }
}
