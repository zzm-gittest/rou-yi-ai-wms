package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsBizOperLog;
import com.ruoyi.wms.mapper.WmsBizOperLogMapper;
import com.ruoyi.wms.service.IWmsBizOperLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * WMS业务操作日志 Service 实现
 */
@Service
public class WmsBizOperLogServiceImpl implements IWmsBizOperLogService {

    private static final Logger log = LoggerFactory.getLogger(WmsBizOperLogServiceImpl.class);

    private static final Map<String, String[]> BIZ_NO_COLUMN_MAP = new LinkedHashMap<>();

    static {
        BIZ_NO_COLUMN_MAP.put("wms_purchase_order", new String[]{"order_no"});
        BIZ_NO_COLUMN_MAP.put("wms_purchase_in", new String[]{"in_no", "ocr_order_no"});
        BIZ_NO_COLUMN_MAP.put("wms_purchase_return", new String[]{"return_no"});
        BIZ_NO_COLUMN_MAP.put("wms_sales_order", new String[]{"order_no"});
        BIZ_NO_COLUMN_MAP.put("wms_sales_out", new String[]{"out_no", "order_no"});
        BIZ_NO_COLUMN_MAP.put("wms_sales_return", new String[]{"return_no"});
        BIZ_NO_COLUMN_MAP.put("wms_stock_transfer", new String[]{"transfer_no"});
        BIZ_NO_COLUMN_MAP.put("wms_stock_check", new String[]{"check_no"});
        BIZ_NO_COLUMN_MAP.put("wms_goods", new String[]{"goods_code", "goods_name"});
        BIZ_NO_COLUMN_MAP.put("wms_goods_sku", new String[]{"sku_code", "barcode", "sku_name"});
        BIZ_NO_COLUMN_MAP.put("wms_goods_category", new String[]{"category_name"});
        BIZ_NO_COLUMN_MAP.put("wms_supplier", new String[]{"supplier_code", "supplier_name"});
        BIZ_NO_COLUMN_MAP.put("wms_customer", new String[]{"customer_code", "customer_name"});
        BIZ_NO_COLUMN_MAP.put("wms_unit", new String[]{"unit_name"});
        BIZ_NO_COLUMN_MAP.put("wms_warehouse", new String[]{"warehouse_code", "warehouse_name"});
        BIZ_NO_COLUMN_MAP.put("wms_account", new String[]{"account_code", "account_name"});
        BIZ_NO_COLUMN_MAP.put("wms_expense", new String[]{"expense_no"});
        BIZ_NO_COLUMN_MAP.put("wms_expense_type", new String[]{"type_name"});
        BIZ_NO_COLUMN_MAP.put("wms_import_task", new String[]{"task_no"});
        BIZ_NO_COLUMN_MAP.put("wms_bill_approve_policy", new String[]{"bill_type"});
        BIZ_NO_COLUMN_MAP.put("wms_accounts_receivable", new String[]{"bill_no"});
        BIZ_NO_COLUMN_MAP.put("wms_accounts_payable", new String[]{"bill_no"});
        BIZ_NO_COLUMN_MAP.put("wms_stock_log", new String[]{"bill_no"});
    }

    @Autowired
    private WmsBizOperLogMapper bizOperLogMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Async
    @Override
    public void asyncInsertLog(WmsBizOperLog operLog) {
        try {
            bizOperLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("插入WMS业务操作日志失败", e);
        }
    }

    @Override
    public String resolveBizNo(String bizTable, Long bizId) {
        if (StringUtils.isEmpty(bizTable) || bizId == null) {
            return null;
        }

        String[] columns = BIZ_NO_COLUMN_MAP.get(bizTable);
        if (columns == null || columns.length == 0) {
            return null;
        }

        for (String column : columns) {
            String sql = "SELECT " + column + " FROM " + bizTable + " WHERE id = ? LIMIT 1";
            try {
                List<String> values = jdbcTemplate.query(sql, ps -> ps.setLong(1, bizId),
                    (rs, rowNum) -> rs.getString(1));
                if (!values.isEmpty() && StringUtils.isNotEmpty(values.get(0))) {
                    return values.get(0);
                }
            } catch (Exception e) {
                log.debug("解析业务日志标识失败, bizTable={}, bizId={}, column={}", bizTable, bizId, column, e);
            }
        }
        return null;
    }

    @Override
    public IPage<WmsBizOperLog> page(Long tenantId, String bizModule, String bizType,
                                      String operUserName, String bizNo,
                                      String beginTime, String endTime,
                                      int pageNo, int pageSize) {
        LambdaQueryWrapper<WmsBizOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(tenantId != null, WmsBizOperLog::getTenantId, tenantId)
               .like(StringUtils.isNotEmpty(bizModule), WmsBizOperLog::getBizModule, bizModule)
               .eq(StringUtils.isNotEmpty(bizType), WmsBizOperLog::getBizType, bizType)
               .like(StringUtils.isNotEmpty(operUserName), WmsBizOperLog::getOperUserName, operUserName)
               .like(StringUtils.isNotEmpty(bizNo), WmsBizOperLog::getBizNo, bizNo)
               .ge(StringUtils.isNotEmpty(beginTime), WmsBizOperLog::getCreateTime, beginTime)
               .le(StringUtils.isNotEmpty(endTime), WmsBizOperLog::getCreateTime, endTime)
               .orderByDesc(WmsBizOperLog::getCreateTime);
        return bizOperLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    }
}
