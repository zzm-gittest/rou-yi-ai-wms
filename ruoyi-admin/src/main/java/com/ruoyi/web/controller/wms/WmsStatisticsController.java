package com.ruoyi.web.controller.wms;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.WmsPurchaseReturn;
import com.ruoyi.wms.domain.WmsSalesOut;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.WmsSalesReturn;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.WmsAccountsReceivable;
import com.ruoyi.wms.domain.WmsAccountsPayable;
import com.ruoyi.wms.domain.vo.stock.WmsStockRespVO;
import com.ruoyi.wms.mapper.WmsGoodsMapper;
import com.ruoyi.wms.mapper.WmsGoodsSkuMapper;
import com.ruoyi.wms.mapper.WmsPurchaseInMapper;
import com.ruoyi.wms.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.wms.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.wms.mapper.WmsSalesOutMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderMapper;
import com.ruoyi.wms.mapper.WmsSalesReturnMapper;
import com.ruoyi.wms.mapper.WmsStockMapper;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.mapper.WmsAccountsReceivableMapper;
import com.ruoyi.wms.mapper.WmsAccountsPayableMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * WMS统计分析Controller
 *
 * @author ruoyi
 */
@Api("WMS统计分析")
@RestController
@RequestMapping("/wms/statistics")
public class WmsStatisticsController extends BaseController {

    private final WmsGoodsMapper goodsMapper;
    private final WmsGoodsSkuMapper goodsSkuMapper;
    private final WmsPurchaseOrderMapper purchaseOrderMapper;
    private final WmsPurchaseInMapper purchaseInMapper;
    private final WmsPurchaseReturnMapper purchaseReturnMapper;
    private final WmsSalesOrderMapper salesOrderMapper;
    private final WmsSalesOutMapper salesOutMapper;
    private final WmsSalesReturnMapper salesReturnMapper;
    private final WmsStockMapper stockMapper;
    private final WmsCustomerMapper customerMapper;
    private final WmsSupplierMapper supplierMapper;
    private final WmsWarehouseMapper warehouseMapper;
    private final WmsAccountsReceivableMapper accountsReceivableMapper;
    private final WmsAccountsPayableMapper accountsPayableMapper;

    public WmsStatisticsController(WmsGoodsMapper goodsMapper, WmsGoodsSkuMapper goodsSkuMapper,
            WmsPurchaseOrderMapper purchaseOrderMapper, WmsPurchaseInMapper purchaseInMapper,
            WmsPurchaseReturnMapper purchaseReturnMapper, WmsSalesOrderMapper salesOrderMapper,
            WmsSalesOutMapper salesOutMapper, WmsSalesReturnMapper salesReturnMapper,
            WmsStockMapper stockMapper, WmsCustomerMapper customerMapper,
            WmsSupplierMapper supplierMapper, WmsWarehouseMapper warehouseMapper,
            WmsAccountsReceivableMapper accountsReceivableMapper, WmsAccountsPayableMapper accountsPayableMapper) {
        this.goodsMapper = goodsMapper;
        this.goodsSkuMapper = goodsSkuMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseInMapper = purchaseInMapper;
        this.purchaseReturnMapper = purchaseReturnMapper;
        this.salesOrderMapper = salesOrderMapper;
        this.salesOutMapper = salesOutMapper;
        this.salesReturnMapper = salesReturnMapper;
        this.stockMapper = stockMapper;
        this.customerMapper = customerMapper;
        this.supplierMapper = supplierMapper;
        this.warehouseMapper = warehouseMapper;
        this.accountsReceivableMapper = accountsReceivableMapper;
        this.accountsPayableMapper = accountsPayableMapper;
    }

