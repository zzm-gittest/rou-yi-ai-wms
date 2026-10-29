package com.ruoyi.wms.service.ai.analysis;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.wms.domain.vo.ai.analysis.AnomalyVO;
import com.ruoyi.wms.domain.vo.ai.analysis.AnomalyVO.SuggestedAction;
import com.ruoyi.wms.mapper.report.WmsAnalysisMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 异常检测服务
 * <p>
 * 扫描库存、订单、财务数据，识别经营异常并生成提示。
 * 阈值统一定义为常量，后续可改为配置。
 *
 * @author ruoyi
 */
@Service
public class AnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionService.class);

    /** 库存断货预警天数阈值 */
    private static final int STOCK_SHORTAGE_DAYS = 3;
    /** 订单积压天数阈值 */
    private static final int ORDER_PENDING_DAYS = 3;
    /** 应收逾期天数阈值 */
    private static final int RECEIVABLE_OVERDUE_DAYS = 30;
    /** 滞销判断天数 */
    private static final int STALED_DAYS = 30;
    /** 滞销最低库存金额 */
    private static final BigDecimal STALED_MIN_AMOUNT = new BigDecimal("500");

    /** severity 排序权重 */
    private static final Map<String, Integer> SEVERITY_ORDER = new LinkedHashMap<>();
    static {
        SEVERITY_ORDER.put("RED", 0);
        SEVERITY_ORDER.put("ORANGE", 1);
        SEVERITY_ORDER.put("YELLOW", 2);
        SEVERITY_ORDER.put("BLUE", 3);
        SEVERITY_ORDER.put("WHITE", 4);
    }

    private final WmsAnalysisMapper analysisMapper;

    public AnomalyDetectionService(WmsAnalysisMapper analysisMapper) {
        this.analysisMapper = analysisMapper;
    }

    /**
     * 一次扫描全部 5 类异常
     */
    public List<AnomalyVO> scanAll() {
        List<AnomalyVO> anomalies = new ArrayList<>();
        try {
            anomalies.addAll(detectStockShortage());
            anomalies.addAll(detectPendingSalesOut());
            anomalies.addAll(detectPendingPurchaseIn());
            anomalies.addAll(detectOverdueReceivables());
            anomalies.addAll(detectStaledStock());
        } catch (Exception e) {
            log.error("异常扫描出错", e);
        }
        // 按 severity 排序
        anomalies.sort(Comparator.comparingInt(a -> SEVERITY_ORDER.getOrDefault(a.getSeverity(), 99)));
        return anomalies;
    }

    /**
     * 检测库存即将断货
     */
    public List<AnomalyVO> detectStockShortage() {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectStockShortage(tenantId, 30, STOCK_SHORTAGE_DAYS);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<AnomalyVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String goodsName = str(row.get("goodsName"));
            int days = intVal(row.get("estimatedDays"));
            String stock = str(row.get("currentStock"));
            String unit = str(row.get("unitName"));

            AnomalyVO anomaly = new AnomalyVO();
            anomaly.setType("STOCK_SHORTAGE");
            anomaly.setSeverity(days <= 1 ? "RED" : "ORANGE");
            anomaly.setTitle("\u5E93\u5B58\u5373\u5C06\u65AD\u8D27");
            anomaly.setDescription(goodsName + " \u5269\u4F59 " + stock + unit
                    + "\uFF0C\u9884\u8BA1\u53EF\u552E " + days + " \u5929");
            anomaly.setAnalysisHint("\u5546\u54C1\u201C" + goodsName + "\u201D\u5F53\u524D\u5E93\u5B58 "
                    + stock + unit + "\uFF0C\u65E5\u5747\u9500\u91CF " + str(row.get("dailyAvgQty")) + unit);
            anomaly.setSuggestedActions(Collections.singletonList(
                    new SuggestedAction("\u7ACB\u5373\u91C7\u8D2D\u8865\u8D27",
                            "__GOTO_PURCHASE_ORDER", "action")));
            list.add(anomaly);
        }
        return list;
    }

    /**
     * 检测订单积压未出库
     */
    public List<AnomalyVO> detectPendingSalesOut() {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectPendingSalesOut(tenantId, ORDER_PENDING_DAYS);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        AnomalyVO anomaly = new AnomalyVO();
        anomaly.setType("PENDING_SALES_OUT");
        anomaly.setSeverity("ORANGE");
        anomaly.setTitle("\u8BA2\u5355\u79EF\u538B\u672A\u51FA\u5E93");
        anomaly.setDescription(rows.size() + " \u7B14\u5DF2\u5BA1\u6838\u9500\u552E\u8BA2\u5355\u8D85\u8FC7 "
                + ORDER_PENDING_DAYS + " \u5929\u672A\u521B\u5EFA\u51FA\u5E93\u5355");

        StringBuilder hint = new StringBuilder();
        int showCount = Math.min(rows.size(), 3);
        for (int i = 0; i < showCount; i++) {
            Map<String, Object> row = rows.get(i);
            hint.append(str(row.get("orderNo"))).append("(")
                    .append(str(row.get("customerName"))).append(", \u7B49\u5F85")
                    .append(str(row.get("pendingDays"))).append("\u5929)");
            if (i < showCount - 1) {
                hint.append("; ");
            }
        }
        anomaly.setAnalysisHint(hint.toString());
        anomaly.setSuggestedActions(Collections.singletonList(
                new SuggestedAction("\u53BB\u6279\u91CF\u51FA\u5E93",
                        "__GOTO_SALES_OUT", "action")));
        return Collections.singletonList(anomaly);
    }

    /**
     * 检测采购到货未入库
     */
    public List<AnomalyVO> detectPendingPurchaseIn() {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectPendingPurchaseIn(tenantId, ORDER_PENDING_DAYS);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        AnomalyVO anomaly = new AnomalyVO();
        anomaly.setType("PENDING_PURCHASE_IN");
        anomaly.setSeverity("YELLOW");
        anomaly.setTitle("\u91C7\u8D2D\u5230\u8D27\u672A\u5165\u5E93");
        anomaly.setDescription(rows.size() + " \u7B14\u5DF2\u5BA1\u6838\u91C7\u8D2D\u8BA2\u5355\u8D85\u8FC7 "
                + ORDER_PENDING_DAYS + " \u5929\u672A\u521B\u5EFA\u5165\u5E93\u5355");

        StringBuilder hint = new StringBuilder();
        int showCount = Math.min(rows.size(), 3);
        for (int i = 0; i < showCount; i++) {
            Map<String, Object> row = rows.get(i);
            hint.append(str(row.get("orderNo"))).append("(")
                    .append(str(row.get("supplierName"))).append(", \u7B49\u5F85")
                    .append(str(row.get("pendingDays"))).append("\u5929)");
            if (i < showCount - 1) {
                hint.append("; ");
            }
        }
        anomaly.setAnalysisHint(hint.toString());
        anomaly.setSuggestedActions(Collections.singletonList(
                new SuggestedAction("\u53BB\u5165\u5E93",
                        "__GOTO_PURCHASE_IN", "action")));
        return Collections.singletonList(anomaly);
    }

    /**
     * 检测应收严重逾期
     */
    public List<AnomalyVO> detectOverdueReceivables() {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectOverdueReceivables(tenantId, RECEIVABLE_OVERDUE_DAYS);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        AnomalyVO anomaly = new AnomalyVO();
        anomaly.setType("OVERDUE_RECEIVABLE");
        anomaly.setSeverity("BLUE");
        anomaly.setTitle("\u5E94\u6536\u4E25\u91CD\u903E\u671F");

        BigDecimal totalOverdue = BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            Object remain = row.get("remainAmount");
            if (remain instanceof BigDecimal) {
                totalOverdue = totalOverdue.add((BigDecimal) remain);
            } else if (remain != null) {
                try {
                    totalOverdue = totalOverdue.add(new BigDecimal(remain.toString()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        anomaly.setDescription(rows.size() + " \u7B14\u5E94\u6536\u903E\u671F\u8D85 "
                + RECEIVABLE_OVERDUE_DAYS + " \u5929\uFF0C\u603B\u8BA1 \u00A5" + totalOverdue.toPlainString());

        StringBuilder hint = new StringBuilder();
        int showCount = Math.min(rows.size(), 3);
        for (int i = 0; i < showCount; i++) {
            Map<String, Object> row = rows.get(i);
            hint.append(str(row.get("customerName"))).append("(\u6B20\u6B3E\u00A5")
                    .append(str(row.get("remainAmount"))).append(", \u903E\u671F")
                    .append(str(row.get("overdueDays"))).append("\u5929)");
            if (i < showCount - 1) {
                hint.append("; ");
            }
        }
        anomaly.setAnalysisHint(hint.toString());
        anomaly.setSuggestedActions(Collections.singletonList(
                new SuggestedAction("\u67E5\u770B\u5E94\u6536\u8BE6\u60C5",
                        "__VIEW_RECEIVABLES", "action")));
        return Collections.singletonList(anomaly);
    }

    /**
     * 检测滞销库存积压
     */
    public List<AnomalyVO> detectStaledStock() {
        Long tenantId = SecurityUtils.getTenantId();
        List<Map<String, Object>> rows = analysisMapper.selectStaledGoods(tenantId, STALED_DAYS, STALED_MIN_AMOUNT);
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        AnomalyVO anomaly = new AnomalyVO();
        anomaly.setType("STALED_STOCK");
        anomaly.setSeverity("WHITE");
        anomaly.setTitle("\u6EDE\u9500\u5E93\u5B58\u79EF\u538B");
        anomaly.setDescription(rows.size() + " \u6B3E\u5546\u54C1\u8FD1 " + STALED_DAYS
                + " \u5929\u96F6\u9500\u552E\uFF0C\u5E93\u5B58\u91D1\u989D > \u00A5" + STALED_MIN_AMOUNT.toPlainString());

        StringBuilder hint = new StringBuilder();
        int showCount = Math.min(rows.size(), 3);
        for (int i = 0; i < showCount; i++) {
            Map<String, Object> row = rows.get(i);
            hint.append(str(row.get("goodsName"))).append("(\u5E93\u5B58\u00A5")
                    .append(str(row.get("stockAmount"))).append(")");
            if (i < showCount - 1) {
                hint.append("; ");
            }
        }
        anomaly.setAnalysisHint(hint.toString());
        anomaly.setSuggestedActions(Collections.emptyList());
        return Collections.singletonList(anomaly);
    }

    private String str(Object val) {
        return val != null ? val.toString() : "";
    }

    private int intVal(Object val) {
        if (val == null) {
            return 0;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
