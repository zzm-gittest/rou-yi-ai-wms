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
import com.ruoyi.wms.domain.WmsPurchaseReturn;
import com.ruoyi.wms.domain.WmsPurchaseReturnItem;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.WmsPurchaseInItem;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnExportVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnItemSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnPageReqVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnRespVO;
import com.ruoyi.wms.domain.vo.purchaseReturn.WmsPurchaseReturnSaveReqVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInItemRespVO;
import com.ruoyi.wms.domain.vo.purchaseIn.WmsPurchaseInRespVO;
import com.ruoyi.wms.mapper.WmsPurchaseReturnItemMapper;
import com.ruoyi.wms.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsPurchaseReturnService;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsStockLogService;
import com.ruoyi.wms.service.WmsPurchaseInService;
import com.ruoyi.wms.service.WmsPurchaseInItemService;
import com.ruoyi.wms.service.WmsAccountsReceivableService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.wms.domain.WmsGoodsSku;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * 采购退货单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsPurchaseReturnServiceImpl extends ServiceImpl<WmsPurchaseReturnMapper, WmsPurchaseReturn> implements WmsPurchaseReturnService {

    private static final Logger log = LoggerFactory.getLogger(WmsPurchaseReturnServiceImpl.class);

    @Autowired
    private WmsPurchaseReturnItemMapper purchaseReturnItemMapper;

    @Autowired
    private WmsPurchaseInService purchaseInService;

    @Autowired
    private WmsPurchaseInItemService purchaseInItemService;

    @Autowired
    private WmsSupplierMapper supplierMapper;

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
    private WmsAccountsReceivableService accountsReceivableService;

    @Autowired
    private WmsBillApproveFlowService billApproveFlowService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<WmsPurchaseReturnRespVO> page(WmsPurchaseReturnPageReqVO reqVO) {
        log.info("=== 采购退货分页查询, approveStatus参数: {}", reqVO.getApproveStatus());
        LambdaQueryWrapper<WmsPurchaseReturn> wrapper = new LambdaQueryWrapper<WmsPurchaseReturn>()
                .like(StringUtils.isNotEmpty(reqVO.getReturnNo()), WmsPurchaseReturn::getReturnNo, reqVO.getReturnNo())
                .eq(reqVO.getSupplierId() != null, WmsPurchaseReturn::getSupplierId, reqVO.getSupplierId())
                .eq(reqVO.getWarehouseId() != null, WmsPurchaseReturn::getWarehouseId, reqVO.getWarehouseId())
                // 根据审核状态过滤：0-待审核(approveBy为空)，1-已审核(approveBy不为空)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY IS NULL OR APPROVE_BY = ''")
                .isNotNull(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsPurchaseReturn::getApproveBy)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY != ''")
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsPurchaseReturn::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsPurchaseReturn::getCreateTime)
                .orderByDesc(WmsPurchaseReturn::getId);

        Page<WmsPurchaseReturn> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsPurchaseReturn> result = baseMapper.selectPage(page, wrapper);

        // 转换为RespVO
        return result.convert(item -> {
            WmsPurchaseReturnRespVO vo = new WmsPurchaseReturnRespVO();
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

            // 填充供应商名称
            if (item.getSupplierId() != null) {
                var supplier = supplierMapper.selectById(item.getSupplierId());
                if (supplier != null) {
                    vo.setSupplierName(supplier.getSupplierName());
                }
            }
            // 填充仓库名称
            if (item.getWarehouseId() != null) {
                var warehouse = warehouseMapper.selectById(item.getWarehouseId());
                if (warehouse != null) {
                    vo.setWarehouseName(warehouse.getWarehouseName());
                }
            }
            // 填充入库单号
            if (item.getInId() != null) {
                WmsPurchaseInRespVO in = purchaseInService.get(item.getInId());
                if (in != null) {
                    vo.setInNo(in.getInNo());
                }
            }
            // 设置审核状态
            vo.setApproveStatus(StringUtils.isNotEmpty(item.getApproveBy()) ? "1" : "0");
            return vo;
        });
    }

    @Override
    public WmsPurchaseReturnRespVO get(Long id) {
        WmsPurchaseReturn purchaseReturn = baseMapper.selectById(id);
        if (purchaseReturn == null) {
            throw new ServiceException("退货单不存在");
        }

        WmsPurchaseReturnRespVO vo = new WmsPurchaseReturnRespVO();
        BeanUtils.copyProperties(purchaseReturn, vo);

        // 计算退款状态
        BigDecimal totalAmount = purchaseReturn.getTotalAmount() != null ? purchaseReturn.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = purchaseReturn.getRefundAmount() != null ? purchaseReturn.getRefundAmount() : BigDecimal.ZERO;
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            vo.setPaymentStatus("0"); // 未退
        } else if (refundAmount.compareTo(totalAmount) >= 0) {
            vo.setPaymentStatus("2"); // 已退
        } else {
            vo.setPaymentStatus("1"); // 部分
        }

        // 填充供应商名称
        if (purchaseReturn.getSupplierId() != null) {
            var supplier = supplierMapper.selectById(purchaseReturn.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getSupplierName());
            }
        }

        // 填充仓库名称
        if (purchaseReturn.getWarehouseId() != null) {
            var warehouse = warehouseMapper.selectById(purchaseReturn.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        // 填充采购入库单号
        WmsPurchaseInRespVO inData = null;
        if (purchaseReturn.getInId() != null) {
            inData = purchaseInService.get(purchaseReturn.getInId());
            if (inData != null) {
                vo.setInNo(inData.getInNo());
            }
        }

        // 查询明细
        LambdaQueryWrapper<WmsPurchaseReturnItem> itemWrapper = new LambdaQueryWrapper<WmsPurchaseReturnItem>()
                .eq(WmsPurchaseReturnItem::getReturnId, id);
        List<WmsPurchaseReturnItem> items = purchaseReturnItemMapper.selectList(itemWrapper);
        List<WmsPurchaseReturnItemRespVO> itemVOs = items.stream()
                .map(item -> {
                    WmsPurchaseReturnItemRespVO itemVO = new WmsPurchaseReturnItemRespVO();
                    BeanUtils.copyProperties(item, itemVO);
                    return itemVO;
                })
                .collect(Collectors.toList());
        fillGoodsName(itemVOs);
        fillBarcode(itemVOs);

        // 如果是入库单退货，填充入库数量和已退货数量
        if (purchaseReturn.getInId() != null && inData != null && inData.getItems() != null) {
            // 创建入库单item的映射
            java.util.Map<Long, WmsPurchaseInItemRespVO> inItemMap = inData.getItems().stream()
                    .collect(java.util.stream.Collectors.toMap(WmsPurchaseInItemRespVO::getSkuId, item -> item));

            // 为每个退货明细填充入库数量和已退货数量
            for (WmsPurchaseReturnItemRespVO itemVO : itemVOs) {
                WmsPurchaseInItemRespVO inItem = inItemMap.get(itemVO.getSkuId());
                if (inItem != null) {
                    itemVO.setInQuantity(inItem.getQuantity()); // 入库数量
                    itemVO.setReturnedQty(inItem.getReturnedQty() != null ? inItem.getReturnedQty() : BigDecimal.ZERO); // 已退货数量（不包含本次）
                }
            }
        }

        // 为所有退货明细填充库存数量和退货数量
        for (WmsPurchaseReturnItemRespVO itemVO : itemVOs) {
            // 查询当前库存数量
            WmsStock stock = stockService.lambdaQuery()
                    .eq(WmsStock::getWarehouseId, purchaseReturn.getWarehouseId())
                    .eq(WmsStock::getSkuId, itemVO.getSkuId())
                    .one();
            if (stock != null) {
                itemVO.setReceivedQty(stock.getQuantity());
            } else {
                itemVO.setReceivedQty(BigDecimal.ZERO);
            }
            // 退货数量
            itemVO.setReturnQty(itemVO.getQuantity());
        }

        vo.setItems(itemVOs);

        // 设置审核状态
        String approveStatus = StringUtils.isNotEmpty(purchaseReturn.getApproveBy()) ? "1" : "0";
        vo.setApproveStatus(approveStatus);
        log.info("=== 采购退货详情查询 id={}, approveBy={}, approveStatus={}, remark={}",
            id, purchaseReturn.getApproveBy(), approveStatus, purchaseReturn.getRemark());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsPurchaseReturnSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsPurchaseReturn purchaseReturn = new WmsPurchaseReturn();
        BeanUtils.copyProperties(reqVO, purchaseReturn);
        purchaseReturn.setReturnNo(generateReturnNo());
        purchaseReturn.setTotalAmount(BigDecimal.ZERO);
        purchaseReturn.setRefundAmount(reqVO.getRefundAmount() != null ? reqVO.getRefundAmount() : BigDecimal.ZERO);
        purchaseReturn.setPaymentStatus("0");
        purchaseReturn.setSourceType(reqVO.getSourceType() != null ? reqVO.getSourceType() : 1);
        purchaseReturn.setCreateTime(LocalDateTime.now());

        // 插入主表
        baseMapper.insert(purchaseReturn);

        // 插入明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsPurchaseReturnItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsPurchaseReturnItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsPurchaseReturnItem item = new WmsPurchaseReturnItem();
                item.setReturnId(purchaseReturn.getId());
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

                purchaseReturnItemMapper.insert(item);
            }
            purchaseReturn.setTotalAmount(totalAmount);
            baseMapper.updateById(purchaseReturn);

            // 更新付款状态
            updatePaymentStatus(purchaseReturn);
        }

        billApproveFlowService.autoApproveAfterCreate(
                WmsApproveConstants.BILL_TYPE_PURCHASE_RETURN,
                purchaseReturn.getId(),
                purchaseReturn.getReturnNo(),
                () -> approve(purchaseReturn.getId())
        );

        return purchaseReturn.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsPurchaseReturnSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsPurchaseReturn existing = baseMapper.selectById(reqVO.getId());
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
        LambdaQueryWrapper<WmsPurchaseReturnItem> wrapper = new LambdaQueryWrapper<WmsPurchaseReturnItem>()
                .eq(WmsPurchaseReturnItem::getReturnId, reqVO.getId());
        purchaseReturnItemMapper.delete(wrapper);

        // 插入新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsPurchaseReturnItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsPurchaseReturnItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsPurchaseReturnItem item = new WmsPurchaseReturnItem();
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

                purchaseReturnItemMapper.insert(item);
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
        WmsPurchaseReturn existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("退货单不存在");
        }

        // 删除明细
        LambdaQueryWrapper<WmsPurchaseReturnItem> wrapper = new LambdaQueryWrapper<WmsPurchaseReturnItem>()
                .eq(WmsPurchaseReturnItem::getReturnId, id);
        purchaseReturnItemMapper.delete(wrapper);

        // 删除主表
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsPurchaseReturn purchaseReturn = baseMapper.selectById(id);
        String billNo = purchaseReturn != null ? purchaseReturn.getReturnNo() : null;
        try {
            if (purchaseReturn == null) {
                throw new ServiceException("退货单不存在");
            }
            if (StringUtils.isNotEmpty(purchaseReturn.getApproveBy())) {
                throw new ServiceException("退货单已审核");
            }

            String approveBy = SecurityUtils.getUsername();

            // 使用数据库行锁防止重复审核
            int updated = baseMapper.update(null,
                new LambdaUpdateWrapper<WmsPurchaseReturn>()
                    .eq(WmsPurchaseReturn::getId, id)
                    .isNull(WmsPurchaseReturn::getApproveBy)  // 只在未审核时更新
                    .set(WmsPurchaseReturn::getApproveBy, approveBy)
                    .set(WmsPurchaseReturn::getApproveTime, LocalDateTime.now())
            );

            // 如果更新行数为0，说明已被其他线程审核
            if (updated == 0) {
                throw new ServiceException("退货单已被审核，请刷新页面");
            }

            // 获取退货明细
            List<WmsPurchaseReturnItem> items = purchaseReturnItemMapper.selectList(
                    new LambdaQueryWrapper<WmsPurchaseReturnItem>()
                            .eq(WmsPurchaseReturnItem::getReturnId, id)
            );

            if (items != null && !items.isEmpty()) {
                for (WmsPurchaseReturnItem item : items) {
                    // 如果是入库单退货，更新入库单明细的已退货数量
                    if (purchaseReturn.getInId() != null && item.getSkuId() != null) {
                        WmsPurchaseInItem inItem = purchaseInItemService.lambdaQuery()
                                .eq(WmsPurchaseInItem::getInId, purchaseReturn.getInId())
                                .eq(WmsPurchaseInItem::getSkuId, item.getSkuId())
                                .one();
                        if (inItem != null) {
                            BigDecimal inQty = inItem.getQuantity() != null ? inItem.getQuantity() : BigDecimal.ZERO;
                            BigDecimal currentReturned = inItem.getReturnedQty() != null ? inItem.getReturnedQty() : BigDecimal.ZERO;

                            // 校验：本次退货数量不能超过可退货数量（入库数量 - 已退货数量）
                            BigDecimal availableQty = inQty.subtract(currentReturned);
                            if (item.getQuantity().compareTo(availableQty) > 0) {
                                throw new ServiceException(String.format(
                                    "商品[%s]退货数量超限：入库数量=%s，已退货数量=%s，可退货数量=%s，本次退货数量=%s",
                                    item.getSkuName() != null ? item.getSkuName() : item.getSkuId(),
                                    inQty, currentReturned, availableQty, item.getQuantity()
                                ));
                            }

                            BigDecimal newReturned = currentReturned.add(item.getQuantity());
                            inItem.setReturnedQty(newReturned);
                            purchaseInItemService.updateById(inItem);
                        }
                    }

                    WmsStock stock = stockService.lambdaQuery()
                            .eq(WmsStock::getWarehouseId, purchaseReturn.getWarehouseId())
                            .eq(WmsStock::getSkuId, item.getSkuId())
                            .one();

                    if (stock == null) {
                        throw new ServiceException("SKU库存不存在，无法退货");
                    }
                    if (stock.getQuantity().compareTo(item.getQuantity()) < 0) {
                        throw new ServiceException("SKU库存不足，无法退货");
                    }

                    BigDecimal beforeQty = stock.getQuantity();
                    BigDecimal newQuantity = stock.getQuantity().subtract(item.getQuantity());
                    stock.setQuantity(newQuantity);
                    stockService.updateById(stock);

                    createStockLog(purchaseReturn.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "3", purchaseReturn.getReturnNo(), purchaseReturn.getId(),
                                   "2", item.getQuantity(), beforeQty, newQuantity, null, "采购退货");
                }
            }

            // 如果是入库单退货，更新入库单的退货状态
            if (purchaseReturn.getInId() != null) {
                updateInReturnStatus(purchaseReturn.getInId());
            }

            // 生成应收退款记录（供应商退款给我们）
            accountsReceivableService.createByBill(
                    null, // customerId - 采购退货不涉及客户
                    purchaseReturn.getSupplierId(), // supplierId - 供应商退款给我们
                    "3", // 3=采购退货
                    purchaseReturn.getReturnNo(),
                    purchaseReturn.getId(),
                    purchaseReturn.getTotalAmount(),
                    purchaseReturn.getRefundAmount(),
                    purchaseReturn.getReturnDate()
            );
            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_PURCHASE_RETURN, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_PURCHASE_RETURN, id, billNo, ex);
            throw ex;
        }
    }

    /**
     * 退货后更新入库单的退货状态
     * 如果所有商品都已全部退货，状态改为"全部退货(2)"，否则改为"部分退货(1)"
     */
    private void updateInReturnStatus(Long inId) {
        // 获取入库单所有明细
        List<WmsPurchaseInItem> inItems = purchaseInItemService.lambdaQuery()
                .eq(WmsPurchaseInItem::getInId, inId)
                .list();

        if (inItems == null || inItems.isEmpty()) {
            return;
        }

        // 检查是否所有商品都已全部退货
        boolean allReturned = true;
        for (WmsPurchaseInItem inItem : inItems) {
            BigDecimal inQty = inItem.getQuantity() != null ? inItem.getQuantity() : BigDecimal.ZERO;
            BigDecimal returnedQty = inItem.getReturnedQty() != null ? inItem.getReturnedQty() : BigDecimal.ZERO;
            if (returnedQty.compareTo(inQty) < 0) {
                allReturned = false;
                break;
            }
        }

        // 更新入库单的退货状态
        WmsPurchaseIn in = purchaseInService.getById(inId);
        if (in != null) {
            if (allReturned) {
                in.setReturnStatus("2"); // 全部退货
            } else {
                in.setReturnStatus("1"); // 部分退货
            }
            purchaseInService.updateById(in);
        }
    }

    /**
     * 生成退货单号
     */
    private String generateReturnNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "CGTH" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxNo = baseMapper.selectMaxReturnNoIncludingDeleted(prefix, tenantId);

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
     * 更新付款状态
     */
    private void updatePaymentStatus(WmsPurchaseReturn purchaseReturn) {
        BigDecimal totalAmount = purchaseReturn.getTotalAmount() != null ? purchaseReturn.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal refundAmount = purchaseReturn.getRefundAmount() != null ? purchaseReturn.getRefundAmount() : BigDecimal.ZERO;

        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            purchaseReturn.setPaymentStatus("0"); // 未退
        } else if (refundAmount.compareTo(totalAmount) >= 0) {
            purchaseReturn.setPaymentStatus("2"); // 已退
        } else {
            purchaseReturn.setPaymentStatus("1"); // 部分
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

    @Override
    public List<WmsPurchaseInItemRespVO> getInItems(Long inId) {
        // 获取入库单详情，包含订单数量和库存数量信息
        WmsPurchaseInRespVO in = purchaseInService.get(inId);
        if (in == null || in.getItems() == null) {
            return java.util.Collections.emptyList();
        }

        // 获取当前库存数量
        List<WmsPurchaseInItemRespVO> result = in.getItems().stream()
                .map(item -> {
                    WmsPurchaseInItemRespVO vo = new WmsPurchaseInItemRespVO();
                    vo.setId(item.getId());
                    vo.setInId(item.getInId());
                    vo.setSkuId(item.getSkuId());
                    vo.setGoodsId(item.getGoodsId());
                    vo.setSkuCode(item.getSkuCode());
                    vo.setSkuName(item.getSkuName());
                    vo.setSpecInfo(item.getSpecInfo());
                    vo.setUnitId(item.getUnitId());
                    vo.setUnitName(item.getUnitName());
                    vo.setPrice(item.getPrice());
                    vo.setAmount(item.getAmount());
                    // quantity: 入库数量
                    vo.setQuantity(item.getQuantity());
                    // orderQuantity: 订单数量（如果有）
                    vo.setOrderQuantity(item.getOrderQuantity());
                    // receivedQty: 已入库数量（库存数量）
                    vo.setReceivedQty(item.getReceivedQty());
                    // returnedQty: 已退货数量
                    vo.setReturnedQty(item.getReturnedQty());
                    vo.setRemark(item.getRemark());
                    return vo;
                })
                .collect(Collectors.toList());
        fillInItemGoodsName(result);
        return result;
    }

    private void fillGoodsName(List<WmsPurchaseReturnItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsPurchaseReturnItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillBarcode(List<WmsPurchaseReturnItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsPurchaseReturnItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    private void fillInItemGoodsName(List<WmsPurchaseInItemRespVO> items) {
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

    private void validateItemsForSave(List<WmsPurchaseReturnItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            throw new ServiceException("采购退货明细不能为空");
        }
    }

    @Override
    public List<WmsPurchaseReturnExportVO> exportList(WmsPurchaseReturnPageReqVO reqVO) {
        // 1. 按条件查询（不分页）
        LambdaQueryWrapper<WmsPurchaseReturn> wrapper = new LambdaQueryWrapper<WmsPurchaseReturn>()
                .like(StringUtils.isNotEmpty(reqVO.getReturnNo()), WmsPurchaseReturn::getReturnNo, reqVO.getReturnNo())
                .eq(reqVO.getSupplierId() != null, WmsPurchaseReturn::getSupplierId, reqVO.getSupplierId())
                .eq(reqVO.getWarehouseId() != null, WmsPurchaseReturn::getWarehouseId, reqVO.getWarehouseId())
                .eq(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    WmsPurchaseReturn::getStatus, "0")
                .eq(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsPurchaseReturn::getStatus, "1")
                .orderByDesc(WmsPurchaseReturn::getCreateTime);
        // Use baseMapper since this service extends IService
        List<WmsPurchaseReturn> list = baseMapper.selectList(wrapper);
        if (list.isEmpty()) return java.util.Collections.emptyList();

        // 2. 批量查供应商/仓库
        Set<Long> supplierIds = list.stream().map(WmsPurchaseReturn::getSupplierId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> supplierNameMap = supplierIds.isEmpty() ? Collections.emptyMap() :
                supplierMapper.selectBatchIds(supplierIds).stream()
                        .collect(Collectors.toMap(WmsSupplier::getId, WmsSupplier::getSupplierName));
        Set<Long> warehouseIds = list.stream().map(WmsPurchaseReturn::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> warehouseNameMap = warehouseIds.isEmpty() ? Collections.emptyMap() :
                warehouseMapper.selectBatchIds(warehouseIds).stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getWarehouseName));

        // 3. 批量查明细
        Set<Long> returnIds = list.stream().map(WmsPurchaseReturn::getId).collect(Collectors.toSet());
        List<WmsPurchaseReturnItem> allItems = purchaseReturnItemMapper.selectList(
                new LambdaQueryWrapper<WmsPurchaseReturnItem>().in(WmsPurchaseReturnItem::getReturnId, returnIds));
        Map<Long, List<WmsPurchaseReturnItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsPurchaseReturnItem::getReturnId));

        // 4. 批量查商品名称
        Set<Long> goodsIds = allItems.stream().map(WmsPurchaseReturnItem::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        // 4b. 批量查关联入库单号
        Set<Long> inIds = list.stream().map(WmsPurchaseReturn::getInId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> inNoMap = inIds.isEmpty() ? Collections.emptyMap() :
                purchaseInService.listByIds(inIds).stream()
                        .collect(Collectors.toMap(WmsPurchaseIn::getId, WmsPurchaseIn::getInNo));

        // 5. 组装
        List<WmsPurchaseReturnExportVO> exportList = new java.util.ArrayList<>();
        for (WmsPurchaseReturn ret : list) {
            String approveStatus = StringUtils.isNotBlank(ret.getApproveBy()) ? "1" : "0";
            List<WmsPurchaseReturnItem> items = itemMap.getOrDefault(ret.getId(), Collections.emptyList());
            for (WmsPurchaseReturnItem item : items) {
                WmsPurchaseReturnExportVO vo = new WmsPurchaseReturnExportVO();
                vo.setReturnNo(ret.getReturnNo());
                vo.setInNo(inNoMap.get(ret.getInId()));
                vo.setSourceType(ret.getSourceType());
                vo.setReturnDate(ret.getReturnDate());
                vo.setSupplierName(supplierNameMap.get(ret.getSupplierId()));
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
