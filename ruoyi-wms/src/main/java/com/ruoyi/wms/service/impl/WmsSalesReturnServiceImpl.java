package com.ruoyi.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsSalesOut;
import com.ruoyi.wms.domain.WmsSalesOutItem;
import com.ruoyi.wms.domain.WmsSalesReturn;
import com.ruoyi.wms.domain.WmsSalesReturnItem;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnItemRespVO;import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnExportVO;import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnPageReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnSaveReqVO;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsSalesOutItemMapper;
import com.ruoyi.wms.mapper.WmsSalesOutMapper;
import com.ruoyi.wms.mapper.WmsSalesReturnItemMapper;
import com.ruoyi.wms.mapper.WmsSalesReturnMapper;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsSalesReturnService;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsStockLogService;
import com.ruoyi.wms.service.WmsAccountsPayableService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 销售退货单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsSalesReturnServiceImpl extends ServiceImpl<WmsSalesReturnMapper, WmsSalesReturn> implements WmsSalesReturnService {

    private static final Logger log = LoggerFactory.getLogger(WmsSalesReturnServiceImpl.class);

    @Autowired
    private WmsSalesReturnItemMapper salesReturnItemMapper;

    @Autowired
    private WmsSalesOutMapper salesOutMapper;

    @Autowired
    private WmsSalesOutItemMapper salesOutItemMapper;

    @Autowired
    private WmsCustomerMapper customerMapper;

    @Autowired
    private WmsWarehouseMapper warehouseMapper;

    @Autowired
    private WmsGoodsSkuService goodsSkuService;

    @Autowired
    private WmsGoodsService goodsService;

    @Autowired
    private WmsStockService stockService;

    @Autowired
    private WmsStockLogService stockLogService;

    @Autowired
    private WmsAccountsPayableService accountsPayableService;

    @Autowired
    private WmsBillApproveFlowService billApproveFlowService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<WmsSalesReturnRespVO> page(WmsSalesReturnPageReqVO reqVO) {
        log.info("=== 销售退货分页查询, approveStatus参数: {}", reqVO.getApproveStatus());
        LambdaQueryWrapper<WmsSalesReturn> wrapper = new LambdaQueryWrapper<WmsSalesReturn>()
                .like(StringUtils.isNotEmpty(reqVO.getReturnNo()), WmsSalesReturn::getReturnNo, reqVO.getReturnNo())
                .eq(reqVO.getCustomerId() != null, WmsSalesReturn::getCustomerId, reqVO.getCustomerId())
                .eq(reqVO.getWarehouseId() != null, WmsSalesReturn::getWarehouseId, reqVO.getWarehouseId())
                // 根据审核状态过滤：0-待审核(approveBy为空)，1-已审核(approveBy不为空)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY IS NULL OR APPROVE_BY = ''")
                .isNotNull(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsSalesReturn::getApproveBy)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY != ''")
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsSalesReturn::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsSalesReturn::getCreateTime)
                .orderByDesc(WmsSalesReturn::getId);

        Page<WmsSalesReturn> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsSalesReturn> result = baseMapper.selectPage(page, wrapper);

        // 转换为RespVO
        return result.convert(item -> {
            WmsSalesReturnRespVO vo = new WmsSalesReturnRespVO();
            BeanUtils.copyProperties(item, vo);

            // 计算退款状态
            BigDecimal totalAmount = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal refundAmount = item.getRefundAmount() != null ? item.getRefundAmount() : BigDecimal.ZERO;
            if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
                vo.setPaymentStatus("0"); // 未退
            } else if (refundAmount.compareTo(totalAmount) >= 0) {
                vo.setPaymentStatus("2"); // 已退
            } else {
                vo.setPaymentStatus("1"); // 部分
            }

            // 填充客户名称
            if (item.getCustomerId() != null) {
                var customer = customerMapper.selectById(item.getCustomerId());
                if (customer != null) {
                    vo.setCustomerName(customer.getCustomerName());
                }
            }

            // 填充仓库名称
            if (item.getWarehouseId() != null) {
                var warehouse = warehouseMapper.selectById(item.getWarehouseId());
                if (warehouse != null) {
                    vo.setWarehouseName(warehouse.getWarehouseName());
                }
            }

            // 填充出库单号
            if (item.getOutId() != null) {
                WmsSalesOut salesOut = salesOutMapper.selectById(item.getOutId());
                if (salesOut != null) {
                    vo.setOutNo(salesOut.getOutNo());
                }
            }

            // 设置审核状态
            vo.setApproveStatus(StringUtils.isNotEmpty(item.getApproveBy()) ? "1" : "0");

            return vo;
        });
    }

    @Override
    public WmsSalesReturnRespVO get(Long id) {
        WmsSalesReturn salesReturn = baseMapper.selectById(id);
        if (salesReturn == null) {
            throw new ServiceException("退货单不存在");
        }

        WmsSalesReturnRespVO vo = new WmsSalesReturnRespVO();
        BeanUtils.copyProperties(salesReturn, vo);

        // 计算退款状态
        BigDecimal totalAmount = salesReturn.getTotalAmount() != null ? salesReturn.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = salesReturn.getRefundAmount() != null ? salesReturn.getRefundAmount() : BigDecimal.ZERO;
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            vo.setPaymentStatus("0"); // 未退
        } else if (refundAmount.compareTo(totalAmount) >= 0) {
            vo.setPaymentStatus("2"); // 已退
        } else {
            vo.setPaymentStatus("1"); // 部分
        }

        // 填充客户名称
        if (salesReturn.getCustomerId() != null) {
            var customer = customerMapper.selectById(salesReturn.getCustomerId());
            if (customer != null) {
                vo.setCustomerName(customer.getCustomerName());
            }
        }

        // 填充仓库名称
        if (salesReturn.getWarehouseId() != null) {
            var warehouse = warehouseMapper.selectById(salesReturn.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        // 填充出库单号
        if (salesReturn.getOutId() != null) {
            WmsSalesOut salesOut = salesOutMapper.selectById(salesReturn.getOutId());
            if (salesOut != null) {
                vo.setOutNo(salesOut.getOutNo());
            }
        }

        // 查询明细
        LambdaQueryWrapper<WmsSalesReturnItem> itemWrapper = new LambdaQueryWrapper<WmsSalesReturnItem>()
                .eq(WmsSalesReturnItem::getReturnId, id);
        List<WmsSalesReturnItem> items = salesReturnItemMapper.selectList(itemWrapper);
        List<WmsSalesReturnItemRespVO> itemVOs = items.stream()
                .map(item -> {
                WmsSalesReturnItemRespVO itemVO = new WmsSalesReturnItemRespVO();
                BeanUtils.copyProperties(item, itemVO);
                return itemVO;
            })
                .collect(Collectors.toList());
        fillGoodsName(itemVOs);
        fillBarcode(itemVOs);

        // 如果是出库单退货，填充出库单相关字段
        if (salesReturn.getOutId() != null) {
            // 获取出库单明细
            List<WmsSalesOutItem> outItems = salesOutItemMapper.selectList(
                    new LambdaQueryWrapper<WmsSalesOutItem>()
                            .eq(WmsSalesOutItem::getOutId, salesReturn.getOutId())
            );
            // 创建出库单item的映射（用Long类型的skuId作为键）
            Map<Long, WmsSalesOutItem> outItemMap = outItems.stream()
                    .collect(Collectors.toMap(WmsSalesOutItem::getSkuId, item -> item));

            // 为每个退货单明细填充出库单相关字段
            for (WmsSalesReturnItemRespVO itemVO : itemVOs) {
                if (itemVO.getSkuId() != null) {
                    WmsSalesOutItem outItem = outItemMap.get(itemVO.getSkuId());
                    if (outItem != null) {
                        BigDecimal outQty = outItem.getQuantity() != null ? outItem.getQuantity() : BigDecimal.ZERO;
                        BigDecimal totalReturnedQty = outItem.getReturnedQty() != null ? outItem.getReturnedQty() : BigDecimal.ZERO;
                        BigDecimal currentReturnQty = itemVO.getQuantity() != null ? itemVO.getQuantity() : BigDecimal.ZERO;

                        // 判断退货单是否已审核
                        boolean isApproved = StringUtils.isNotEmpty(salesReturn.getApproveBy());

                        // 计算本次退货之前的已退货数量
                        BigDecimal returnedQtyBeforeThis = isApproved
                            ? totalReturnedQty.subtract(currentReturnQty).max(BigDecimal.ZERO)
                            : totalReturnedQty;

                        // 设置出库单相关字段
                        itemVO.setOutQuantity(outQty);
                        itemVO.setReturnedQty(returnedQtyBeforeThis);
                        // 可退货数量 = 出库数量 - 累计已退货数量
                        itemVO.setAvailableQty(outQty.subtract(totalReturnedQty).max(BigDecimal.ZERO));
                    } else {
                        // 没有找到对应的出库单明细
                        itemVO.setOutQuantity(BigDecimal.ZERO);
                        itemVO.setReturnedQty(BigDecimal.ZERO);
                        itemVO.setAvailableQty(BigDecimal.ZERO);
                    }
                }
            }
        }

        vo.setItems(itemVOs);

        // 设置审核状态
        String approveStatus = StringUtils.isNotEmpty(salesReturn.getApproveBy()) ? "1" : "0";
        vo.setApproveStatus(approveStatus);
        log.info("=== 销售退货详情查询 id={}, approveBy={}, approveStatus={}",
            id, salesReturn.getApproveBy(), approveStatus);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsSalesReturnSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsSalesReturn salesReturn = new WmsSalesReturn();
        BeanUtils.copyProperties(reqVO, salesReturn);
        salesReturn.setReturnNo(generateReturnNo());
        salesReturn.setTotalAmount(BigDecimal.ZERO);
        salesReturn.setRefundAmount(reqVO.getRefundAmount() != null ? reqVO.getRefundAmount() : BigDecimal.ZERO);
        salesReturn.setPaymentStatus("0");
        // 设置来源类型：1直接入库 2出库单退货
        salesReturn.setSourceType(reqVO.getSourceType() != null ? reqVO.getSourceType() :
                                (reqVO.getOutId() != null ? 2 : 1));
        salesReturn.setCreateTime(LocalDateTime.now());

        // 插入主表
        baseMapper.insert(salesReturn);

        // 插入明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsSalesReturnItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsSalesReturnItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsSalesReturnItem item = new WmsSalesReturnItem();
                item.setReturnId(salesReturn.getId());
                item.setSkuId(itemReq.getSkuId());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice() != null ? itemReq.getPrice() : BigDecimal.ZERO);
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
                BigDecimal amount = itemReq.getQuantity().multiply(item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO);
                item.setAmount(amount);
                totalAmount = totalAmount.add(amount);

                salesReturnItemMapper.insert(item);
            }
            salesReturn.setTotalAmount(totalAmount);
            baseMapper.updateById(salesReturn);

            // 更新付款状态
            updatePaymentStatus(salesReturn);
        }

        billApproveFlowService.autoApproveAfterCreate(
                WmsApproveConstants.BILL_TYPE_SALES_RETURN,
                salesReturn.getId(),
                salesReturn.getReturnNo(),
                () -> approve(salesReturn.getId())
        );

        return salesReturn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsSalesReturnSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsSalesReturn existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("退货单不存在");
        }
        if (StringUtils.isNotEmpty(existing.getApproveBy())) {
            throw new ServiceException("已审核的退货单不能修改");
        }

        BeanUtils.copyProperties(reqVO, existing);
        if (reqVO.getRefundAmount() != null) {
            existing.setRefundAmount(reqVO.getRefundAmount());
        }

        // 删除原明细
        LambdaQueryWrapper<WmsSalesReturnItem> wrapper = new LambdaQueryWrapper<WmsSalesReturnItem>()
                .eq(WmsSalesReturnItem::getReturnId, reqVO.getId());
        salesReturnItemMapper.delete(wrapper);

        // 插入新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsSalesReturnItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsSalesReturnItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsSalesReturnItem item = new WmsSalesReturnItem();
                item.setReturnId(existing.getId());
                item.setSkuId(itemReq.getSkuId());
                item.setQuantity(itemReq.getQuantity());
                item.setPrice(itemReq.getPrice() != null ? itemReq.getPrice() : BigDecimal.ZERO);
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
                BigDecimal amount = itemReq.getQuantity().multiply(item.getPrice() != null ? itemReq.getPrice() : BigDecimal.ZERO);
                item.setAmount(amount);
                totalAmount = totalAmount.add(amount);

                salesReturnItemMapper.insert(item);
            }
            existing.setTotalAmount(totalAmount);
        }

        // 更新付款状态
        updatePaymentStatus(existing);

        baseMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsSalesReturn existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("退货单不存在");
        }

        // 删除明细
        LambdaQueryWrapper<WmsSalesReturnItem> wrapper = new LambdaQueryWrapper<WmsSalesReturnItem>()
                .eq(WmsSalesReturnItem::getReturnId, id);
        salesReturnItemMapper.delete(wrapper);

        // 删除主表
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsSalesReturn salesReturn = baseMapper.selectById(id);
        String billNo = salesReturn != null ? salesReturn.getReturnNo() : null;
        try {
            if (salesReturn == null) {
                throw new ServiceException("退货单不存在");
            }
            if (StringUtils.isNotEmpty(salesReturn.getApproveBy())) {
                throw new ServiceException("退货单已审核");
            }

            String approveBy = SecurityUtils.getUsername();

            // 使用数据库行锁防止重复审核
            int updated = baseMapper.update(null,
                new LambdaUpdateWrapper<WmsSalesReturn>()
                    .eq(WmsSalesReturn::getId, id)
                    .isNull(WmsSalesReturn::getApproveBy)  // 只在未审核时更新
                    .set(WmsSalesReturn::getApproveBy, approveBy)
                    .set(WmsSalesReturn::getApproveTime, LocalDateTime.now())
            );

            // 如果更新行数为0，说明已被其他线程审核
            if (updated == 0) {
                throw new ServiceException("退货单已被审核，请刷新页面");
            }

            // 获取退货明细
            List<WmsSalesReturnItem> items = salesReturnItemMapper.selectList(
                    new LambdaQueryWrapper<WmsSalesReturnItem>()
                            .eq(WmsSalesReturnItem::getReturnId, id)
            );

            if (items != null && !items.isEmpty()) {
                List<Long> skuIds = items.stream()
                        .map(WmsSalesReturnItem::getSkuId)
                        .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

                for (WmsSalesReturnItem item : items) {
                    // 如果是出库单退货，更新出库单明细的已退货数量
                    if (salesReturn.getOutId() != null && item.getSkuId() != null) {
                        WmsSalesOutItem outItem = salesOutItemMapper.selectOne(
                                new LambdaQueryWrapper<WmsSalesOutItem>()
                                        .eq(WmsSalesOutItem::getOutId, salesReturn.getOutId())
                                        .eq(WmsSalesOutItem::getSkuId, item.getSkuId())
                        );
                        if (outItem != null) {
                            BigDecimal outQty = outItem.getQuantity() != null ? outItem.getQuantity() : BigDecimal.ZERO;
                            BigDecimal currentReturned = outItem.getReturnedQty() != null ? outItem.getReturnedQty() : BigDecimal.ZERO;

                            // 校验：本次退货数量不能超过可退货数量（出库数量 - 已退货数量）
                            BigDecimal availableQty = outQty.subtract(currentReturned);
                            if (item.getQuantity().compareTo(availableQty) > 0) {
                                throw new ServiceException(String.format(
                                    "商品[%s]退货数量超限：出库数量=%s，已退货数量=%s，可退货数量=%s，本次退货数量=%s",
                                    item.getSkuName() != null ? item.getSkuName() : item.getSkuId(),
                                    outQty, currentReturned, availableQty, item.getQuantity()
                                ));
                            }

                            BigDecimal newReturned = currentReturned.add(item.getQuantity());
                            outItem.setReturnedQty(newReturned);
                            salesOutItemMapper.updateById(outItem);
                        }
                    }

                    WmsStock stock = stockService.lambdaQuery()
                            .eq(WmsStock::getWarehouseId, salesReturn.getWarehouseId())
                            .eq(WmsStock::getSkuId, item.getSkuId())
                            .one();

                    if (stock != null) {
                        BigDecimal beforeQty = stock.getQuantity();
                        BigDecimal costPriceSnapshot = stock.getCostPrice() != null ? stock.getCostPrice() : BigDecimal.ZERO;
                        BigDecimal newQuantity = stock.getQuantity().add(item.getQuantity());
                        stock.setQuantity(newQuantity);
                        stockService.updateById(stock);

                        createStockLog(salesReturn.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "4", salesReturn.getReturnNo(), salesReturn.getId(),
                                       "1", item.getQuantity(), beforeQty, newQuantity, costPriceSnapshot, "销售退货");
                    } else {
                        BigDecimal beforeQty = BigDecimal.ZERO;
                        BigDecimal newQuantity = item.getQuantity();
                        WmsGoodsSku sku = skuMap.get(item.getSkuId());
                        BigDecimal costPriceSnapshot = (sku != null && sku.getCostPrice() != null) ? sku.getCostPrice() : BigDecimal.ZERO;
                        stock = new WmsStock();
                        stock.setWarehouseId(salesReturn.getWarehouseId());
                        stock.setSkuId(item.getSkuId());
                        stock.setGoodsId(item.getGoodsId());
                        stock.setQuantity(newQuantity);
                        stock.setCostPrice(costPriceSnapshot);
                        stockService.save(stock);

                        createStockLog(salesReturn.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "4", salesReturn.getReturnNo(), salesReturn.getId(),
                                       "1", item.getQuantity(), beforeQty, newQuantity, costPriceSnapshot, "销售退货");
                    }
                }
            }

            // 如果是出库单退货，更新出库单的退货状态
            if (salesReturn.getOutId() != null) {
                updateOutReturnStatus(salesReturn.getOutId());
            }

            // 生成应付退款记录（我们退款给客户）
            accountsPayableService.createByBill(
                    null, // supplierId - 销售退货不涉及供应商
                    salesReturn.getCustomerId(), // customerId - 我们退款给客户
                    "3", // 3=销售退货
                    salesReturn.getReturnNo(),
                    salesReturn.getId(),
                    salesReturn.getTotalAmount(),
                    salesReturn.getRefundAmount(),
                    salesReturn.getReturnDate()
            );
            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_SALES_RETURN, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_SALES_RETURN, id, billNo, ex);
            throw ex;
        }
    }

    /**
     * 退货后更新出库单的退货状态
     * 如果所有商品都已全部退货，状态改为"全部退货(2)"，否则改为"部分退货(1)"
     */
    private void updateOutReturnStatus(Long outId) {
        // 获取出库单所有明细
        List<WmsSalesOutItem> outItems = salesOutItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOutItem>()
                        .eq(WmsSalesOutItem::getOutId, outId)
        );

        if (outItems == null || outItems.isEmpty()) {
            return;
        }

        // 检查是否所有商品都已全部退货
        boolean allReturned = true;
        for (WmsSalesOutItem outItem : outItems) {
            BigDecimal outQty = outItem.getQuantity() != null ? outItem.getQuantity() : BigDecimal.ZERO;
            BigDecimal returnedQty = outItem.getReturnedQty() != null ? outItem.getReturnedQty() : BigDecimal.ZERO;
            if (returnedQty.compareTo(outQty) < 0) {
                allReturned = false;
                break;
            }
        }

        // 更新出库单的退货状态
        WmsSalesOut salesOut = salesOutMapper.selectById(outId);
        if (salesOut != null) {
            if (allReturned) {
                salesOut.setReturnStatus("2"); // 全部退货
            } else {
                salesOut.setReturnStatus("1"); // 部分退货
            }
            salesOutMapper.updateById(salesOut);
        }
    }

    /**
     * 生成退货单号（使用原生SQL查询包括已删除记录）
     */
    private String generateReturnNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "XST" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxReturnNo = baseMapper.selectMaxReturnNoIncludingDeleted(prefix, tenantId);

        int serial = 1;
        if (maxReturnNo != null) {
            try {
                String serialStr = maxReturnNo.substring(prefix.length());
                serial = Integer.parseInt(serialStr) + 1;
            } catch (Exception e) {
                // 使用默认值
            }
        }

        return prefix + String.format("%04d", serial);
    }

    /**
     * 更新付款状态
     */
    private void updatePaymentStatus(WmsSalesReturn salesReturn) {
        BigDecimal totalAmount = salesReturn.getTotalAmount() != null ? salesReturn.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = salesReturn.getRefundAmount() != null ? salesReturn.getRefundAmount() : BigDecimal.ZERO;

        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            salesReturn.setPaymentStatus("0"); // 未退
        } else if (refundAmount.compareTo(totalAmount) >= 0) {
            salesReturn.setPaymentStatus("2"); // 已退
        } else {
            salesReturn.setPaymentStatus("1"); // 部分
        }
    }

    private void validateItemsForSave(List<WmsSalesReturnItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            throw new ServiceException("销售退货明细不能为空");
        }
    }

    private void fillGoodsName(List<WmsSalesReturnItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsSalesReturnItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillBarcode(List<WmsSalesReturnItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsSalesReturnItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
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

    @Override
    public List<WmsSalesReturnExportVO> exportList(WmsSalesReturnPageReqVO reqVO) {
        LambdaQueryWrapper<WmsSalesReturn> wrapper = new LambdaQueryWrapper<WmsSalesReturn>()
                .like(StringUtils.isNotEmpty(reqVO.getReturnNo()), WmsSalesReturn::getReturnNo, reqVO.getReturnNo())
                .eq(reqVO.getCustomerId() != null, WmsSalesReturn::getCustomerId, reqVO.getCustomerId())
                .eq(reqVO.getWarehouseId() != null, WmsSalesReturn::getWarehouseId, reqVO.getWarehouseId())
                .eq(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    WmsSalesReturn::getStatus, "0")
                .eq(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsSalesReturn::getStatus, "1")
                .orderByDesc(WmsSalesReturn::getCreateTime);
        List<WmsSalesReturn> list = list(wrapper);
        if (list.isEmpty()) return java.util.Collections.emptyList();

        Set<Long> customerIds = list.stream().map(WmsSalesReturn::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> customerNameMap = customerIds.isEmpty() ? Collections.emptyMap() :
                customerMapper.selectBatchIds(customerIds).stream()
                        .collect(Collectors.toMap(WmsCustomer::getId, WmsCustomer::getCustomerName));
        Set<Long> warehouseIds = list.stream().map(WmsSalesReturn::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> warehouseNameMap = warehouseIds.isEmpty() ? Collections.emptyMap() :
                warehouseMapper.selectBatchIds(warehouseIds).stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getWarehouseName));

        Set<Long> returnIds = list.stream().map(WmsSalesReturn::getId).collect(Collectors.toSet());
        List<WmsSalesReturnItem> allItems = salesReturnItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesReturnItem>().in(WmsSalesReturnItem::getReturnId, returnIds));
        Map<Long, List<WmsSalesReturnItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsSalesReturnItem::getReturnId));

        Set<Long> goodsIds = allItems.stream().map(WmsSalesReturnItem::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        // 批量查关联出库单号
        Set<Long> outIds = list.stream().map(WmsSalesReturn::getOutId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> outNoMap = outIds.isEmpty() ? Collections.emptyMap() :
                salesOutMapper.selectBatchIds(outIds).stream()
                        .collect(Collectors.toMap(WmsSalesOut::getId, WmsSalesOut::getOutNo));

        List<WmsSalesReturnExportVO> exportList = new java.util.ArrayList<>();
        for (WmsSalesReturn ret : list) {
            String approveStatus = StringUtils.isNotBlank(ret.getApproveBy()) ? "1" : "0";
            List<WmsSalesReturnItem> items = itemMap.getOrDefault(ret.getId(), Collections.emptyList());
            for (WmsSalesReturnItem item : items) {
                WmsSalesReturnExportVO vo = new WmsSalesReturnExportVO();
                vo.setReturnNo(ret.getReturnNo());
                vo.setOutNo(outNoMap.get(ret.getOutId()));
                vo.setSourceType(ret.getSourceType());
                vo.setReturnDate(ret.getReturnDate());
                vo.setCustomerName(customerNameMap.get(ret.getCustomerId()));
                vo.setWarehouseName(warehouseNameMap.get(ret.getWarehouseId()));
                vo.setGoodsName(goodsNameMap.get(item.getGoodsId()));
                vo.setSkuName(item.getSkuName());
                vo.setSkuCode(item.getSkuCode());
                vo.setQuantity(item.getQuantity());
                vo.setPrice(item.getPrice());
                vo.setAmount(item.getAmount());
                vo.setTotalAmount(ret.getTotalAmount());
                vo.setPaymentStatus(ret.getPaymentStatus());
                vo.setApproveStatus(approveStatus);
                vo.setRemark(ret.getRemark());
                vo.setCreateTime(ret.getCreateTime());
                exportList.add(vo);
            }
        }
        return exportList;
    }
}
