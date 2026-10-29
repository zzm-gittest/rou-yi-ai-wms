package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsGoodsSkuPriceLog;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.WmsPurchaseOrderItem;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderExportVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderRespVO;
import com.ruoyi.wms.domain.vo.purchaseOrder.WmsPurchaseOrderSaveReqVO;
import com.ruoyi.wms.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.wms.service.WmsPurchaseOrderItemService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsCategoryService;
import com.ruoyi.wms.service.WmsUnitService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import com.ruoyi.wms.mapper.WmsGoodsSkuPriceLogMapper;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsUnit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 采购订单表 Service实现
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class WmsPurchaseOrderServiceImpl extends ServiceImpl<WmsPurchaseOrderMapper, WmsPurchaseOrder> implements WmsPurchaseOrderService {

    private final WmsPurchaseOrderItemService itemService;
    private final WmsSupplierService supplierService;
    private final WmsGoodsSkuService goodsSkuService;
    private final WmsGoodsService goodsService;
    private final WmsGoodsCategoryService categoryService;
    private final WmsUnitService unitService;
    private final WmsBillApproveFlowService billApproveFlowService;
    private final WmsGoodsSkuPriceLogMapper goodsSkuPriceLogMapper;

    public WmsPurchaseOrderServiceImpl(WmsPurchaseOrderItemService itemService,
                                        WmsSupplierService supplierService,
                                        WmsGoodsSkuService goodsSkuService,
                                        WmsGoodsService goodsService,
                                        WmsGoodsCategoryService categoryService,
                                        WmsUnitService unitService,
                                        WmsBillApproveFlowService billApproveFlowService,
                                        WmsGoodsSkuPriceLogMapper goodsSkuPriceLogMapper) {
        this.itemService = itemService;
        this.supplierService = supplierService;
        this.goodsSkuService = goodsSkuService;
        this.goodsService = goodsService;
        this.categoryService = categoryService;
        this.unitService = unitService;
        this.billApproveFlowService = billApproveFlowService;
        this.goodsSkuPriceLogMapper = goodsSkuPriceLogMapper;
    }

    @Override
    public IPage<WmsPurchaseOrderRespVO> page(WmsPurchaseOrderPageReqVO reqVO) {
        LambdaQueryWrapper<WmsPurchaseOrder> wrapper = new LambdaQueryWrapper<WmsPurchaseOrder>()
                .like(StringUtils.isNotEmpty(reqVO.getOrderNo()), WmsPurchaseOrder::getOrderNo, reqVO.getOrderNo())
                .eq(reqVO.getSupplierId() != null, WmsPurchaseOrder::getSupplierId, reqVO.getSupplierId())
                .eq(StringUtils.isNotEmpty(reqVO.getOrderStatus()), WmsPurchaseOrder::getOrderStatus, reqVO.getOrderStatus())
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsPurchaseOrder::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsPurchaseOrder::getCreateTime)
                .orderByDesc(WmsPurchaseOrder::getId);

        Page<WmsPurchaseOrder> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsPurchaseOrderRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<WmsPurchaseOrderRespVO> respVOList = VoUtils.convertList(page.getRecords(), WmsPurchaseOrderRespVO.class);

        // 填充供应商名称和应付金额
        fillSupplierInfo(respVOList);

        resultPage.setRecords(respVOList);
        return resultPage;
    }

    @Override
    public WmsPurchaseOrderRespVO get(Long id) {
        WmsPurchaseOrder order = getById(id);
        WmsPurchaseOrderRespVO respVO = VoUtils.convert(order, WmsPurchaseOrderRespVO.class);

        // 填充供应商名称
        if (respVO != null) {
            WmsSupplier supplier = supplierService.getById(respVO.getSupplierId());
            if (supplier != null) {
                respVO.setSupplierName(supplier.getSupplierName());
            }

            // 计算应付金额
            BigDecimal payableAmount = respVO.getTotalAmount();
            if (payableAmount == null) {
                payableAmount = BigDecimal.ZERO;
            }
            BigDecimal discountAmount = respVO.getDiscountAmount();
            if (discountAmount != null) {
                payableAmount = payableAmount.subtract(discountAmount);
            }
            respVO.setPayableAmount(payableAmount);

            // 计算付款状态
            BigDecimal paidAmount = respVO.getPaidAmount() != null ? respVO.getPaidAmount() : BigDecimal.ZERO;
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                respVO.setPaymentStatus("0"); // 未付
            } else if (paidAmount.compareTo(payableAmount) < 0) {
                respVO.setPaymentStatus("1"); // 部分
            } else {
                respVO.setPaymentStatus("2"); // 已付
            }

            // 填充明细
            List<WmsPurchaseOrderItem> items = itemService.lambdaQuery()
                    .eq(WmsPurchaseOrderItem::getOrderId, id)
                    .list();
                List<WmsPurchaseOrderItemRespVO> itemVOs = VoUtils.convertList(items, WmsPurchaseOrderItemRespVO.class);
                fillGoodsName(itemVOs);
                fillBarcode(itemVOs);
                respVO.setItems(itemVOs);
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsPurchaseOrderSaveReqVO reqVO) {
        validateItemsPositive(reqVO.getItems());

        // 生成订单编号
        String orderNo = generateOrderNo();

        // 计算订单总额
        BigDecimal totalAmount = calculateTotalAmount(reqVO.getItems());

        WmsPurchaseOrder order = new WmsPurchaseOrder();
        order.setOrderNo(orderNo);
        order.setSupplierId(reqVO.getSupplierId());
        order.setOrderDate(reqVO.getOrderDate() != null ? reqVO.getOrderDate() : LocalDate.now());
        order.setDeliveryDate(reqVO.getDeliveryDate());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(reqVO.getDiscountAmount() != null ? reqVO.getDiscountAmount() : BigDecimal.ZERO);
        order.setPaidAmount(reqVO.getPaidAmount() != null ? reqVO.getPaidAmount() : BigDecimal.ZERO);
        order.setPaymentStatus("0");
        order.setOrderStatus("0");
        order.setAccountId(reqVO.getAccountId());
        order.setHandler(reqVO.getHandler());
        order.setRemark(reqVO.getRemark());
        order.setCreateTime(LocalDateTime.now());
        save(order);

        // 保存明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 先处理需要自动创建商品的情况
            for (WmsPurchaseOrderItemSaveReqVO vo : reqVO.getItems()) {
                log.info("处理订单明细: skuId={}, goodsName='{}'", vo.getSkuId(), vo.getGoodsName());
                if (vo.getSkuId() == null && StringUtils.isNotEmpty(vo.getGoodsName())) {
                    log.info("开始自动创建商品和SKU: goodsName='{}'", vo.getGoodsName());
                    try {
                        Long newSkuId = createGoodsAndSku(vo);
                        vo.setSkuId(newSkuId);
                        log.info("自动创建商品成功: newSkuId={}", newSkuId);
                    } catch (Exception e) {
                        log.error("自动创建商品失败: goodsName='{}'", vo.getGoodsName(), e);
                        throw new RuntimeException("自动创建商品失败: " + e.getMessage(), e);
                    }
                } else if (vo.getSkuId() == null) {
                    throw new IllegalArgumentException("商品名称和SKU ID不能同时为空");
                }
            }

            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsPurchaseOrderItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsPurchaseOrderItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsUnit resolvedUnit = resolveOrderItemUnit(vo, sku);
                        WmsPurchaseOrderItem item = new WmsPurchaseOrderItem();
                        item.setOrderId(order.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(resolvedUnit != null ? resolvedUnit.getId() : (sku != null ? sku.getUnitId() : null));
                        item.setUnitName(resolvedUnit != null ? resolvedUnit.getUnitName() : (sku != null ? sku.getUnitName() : null));
                        item.setQuantity(vo.getQuantity());
                        item.setPrice(vo.getPrice());
                        item.setDiscount(vo.getDiscount() != null ? vo.getDiscount() : BigDecimal.ZERO);
                        // 计算金额
                        BigDecimal amount = vo.getQuantity().multiply(vo.getPrice() != null ? vo.getPrice() : BigDecimal.ZERO);
                        BigDecimal discount = vo.getDiscount() != null ? vo.getDiscount() : BigDecimal.ZERO;
                        item.setAmount(amount.subtract(discount));
                        item.setReceivedQty(BigDecimal.ZERO);
                        item.setRemark(vo.getRemark());
                        return item;
                    })
                    .collect(Collectors.toList());
            itemService.saveBatch(items);
        }

        billApproveFlowService.autoApproveAfterCreate(
                WmsApproveConstants.BILL_TYPE_PURCHASE_ORDER,
                order.getId(),
                order.getOrderNo(),
                () -> approve(order.getId())
        );

        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsPurchaseOrderSaveReqVO reqVO) {
        validateItemsPositive(reqVO.getItems());

        // 计算订单总额
        BigDecimal totalAmount = calculateTotalAmount(reqVO.getItems());

        WmsPurchaseOrder order = new WmsPurchaseOrder();
        order.setId(reqVO.getId());
        order.setSupplierId(reqVO.getSupplierId());
        order.setOrderDate(reqVO.getOrderDate());
        order.setDeliveryDate(reqVO.getDeliveryDate());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(reqVO.getDiscountAmount() != null ? reqVO.getDiscountAmount() : BigDecimal.ZERO);
        order.setPaidAmount(reqVO.getPaidAmount() != null ? reqVO.getPaidAmount() : BigDecimal.ZERO);
        order.setAccountId(reqVO.getAccountId());
        order.setHandler(reqVO.getHandler());
        order.setRemark(reqVO.getRemark());
        updateById(order);

        // 删除原有明细
        itemService.lambdaUpdate()
                .eq(WmsPurchaseOrderItem::getOrderId, reqVO.getId())
                .remove();

        // 保存新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsPurchaseOrderItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsPurchaseOrderItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsUnit resolvedUnit = resolveOrderItemUnit(vo, sku);
                        WmsPurchaseOrderItem item = new WmsPurchaseOrderItem();
                        item.setOrderId(order.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(resolvedUnit != null ? resolvedUnit.getId() : (sku != null ? sku.getUnitId() : null));
                        item.setUnitName(resolvedUnit != null ? resolvedUnit.getUnitName() : (sku != null ? sku.getUnitName() : null));
                        item.setQuantity(vo.getQuantity());
                        item.setPrice(vo.getPrice());
                        item.setDiscount(vo.getDiscount() != null ? vo.getDiscount() : BigDecimal.ZERO);
                        // 计算金额
                        BigDecimal amount = vo.getQuantity().multiply(vo.getPrice() != null ? vo.getPrice() : BigDecimal.ZERO);
                        BigDecimal discount = vo.getDiscount() != null ? vo.getDiscount() : BigDecimal.ZERO;
                        item.setAmount(amount.subtract(discount));
                        item.setRemark(vo.getRemark());
                        return item;
                    })
                    .collect(Collectors.toList());
            itemService.saveBatch(items);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsPurchaseOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        // 删除明细
        itemService.lambdaUpdate()
                .eq(WmsPurchaseOrderItem::getOrderId, id)
                .remove();

        // 删除订单
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsPurchaseOrder order = getById(id);
        String billNo = order != null ? order.getOrderNo() : null;
        try {
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (!"0".equals(order.getOrderStatus())) {
                throw new RuntimeException("订单状态不是待审核");
            }

            LoginUser user = SecurityUtils.getLoginUser();
            order.setOrderStatus("1");
            order.setApproveBy(user.getUsername());
            order.setApproveTime(LocalDateTime.now());
            updateById(order);

            syncSkuPurchasePriceByOrder(order, user.getUsername());

            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_PURCHASE_ORDER, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_PURCHASE_ORDER, id, billNo, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        WmsPurchaseOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if ("4".equals(order.getOrderStatus())) {
            throw new RuntimeException("订单已关闭");
        }
        // 只有待审核状态才能关闭，已审核后的订单可能已产生关联业务数据
        if (!"0".equals(order.getOrderStatus())) {
            throw new RuntimeException("只有待审核状态的订单才能关闭");
        }

        order.setOrderStatus("4");
        updateById(order);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CG" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxNo = baseMapper.selectMaxOrderNoIncludingDeleted(prefix, tenantId);

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
     * 计算订单总额
     */
    private BigDecimal calculateTotalAmount(List<WmsPurchaseOrderItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(item -> {
                    BigDecimal amount = item.getQuantity().multiply(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                    BigDecimal discount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
                    return amount.subtract(discount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private WmsUnit resolveOrderItemUnit(WmsPurchaseOrderItemSaveReqVO vo, WmsGoodsSku sku) {
        if (vo != null && StringUtils.isNotEmpty(vo.getUnit())) {
            return getOrCreateUnit(vo.getUnit());
        }
        if (sku != null && sku.getUnitId() != null) {
            return unitService.getById(sku.getUnitId());
        }
        return null;
    }

    private void validateItemsPositive(List<WmsPurchaseOrderItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (WmsPurchaseOrderItemSaveReqVO item : items) {
            if (item == null) {
                throw new ServiceException("采购订单明细不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("采购订单明细数量必须大于0");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("采购订单明细单价必须大于0");
            }
        }
    }

    /**
     * 自动创建商品和SKU（用于语音录入时商品不存在的情况）
     *
     * @param item 订单明细项，包含商品名称、规格、单位等信息
     * @return 新创建的SKU ID
     */
    private Long createGoodsAndSku(WmsPurchaseOrderItemSaveReqVO item) {
        // 1. 获取或创建默认分类
        String categoryId = getOrCreateDefaultCategory();

        // 2. 获取或创建单位
        Long unitId = null;
        String unitName = null;
        if (StringUtils.isNotEmpty(item.getUnit())) {
            WmsUnit unit = getOrCreateUnit(item.getUnit());
            unitId = unit.getId();
            unitName = unit.getUnitName();
        }

        // 3. 创建商品
        WmsGoodsSaveReqVO goodsReq = new WmsGoodsSaveReqVO();
        goodsReq.setGoodsName(item.getGoodsName());
        goodsReq.setCategoryId(categoryId);
        goodsReq.setHasSpec("0"); // 无规格
        goodsReq.setStatus("0"); // 正常
        goodsReq.setRemark("语音录入自动创建");

        Long goodsId = goodsService.create(goodsReq);

        // 4. 创建SKU
        String skuCode = goodsService.generateSkuCode(goodsId);

        WmsGoodsSkuSaveReqVO skuReq = new WmsGoodsSkuSaveReqVO();
        skuReq.setGoodsId(goodsId);
        skuReq.setSkuCode(skuCode);
        skuReq.setSkuName(item.getGoodsName());
        skuReq.setSpecInfo(StringUtils.isNotEmpty(item.getSpec()) ? item.getSpec() : "默认规格");
        skuReq.setUnitId(unitId);
        skuReq.setPurchasePrice(item.getPrice()); // 使用采购价
        skuReq.setSalePrice(item.getPrice()); // 默认销售价=采购价
        skuReq.setStatus("0"); // 正常

        Long skuId = goodsSkuService.create(skuReq);

        log.info("自动创建商品和SKU成功: goodsId={}, skuId={}, goodsName={}", goodsId, skuId, item.getGoodsName());

        return skuId;
    }

    /**
     * 获取或创建默认商品分类
     *
     * @return 分类ID
     */
    private String getOrCreateDefaultCategory() {
        // 先尝试查找第一个可用的分类
        var category = categoryService.lambdaQuery()
                .eq(com.ruoyi.wms.domain.WmsGoodsCategory::getStatus, "0")
                .orderByAsc(com.ruoyi.wms.domain.WmsGoodsCategory::getId)
                .last("LIMIT 1")
                .one();

        if (category != null) {
            return String.valueOf(category.getId());
        }

        // 没有分类则创建默认分类
        WmsGoodsCategorySaveReqVO categoryReq = new WmsGoodsCategorySaveReqVO();
        categoryReq.setCategoryName("默认分类");
        categoryReq.setStatus("0");
        categoryReq.setRemark("语音录入自动创建");

        Long categoryId = categoryService.create(categoryReq);
        log.info("自动创建默认商品分类: categoryId={}", categoryId);

        return String.valueOf(categoryId);
    }

    /**
     * 获取或创建单位
     *
     * @param unitName 单位名称
     * @return 单位实体
     */
    private WmsUnit getOrCreateUnit(String unitName) {
        // 先查找是否已存在
        WmsUnit unit = unitService.lambdaQuery()
                .eq(WmsUnit::getUnitName, unitName)
                .one();

        if (unit != null) {
            return unit;
        }

        // 不存在则创建
        WmsUnitSaveReqVO unitReq = new WmsUnitSaveReqVO();
        unitReq.setUnitName(unitName);
        unitReq.setStatus("0");

        Long unitId = unitService.create(unitReq);
        log.info("自动创建单位: unitId={}, unitName={}", unitId, unitName);

        return unitService.getById(unitId);
    }

    /**
     * 批量填充供应商信息
     */
    private void fillSupplierInfo(List<WmsPurchaseOrderRespVO> list) {
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

            // 计算应付金额和付款状态
            BigDecimal payableAmount = item.getTotalAmount();
            if (payableAmount == null) {
                payableAmount = BigDecimal.ZERO;
            }
            BigDecimal discountAmount = item.getDiscountAmount();
            if (discountAmount != null) {
                payableAmount = payableAmount.subtract(discountAmount);
            }
            item.setPayableAmount(payableAmount);

            // 计算付款状态
            BigDecimal paidAmount = item.getPaidAmount() != null ? item.getPaidAmount() : BigDecimal.ZERO;
            if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
                item.setPaymentStatus("0"); // 未付
            } else if (paidAmount.compareTo(payableAmount) < 0) {
                item.setPaymentStatus("1"); // 部分
            } else {
                item.setPaymentStatus("2"); // 已付
            }
        });
    }

    private void fillGoodsName(List<WmsPurchaseOrderItemRespVO> items) {
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

    private void fillBarcode(List<WmsPurchaseOrderItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsPurchaseOrderItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    @Override
    public java.util.List<WmsPurchaseOrderRespVO> listAvailableForIn() {
        // 查询已审核且未全部入库的订单（状态为1=已审核 或 2=部分入库）
        java.util.List<WmsPurchaseOrder> list = lambdaQuery()
                .in(WmsPurchaseOrder::getOrderStatus, "1", "2")
                .orderByDesc(WmsPurchaseOrder::getApproveTime)
                .list();

        return list.stream()
                .map(item -> {
                    WmsPurchaseOrderRespVO vo = new WmsPurchaseOrderRespVO();
                    vo.setId(item.getId());
                    vo.setOrderNo(item.getOrderNo());
                    vo.setSupplierId(item.getSupplierId());
                    vo.setOrderDate(item.getOrderDate());
                    vo.setTotalAmount(item.getTotalAmount());
                    vo.setDiscountAmount(item.getDiscountAmount());
                    vo.setOrderStatus(item.getOrderStatus());
                    // 填充供应商名称
                    if (item.getSupplierId() != null) {
                        WmsSupplier supplier = supplierService.getById(item.getSupplierId());
                        if (supplier != null) {
                            vo.setSupplierName(supplier.getSupplierName());
                        }
                    }
                    return vo;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    private void syncSkuPurchasePriceByOrder(WmsPurchaseOrder order, String operator) {
        List<WmsPurchaseOrderItem> items = itemService.lambdaQuery()
                .eq(WmsPurchaseOrderItem::getOrderId, order.getId())
                .list();
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Long> skuIds = items.stream()
                .map(WmsPurchaseOrderItem::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }

        Map<Long, WmsGoodsSku> skuMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, s -> s, (a, b) -> a));

        Long tenantId = SecurityUtils.getTenantId();
        LocalDateTime operateTime = LocalDateTime.now();

        for (WmsPurchaseOrderItem item : items) {
            if (item.getSkuId() == null || item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            WmsGoodsSku sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                continue;
            }

            BigDecimal oldPrice = sku.getPurchasePrice();
            BigDecimal newPrice = item.getPrice();

            WmsGoodsSku updateSku = new WmsGoodsSku();
            updateSku.setId(sku.getId());
            updateSku.setPurchasePrice(newPrice);
            goodsSkuService.updateById(updateSku);

            WmsGoodsSkuPriceLog log = new WmsGoodsSkuPriceLog();
            log.setTenantId(tenantId);
            log.setSkuId(sku.getId());
            log.setGoodsId(sku.getGoodsId());
            log.setSkuCode(sku.getSkuCode());
            log.setSkuName(sku.getSkuName());
            log.setOrderType(WmsApproveConstants.BILL_TYPE_PURCHASE_ORDER);
            log.setOrderId(order.getId());
            log.setOrderNo(order.getOrderNo());
            log.setOrderItemId(item.getId());
            log.setPriceType("PURCHASE");
            log.setOldPrice(oldPrice);
            log.setNewPrice(newPrice);
            log.setOperator(operator);
            log.setOperateTime(operateTime);
            goodsSkuPriceLogMapper.insert(log);
        }
    }

    @Override
    public List<WmsPurchaseOrderExportVO> exportList(WmsPurchaseOrderPageReqVO reqVO) {
        // 1. 按条件查询（不分页）
        LambdaQueryWrapper<WmsPurchaseOrder> wrapper = new LambdaQueryWrapper<WmsPurchaseOrder>()
                .like(StringUtils.isNotEmpty(reqVO.getOrderNo()), WmsPurchaseOrder::getOrderNo, reqVO.getOrderNo())
                .eq(reqVO.getSupplierId() != null, WmsPurchaseOrder::getSupplierId, reqVO.getSupplierId())
                .eq(StringUtils.isNotEmpty(reqVO.getOrderStatus()), WmsPurchaseOrder::getOrderStatus, reqVO.getOrderStatus())
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsPurchaseOrder::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsPurchaseOrder::getCreateTime);
        List<WmsPurchaseOrder> list = list(wrapper);
        if (list.isEmpty()) return java.util.Collections.emptyList();

        // 2. 批量查供应商
        Set<Long> supplierIds = list.stream().map(WmsPurchaseOrder::getSupplierId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> supplierNameMap = supplierIds.isEmpty() ? Collections.emptyMap() :
                supplierService.listByIds(supplierIds).stream().collect(Collectors.toMap(WmsSupplier::getId, WmsSupplier::getSupplierName));

        // 3. 批量查明细
        Set<Long> orderIds = list.stream().map(WmsPurchaseOrder::getId).collect(Collectors.toSet());
        List<WmsPurchaseOrderItem> allItems = itemService.lambdaQuery().in(WmsPurchaseOrderItem::getOrderId, orderIds).list();
        Map<Long, List<WmsPurchaseOrderItem>> itemMap = allItems.stream().collect(Collectors.groupingBy(WmsPurchaseOrderItem::getOrderId));

        // 4. 批量查商品名称
        Set<Long> goodsIds = allItems.stream().map(WmsPurchaseOrderItem::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        // 5. 组装
        List<WmsPurchaseOrderExportVO> exportList = new java.util.ArrayList<>();
        for (WmsPurchaseOrder order : list) {
            List<WmsPurchaseOrderItem> items = itemMap.getOrDefault(order.getId(), Collections.emptyList());
            for (WmsPurchaseOrderItem item : items) {
                WmsPurchaseOrderExportVO vo = new WmsPurchaseOrderExportVO();
                vo.setOrderNo(order.getOrderNo());
                vo.setOrderDate(order.getOrderDate());
                vo.setDeliveryDate(order.getDeliveryDate());
                vo.setSupplierName(supplierNameMap.get(order.getSupplierId()));
                vo.setGoodsName(goodsNameMap.get(item.getGoodsId()));
                vo.setSkuName(item.getSkuName());
                vo.setSkuCode(item.getSkuCode());
                vo.setQuantity(item.getQuantity());
                vo.setPrice(item.getPrice());
                vo.setAmount(item.getAmount());
                vo.setTotalAmount(order.getTotalAmount());
                vo.setDiscountAmount(order.getDiscountAmount());
                vo.setPaymentStatus(order.getPaymentStatus());
                vo.setOrderStatus(order.getOrderStatus());
                vo.setHandler(order.getHandler());
                vo.setRemark(order.getRemark());
                vo.setCreateTime(order.getCreateTime());
                exportList.add(vo);
            }
        }
        return exportList;
    }
}
