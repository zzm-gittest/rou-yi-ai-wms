package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.WmsPurchaseInItem;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.WmsPurchaseOrderItem;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.WmsAccountsPayable;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintItemRespVO;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInExportVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.mapper.WmsPurchaseInMapper;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.mapper.WmsTenantPrintProfileMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsPurchaseInItemService;
import com.ruoyi.wms.service.WmsPurchaseInService;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsStockLogService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.service.WmsPurchaseOrderItemService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsAccountsPayableService;
import com.ruoyi.wms.service.WmsFinanceLogService;
import com.ruoyi.wms.service.WmsAccountService;
import com.ruoyi.wms.service.WmsUserOnboardingService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 采购入库单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsPurchaseInServiceImpl extends ServiceImpl<WmsPurchaseInMapper, WmsPurchaseIn> implements WmsPurchaseInService {

    private static final Logger log = LoggerFactory.getLogger(WmsPurchaseInServiceImpl.class);

    @Autowired
    private WmsPurchaseInItemService itemService;

    @Autowired
    private WmsSupplierService supplierService;

    @Autowired
    private WmsWarehouseService warehouseService;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsGoodsService goodsService;

    @Autowired
    private WmsStockService stockService;

    @Autowired
    private WmsStockLogService stockLogService;

    @Autowired
    private WmsPurchaseOrderItemService purchaseOrderItemService;

    @Autowired
    private WmsPurchaseOrderService purchaseOrderService;

    @Autowired
    private WmsAccountsPayableService accountsPayableService;

    @Autowired
    private WmsFinanceLogService financeLogService;

    @Autowired
    private WmsAccountService accountService;

    @Autowired
    private WmsUserOnboardingService onboardingService;

    @Autowired
    private WmsBillApproveFlowService billApproveFlowService;

    @Autowired
    private WmsTenantPrintProfileMapper tenantPrintProfileMapper;

    @Override
    public IPage<WmsPurchaseInRespVO> page(WmsPurchaseInPageReqVO reqVO) {
        LambdaQueryWrapper<WmsPurchaseIn> wrapper = new LambdaQueryWrapper<WmsPurchaseIn>()
                .like(StringUtils.isNotEmpty(reqVO.getInNo()), WmsPurchaseIn::getInNo, reqVO.getInNo())
                .eq(reqVO.getSupplierId() != null, WmsPurchaseIn::getSupplierId, reqVO.getSupplierId())
                .eq(reqVO.getWarehouseId() != null, WmsPurchaseIn::getWarehouseId, reqVO.getWarehouseId())
                // 根据审核状态过滤：0-待审核(approveBy为空)，1-已审核(approveBy不为空)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY IS NULL OR TRIM(APPROVE_BY) = ''")
                .isNotNull(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsPurchaseIn::getApproveBy)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    "TRIM(APPROVE_BY) != ''")
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsPurchaseIn::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsPurchaseIn::getCreateTime)
                .orderByDesc(WmsPurchaseIn::getId);

        Page<WmsPurchaseIn> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsPurchaseInRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<WmsPurchaseInRespVO> respVOList = VoUtils.convertList(page.getRecords(), WmsPurchaseInRespVO.class);

        // 设置审核状态
        for (int i = 0; i < page.getRecords().size(); i++) {
            WmsPurchaseIn in = page.getRecords().get(i);
            WmsPurchaseInRespVO respVO = respVOList.get(i);
            String approveStatus = StringUtils.isNotBlank(in.getApproveBy()) ? "1" : "0";
            respVO.setApproveStatus(approveStatus);
        }

        // 填充供应商和仓库信息
        fillInfo(respVOList);

        resultPage.setRecords(respVOList);
        return resultPage;
    }

    @Override
    public WmsPurchaseInRespVO get(Long id) {
        WmsPurchaseIn in = getById(id);
        WmsPurchaseInRespVO respVO = VoUtils.convert(in, WmsPurchaseInRespVO.class);

        // 填充信息
        if (respVO != null) {
            // 确保sourceType和orderId被正确复制
            respVO.setSourceType(in.getSourceType());
            respVO.setOrderId(in.getOrderId());
            WmsSupplier supplier = supplierService.getById(respVO.getSupplierId());
            if (supplier != null) {
                respVO.setSupplierName(supplier.getSupplierName());
            }
            WmsWarehouse warehouse = warehouseService.getById(respVO.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getWarehouseName());
            }

            // 填充采购订单号
            if (respVO.getOrderId() != null) {
                var order = purchaseOrderService.getById(respVO.getOrderId());
                if (order != null) {
                    respVO.setOrderNo(order.getOrderNo());
                }
            }

            // 计算应付金额和付款状态
            BigDecimal payableAmount = respVO.getTotalAmount();
            if (payableAmount == null) {
                payableAmount = BigDecimal.ZERO;
            }
            BigDecimal paidAmount = respVO.getPaidAmount() != null ? respVO.getPaidAmount() : BigDecimal.ZERO;
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                respVO.setPaymentStatus("0"); // 未付
            } else if (paidAmount.compareTo(payableAmount) < 0) {
                respVO.setPaymentStatus("1"); // 部分
            } else {
                respVO.setPaymentStatus("2"); // 已付
            }
            respVO.setPayableAmount(payableAmount.subtract(paidAmount));

            // 填充明细
            List<WmsPurchaseInItem> items = itemService.lambdaQuery()
                    .eq(WmsPurchaseInItem::getInId, id)
                    .list();
            List<WmsPurchaseInItemRespVO> itemVOs = VoUtils.convertList(items, WmsPurchaseInItemRespVO.class);

            // 如果是订单入库，填充订单相关字段
            if (respVO.getOrderId() != null && !itemVOs.isEmpty()) {
                List<WmsPurchaseOrderItem> orderItems = purchaseOrderItemService.lambdaQuery()
                        .eq(WmsPurchaseOrderItem::getOrderId, respVO.getOrderId())
                        .list();
                // 创建订单item的映射
                java.util.Map<Long, WmsPurchaseOrderItem> orderItemMap = orderItems.stream()
                        .collect(java.util.stream.Collectors.toMap(WmsPurchaseOrderItem::getSkuId, item -> item));
                // 为每个item填充订单数量和已入库数量
                for (WmsPurchaseInItemRespVO itemVO : itemVOs) {
                    WmsPurchaseOrderItem orderItem = orderItemMap.get(itemVO.getSkuId());
                    if (orderItem != null) {
                        itemVO.setOrderQuantity(orderItem.getQuantity());
                        BigDecimal receivedQty = orderItem.getReceivedQty() != null ? orderItem.getReceivedQty() : BigDecimal.ZERO;
                        itemVO.setReceivedQty(receivedQty);
                    }
                }
            }

            fillPurchaseInItemGoodsName(itemVOs);
            fillPurchaseInItemBarcode(itemVOs);

            respVO.setItems(itemVOs);

            // 设置审核状态
            String approveStatus = StringUtils.isNotBlank(in.getApproveBy()) ? "1" : "0";
            respVO.setApproveStatus(approveStatus);
        }

        return respVO;
    }

    @Override
    public WmsBillPrintRespVO getPrintData(Long id) {
        WmsPurchaseIn bill = getById(id);
        if (bill == null) {
            throw new ServiceException("采购入库单不存在");
        }

        WmsPurchaseInRespVO detail = get(id);
        SysTenant tenant = getTenantPrintProfile();

        WmsBillPrintRespVO respVO = new WmsBillPrintRespVO();
        respVO.setTitle("采购入库单");
        respVO.setCompanyName(resolvePrintCompanyName(tenant));
        respVO.setCompanyAddress(tenant != null ? tenant.getPrintCompanyAddress() : null);
        respVO.setCompanyPhone(tenant != null ? tenant.getPrintCompanyPhone() : null);
        respVO.setLogoPath(tenant != null ? tenant.getPrintLogoPath() : null);
        respVO.setPrintFontScale(tenant != null ? tenant.getPrintFontScale() : java.math.BigDecimal.ONE);
        respVO.setPrintPageSize(tenant != null ? tenant.getPrintPageSize() : "A4");
        respVO.setBillNo(detail.getInNo());
        respVO.setBillDate(detail.getInDate() != null ? detail.getInDate().toString() : null);
        respVO.setWarehouseName(detail.getWarehouseName());
        respVO.setCounterpartName(detail.getSupplierName());
        respVO.setHandler(detail.getHandler());
        respVO.setApproveBy(detail.getApproveBy());
        respVO.setApproveTime(detail.getApproveTime());
        respVO.setTotalAmount(detail.getTotalAmount());
        respVO.setRemark(detail.getRemark());
        respVO.setPrintTime(LocalDateTime.now());
        respVO.setItems(buildPurchasePrintItems(detail.getItems()));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsPurchaseInSaveReqVO reqVO) {
        validateItemsPositive(reqVO.getItems());

        // 生成入库单号
        String inNo = generateInNo();

        // 计算入库总额
        BigDecimal totalAmount = calculateTotalAmount(reqVO.getItems());

        WmsPurchaseIn in = new WmsPurchaseIn();
        in.setInNo(inNo);
        in.setOrderId(reqVO.getOrderId());
        in.setSourceType(reqVO.getSourceType() != null ? reqVO.getSourceType() : 1);
        in.setSupplierId(reqVO.getSupplierId());
        in.setWarehouseId(reqVO.getWarehouseId());
        in.setInDate(reqVO.getInDate() != null ? reqVO.getInDate() : LocalDate.now());
        in.setTotalAmount(totalAmount);
        in.setPaidAmount(reqVO.getPaidAmount() != null ? reqVO.getPaidAmount() : BigDecimal.ZERO);
        in.setPaymentStatus("0");
        in.setAccountId(reqVO.getAccountId());
        in.setHandler(reqVO.getHandler());
        in.setRemark(reqVO.getRemark());
        save(in);

        // 保存明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsPurchaseInItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsPurchaseInItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsPurchaseInItem item = new WmsPurchaseInItem();
                        item.setInId(in.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(sku != null ? sku.getUnitId() : null);
                        item.setUnitName(sku != null ? sku.getUnitName() : null);
                        item.setQuantity(vo.getQuantity());
                        item.setPrice(vo.getPrice());
                        // 计算金额
                        BigDecimal amount = vo.getQuantity().multiply(vo.getPrice() != null ? vo.getPrice() : BigDecimal.ZERO);
                        item.setAmount(amount);
                        item.setRemark(vo.getRemark());
                        return item;
                    })
                    .collect(Collectors.toList());
            itemService.saveBatch(items);
        }

        // 新手任务要求是“创建采购入库单”，创建成功即标记完成。
        tryCompletePurchaseInOnboardingOnCreate(SecurityUtils.getLoginUser());

        billApproveFlowService.autoApproveAfterCreate(
            WmsApproveConstants.BILL_TYPE_PURCHASE_IN,
            in.getId(),
            in.getInNo(),
            () -> approve(in.getId())
        );

        return in.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsPurchaseInSaveReqVO reqVO) {
        validateItemsPositive(reqVO.getItems());
        validateCriticalFieldsForAutoApprovedBill(reqVO);

        // 计算入库总额
        BigDecimal totalAmount = calculateTotalAmount(reqVO.getItems());

        WmsPurchaseIn in = new WmsPurchaseIn();
        in.setId(reqVO.getId());
        in.setOrderId(reqVO.getOrderId());
        in.setSourceType(reqVO.getSourceType() != null ? reqVO.getSourceType() : 1);
        in.setSupplierId(reqVO.getSupplierId());
        in.setWarehouseId(reqVO.getWarehouseId());
        in.setInDate(reqVO.getInDate());
        in.setTotalAmount(totalAmount);
        in.setPaidAmount(reqVO.getPaidAmount() != null ? reqVO.getPaidAmount() : BigDecimal.ZERO);
        in.setAccountId(reqVO.getAccountId());
        in.setHandler(reqVO.getHandler());
        in.setRemark(reqVO.getRemark());
        updateById(in);

        // 删除原有明细
        itemService.lambdaUpdate()
                .eq(WmsPurchaseInItem::getInId, reqVO.getId())
                .remove();

        // 保存新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsPurchaseInItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsPurchaseInItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsPurchaseInItem item = new WmsPurchaseInItem();
                        item.setInId(reqVO.getId()); // 直接使用 reqVO.getId()
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(sku != null ? sku.getUnitId() : null);
                        item.setUnitName(sku != null ? sku.getUnitName() : null);
                        item.setQuantity(vo.getQuantity());
                        item.setPrice(vo.getPrice());
                        // 计算金额
                        BigDecimal amount = vo.getQuantity().multiply(vo.getPrice() != null ? vo.getPrice() : BigDecimal.ZERO);
                        item.setAmount(amount);
                        item.setRemark(vo.getRemark());
                        return item;
                    })
                    .collect(Collectors.toList());
            itemService.saveBatch(items);
        }
    }

    /**
     * 自动审核模式下，已审核单据禁止修改关键对账字段（数量/单价/明细结构）。
     */
    private void validateCriticalFieldsForAutoApprovedBill(WmsPurchaseInSaveReqVO reqVO) {
        if (reqVO == null || reqVO.getId() == null) {
            return;
        }

        // 非自动审核策略，允许按原流程修改。
        if (billApproveFlowService.needApprove(WmsApproveConstants.BILL_TYPE_PURCHASE_IN)) {
            return;
        }

        WmsPurchaseIn existed = getById(reqVO.getId());
        if (existed == null || StringUtils.isEmpty(existed.getApproveBy())) {
            return;
        }

        List<WmsPurchaseInItem> existedItems = itemService.lambdaQuery()
                .eq(WmsPurchaseInItem::getInId, reqVO.getId())
                .list();

        List<WmsPurchaseInItemSaveReqVO> newItems = reqVO.getItems() == null
                ? java.util.Collections.emptyList()
                : reqVO.getItems();

        if (existedItems.size() != newItems.size()) {
            throw new ServiceException("当前单据为自动审核且已审核，禁止新增/删除明细。");
        }

        Map<Long, WmsPurchaseInItem> existedMap = existedItems.stream()
                .collect(Collectors.toMap(WmsPurchaseInItem::getSkuId, item -> item, (a, b) -> a));

        for (WmsPurchaseInItemSaveReqVO item : newItems) {
            WmsPurchaseInItem old = existedMap.get(item.getSkuId());
            if (old == null) {
                throw new ServiceException("当前单据为自动审核且已审核，禁止调整明细商品。");
            }

            BigDecimal oldQty = old.getQuantity() == null ? BigDecimal.ZERO : old.getQuantity();
            BigDecimal newQty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            if (oldQty.compareTo(newQty) != 0) {
                throw new ServiceException("当前单据为自动审核且已审核，禁止修改数量。");
            }

            BigDecimal oldPrice = old.getPrice() == null ? BigDecimal.ZERO : old.getPrice();
            BigDecimal newPrice = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            if (oldPrice.compareTo(newPrice) != 0) {
                throw new ServiceException("当前单据为自动审核且已审核，禁止修改单价。");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsPurchaseIn in = getById(id);
        if (in == null) {
            throw new RuntimeException("入库单不存在");
        }

        // 删除明细
        itemService.lambdaUpdate()
                .eq(WmsPurchaseInItem::getInId, id)
                .remove();

        // 删除入库单
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsPurchaseIn in = getById(id);
        String billNo = in != null ? in.getInNo() : null;
        try {
            if (in == null) {
                throw new RuntimeException("入库单不存在");
            }
            if (in.getApproveBy() != null) {
                throw new RuntimeException("入库单已审核");
            }

            // 获取入库明细
            List<WmsPurchaseInItem> items = itemService.lambdaQuery()
                .eq(WmsPurchaseInItem::getInId, id)
                .list();

            if (items != null && !items.isEmpty()) {
                for (WmsPurchaseInItem item : items) {
                    // 如果是订单入库，更新采购订单明细的已入库数量
                    if (in.getOrderId() != null && item.getSkuId() != null) {
                        WmsPurchaseOrderItem orderItem = purchaseOrderItemService.lambdaQuery()
                                .eq(WmsPurchaseOrderItem::getOrderId, in.getOrderId())
                                .eq(WmsPurchaseOrderItem::getSkuId, item.getSkuId())
                                .one();
                        if (orderItem != null) {
                            BigDecimal orderQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : BigDecimal.ZERO;
                            BigDecimal currentReceived = orderItem.getReceivedQty() != null ? orderItem.getReceivedQty() : BigDecimal.ZERO;
                            BigDecimal newReceived = currentReceived.add(item.getQuantity());

                            // 校验：已入库数量 + 本次入库数量不能超过订单数量
                            if (newReceived.compareTo(orderQty) > 0) {
                                throw new ServiceException(String.format(
                                    "商品[%s]入库数量超限：订单数量=%s，已入库数量=%s，本次入库数量=%s",
                                    item.getSkuName() != null ? item.getSkuName() : item.getSkuId(),
                                    orderQty, currentReceived, item.getQuantity()
                                ));
                            }

                            orderItem.setReceivedQty(newReceived);
                            purchaseOrderItemService.updateById(orderItem);
                        }
                    }

                    // 查询现有库存
                    WmsStock stock = stockService.lambdaQuery()
                            .eq(WmsStock::getWarehouseId, in.getWarehouseId())
                            .eq(WmsStock::getSkuId, item.getSkuId())
                            .one();

                    if (stock != null) {
                        // 更新现有库存
                        BigDecimal beforeQty = stock.getQuantity();
                        BigDecimal newQuantity = stock.getQuantity().add(item.getQuantity());
                        stock.setQuantity(newQuantity);
                        // 更新成本价
                        BigDecimal inPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                        if (beforeQty.compareTo(BigDecimal.ZERO) <= 0 || newQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                            // 原库存为零或负数，或入库后仍为零/负数，直接采用本次采购价
                            stock.setCostPrice(inPrice);
                        } else {
                            // 正常移动加权平均
                            BigDecimal oldCostPrice = stock.getCostPrice() != null ? stock.getCostPrice() : BigDecimal.ZERO;
                            BigDecimal oldTotal = beforeQty.multiply(oldCostPrice);
                            BigDecimal newTotal = item.getQuantity().multiply(inPrice);
                            BigDecimal newCostPrice = oldTotal.add(newTotal).divide(newQuantity, 2, BigDecimal.ROUND_HALF_UP);
                            stock.setCostPrice(newCostPrice);
                        }
                        stockService.updateById(stock);

                        createStockLog(in.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "1", in.getInNo(), in.getId(),
                                       "1", item.getQuantity(), beforeQty, newQuantity, item.getPrice(), "采购入库");
                    } else {
                        // 创建新库存记录
                        BigDecimal beforeQty = BigDecimal.ZERO;
                        stock = new WmsStock();
                        stock.setWarehouseId(in.getWarehouseId());
                        stock.setSkuId(item.getSkuId());
                        stock.setGoodsId(item.getGoodsId());
                        stock.setQuantity(item.getQuantity());
                        stock.setCostPrice(item.getPrice());
                        stockService.save(stock);

                        createStockLog(in.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "1", in.getInNo(), in.getId(),
                                       "1", item.getQuantity(), beforeQty, item.getQuantity(), item.getPrice(), "采购入库");
                    }
                }

                // 采购入库审核后，将本单据成交价回写到SKU采购价（按SKU加权均价）。
                syncSkuPurchasePriceFromApprovedItems(items);
            }

            LoginUser user = SecurityUtils.getLoginUser();
            in.setApproveBy(user.getUsername());
            in.setApproveTime(LocalDateTime.now());
            in.setStatus("1");
            updateById(in);

            // 如果是订单入库，更新订单状态
            if (in.getOrderId() != null) {
                updateOrderStatusAfterIn(in.getOrderId());
            }

            // 生成应付账款
            accountsPayableService.createByBill(
                    in.getSupplierId(),
                    null, // customerId - 采购入库不涉及客户
                    "2", // 2=采购入库
                    in.getInNo(),
                    in.getId(),
                    in.getTotalAmount(),
                    in.getPaidAmount(),
                    in.getInDate()
            );

            tryCompletePurchaseInOnboarding(user);
            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_PURCHASE_IN, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_PURCHASE_IN, id, billNo, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApprove(Long id) {
        WmsPurchaseIn in = getById(id);
        if (in == null) {
            throw new RuntimeException("入库单不存在");
        }
        if (in.getApproveBy() == null) {
            throw new RuntimeException("入库单未审核，无需反审核");
        }

        // 获取入库明细
        List<WmsPurchaseInItem> items = itemService.lambdaQuery()
                .eq(WmsPurchaseInItem::getInId, id)
                .list();

        if (items != null && !items.isEmpty()) {
            for (WmsPurchaseInItem item : items) {
                // 回退库存
                WmsStock stock = stockService.lambdaQuery()
                        .eq(WmsStock::getWarehouseId, in.getWarehouseId())
                        .eq(WmsStock::getSkuId, item.getSkuId())
                        .one();

                if (stock != null) {
                    BigDecimal beforeQty = stock.getQuantity();
                    BigDecimal newQuantity = stock.getQuantity().subtract(item.getQuantity());
                    if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                        throw new RuntimeException("库存不足，无法反审核");
                    }
                    stock.setQuantity(newQuantity);
                    stockService.updateById(stock);

                    createStockLog(in.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "1", in.getInNo(), in.getId(),
                                   "2", item.getQuantity(), beforeQty, newQuantity, item.getPrice(), "采购入库反审核");
                }

                // 如果是订单入库，回退订单明细的已入库数量
                if (in.getOrderId() != null && item.getSkuId() != null) {
                    WmsPurchaseOrderItem orderItem = purchaseOrderItemService.lambdaQuery()
                            .eq(WmsPurchaseOrderItem::getOrderId, in.getOrderId())
                            .eq(WmsPurchaseOrderItem::getSkuId, item.getSkuId())
                            .one();
                    if (orderItem != null) {
                        BigDecimal currentReceived = orderItem.getReceivedQty() != null ? orderItem.getReceivedQty() : BigDecimal.ZERO;
                        BigDecimal newReceived = currentReceived.subtract(item.getQuantity());
                        if (newReceived.compareTo(BigDecimal.ZERO) < 0) {
                            throw new RuntimeException("订单已入库数量异常，无法反审核");
                        }
                        orderItem.setReceivedQty(newReceived);
                        purchaseOrderItemService.updateById(orderItem);
                    }
                }
            }
        }

        // 清空审核信息。使用set方式强制写入NULL，避免字段更新策略跳过null导致页面仍显示已审核。
        lambdaUpdate()
            .eq(WmsPurchaseIn::getId, id)
            .set(WmsPurchaseIn::getApproveBy, null)
            .set(WmsPurchaseIn::getApproveTime, null)
            .set(WmsPurchaseIn::getStatus, "0")
            .update();

        // 更新订单状态
        if (in.getOrderId() != null) {
            updateOrderStatusAfterIn(in.getOrderId());
        }

        // 删除应付账款记录
        accountsPayableService.lambdaQuery()
                .eq(WmsAccountsPayable::getBillId, id)
                .eq(WmsAccountsPayable::getBillType, "2")
                .list()
                .forEach(ap -> accountsPayableService.removeById(ap.getId()));
    }

    /**
     * 入库后更新订单状态
     * 如果所有商品都已入库，订单状态改为"全部入库(3)"，否则改为"部分入库(2)"
     */
    private void updateOrderStatusAfterIn(Long orderId) {
        // 获取订单所有明细
        List<WmsPurchaseOrderItem> orderItems = purchaseOrderItemService.lambdaQuery()
                .eq(WmsPurchaseOrderItem::getOrderId, orderId)
                .list();

        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        // 检查是否所有商品都已全部入库
        boolean allReceived = true;
        for (WmsPurchaseOrderItem orderItem : orderItems) {
            BigDecimal orderQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : BigDecimal.ZERO;
            BigDecimal receivedQty = orderItem.getReceivedQty() != null ? orderItem.getReceivedQty() : BigDecimal.ZERO;
            if (receivedQty.compareTo(orderQty) < 0) {
                allReceived = false;
                break;
            }
        }

        // 更新订单状态
        WmsPurchaseOrder order = purchaseOrderService.getById(orderId);
        if (order != null) {
            if (allReceived) {
                order.setOrderStatus("3"); // 全部入库
            } else {
                order.setOrderStatus("2"); // 部分入库
            }
            purchaseOrderService.updateById(order);
        }
    }

    private void syncSkuPurchasePriceFromApprovedItems(List<WmsPurchaseInItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Map<Long, BigDecimal> qtyBySku = new java.util.HashMap<>();
        Map<Long, BigDecimal> amountBySku = new java.util.HashMap<>();

        for (WmsPurchaseInItem item : items) {
            if (item == null || item.getSkuId() == null || item.getQuantity() == null || item.getPrice() == null) {
                continue;
            }
            if (item.getQuantity().compareTo(BigDecimal.ZERO) <= 0 || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            qtyBySku.merge(item.getSkuId(), item.getQuantity(), BigDecimal::add);
            amountBySku.merge(item.getSkuId(), item.getQuantity().multiply(item.getPrice()), BigDecimal::add);
        }

        if (qtyBySku.isEmpty()) {
            return;
        }

        List<WmsGoodsSku> skuList = goodsSkuService.lambdaQuery()
                .in(WmsGoodsSku::getId, qtyBySku.keySet())
                .list();

        for (WmsGoodsSku sku : skuList) {
            if (sku == null || sku.getId() == null) {
                continue;
            }
            BigDecimal qty = qtyBySku.get(sku.getId());
            BigDecimal amount = amountBySku.get(sku.getId());
            if (qty == null || amount == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal newPurchasePrice = amount.divide(qty, 2, BigDecimal.ROUND_HALF_UP);
            BigDecimal oldPurchasePrice = sku.getPurchasePrice();
            if (oldPurchasePrice == null || oldPurchasePrice.compareTo(newPurchasePrice) != 0) {
                sku.setPurchasePrice(newPurchasePrice);
                goodsSkuService.updateById(sku);
            }
        }
    }

    /**
     * 生成入库单号
     */
    private String generateInNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "RK" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxNo = baseMapper.selectMaxInNoIncludingDeleted(prefix, tenantId);

        int serial = 1;
        if (maxNo != null) {
            try {
                String serialStr = maxNo.substring(prefix.length());
                serial = Integer.parseInt(serialStr) + 1;
            } catch (Exception e) {
                // 使用默认值
            }
        }

        return prefix + String.format("%04d", serial);
    }

    /**
     * 计算入库总额
     */
    private BigDecimal calculateTotalAmount(List<WmsPurchaseInItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> item.getQuantity().multiply(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateItemsPositive(List<WmsPurchaseInItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (WmsPurchaseInItemSaveReqVO item : items) {
            if (item == null) {
                throw new ServiceException("入库明细不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("入库明细数量必须大于0");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("入库明细单价必须大于0");
            }
        }
    }

    /**
     * 批量填充信息
     */
    private void fillInfo(List<WmsPurchaseInRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        list.forEach(item -> {
            if (item.getSupplierId() != null) {
                WmsSupplier supplier = supplierService.getById(item.getSupplierId());
                if (supplier != null) {
                    item.setSupplierName(supplier.getSupplierName());
                }
            }
            if (item.getWarehouseId() != null) {
                WmsWarehouse warehouse = warehouseService.getById(item.getWarehouseId());
                if (warehouse != null) {
                    item.setWarehouseName(warehouse.getWarehouseName());
                }
            }

            // 填充采购订单号
            if (item.getOrderId() != null) {
                var order = purchaseOrderService.getById(item.getOrderId());
                if (order != null) {
                    item.setOrderNo(order.getOrderNo());
                }
            }

            // 计算应付金额和付款状态
            BigDecimal payableAmount = item.getTotalAmount();
            if (payableAmount == null) {
                payableAmount = BigDecimal.ZERO;
            }
            BigDecimal paidAmount = item.getPaidAmount() != null ? item.getPaidAmount() : BigDecimal.ZERO;
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                item.setPaymentStatus("0"); // 未付
            } else if (paidAmount.compareTo(payableAmount) < 0) {
                item.setPaymentStatus("1"); // 部分
            } else {
                item.setPaymentStatus("2"); // 已付
            }
            item.setPayableAmount(payableAmount.subtract(paidAmount));
        });
    }

    private void tryCompletePurchaseInOnboarding(LoginUser user) {
        try {
            if (user == null || user.getUserId() == null || StringUtils.isEmpty(user.getUsername())) {
                return;
            }

            Long approvedCount = lambdaQuery()
                    .eq(WmsPurchaseIn::getCreateBy, user.getUsername())
                    .isNotNull(WmsPurchaseIn::getApproveBy)
                    .count();
            if (approvedCount != null && approvedCount == 1L) {
                onboardingService.completeTask(user.getUserId(), "purchase_in");
            }
        } catch (Exception ex) {
            // 新手引导失败不应阻塞采购入库主流程
            log.warn("complete onboarding purchase_in failed", ex);
        }
    }

    private void tryCompletePurchaseInOnboardingOnCreate(LoginUser user) {
        try {
            if (user == null || user.getUserId() == null) {
                return;
            }
            onboardingService.completeTask(user.getUserId(), "purchase_in");
        } catch (Exception ex) {
            // 新手引导失败不应阻塞采购入库主流程
            log.warn("complete onboarding purchase_in on create failed", ex);
        }
    }

    private void createStockLog(Long warehouseId, Long skuId, Long goodsId, String billType, String billNo, Long billId,
                                String changeType, BigDecimal quantity, BigDecimal beforeQty, BigDecimal afterQty,
                                BigDecimal costPrice, String remark) {
        WmsStockLog log = new WmsStockLog();
        log.setWarehouseId(warehouseId);
        log.setSkuId(skuId);
        log.setGoodsId(goodsId);
        log.setBillType(billType);
        log.setBillNo(billNo);
        log.setBillId(billId);
        log.setChangeType(changeType);
        log.setQuantity(quantity);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(afterQty);
        log.setCostPrice(costPrice);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        stockLogService.save(log);
    }

    private List<WmsBillPrintItemRespVO> buildPurchasePrintItems(List<WmsPurchaseInItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查询条码，构建 skuId -> barcode 映射
        Set<Long> skuIds = items.stream()
                .map(WmsPurchaseInItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> skuBarcodeMap = skuIds.isEmpty()
                ? Collections.emptyMap()
                : goodsSkuService.listByIds(skuIds).stream()
                        .filter(sku -> StringUtils.isNotBlank(sku.getBarcode()))
                        .collect(Collectors.toMap(WmsGoodsSku::getId, WmsGoodsSku::getBarcode, (a, b) -> a));
        Set<Long> goodsIds = items.stream()
            .map(WmsPurchaseInItemRespVO::getGoodsId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, String> goodsHasSpecMap = goodsIds.isEmpty()
            ? Collections.emptyMap()
            : goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getHasSpec, (a, b) -> a));
        return items.stream().map(item -> {
            WmsBillPrintItemRespVO vo = new WmsBillPrintItemRespVO();
            vo.setGoodsName(item.getGoodsName());
            vo.setSkuName(item.getSkuName());
            String barcode = item.getSkuId() != null ? skuBarcodeMap.get(item.getSkuId()) : null;
            vo.setSkuCode(item.getSkuCode());
            vo.setBarcode(StringUtils.trimToEmpty(barcode));
            vo.setSpecInfo(item.getSpecInfo());
            vo.setHasSpec(item.getGoodsId() != null ? goodsHasSpecMap.get(item.getGoodsId()) : null);
            vo.setUnitName(item.getUnitName());
            vo.setQuantity(item.getQuantity());
            vo.setPrice(item.getPrice());
            vo.setAmount(item.getAmount());
            vo.setRemark(item.getRemark());
            return vo;
        }).collect(Collectors.toList());
    }

    private SysTenant getTenantPrintProfile() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            return null;
        }
        return tenantPrintProfileMapper.selectByTenantId(tenantId);
    }

    private String resolvePrintCompanyName(SysTenant tenant) {
        if (tenant == null) {
            return null;
        }
        if (StringUtils.isNotBlank(tenant.getPrintCompanyName())) {
            return tenant.getPrintCompanyName();
        }
        return tenant.getTenantName();
    }

    @Override
    public List<WmsPurchaseOrderItemRespVO> getOrderItems(Long orderId) {
        List<WmsPurchaseOrderItemRespVO> result = purchaseOrderItemService.lambdaQuery()
                .eq(WmsPurchaseOrderItem::getOrderId, orderId)
                .list()
                .stream()
                .map(item -> {
                    WmsPurchaseOrderItemRespVO vo = new WmsPurchaseOrderItemRespVO();
                    vo.setId(item.getId());
                    vo.setOrderId(item.getOrderId());
                    vo.setSkuId(item.getSkuId());
                    vo.setGoodsId(item.getGoodsId());
                    vo.setSkuCode(item.getSkuCode());
                    vo.setSkuName(item.getSkuName());
                    vo.setSpecInfo(item.getSpecInfo());
                    vo.setUnitId(item.getUnitId());
                    vo.setUnitName(item.getUnitName());
                    vo.setQuantity(item.getQuantity());
                    vo.setPrice(item.getPrice());
                    vo.setDiscount(item.getDiscount());
                    vo.setAmount(item.getAmount());
                    // receivedQty返回已入库数量（不修改原始值）
                    BigDecimal receivedQty = item.getReceivedQty() != null ? item.getReceivedQty() : BigDecimal.ZERO;
                    vo.setReceivedQty(receivedQty);
                    vo.setRemark(item.getRemark());
                    return vo;
                })
                .collect(Collectors.toList());
        fillPurchaseOrderItemGoodsName(result);
        return result;
    }

    @Override
    public Map<Long, BigDecimal> getPendingInboundQtyByOrder(Long orderId) {
        // 获取该订单下所有待审核（未审核）的入库单ID
        List<Long> pendingInIds = this.lambdaQuery()
                .eq(WmsPurchaseIn::getOrderId, orderId)
                .isNull(WmsPurchaseIn::getApproveBy)
                .select(WmsPurchaseIn::getId)
                .list()
                .stream()
                .map(WmsPurchaseIn::getId)
                .collect(Collectors.toList());
        if (pendingInIds.isEmpty()) {
            return Collections.emptyMap();
        }
        // 汇总每个 SKU 的待审核入库数量
        return itemService.lambdaQuery()
                .in(WmsPurchaseInItem::getInId, pendingInIds)
                .list()
                .stream()
                .collect(Collectors.toMap(
                        WmsPurchaseInItem::getSkuId,
                        item -> item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO,
                        BigDecimal::add
                ));
    }

    private void fillPurchaseInItemGoodsName(List<WmsPurchaseInItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsPurchaseInItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillPurchaseInItemBarcode(List<WmsPurchaseInItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsPurchaseInItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    private void fillPurchaseOrderItemGoodsName(List<WmsPurchaseOrderItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsPurchaseOrderItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    @Override
    public List<WmsPurchaseInRespVO> listApproved() {
        List<WmsPurchaseIn> list = lambdaQuery()
                .isNotNull(WmsPurchaseIn::getApproveBy)
                .orderByDesc(WmsPurchaseIn::getApproveTime)
                .list();

        return list.stream()
                .map(item -> {
                    WmsPurchaseInRespVO vo = new WmsPurchaseInRespVO();
                    vo.setId(item.getId());
                    vo.setInNo(item.getInNo());
                    vo.setSupplierId(item.getSupplierId());
                    vo.setWarehouseId(item.getWarehouseId());
                    vo.setInDate(item.getInDate());
                    vo.setTotalAmount(item.getTotalAmount());
                    vo.setPaidAmount(item.getPaidAmount());
                    vo.setHandler(item.getHandler());
                    vo.setRemark(item.getRemark());
                    vo.setApproveBy(item.getApproveBy());
                    // 填充供应商名称
                    if (item.getSupplierId() != null) {
                        var supplier = supplierService.getById(item.getSupplierId());
                        if (supplier != null) {
                            vo.setSupplierName(supplier.getSupplierName());
                        }
                    }
                    // 填充仓库名称
                    if (item.getWarehouseId() != null) {
                        WmsWarehouse warehouse = warehouseService.getById(item.getWarehouseId());
                        if (warehouse != null) {
                            vo.setWarehouseName(warehouse.getWarehouseName());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WmsPurchaseInRespVO> listAvailableForReturn() {
        // 查询已审核且未全部退货的入库单（退货状态为null、0或1）
        List<WmsPurchaseIn> list = lambdaQuery()
                .isNotNull(WmsPurchaseIn::getApproveBy)
                .and(wrapper -> wrapper.isNull(WmsPurchaseIn::getReturnStatus)
                        .or()
                        .in(WmsPurchaseIn::getReturnStatus, "0", "1"))
                .orderByDesc(WmsPurchaseIn::getApproveTime)
                .list();

        return list.stream()
                .map(item -> {
                    WmsPurchaseInRespVO vo = new WmsPurchaseInRespVO();
                    vo.setId(item.getId());
                    vo.setInNo(item.getInNo());
                    vo.setSupplierId(item.getSupplierId());
                    vo.setWarehouseId(item.getWarehouseId());
                    vo.setInDate(item.getInDate());
                    vo.setTotalAmount(item.getTotalAmount());
                    vo.setPaidAmount(item.getPaidAmount());
                    vo.setHandler(item.getHandler());
                    vo.setRemark(item.getRemark());
                    vo.setApproveBy(item.getApproveBy());
                    vo.setReturnStatus(item.getReturnStatus());
                    // 填充供应商名称
                    if (item.getSupplierId() != null) {
                        var supplier = supplierService.getById(item.getSupplierId());
                        if (supplier != null) {
                            vo.setSupplierName(supplier.getSupplierName());
                        }
                    }
                    // 填充仓库名称
                    if (item.getWarehouseId() != null) {
                        WmsWarehouse warehouse = warehouseService.getById(item.getWarehouseId());
                        if (warehouse != null) {
                            vo.setWarehouseName(warehouse.getWarehouseName());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WmsPurchaseInRespVO> listAvailableForReturn(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listAvailableForReturn();
        }
        List<java.util.Map<String, Object>> rows = baseMapper.searchAvailableForReturn(keyword.trim());
        return rows.stream().map(row -> {
            WmsPurchaseInRespVO vo = new WmsPurchaseInRespVO();
            vo.setId(toLong(row.get("id")));
            vo.setInNo(toStr(row.get("in_no")));
            vo.setSupplierId(toLong(row.get("supplier_id")));
            vo.setWarehouseId(toLong(row.get("warehouse_id")));
            vo.setSupplierName(toStr(row.get("supplier_name")));
            vo.setWarehouseName(toStr(row.get("warehouse_name")));
            vo.setApproveBy(toStr(row.get("approve_by")));
            vo.setReturnStatus(toStr(row.get("return_status")));
            vo.setRemark(toStr(row.get("remark")));
            return vo;
        }).collect(Collectors.toList());
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return null; }
    }

    private String toStr(Object val) {
        return val == null ? null : val.toString();
    }

    @Override
    public List<WmsPurchaseInExportVO> exportList(WmsPurchaseInPageReqVO reqVO) {
        // 1. 按查询条件查询所有入库单（不分页）
        LambdaQueryWrapper<WmsPurchaseIn> wrapper = new LambdaQueryWrapper<WmsPurchaseIn>()
                .like(StringUtils.isNotEmpty(reqVO.getInNo()), WmsPurchaseIn::getInNo, reqVO.getInNo())
                .eq(reqVO.getSupplierId() != null, WmsPurchaseIn::getSupplierId, reqVO.getSupplierId())
                .eq(reqVO.getWarehouseId() != null, WmsPurchaseIn::getWarehouseId, reqVO.getWarehouseId())
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY IS NULL OR TRIM(APPROVE_BY) = ''")
                .isNotNull(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsPurchaseIn::getApproveBy)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    "TRIM(APPROVE_BY) != ''")
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsPurchaseIn::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsPurchaseIn::getCreateTime)
                .orderByDesc(WmsPurchaseIn::getId);

        List<WmsPurchaseIn> list = list(wrapper);
        if (list.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. 批量查询供应商名称
        Set<Long> supplierIds = list.stream().map(WmsPurchaseIn::getSupplierId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> supplierNameMap = supplierIds.isEmpty() ? java.util.Collections.emptyMap() :
                supplierService.listByIds(supplierIds).stream()
                        .collect(Collectors.toMap(WmsSupplier::getId, WmsSupplier::getSupplierName));

        // 3. 批量查询仓库名称
        Set<Long> warehouseIds = list.stream().map(WmsPurchaseIn::getWarehouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> warehouseNameMap = warehouseIds.isEmpty() ? java.util.Collections.emptyMap() :
                warehouseService.listByIds(warehouseIds).stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getWarehouseName));

        // 4. 批量查询所有明细（一次性查出所有入库单的明细）
        Set<Long> inIds = list.stream().map(WmsPurchaseIn::getId).collect(Collectors.toSet());
        List<WmsPurchaseInItem> allItems = itemService.lambdaQuery()
                .in(WmsPurchaseInItem::getInId, inIds)
                .list();
        Map<Long, List<WmsPurchaseInItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsPurchaseInItem::getInId));

        // 5. 批量查询商品名称（用于明细中的商品名称）
        Set<Long> goodsIds = allItems.stream().map(WmsPurchaseInItem::getGoodsId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? java.util.Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream()
                        .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        // 5b. 批量查询采购订单号
        Set<Long> orderIds = list.stream().map(WmsPurchaseIn::getOrderId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> orderNoMap = orderIds.isEmpty() ? java.util.Collections.emptyMap() :
                purchaseOrderService.listByIds(orderIds).stream()
                        .collect(Collectors.toMap(WmsPurchaseOrder::getId, WmsPurchaseOrder::getOrderNo));

        // 6. 组装导出数据
        List<WmsPurchaseInExportVO> exportList = new java.util.ArrayList<>();
        for (WmsPurchaseIn in : list) {
            String approveStatus = StringUtils.isNotBlank(in.getApproveBy()) ? "1" : "0";
            String supplierName = supplierNameMap.get(in.getSupplierId());
            String warehouseName = warehouseNameMap.get(in.getWarehouseId());

            List<WmsPurchaseInItem> items = itemMap.getOrDefault(in.getId(), java.util.Collections.emptyList());
            String orderNo = orderNoMap.get(in.getOrderId());
            if (items.isEmpty()) {
                WmsPurchaseInExportVO vo = buildPurchaseInExportRow(in, null,
                        supplierName, warehouseName, approveStatus);
                vo.setOrderNo(orderNo);
                exportList.add(vo);
            } else {
                for (WmsPurchaseInItem item : items) {
                    // 填充商品名称
                    String goodsName = goodsNameMap.get(item.getGoodsId());
                    WmsPurchaseInExportVO vo = buildPurchaseInExportRow(in, item,
                            supplierName, warehouseName, approveStatus);
                    vo.setGoodsName(goodsName);
                    vo.setOrderNo(orderNo);
                    exportList.add(vo);
                }
            }
        }
        return exportList;
    }

    private WmsPurchaseInExportVO buildPurchaseInExportRow(WmsPurchaseIn in, WmsPurchaseInItem item,
            String supplierName, String warehouseName, String approveStatus) {
        WmsPurchaseInExportVO vo = new WmsPurchaseInExportVO();
        vo.setInNo(in.getInNo());
        vo.setInDate(in.getInDate());
        vo.setSupplierName(supplierName);
        vo.setWarehouseName(warehouseName);
        vo.setSourceType(in.getSourceType());
        vo.setTotalAmount(in.getTotalAmount());
        vo.setPaymentStatus(in.getPaymentStatus());
        vo.setApproveStatus(approveStatus);
        vo.setRemark(in.getRemark());
        vo.setCreateTime(in.getCreateTime());

        if (item != null) {
            vo.setSkuCode(item.getSkuCode());
            vo.setSkuName(item.getSkuName());
            vo.setQuantity(item.getQuantity());
            vo.setPrice(item.getPrice());
            vo.setAmount(item.getAmount());
        }
        return vo;
    }
}
