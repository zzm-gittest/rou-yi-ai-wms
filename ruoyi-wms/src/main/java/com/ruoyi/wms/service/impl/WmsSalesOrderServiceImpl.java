package com.ruoyi.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsGoodsSkuPriceLog;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.WmsSalesOrderItem;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderExportVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderItemSaveReqVO;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderPageReqVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderRespVO;
import com.ruoyi.wms.domain.vo.salesOrder.WmsSalesOrderSaveReqVO;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsGoodsSkuPriceLogMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderItemMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import com.ruoyi.wms.service.WmsSalesOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.wms.domain.WmsGoodsSku;

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
 * 销售订单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsSalesOrderServiceImpl extends ServiceImpl<WmsSalesOrderMapper, WmsSalesOrder> implements WmsSalesOrderService {

    @Autowired
    private WmsSalesOrderItemMapper salesOrderItemMapper;

    @Autowired
    private WmsCustomerMapper customerMapper;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsGoodsService goodsService;

    @Autowired
    private WmsBillApproveFlowService billApproveFlowService;

    @Autowired
    private WmsGoodsSkuPriceLogMapper goodsSkuPriceLogMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<WmsSalesOrderRespVO> page(WmsSalesOrderPageReqVO reqVO) {
        LambdaQueryWrapper<WmsSalesOrder> wrapper = new LambdaQueryWrapper<WmsSalesOrder>()
                .like(StringUtils.isNotEmpty(reqVO.getOrderNo()), WmsSalesOrder::getOrderNo, reqVO.getOrderNo())
                .eq(reqVO.getCustomerId() != null, WmsSalesOrder::getCustomerId, reqVO.getCustomerId())
                .eq(StringUtils.isNotEmpty(reqVO.getOrderStatus()), WmsSalesOrder::getOrderStatus, reqVO.getOrderStatus())
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsSalesOrder::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsSalesOrder::getCreateTime)
                .orderByDesc(WmsSalesOrder::getId);

        Page<WmsSalesOrder> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsSalesOrder> result = baseMapper.selectPage(page, wrapper);

        // 转换为RespVO
        return result.convert(item -> {
            WmsSalesOrderRespVO vo = new WmsSalesOrderRespVO();
            BeanUtils.copyProperties(item, vo);
            // 计算应收金额
            BigDecimal receivableAmount = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
            vo.setReceivableAmount(receivableAmount.subtract(discountAmount));

            // 计算收款状态
            BigDecimal receivedAmount = item.getReceivedAmount() != null ? item.getReceivedAmount() : BigDecimal.ZERO;
            BigDecimal actualReceivable = receivableAmount.subtract(discountAmount);
            if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
                vo.setPaymentStatus("0"); // 未收
            } else if (receivedAmount.compareTo(actualReceivable) < 0) {
                vo.setPaymentStatus("1"); // 部分
            } else {
                vo.setPaymentStatus("2"); // 已收
            }

            // 填充客户名称
            if (item.getCustomerId() != null) {
                var customer = customerMapper.selectById(item.getCustomerId());
                if (customer != null) {
                    vo.setCustomerName(customer.getCustomerName());
                }
            }
            return vo;
        });
    }

    @Override
    public WmsSalesOrderRespVO get(Long id) {
        WmsSalesOrder salesOrder = baseMapper.selectById(id);
        if (salesOrder == null) {
            throw new ServiceException("订单不存在");
        }

        WmsSalesOrderRespVO vo = new WmsSalesOrderRespVO();
        BeanUtils.copyProperties(salesOrder, vo);

        // 计算应收金额
        BigDecimal receivableAmount = salesOrder.getTotalAmount() != null ? salesOrder.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = salesOrder.getDiscountAmount() != null ? salesOrder.getDiscountAmount() : BigDecimal.ZERO;
        vo.setReceivableAmount(receivableAmount.subtract(discountAmount));

        // 填充客户名称
        if (salesOrder.getCustomerId() != null) {
            var customer = customerMapper.selectById(salesOrder.getCustomerId());
            if (customer != null) {
                vo.setCustomerName(customer.getCustomerName());
            }
        }

        // 查询明细
        LambdaQueryWrapper<WmsSalesOrderItem> itemWrapper = new LambdaQueryWrapper<WmsSalesOrderItem>()
                .eq(WmsSalesOrderItem::getOrderId, id);
        List<WmsSalesOrderItem> items = salesOrderItemMapper.selectList(itemWrapper);
        List<WmsSalesOrderItemRespVO> itemVOs = items.stream()
                .map(item -> {
                WmsSalesOrderItemRespVO itemVO = new WmsSalesOrderItemRespVO();
                BeanUtils.copyProperties(item, itemVO);
                return itemVO;
            })
                .collect(Collectors.toList());
        fillGoodsName(itemVOs);
        fillBarcode(itemVOs);
        vo.setItems(itemVOs);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsSalesOrderSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsSalesOrder salesOrder = new WmsSalesOrder();
        BeanUtils.copyProperties(reqVO, salesOrder);
        salesOrder.setOrderNo(generateOrderNo());
        salesOrder.setTotalAmount(BigDecimal.ZERO);
        salesOrder.setDiscountAmount(reqVO.getDiscountAmount() != null ? reqVO.getDiscountAmount() : BigDecimal.ZERO);
        salesOrder.setReceivedAmount(reqVO.getReceivedAmount() != null ? reqVO.getReceivedAmount() : BigDecimal.ZERO);
        salesOrder.setOrderStatus("0"); // 待审核
        salesOrder.setCreateTime(LocalDateTime.now());

        // 插入主表
        baseMapper.insert(salesOrder);

        // 插入明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsSalesOrderItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsSalesOrderItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsSalesOrderItem item = new WmsSalesOrderItem();
                item.setOrderId(salesOrder.getId());
                item.setSkuId(itemReq.getSkuId());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice() != null ? itemReq.getPrice() : BigDecimal.ZERO);
                item.setDiscount(itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO);
                item.setRemark(itemReq.getRemark());

                // 填充SKU信息
                WmsGoodsSku sku = skuMap.get(itemReq.getSkuId());
                if (sku != null) {
                    item.setSkuCode(sku.getSkuCode());
                    item.setSkuName(sku.getSkuName());
                    item.setSpecInfo(sku.getSpecInfo());
                    item.setUnitId(sku.getUnitId());
                    item.setUnitName(sku.getUnitName());
                    item.setGoodsId(sku.getGoodsId());
                }

                // 计算金额
                BigDecimal amount = itemReq.getQuantity().multiply(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                        .subtract(item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
                item.setAmount(amount);
                totalAmount = totalAmount.add(amount);

                salesOrderItemMapper.insert(item);
            }
            salesOrder.setTotalAmount(totalAmount);
            baseMapper.updateById(salesOrder);

            // 更新收款状态
            updatePaymentStatus(salesOrder);
        }

        billApproveFlowService.autoApproveAfterCreate(
                WmsApproveConstants.BILL_TYPE_SALES_ORDER,
                salesOrder.getId(),
                salesOrder.getOrderNo(),
                () -> approve(salesOrder.getId())
        );

        return salesOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsSalesOrderSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsSalesOrder existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("订单不存在");
        }
        if (!"0".equals(existing.getOrderStatus())) {
            throw new ServiceException("只有待审核的订单可以修改");
        }

        BeanUtils.copyProperties(reqVO, existing);
        if (reqVO.getDiscountAmount() != null) {
            existing.setDiscountAmount(reqVO.getDiscountAmount());
        }
        if (reqVO.getReceivedAmount() != null) {
            existing.setReceivedAmount(reqVO.getReceivedAmount());
        }

        // 删除原明细
        LambdaQueryWrapper<WmsSalesOrderItem> wrapper = new LambdaQueryWrapper<WmsSalesOrderItem>()
                .eq(WmsSalesOrderItem::getOrderId, reqVO.getId());
        salesOrderItemMapper.delete(wrapper);

        // 插入新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsSalesOrderItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsSalesOrderItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsSalesOrderItem item = new WmsSalesOrderItem();
                item.setOrderId(existing.getId());
                item.setSkuId(itemReq.getSkuId());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice() != null ? itemReq.getPrice() : BigDecimal.ZERO);
                item.setDiscount(itemReq.getDiscount() != null ? itemReq.getDiscount() : BigDecimal.ZERO);
                item.setRemark(itemReq.getRemark());

                // 填充SKU信息
                WmsGoodsSku sku = skuMap.get(itemReq.getSkuId());
                if (sku != null) {
                    item.setSkuCode(sku.getSkuCode());
                    item.setSkuName(sku.getSkuName());
                    item.setSpecInfo(sku.getSpecInfo());
                    item.setUnitId(sku.getUnitId());
                    item.setUnitName(sku.getUnitName());
                    item.setGoodsId(sku.getGoodsId());
                }

                // 计算金额
                BigDecimal amount = itemReq.getQuantity().multiply(item.getPrice() != null ? itemReq.getPrice() : BigDecimal.ZERO)
                        .subtract(item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
                item.setAmount(amount);
                totalAmount = totalAmount.add(amount);

                salesOrderItemMapper.insert(item);
            }
            existing.setTotalAmount(totalAmount);
        }

        // 更新收款状态
        updatePaymentStatus(existing);

        baseMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsSalesOrder existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("订单不存在");
        }

        // 删除明细
        LambdaQueryWrapper<WmsSalesOrderItem> wrapper = new LambdaQueryWrapper<WmsSalesOrderItem>()
                .eq(WmsSalesOrderItem::getOrderId, id);
        salesOrderItemMapper.delete(wrapper);

        // 删除主表
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsSalesOrder salesOrder = baseMapper.selectById(id);
        String billNo = salesOrder != null ? salesOrder.getOrderNo() : null;
        try {
            if (salesOrder == null) {
                throw new ServiceException("订单不存在");
            }
            if (!"0".equals(salesOrder.getOrderStatus())) {
                throw new ServiceException("只有待审核的订单可以审核");
            }

            salesOrder.setOrderStatus("1"); // 已审核
            salesOrder.setApproveBy(SecurityUtils.getUsername());
            salesOrder.setApproveTime(LocalDateTime.now());
            baseMapper.updateById(salesOrder);

            // 根据客户类型回写价格：批发客户→更新批发价，零售客户→更新销售价
            WmsCustomer customer = salesOrder.getCustomerId() != null ? customerMapper.selectById(salesOrder.getCustomerId()) : null;
            String customerType = customer != null ? customer.getCustomerType() : null;
            syncSkuPriceByOrder(salesOrder, customerType);

            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_SALES_ORDER, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_SALES_ORDER, id, billNo, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        WmsSalesOrder salesOrder = baseMapper.selectById(id);
        if (salesOrder == null) {
            throw new ServiceException("订单不存在");
        }
        if ("4".equals(salesOrder.getOrderStatus())) {
            throw new ServiceException("订单已关闭");
        }
        // 只有待审核状态才能关闭，已审核后的订单可能已产生关联业务数据
        if (!"0".equals(salesOrder.getOrderStatus())) {
            throw new ServiceException("只有待审核状态的订单才能关闭");
        }

        salesOrder.setOrderStatus("4"); // 已关闭
        baseMapper.updateById(salesOrder);
    }

    /**
     * 生成订单号（使用原生SQL查询包括已删除记录）
     */
    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "XS" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxOrderNo = baseMapper.selectMaxOrderNoIncludingDeleted(prefix, tenantId);

        int serial = 1;
        if (maxOrderNo != null) {
            try {
                String serialStr = maxOrderNo.substring(prefix.length());
                serial = Integer.parseInt(serialStr) + 1;
            } catch (Exception e) {
                // 使用默认值
            }
        }

        return prefix + String.format("%04d", serial);
    }

    /**
     * 更新收款状态
     */
    private void updatePaymentStatus(WmsSalesOrder salesOrder) {
        BigDecimal receivableAmount = salesOrder.getTotalAmount() != null ? salesOrder.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal discountAmount = salesOrder.getDiscountAmount() != null ? salesOrder.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal receivedAmount = salesOrder.getReceivedAmount() != null ? salesOrder.getReceivedAmount() : BigDecimal.ZERO;

        BigDecimal actualReceivable = receivableAmount.subtract(discountAmount);

        if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
            salesOrder.setPaymentStatus("0"); // 未收
        } else if (receivedAmount.compareTo(actualReceivable) >= 0) {
            salesOrder.setPaymentStatus("2"); // 已收
        } else {
            salesOrder.setPaymentStatus("1"); // 部分
        }
    }

    @Override
    public List<WmsSalesOrderRespVO> listApproved() {
        // 查询可用于出库的销售订单
        // orderStatus: 0-待审核, 1-已审核, 2-部分发货, 3-全部发货, 4-已关闭
        // 可出库状态: 已审核(1) 或 部分发货(2)
        LambdaQueryWrapper<WmsSalesOrder> wrapper = new LambdaQueryWrapper<WmsSalesOrder>()
                .in(WmsSalesOrder::getOrderStatus, "1", "2") // 已审核或部分发货
                .orderByDesc(WmsSalesOrder::getCreateTime);
        List<WmsSalesOrder> list = baseMapper.selectList(wrapper);

        return list.stream().map(item -> {
            WmsSalesOrderRespVO vo = new WmsSalesOrderRespVO();
            BeanUtils.copyProperties(item, vo);
            // 计算应收金额
            BigDecimal receivableAmount = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal discountAmount = item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO;
            vo.setReceivableAmount(receivableAmount.subtract(discountAmount));

            // 填充客户名称
            if (item.getCustomerId() != null) {
                var customer = customerMapper.selectById(item.getCustomerId());
                if (customer != null) {
                    vo.setCustomerName(customer.getCustomerName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WmsSalesOrderItemRespVO> getItems(Long orderId) {
        LambdaQueryWrapper<WmsSalesOrderItem> wrapper = new LambdaQueryWrapper<WmsSalesOrderItem>()
                .eq(WmsSalesOrderItem::getOrderId, orderId);
        List<WmsSalesOrderItem> items = salesOrderItemMapper.selectList(wrapper);

        List<WmsSalesOrderItemRespVO> result = items.stream().map(item -> {
            WmsSalesOrderItemRespVO itemVO = new WmsSalesOrderItemRespVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());
        fillGoodsName(result);
        fillBarcode(result);
        return result;
    }

    private void fillGoodsName(List<WmsSalesOrderItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsSalesOrderItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillBarcode(List<WmsSalesOrderItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsSalesOrderItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    private void validateItemsForSave(List<WmsSalesOrderItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            throw new ServiceException("销售订单明细不能为空");
        }
    }

    @Override
    public List<Map<String, Object>> searchAvailableForOut(String keyword) {
        return baseMapper.searchAvailableForOut(keyword);
    }

    private void syncSkuPriceByOrder(WmsSalesOrder salesOrder, String customerType) {
        List<WmsSalesOrderItem> items = salesOrderItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOrderItem>()
                        .eq(WmsSalesOrderItem::getOrderId, salesOrder.getId()));
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Long> skuIds = items.stream()
                .map(WmsSalesOrderItem::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }

        Map<Long, WmsGoodsSku> skuMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, s -> s, (a, b) -> a));

        Long tenantId = SecurityUtils.getTenantId();
        String operator = SecurityUtils.getUsername();
        LocalDateTime operateTime = LocalDateTime.now();

        boolean isWholesale = "1".equals(customerType);

        for (WmsSalesOrderItem item : items) {
            if (item.getSkuId() == null || item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            WmsGoodsSku sku = skuMap.get(item.getSkuId());
            if (sku == null) {
                continue;
            }

            BigDecimal newPrice = item.getPrice();
            WmsGoodsSku updateSku = new WmsGoodsSku();
            updateSku.setId(sku.getId());

            BigDecimal oldPrice;
            String priceType;
            if (isWholesale) {
                oldPrice = sku.getWholesalePrice();
                updateSku.setWholesalePrice(newPrice);
                priceType = "WHOLESALE";
            } else {
                oldPrice = sku.getSalePrice();
                updateSku.setSalePrice(newPrice);
                priceType = "SALE";
            }
            goodsSkuService.updateById(updateSku);

            WmsGoodsSkuPriceLog log = new WmsGoodsSkuPriceLog();
            log.setTenantId(tenantId);
            log.setSkuId(sku.getId());
            log.setGoodsId(sku.getGoodsId());
            log.setSkuCode(sku.getSkuCode());
            log.setSkuName(sku.getSkuName());
            log.setOrderType(WmsApproveConstants.BILL_TYPE_SALES_ORDER);
            log.setOrderId(salesOrder.getId());
            log.setOrderNo(salesOrder.getOrderNo());
            log.setOrderItemId(item.getId());
            log.setPriceType(priceType);
            log.setOldPrice(oldPrice);
            log.setNewPrice(newPrice);
            log.setOperator(operator);
            log.setOperateTime(operateTime);
            goodsSkuPriceLogMapper.insert(log);
        }
    }

    @Override
    public List<WmsSalesOrderExportVO> exportList(WmsSalesOrderPageReqVO reqVO) {
        LambdaQueryWrapper<WmsSalesOrder> wrapper = new LambdaQueryWrapper<WmsSalesOrder>()
                .like(StringUtils.isNotEmpty(reqVO.getOrderNo()), WmsSalesOrder::getOrderNo, reqVO.getOrderNo())
                .eq(reqVO.getCustomerId() != null, WmsSalesOrder::getCustomerId, reqVO.getCustomerId())
                .eq(StringUtils.isNotEmpty(reqVO.getOrderStatus()), WmsSalesOrder::getOrderStatus, reqVO.getOrderStatus())
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsSalesOrder::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsSalesOrder::getCreateTime);
        List<WmsSalesOrder> list = list(wrapper);
        if (list.isEmpty()) return java.util.Collections.emptyList();

        Set<Long> customerIds = list.stream().map(WmsSalesOrder::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> customerNameMap = customerIds.isEmpty() ? Collections.emptyMap() :
                customerMapper.selectBatchIds(customerIds).stream()
                        .collect(Collectors.toMap(WmsCustomer::getId, WmsCustomer::getCustomerName));

        Set<Long> orderIds = list.stream().map(WmsSalesOrder::getId).collect(Collectors.toSet());
        List<WmsSalesOrderItem> allItems = salesOrderItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOrderItem>().in(WmsSalesOrderItem::getOrderId, orderIds));
        Map<Long, List<WmsSalesOrderItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsSalesOrderItem::getOrderId));

        Set<Long> goodsIds = allItems.stream().map(WmsSalesOrderItem::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        List<WmsSalesOrderExportVO> exportList = new java.util.ArrayList<>();
        for (WmsSalesOrder order : list) {
            List<WmsSalesOrderItem> items = itemMap.getOrDefault(order.getId(), Collections.emptyList());
            for (WmsSalesOrderItem item : items) {
                WmsSalesOrderExportVO vo = new WmsSalesOrderExportVO();
                vo.setOrderNo(order.getOrderNo());
                vo.setOrderDate(order.getOrderDate());
                vo.setDeliveryDate(order.getDeliveryDate());
                vo.setCustomerName(customerNameMap.get(order.getCustomerId()));
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
