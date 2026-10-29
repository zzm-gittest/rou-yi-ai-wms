package com.ruoyi.wms.service.ai.query;

import java.util.List;

/**
 * 统一查单推荐模板（与 PC 欢迎卡槽一致，便于 IM 引导用户话术）
 */
public final class UnifiedBillQueryTemplateProvider {

    private UnifiedBillQueryTemplateProvider() {
    }

    public static List<String> defaultTemplates() {
        return List.of(
            "查询待审核单据",
            "查询单号 PO20260324001",
            "查询销售出库单据",
            "查询近7天库存盘点单",
            "查询全部"
        );
    }
}
