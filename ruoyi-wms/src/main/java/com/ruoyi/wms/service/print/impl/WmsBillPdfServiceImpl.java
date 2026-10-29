package com.ruoyi.wms.service.print.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.mapper.WmsTenantPrintProfileMapper;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintItemRespVO;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.print.WmsBillPdfService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WmsBillPdfServiceImpl implements WmsBillPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int ITEMS_PER_PAGE = 25;

    // ========== 表格布局常量 ==========
    private static final float MARGIN = 30f;
    private static final float FONT_TITLE = 16f;
    private static final float FONT_INFO = 10f;
    private static final float FONT_TABLE = 9f;
    private static final float INFO_LINE_HEIGHT = 14f;
    private static final float TABLE_ROW_HEIGHT = 20f;
    private static final float TABLE_HEADER_HEIGHT = 22f;
    private static final float CELL_PADDING = 4f;
    /** 表格列宽比例：序号、商品名称、编码、数量、单位、单价、金额 */
    private static final float[] COL_RATIOS = {0.05f, 0.26f, 0.18f, 0.10f, 0.08f, 0.13f, 0.20f};

    private final ObjectMapper objectMapper;
    private final WmsTenantPrintProfileMapper tenantPrintProfileMapper;
    private final WmsGoodsService goodsService;
    private final WmsGoodsSkuService goodsSkuService;

    public WmsBillPdfServiceImpl(ObjectMapper objectMapper, WmsTenantPrintProfileMapper tenantPrintProfileMapper,
                                 WmsGoodsService goodsService,
                                 WmsGoodsSkuService goodsSkuService) {
        this.objectMapper = objectMapper;
        this.tenantPrintProfileMapper = tenantPrintProfileMapper;
        this.goodsService = goodsService;
        this.goodsSkuService = goodsSkuService;
    }

    @Override
    public WmsBillPrintRespVO buildPrintData(String title, Object sourceData) {
        Map<String, Object> data = objectMapper.convertValue(sourceData, new TypeReference<>() {
        });

        WmsBillPrintRespVO respVO = new WmsBillPrintRespVO();
        respVO.setTitle(defaultText(title));
        respVO.setBillNo(firstText(data, "billNo", "inNo", "outNo", "orderNo", "returnNo", "transferNo", "checkNo"));
        respVO.setBillDate(firstDateText(data, "billDate", "inDate", "outDate", "orderDate", "returnDate", "transferDate", "checkDate", "createTime"));

        String outWarehouse = firstText(data, "outWarehouseName");
        String inWarehouse = firstText(data, "inWarehouseName");
        if (!isBlank(outWarehouse) && !isBlank(inWarehouse)) {
            respVO.setWarehouseName(outWarehouse + " -> " + inWarehouse);
        } else {
            respVO.setWarehouseName(firstText(data, "warehouseName", "outWarehouseName", "inWarehouseName"));
        }

        respVO.setCounterpartName(firstText(data, "counterpartName", "supplierName", "customerName"));
        respVO.setHandler(firstText(data, "handler", "createBy"));
        respVO.setApproveBy(firstText(data, "approveBy"));
        respVO.setApproveTime(firstLocalDateTime(data.get("approveTime")));
        respVO.setRemark(firstText(data, "remark"));
        respVO.setTotalAmount(firstBigDecimal(data, "totalAmount", "payableAmount", "receivableAmount", "refundAmount"));
        respVO.setPrintTime(LocalDateTime.now());
        respVO.setItems(buildItems(data.get("items")));

        // 从租户表读取打印公司抬头
        try {
            Long tenantId = SecurityUtils.getTenantId();
            if (tenantId != null) {
                SysTenant tenant = tenantPrintProfileMapper.selectByTenantId(tenantId);
                if (tenant != null) {
                    if (!isBlank(tenant.getPrintCompanyName())) {
                        respVO.setCompanyName(tenant.getPrintCompanyName());
                    }
                    if (!isBlank(tenant.getPrintCompanyAddress())) {
                        respVO.setCompanyAddress(tenant.getPrintCompanyAddress());
                    }
                    if (!isBlank(tenant.getPrintCompanyPhone())) {
                        respVO.setCompanyPhone(tenant.getPrintCompanyPhone());
                    }
                    // 字体缩放倍数，默认1.0
                    respVO.setPrintFontScale(tenant.getPrintFontScale() != null
                        ? tenant.getPrintFontScale() : java.math.BigDecimal.ONE);
                    // 纸张大小，默认A4
                    respVO.setPrintPageSize(isBlank(tenant.getPrintPageSize())
                        ? "A4" : tenant.getPrintPageSize());
                }
            }
        } catch (Exception ignore) {
            // 获取租户信息失败不影响打印
        }

        return respVO;
    }

    @Override
    public byte[] generatePdf(WmsBillPrintRespVO printData) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont font = loadFont(document);

            // 字体缩放倍数
            float scale = printData.getPrintFontScale() != null
                ? printData.getPrintFontScale().floatValue() : 1.0f;
            if (scale <= 0f) scale = 1.0f; // 防止异常值

            // 根据缩放倍数计算实际使用的字号和行高
            float fontTitle = FONT_TITLE * scale;
            float fontInfo = FONT_INFO * scale;
            float fontTable = FONT_TABLE * scale;
            float infoLineHeight = INFO_LINE_HEIGHT * scale;
            float tableRowHeight = TABLE_ROW_HEIGHT * scale;
            float tableHeaderHeight = TABLE_HEADER_HEIGHT * scale;

            // 解析纸张大小
            PDRectangle pageSize = resolvePageSize(printData.getPrintPageSize());

            List<WmsBillPrintItemRespVO> items = printData.getItems() == null ? Collections.emptyList() : printData.getItems();
            int totalItems = items.size();
            int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) ITEMS_PER_PAGE));

            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                PDPage page = new PDPage(pageSize);
                document.addPage(page);

                float pageWidth = page.getMediaBox().getWidth();
                float tableWidth = pageWidth - MARGIN * 2;
                float[] colWidths = calcColWidths(tableWidth);
                int start = pageIndex * ITEMS_PER_PAGE;
                int end = Math.min(start + ITEMS_PER_PAGE, totalItems);
                boolean isLastPage = pageIndex == totalPages - 1;

                try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                    // 下移 8pt，让公司名字上方留出可见的呼吸空间
                    float y = pageSize.getHeight() - MARGIN - 8f;

                    // ===== 表头区域：居中公司名 + 标题 =====
                    // 右侧条码占 110pt + 4pt 间隙 = 114pt 安全区，居中基准仍用完整 tableWidth 保证视觉居中，
                    // 但文字宽度超过安全宽度时自动缩小字号，防止延伸进条码区被截断
                    float barcodeReserved = 114f;
                    float safeTextWidth = tableWidth - barcodeReserved;
                    if (!isBlank(printData.getCompanyName())) {
                        float companyFontSize = fontTitle;
                        float nameW = getStringWidth(font, companyFontSize, printData.getCompanyName());
                        if (nameW > safeTextWidth) {
                            companyFontSize = Math.max(11f * scale, companyFontSize * safeTextWidth / nameW);
                        }
                        y = drawCenteredText(cs, font, companyFontSize, MARGIN, y, tableWidth, printData.getCompanyName());
                    }
                    float titleFontSize = isBlank(printData.getCompanyName()) ? fontTitle : 13f * scale;
                    y = drawCenteredText(cs, font, titleFontSize, MARGIN, y, tableWidth, defaultText(printData.getTitle()));

                    // ===== 单据信息（单据编号左对齐，单据日期右对齐到金额列） =====
                    float lastColX = MARGIN + tableWidth * 0.80f; // 金额列起始位置
                    cs.beginText();
                    cs.setFont(font, fontInfo);
                    cs.newLineAtOffset(MARGIN, y);
                    showTextSafe(cs, "单据编号：" + defaultText(printData.getBillNo()));
                    cs.endText();
                    String dateText = "单据日期：" + defaultText(printData.getBillDate());
                    float dateTextWidth = getStringWidth(font, fontInfo, dateText);
                    cs.beginText();
                    cs.setFont(font, fontInfo);
                    cs.newLineAtOffset(lastColX + (tableWidth * 0.20f - dateTextWidth) / 2, y);
                    showTextSafe(cs, dateText);
                    cs.endText();
                    y -= infoLineHeight;
                    y += 8; // 缩小单据编号到表格的间距

                    // ===== 商品明细表格 =====
                    // 右侧：三联单联次标签（与表格头部对齐）
                    drawTriplicateLabels(cs, font, fontInfo, pageSize, y, tableRowHeight);

                    // 表头行
                    String[] headers = {"序号", "商品名称", "商品条码", "数量", "单位", "单价", "金额"};
                    y = drawTableHeaderRow(cs, font, fontTable, MARGIN, y, colWidths, headers, tableHeaderHeight);

                    // 数据行
                    if (totalItems == 0) {
                        y = drawTableMergedRow(cs, font, fontTable, MARGIN, y, tableWidth, "无商品明细", tableRowHeight);
                    } else {
                        for (int itemIndex = start; itemIndex < end; itemIndex++) {
                            WmsBillPrintItemRespVO item = items.get(itemIndex);
                            String goodsDisplay = defaultText(item.getGoodsName());
                            if (isMultiSpec(item) && !isBlank(item.getSkuName())) {
                                goodsDisplay += "\n" + item.getSkuName();
                            }
                            String[] row = {
                                    String.valueOf(itemIndex + 1),
                                    goodsDisplay,
                                    defaultText(item.getBarcode()),
                                    formatDecimal(item.getQuantity()),
                                    defaultText(item.getUnitName()),
                                    formatDecimal(item.getPrice()),
                                    formatDecimal(item.getAmount())
                            };
                            y = drawTableBodyRow(cs, font, fontTable, MARGIN, y, colWidths, row, tableRowHeight);
                        }
                    }

                    // 合计行
                    if (isLastPage) {
                        BigDecimal totalQty = BigDecimal.ZERO;
                        for (WmsBillPrintItemRespVO qi : items) {
                            if (qi.getQuantity() != null) totalQty = totalQty.add(qi.getQuantity());
                        }
                        String[] totalRow = {"", "", "", formatDecimal(totalQty), "", "", formatDecimal(printData.getTotalAmount())};
                        y = drawTableBodyRow(cs, font, fontTable, MARGIN, y, colWidths, totalRow, tableRowHeight);

                        // 合计金额行（合并单元格）：左侧大写金额，右侧 ￥数字金额
                        BigDecimal amount = printData.getTotalAmount() != null ? printData.getTotalAmount() : BigDecimal.ZERO;
                        String chineseAmount = "合计：" + com.ruoyi.common.core.text.Convert.digitUppercase(amount.doubleValue());
                        String numericAmount = "￥" + formatDecimal(amount);
                        y = drawTableMergedRowLR(cs, font, fontTable, MARGIN, y, tableWidth, chineseAmount, numericAmount, tableRowHeight);
                    }

                    // ===== 底部备注及联系信息 =====
                    y -= 18;
                    if (isLastPage && !isBlank(printData.getRemark())) {
                        y = drawSimpleText(cs, font, fontInfo, MARGIN, y, "备注：" + printData.getRemark(), infoLineHeight);
                    }
                    if (isLastPage && (!isBlank(printData.getCompanyAddress()) || !isBlank(printData.getCompanyPhone()))) {
                        // 地址和电话合并到同一行，右对齐到表格最后一列
                        String addr = defaultText(printData.getCompanyAddress());
                        String phone = defaultText(printData.getCompanyPhone());
                        String leftText = "地址：" + addr;
                        String rightText = "电话：" + phone;
                        float rightTextWidth = getStringWidth(font, fontInfo, rightText);
                        float rightX = MARGIN + tableWidth - rightTextWidth - CELL_PADDING;
                        cs.beginText();
                        cs.setFont(font, fontInfo);
                        cs.newLineAtOffset(MARGIN, y);
                        showTextSafe(cs, leftText);
                        cs.endText();
                        cs.beginText();
                        cs.setFont(font, fontInfo);
                        cs.newLineAtOffset(rightX, y);
                        showTextSafe(cs, rightText);
                        cs.endText();
                    }
                }
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("生成PDF失败", e);
        }
    }

    private List<WmsBillPrintItemRespVO> buildItems(Object itemsObj) {
        if (!(itemsObj instanceof List<?> rawItems) || rawItems.isEmpty()) {
            return Collections.emptyList();
        }

        // 先将所有 raw item 转换为 Map，然后批量查询条码
        List<Map<String, Object>> itemMaps = new ArrayList<>();
        for (Object raw : rawItems) {
            itemMaps.add(objectMapper.convertValue(raw, new TypeReference<>() {}));
        }

        // 批量查询条码，构建 skuId -> barcode 映射
        Set<Long> skuIds = itemMaps.stream()
                .map(item -> {
                    Object val = item.get("skuId");
                    if (val instanceof Number) return ((Number) val).longValue();
                    if (val instanceof String) {
                        try { return Long.parseLong((String) val); } catch (NumberFormatException ignored) {}
                    }
                    return null;
                })
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> skuBarcodeMap = skuIds.isEmpty()
                ? Collections.emptyMap()
                : goodsSkuService.listByIds(skuIds).stream()
                        .filter(sku -> StringUtils.isNotBlank(sku.getBarcode()))
                        .collect(Collectors.toMap(WmsGoodsSku::getId, WmsGoodsSku::getBarcode, (a, b) -> a));
        Set<Long> goodsIds = itemMaps.stream()
            .map(item -> parseLong(item.get("goodsId")))
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, String> goodsHasSpecMap = goodsIds.isEmpty()
            ? Collections.emptyMap()
            : goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getHasSpec, (a, b) -> a));

        List<WmsBillPrintItemRespVO> result = new ArrayList<>();
        for (Map<String, Object> item : itemMaps) {
            WmsBillPrintItemRespVO itemRespVO = new WmsBillPrintItemRespVO();
            itemRespVO.setGoodsName(firstText(item, "goodsName"));
            itemRespVO.setSkuName(firstText(item, "skuName"));
            // 打印仅展示商品条码，不再回退商品编码
            Long skuId = null;
            Object skuIdVal = item.get("skuId");
            if (skuIdVal instanceof Number) skuId = ((Number) skuIdVal).longValue();
            else if (skuIdVal instanceof String) {
                try { skuId = Long.parseLong((String) skuIdVal); } catch (NumberFormatException ignored) {}
            }
            String barcode = skuId != null ? skuBarcodeMap.get(skuId) : null;
            itemRespVO.setSkuCode(firstText(item, "skuCode"));
            itemRespVO.setBarcode(StringUtils.trimToEmpty(barcode));
            itemRespVO.setSpecInfo(firstText(item, "specInfo"));
            Long goodsId = parseLong(item.get("goodsId"));
            itemRespVO.setHasSpec(goodsId != null ? goodsHasSpecMap.get(goodsId) : null);
            itemRespVO.setUnitName(firstText(item, "unitName"));
            itemRespVO.setQuantity(firstBigDecimal(item, "quantity", "returnQty", "checkQty", "diffQty", "beforeQty", "outQuantity"));
            itemRespVO.setPrice(firstBigDecimal(item, "price", "costPrice"));
            itemRespVO.setAmount(firstBigDecimal(item, "amount", "diffAmount"));
            itemRespVO.setRemark(firstText(item, "remark"));
            result.add(itemRespVO);
        }
        return result;
    }

    private static String firstText(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty() && !"null".equalsIgnoreCase(text)) {
                return text;
            }
        }
        return "";
    }

    private static Long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static boolean isMultiSpec(WmsBillPrintItemRespVO item) {
        if (item == null) {
            return false;
        }
        if ("1".equals(item.getHasSpec())) {
            return true;
        }
        if ("0".equals(item.getHasSpec())) {
            return false;
        }
        String spec = item.getSpecInfo();
        return StringUtils.isNotBlank(spec)
                && !"无规格".equals(spec)
                && !"默认规格".equals(spec);
    }

    private static String firstDateText(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof LocalDateTime dt) {
                return dt.format(DATE_TIME_FORMATTER);
            }
            String text = String.valueOf(value).trim();
            if (text.isEmpty() || "null".equalsIgnoreCase(text)) {
                continue;
            }
            if (text.length() == 10) {
                return text;
            }
            if (text.contains("T")) {
                return text.replace('T', ' ');
            }
            return text;
        }
        return "";
    }

    private static LocalDateTime firstLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dt) {
            return dt;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            if (text.contains("T")) {
                return LocalDateTime.parse(text);
            }
            if (text.length() == 10) {
                return LocalDateTime.parse(text + " 00:00:00", DATE_TIME_FORMATTER);
            }
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static BigDecimal firstBigDecimal(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof BigDecimal decimal) {
                return decimal;
            }
            try {
                return new BigDecimal(String.valueOf(value));
            } catch (Exception ignore) {
                // ignore
            }
        }
        return BigDecimal.ZERO;
    }

    private static String formatDecimal(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    private static String defaultText(String value) {
        return isBlank(value) ? "-" : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty() || "null".equalsIgnoreCase(value.trim());
    }

    private static String repeat(char ch, int count) {
        return String.valueOf(ch).repeat(Math.max(0, count));
    }

    // ========== 表格绘图方法 ==========

    private static float[] calcColWidths(float tableWidth) {
        float[] widths = new float[COL_RATIOS.length];
        for (int i = 0; i < COL_RATIOS.length; i++) {
            widths[i] = tableWidth * COL_RATIOS[i];
        }
        return widths;
    }

    /** 居中绘制标题文本 */
    private static float drawCenteredText(PDPageContentStream cs, PDFont font, float fontSize,
                                           float x, float y, float width, String text) throws IOException {
        float textWidth = getStringWidth(font, fontSize, text);
        float offsetX = Math.max(0, (width - textWidth) / 2);
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x + offsetX, y);
        showTextSafe(cs, text);
        cs.endText();
        return y - (fontSize + 6);
    }

    /** 左对齐绘制单行文本 */
    private static float drawSimpleText(PDPageContentStream cs, PDFont font, float fontSize,
                                         float x, float y, String text, float lineHeight) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        showTextSafe(cs, text);
        cs.endText();
        return y - lineHeight;
    }

    /** 绘制信息网格（标签:值 | 标签:值 两列布局） */
    private static float drawInfoGrid(PDPageContentStream cs, PDFont font, float fontSize,
                                       float x, float y, float width, String[][] rows,
                                       float lineHeight) throws IOException {
        float halfWidth = width / 2;
        for (String[] row : rows) {
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(x, y);
            showTextSafe(cs, row[0] + "：" + row[1]);
            cs.endText();

            if (row.length >= 4) {
                cs.beginText();
                cs.setFont(font, fontSize);
                cs.newLineAtOffset(x + halfWidth, y);
                showTextSafe(cs, row[2] + "：" + row[3]);
                cs.endText();
            }
            y -= lineHeight;
        }
        return y;
    }

    /** 绘制表格表头行（浅灰背景 + 边框） */
    private static float drawTableHeaderRow(PDPageContentStream cs, PDFont font, float fontSize,
                                             float x, float y, float[] colWidths, String[] texts,
                                             float headerHeight) throws IOException {
        float rowHeight = headerHeight;
        float totalWidth = sumArray(colWidths);

        // 边框 + 竖线
        drawRowBorders(cs, x, y, rowHeight, colWidths);

        // 文字
        float cellX = x;
        for (int i = 0; i < colWidths.length && i < texts.length; i++) {
            boolean rightAlign = i == 3 || i >= 5; // 数量、单价、金额右对齐
            drawCellText(cs, font, fontSize, cellX, y, colWidths[i], rowHeight, texts[i], rightAlign);
            cellX += colWidths[i];
        }
        return y - rowHeight;
    }

    /** 绘制表格数据行（带边框） */
    private static float drawTableBodyRow(PDPageContentStream cs, PDFont font, float fontSize,
                                           float x, float y, float[] colWidths, String[] texts,
                                           float minRowHeight) throws IOException {
        // 计算商品名列需要的行数来决定行高
        float nameColWidth = colWidths.length > 1 ? colWidths[1] : 100f;
        float maxTextWidth = nameColWidth - CELL_PADDING * 2;
        String nameText = (texts.length > 1 && texts[1] != null) ? texts[1] : "";
        List<String> wrappedLines = wrapText(font, fontSize, nameText, maxTextWidth);
        int lineCount = Math.max(1, wrappedLines.size());
        float rowHeight = Math.max(minRowHeight, CELL_PADDING * 2 + lineCount * (fontSize + 2));

        drawRowBorders(cs, x, y, rowHeight, colWidths);

        float cellX = x;
        for (int i = 0; i < colWidths.length && i < texts.length; i++) {
            boolean rightAlign = i == 3 || i >= 5;
            if (i == 1) {
                // 商品名列：自动换行绘制
                drawWrappedCellText(cs, font, fontSize, cellX, y, colWidths[i], rowHeight, wrappedLines);
            } else {
                drawCellText(cs, font, fontSize, cellX, y, colWidths[i], rowHeight, texts[i], rightAlign);
            }
            cellX += colWidths[i];
        }
        return y - rowHeight;
    }

    /** 将文字按列宽自动换行（支持 \n 显式换行） */
    private static List<String> wrapText(PDFont font, float fontSize, String text, float maxWidth) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }
        // 先按显式换行符拆分
        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            if (para.isEmpty()) {
                result.add("");
                continue;
            }
            try {
                float paraWidth = font.getStringWidth(para) / 1000 * fontSize;
                if (paraWidth <= maxWidth) {
                    result.add(para);
                    continue;
                }
                // 逐字符累积换行
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < para.length(); i++) {
                    String candidate = line.toString() + para.charAt(i);
                    float w = font.getStringWidth(candidate) / 1000 * fontSize;
                    if (w > maxWidth && line.length() > 0) {
                        result.add(line.toString());
                        line = new StringBuilder();
                        line.append(para.charAt(i));
                    } else {
                        line.append(para.charAt(i));
                    }
                }
                if (line.length() > 0) {
                    result.add(line.toString());
                }
            } catch (Exception e) {
                result.add(para);
            }
        }
        if (result.isEmpty()) {
            result.add("");
        }
        return result;
    }

    /** 在单元格中绘制已换行的文字列表 */
    private static void drawWrappedCellText(PDPageContentStream cs, PDFont font, float fontSize,
                                             float cellX, float cellY, float cellWidth, float rowHeight,
                                             List<String> lines) throws IOException {
        float startY = cellY - CELL_PADDING - fontSize;
        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(cellX + CELL_PADDING, startY);
            showTextSafe(cs, line);
            cs.endText();
            startY -= (fontSize + 2);
        }
    }

    /** 绘制表格合并行（跨列居中文本） */
    private static float drawTableMergedRow(PDPageContentStream cs, PDFont font, float fontSize,
                                             float x, float y, float tableWidth, String text,
                                             float rowHeight) throws IOException {
        cs.setStrokingColor(0f, 0f, 0f);
        cs.setLineWidth(0.5f);
        cs.addRect(x, y - rowHeight, tableWidth, rowHeight);
        cs.stroke();

        float textWidth = getStringWidth(font, fontSize, text);
        float offsetX = Math.max(0, (tableWidth - textWidth) / 2);
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x + offsetX, y - rowHeight + CELL_PADDING + 4);
        showTextSafe(cs, text);
        cs.endText();
        return y - rowHeight;
    }

    /** 绘制合并行（左对齐文本 + 右对齐文本，同一行） */
    private static float drawTableMergedRowLR(PDPageContentStream cs, PDFont font, float fontSize,
                                               float x, float y, float tableWidth,
                                               String leftText, String rightText,
                                               float rowHeight) throws IOException {
        cs.setStrokingColor(0f, 0f, 0f);
        cs.setLineWidth(0.5f);
        cs.addRect(x, y - rowHeight, tableWidth, rowHeight);
        cs.stroke();

        float textY = y - rowHeight + CELL_PADDING + 4;

        // 左对齐文本
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x + CELL_PADDING, textY);
        showTextSafe(cs, leftText);
        cs.endText();

        // 右对齐文本
        float rightTextWidth = getStringWidth(font, fontSize, rightText);
        float rightX = x + tableWidth - CELL_PADDING - rightTextWidth;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(rightX, textY);
        showTextSafe(cs, rightText);
        cs.endText();

        return y - rowHeight;
    }

    /** 绘制行边框和列分割线 */
    private static void drawRowBorders(PDPageContentStream cs, float x, float y,
                                        float rowHeight, float[] colWidths) throws IOException {
        cs.setStrokingColor(0f, 0f, 0f);
        cs.setLineWidth(0.5f);

        float totalWidth = sumArray(colWidths);
        cs.addRect(x, y - rowHeight, totalWidth, rowHeight);
        cs.stroke();

        // 竖线
        float lineX = x;
        for (int i = 0; i < colWidths.length - 1; i++) {
            lineX += colWidths[i];
            cs.moveTo(lineX, y);
            cs.lineTo(lineX, y - rowHeight);
            cs.stroke();
        }
    }

    /** 在单元格中绘制单行文字 */
    private static void drawCellText(PDPageContentStream cs, PDFont font, float fontSize,
                                      float cellX, float cellY, float cellWidth, float rowHeight,
                                      String text, boolean rightAlign) throws IOException {
        String display = isBlank(text) ? "" : text;
        display = truncateToFit(font, fontSize, display, cellWidth - CELL_PADDING * 2);
        float textWidth = getStringWidth(font, fontSize, display);
        float textX;
        if (rightAlign) {
            textX = cellX + cellWidth - CELL_PADDING - textWidth;
        } else {
            textX = cellX + CELL_PADDING;
        }
        float textY = cellY - rowHeight + (rowHeight - fontSize) / 2 + 1;
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(Math.max(textX, cellX + CELL_PADDING), textY);
        showTextSafe(cs, display);
        cs.endText();
    }

    /** 在单元格中绘制多行文字（商品名 + 规格名） */
    private static void drawMultiLineCellText(PDPageContentStream cs, PDFont font, float fontSize,
                                               float cellX, float cellY, float cellWidth, float rowHeight,
                                               String text) throws IOException {
        String[] lines = text.split("\n");
        float startY = cellY - CELL_PADDING - fontSize;
        float maxTextWidth = cellWidth - CELL_PADDING * 2;
        for (String line : lines) {
            String display = truncateToFit(font, fontSize, line, maxTextWidth);
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.newLineAtOffset(cellX + CELL_PADDING, startY);
            showTextSafe(cs, display);
            cs.endText();
            startY -= (fontSize + 2);
        }
    }

    /** 安全输出文字，CJK 字符回退处理 */
    private static void showTextSafe(PDPageContentStream cs, String text) throws IOException {
        try {
            cs.showText(text == null ? "" : text);
        } catch (Exception ex) {
            cs.showText(toAsciiSafe(text));
        }
    }

    private static String toAsciiSafe(String value) {
        if (value == null || value.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            sb.append((ch >= 32 && ch <= 126) ? ch : '?');
        }
        return sb.toString();
    }

    /** 截断文字使其不超过指定宽度 */
    private static String truncateToFit(PDFont font, float fontSize, String text, float maxWidth) {
        if (text == null || text.isEmpty()) return "";
        try {
            if (font.getStringWidth(text) / 1000 * fontSize <= maxWidth) return text;
            for (int i = text.length() - 1; i > 0; i--) {
                String sub = text.substring(0, i) + "…";
                if (font.getStringWidth(sub) / 1000 * fontSize <= maxWidth) return sub;
            }
        } catch (Exception ignore) {
        }
        return text;
    }

    private static float getStringWidth(PDFont font, float fontSize, String text) {
        if (text == null || text.isEmpty()) return 0;
        try {
            return font.getStringWidth(text) / 1000 * fontSize;
        } catch (Exception e) {
            return text.length() * fontSize * 0.6f;
        }
    }

    private static float sumArray(float[] arr) {
        float sum = 0;
        for (float v : arr) sum += v;
        return sum;
    }

    /** 在 PDF 中绘制 Code128 条码 */
    private static void drawBarcode(PDPageContentStream cs, PDDocument document,
                                     String text, float x, float y, float width, float height) throws IOException {
        Code128Bean barcodeBean = new Code128Bean();
        barcodeBean.setModuleWidth(0.3);
        barcodeBean.setBarHeight(8.0);
        barcodeBean.doQuietZone(false);
        barcodeBean.setMsgPosition(org.krysalis.barcode4j.HumanReadablePlacement.HRP_NONE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(
                bos, "image/png", 200, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        barcodeBean.generateBarcode(canvas, text);
        canvas.finish();

        PDImageXObject barcodeImage = PDImageXObject.createFromByteArray(document, bos.toByteArray(), "barcode");
        cs.drawImage(barcodeImage, x, y, width, height);
    }

    /** 在页面右侧绘制三联单联次标签（与表格头部对齐的标准竖排格式） */
    private static void drawTriplicateLabels(PDPageContentStream cs, PDFont font, float fontSize,
                                              PDRectangle pageSize, float tableStartY,
                                              float rowHeight) throws IOException {
        float labelFontSize = Math.max(8f, fontSize * 0.8f);
        float charSpacing = labelFontSize + 1f;
        float tableWidth = pageSize.getWidth() - MARGIN * 2;
        float labelX = MARGIN + tableWidth + 8f;

        // 每个字/符号单独一行，标准三联单右侧竖排格式
        String[] chars = {
            "白", "︵", "签", "字", "联", "︶",
            "红", "︵", "客", "户", "联", "︶",
            "黄", "︵", "仓", "库", "联", "︶"
        };
        // 从表格头部对齐，"白"字顶部与表头行顶部齐平
        float y = tableStartY;
        for (String ch : chars) {
            cs.beginText();
            cs.setFont(font, labelFontSize);
            cs.newLineAtOffset(labelX, y);
            showTextSafe(cs, ch);
            cs.endText();
            y -= charSpacing;
        }

        // 标签区域和内容之间的竖线
        float lineX = labelX - 4f;
        float lineTop = tableStartY + 4f;
        float lineBottom = y - 4f;
        cs.setLineWidth(0.3f);
        cs.setStrokingColor(0.6f, 0.6f, 0.6f);
        cs.moveTo(lineX, lineTop);
        cs.lineTo(lineX, lineBottom);
        cs.stroke();
        cs.setStrokingColor(0f, 0f, 0f);
    }

    /** 根据页面大小名称解析 PDRectangle */
    private static PDRectangle resolvePageSize(String pageSize) {
        if (pageSize == null) {
            return PDRectangle.A4;
        }
        return switch (pageSize) {
            case "A4_LANDSCAPE" -> new PDRectangle(841.89f, 595.28f); // A4 横排 297mm × 210mm
            case "SLIP_241X279" -> new PDRectangle(683.15f, 790.87f); // 241mm × 279mm 三联等份
            case "SLIP_241X140" -> new PDRectangle(683.15f, 396.85f); // 241mm × 140mm 三联半页
            default -> PDRectangle.A4;
        };
    }

    private PDFont loadFont(PDDocument document) {
        // 优先从 classpath 加载内嵌字体（跨平台可靠）
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/simhei.ttf")) {
            if (fontStream != null) {
                return PDType0Font.load(document, fontStream);
            }
        } catch (Exception ignore) {
            // ignore and try filesystem
        }

        // 回退：尝试系统字体路径
        List<String> fontCandidates = List.of(
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "C:/Windows/Fonts/msyh.ttc",
                "/System/Library/Fonts/STHeiti Light.ttc",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
                "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc"
        );

        for (String candidate : fontCandidates) {
            try {
                Path path = Path.of(candidate);
                if (Files.exists(path)) {
                    return PDType0Font.load(document, path.toFile());
                }
            } catch (Exception ignore) {
                // ignore and fallback
            }
        }

        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }
}