    /**
     * 获取概览统计数据
     */
    @ApiOperation("获取概览统计")
    @GetMapping("/overview")
    @PreAuthorize("@ss.hasPermi('wms:statistics:overview')")
    public AjaxResult getOverview() {
        Map<String, Object> data = new HashMap<>();

        // 基础数据统计
        data.put("goodsCount", goodsMapper.selectCount(null));
        data.put("skuCount", goodsSkuMapper.selectCount(null));
        data.put("warehouseCount", warehouseMapper.selectCount(null));
        data.put("customerCount", customerMapper.selectCount(null));
        data.put("supplierCount", supplierMapper.selectCount(null));

        // 库存统计
        List<WmsStock> stocks = stockMapper.selectList(null);
        BigDecimal totalStock = stocks.stream()
                .map(s -> s.getQuantity() != null ? s.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalStock", totalStock);

        // 低库存预警（库存为0或小于10的SKU数量）
        long lowStockCount = stocks.stream()
                .filter(s -> s.getQuantity() == null || s.getQuantity().compareTo(BigDecimal.TEN) < 0)
                .map(WmsStock::getSkuId)
                .distinct()
                .count();
        data.put("lowStockCount", lowStockCount);

        // 采购统计
        long pendingPurchaseOrder = purchaseOrderMapper.selectCount(
                Wrappers.<WmsPurchaseOrder>lambdaQuery().eq(WmsPurchaseOrder::getOrderStatus, "0"));
        data.put("pendingPurchaseOrder", pendingPurchaseOrder);

        // 销售统计
        long pendingSalesOrder = salesOrderMapper.selectCount(
                Wrappers.<WmsSalesOrder>lambdaQuery().eq(WmsSalesOrder::getOrderStatus, "0"));
        data.put("pendingSalesOrder", pendingSalesOrder);

        // 财务统计 - 应收账款
        BigDecimal totalReceivable = accountsReceivableMapper.selectList(null)
                .stream()
                .map(a -> a.getArAmount() != null ? a.getArAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalReceivable", totalReceivable);

        // 应付账款
        BigDecimal totalPayable = accountsPayableMapper.selectList(null)
                .stream()
                .map(a -> a.getApAmount() != null ? a.getApAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalPayable", totalPayable);

        // 本月销售金额
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlySales = salesOutMapper.selectList(
                Wrappers.<WmsSalesOut>lambdaQuery().ge(WmsSalesOut::getCreateTime, monthStart))
                .stream().map(s -> s.getTotalAmount() != null ? s.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("monthlySales", monthlySales);

        // 本月采购金额
        BigDecimal monthlyPurchase = purchaseInMapper.selectList(
                Wrappers.<WmsPurchaseIn>lambdaQuery().ge(WmsPurchaseIn::getCreateTime, monthStart))
                .stream().map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("monthlyPurchase", monthlyPurchase);

        return success(data);
    }

    /**
     * 获取销售趋势数据（近12个月）
     */
    @ApiOperation("获取销售趋势")
    @GetMapping("/salesTrend")
    @PreAuthorize("@ss.hasPermi('wms:statistics:trend')")
    public AjaxResult getSalesTrend() {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            String monthKey = yearMonth.getMonthValue() + "月";

            // 销售订单数量和金额
            long orderCount = salesOrderMapper.selectCount(
                    Wrappers.<WmsSalesOrder>lambdaQuery()
                            .ge(WmsSalesOrder::getCreateTime, monthStart)
                            .le(WmsSalesOrder::getCreateTime, monthEnd));

            BigDecimal orderAmount = salesOrderMapper.selectList(
                    Wrappers.<WmsSalesOrder>lambdaQuery()
                            .ge(WmsSalesOrder::getCreateTime, monthStart)
                            .le(WmsSalesOrder::getCreateTime, monthEnd))
                    .stream().map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 销售出库金额
            BigDecimal outAmount = salesOutMapper.selectList(
                    Wrappers.<WmsSalesOut>lambdaQuery()
                            .ge(WmsSalesOut::getCreateTime, monthStart)
                            .le(WmsSalesOut::getCreateTime, monthEnd))
                    .stream().map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 销售退货金额
            BigDecimal returnAmount = salesReturnMapper.selectList(
                    Wrappers.<WmsSalesReturn>lambdaQuery()
                            .ge(WmsSalesReturn::getCreateTime, monthStart)
                            .le(WmsSalesReturn::getCreateTime, monthEnd))
                    .stream().map(r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthKey);
            monthData.put("orderCount", orderCount);
            monthData.put("orderAmount", orderAmount);
            monthData.put("outAmount", outAmount);
            monthData.put("returnAmount", returnAmount);
            trendData.add(monthData);
        }

        return success(trendData);
    }

    /**
     * 获取采购趋势数据（近12个月）
     */
    @ApiOperation("获取采购趋势")
    @GetMapping("/purchaseTrend")
    @PreAuthorize("@ss.hasPermi('wms:statistics:trend')")
    public AjaxResult getPurchaseTrend() {
        List<Map<String, Object>> trendData = new ArrayList<>();

        for (int i = 11; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.now().minusMonths(i);
            LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime monthEnd = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            String monthKey = yearMonth.getMonthValue() + "月";

            // 采购订单数量和金额
            long orderCount = purchaseOrderMapper.selectCount(
                    Wrappers.<WmsPurchaseOrder>lambdaQuery()
                            .ge(WmsPurchaseOrder::getCreateTime, monthStart)
                            .le(WmsPurchaseOrder::getCreateTime, monthEnd));

            BigDecimal orderAmount = purchaseOrderMapper.selectList(
                    Wrappers.<WmsPurchaseOrder>lambdaQuery()
                            .ge(WmsPurchaseOrder::getCreateTime, monthStart)
                            .le(WmsPurchaseOrder::getCreateTime, monthEnd))
                    .stream().map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 采购入库金额
            BigDecimal inAmount = purchaseInMapper.selectList(
                    Wrappers.<WmsPurchaseIn>lambdaQuery()
                            .ge(WmsPurchaseIn::getCreateTime, monthStart)
                            .le(WmsPurchaseIn::getCreateTime, monthEnd))
                    .stream().map(p -> p.getTotalAmount() != null ? p.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 采购退货金额
            BigDecimal returnAmount = purchaseReturnMapper.selectList(
                    Wrappers.<WmsPurchaseReturn>lambdaQuery()
                            .ge(WmsPurchaseReturn::getCreateTime, monthStart)
                            .le(WmsPurchaseReturn::getCreateTime, monthEnd))
                    .stream().map(r -> r.getTotalAmount() != null ? r.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthKey);
            monthData.put("orderCount", orderCount);
            monthData.put("orderAmount", orderAmount);
            monthData.put("inAmount", inAmount);
            monthData.put("returnAmount", returnAmount);
            trendData.add(monthData);
        }

        return success(trendData);
    }

    /**
     * 获取库存Top数据（库存最多的商品）
     */
    @ApiOperation("获取库存Top")
    @GetMapping("/stockTop")
    @PreAuthorize("@ss.hasPermi('wms:statistics:stock')")
    public AjaxResult getStockTop() {
        List<WmsStockRespVO> stocks = stockMapper.selectStockTop(10);
        return success(stocks);
    }

    /**
     * 获取库存预警数据
     */
    @ApiOperation("获取库存预警")
    @GetMapping("/stockWarning")
    @PreAuthorize("@ss.hasPermi('wms:statistics:stock')")
    public AjaxResult getStockWarning() {
        // 库存小于10的SKU视为预警
        List<WmsStockRespVO> stocks = stockMapper.selectStockWarning(new BigDecimal("10"), 100);
        return success(stocks);
    }

    /**
     * 获取热销商品Top10
     */
    @ApiOperation("获取热销商品")
    @GetMapping("/topSellingGoods")
    @PreAuthorize("@ss.hasPermi('wms:statistics:goods')")
    public AjaxResult getTopSellingGoods() {
        List<Map<String, Object>> topData = new ArrayList<>();

        // 近3个月销售出库金额最高的订单
        LocalDateTime monthStart = LocalDateTime.now().minusMonths(3).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        List<WmsSalesOut> salesOuts = salesOutMapper.selectList(
                Wrappers.<WmsSalesOut>lambdaQuery()
                        .ge(WmsSalesOut::getCreateTime, monthStart)
                        .orderByDesc(WmsSalesOut::getTotalAmount)
                        .last("LIMIT 10"));

        for (WmsSalesOut out : salesOuts) {
            Map<String, Object> item = new HashMap<>();
            item.put("outNo", out.getOutNo());
            item.put("totalAmount", out.getTotalAmount());
            item.put("customerId", out.getCustomerId());
            item.put("createTime", out.getCreateTime());
            topData.add(item);
        }

        return success(topData);
    }

    /**
     * 获取仓库库存分布
     */
    @ApiOperation("获取仓库库存分布")
    @GetMapping("/warehouseStock")
    @PreAuthorize("@ss.hasPermi('wms:statistics:warehouse')")
    public AjaxResult getWarehouseStock() {
        List<WmsStockRespVO> data = stockMapper.selectWarehouseStockSummary();
        return success(data);
    }

    /**
     * 获取财务汇总数据
     */
    @ApiOperation("获取财务汇总")
    @GetMapping("/financeSummary")
    @PreAuthorize("@ss.hasPermi('wms:statistics:finance')")
    public AjaxResult getFinanceSummary() {
        Map<String, Object> data = new HashMap<>();

        // 应收账款统计（未结清）
        long receivableCount = accountsReceivableMapper.selectCount(
                Wrappers.<WmsAccountsReceivable>lambdaQuery().eq(WmsAccountsReceivable::getArStatus, "0"));
        BigDecimal receivableAmount = accountsReceivableMapper.selectList(
                Wrappers.<WmsAccountsReceivable>lambdaQuery().eq(WmsAccountsReceivable::getArStatus, "0"))
                .stream().map(a -> a.getArAmount() != null ? a.getArAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> receivable = new HashMap<>();
        receivable.put("count", receivableCount);
        receivable.put("amount", receivableAmount);
        data.put("receivable", receivable);

        // 应付账款统计（未结清）
        long payableCount = accountsPayableMapper.selectCount(
                Wrappers.<WmsAccountsPayable>lambdaQuery().eq(WmsAccountsPayable::getApStatus, "0"));
        BigDecimal payableAmount = accountsPayableMapper.selectList(
                Wrappers.<WmsAccountsPayable>lambdaQuery().eq(WmsAccountsPayable::getApStatus, "0"))
                .stream().map(a -> a.getApAmount() != null ? a.getApAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> payable = new HashMap<>();
        payable.put("count", payableCount);
        payable.put("amount", payableAmount);
        data.put("payable", payable);

        // 本月收入（销售出库已收金额）
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyIn = salesOutMapper.selectList(
                        Wrappers.<WmsSalesOut>lambdaQuery().ge(WmsSalesOut::getCreateTime, monthStart))
                .stream().map(s -> s.getReceivedAmount() != null ? s.getReceivedAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 本月支出（采购入库已付金额）
        BigDecimal monthlyOut = purchaseInMapper.selectList(
                        Wrappers.<WmsPurchaseIn>lambdaQuery().ge(WmsPurchaseIn::getCreateTime, monthStart))
                .stream().map(p -> p.getPaidAmount() != null ? p.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.put("monthlyIn", monthlyIn);
        data.put("monthlyOut", monthlyOut);

        return success(data);
    }

    /**
     * 获取待办事项
     */
    @ApiOperation("获取待办事项")
    @GetMapping("/pendingTasks")
    @PreAuthorize("@ss.hasPermi('wms:statistics:tasks')")
    public AjaxResult getPendingTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();

        // 待审核采购订单
        long pendingPurchaseOrder = purchaseOrderMapper.selectCount(
                Wrappers.<WmsPurchaseOrder>lambdaQuery().eq(WmsPurchaseOrder::getOrderStatus, "0"));
        if (pendingPurchaseOrder > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "purchase");
            task.put("name", "待审核采购订单");
            task.put("count", pendingPurchaseOrder);
            task.put("url", "/wms/purchaseOrder");
            tasks.add(task);
        }

        // 待审核销售订单
        long pendingSalesOrder = salesOrderMapper.selectCount(
                Wrappers.<WmsSalesOrder>lambdaQuery().eq(WmsSalesOrder::getOrderStatus, "0"));
        if (pendingSalesOrder > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "sales");
            task.put("name", "待审核销售订单");
            task.put("count", pendingSalesOrder);
            task.put("url", "/wms/salesOrder");
            tasks.add(task);
        }

        // 待审核采购入库
        long pendingPurchaseIn = purchaseInMapper.selectCount(
                Wrappers.<WmsPurchaseIn>lambdaQuery().eq(WmsPurchaseIn::getStatus, "0"));
        if (pendingPurchaseIn > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "purchase");
            task.put("name", "待审核采购入库");
            task.put("count", pendingPurchaseIn);
            task.put("url", "/wms/purchaseIn");
            tasks.add(task);
        }

        // 待审核销售出库
        long pendingSalesOut = salesOutMapper.selectCount(
                Wrappers.<WmsSalesOut>lambdaQuery().eq(WmsSalesOut::getStatus, "0"));
        if (pendingSalesOut > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "sales");
            task.put("name", "待审核销售出库");
            task.put("count", pendingSalesOut);
            task.put("url", "/wms/salesOut");
            tasks.add(task);
        }

        // 库存预警（库存小于10）
        long lowStockCount = stockMapper.selectCount(
                Wrappers.<WmsStock>lambdaQuery().lt(WmsStock::getQuantity, new BigDecimal("10")));
        if (lowStockCount > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "stock");
            task.put("name", "库存预警");
            task.put("count", lowStockCount);
            task.put("url", "/wms/stock");
            tasks.add(task);
        }

        // 待收款
        long pendingPayment = accountsReceivableMapper.selectCount(
                Wrappers.<WmsAccountsReceivable>lambdaQuery().eq(WmsAccountsReceivable::getArStatus, "0"));
        if (pendingPayment > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "finance");
            task.put("name", "待收款单据");
            task.put("count", pendingPayment);
            task.put("url", "/wms/accountsReceivable");
            tasks.add(task);
        }

        // 待付款
        long pendingPay = accountsPayableMapper.selectCount(
                Wrappers.<WmsAccountsPayable>lambdaQuery().eq(WmsAccountsPayable::getApStatus, "0"));
        if (pendingPay > 0) {
            Map<String, Object> task = new HashMap<>();
            task.put("type", "finance");
            task.put("name", "待付款单据");
            task.put("count", pendingPay);
            task.put("url", "/wms/accountsPayable");
            tasks.add(task);
        }

        return success(tasks);
    }
}
