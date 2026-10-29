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
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.WmsStockTransfer;
import com.ruoyi.wms.domain.WmsStockTransferItem;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferItemRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferExportVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferPageReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferRespVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferSaveReqVO;
import com.ruoyi.wms.domain.vo.stockTransfer.WmsStockTransferItemSaveReqVO;
import com.ruoyi.wms.mapper.WmsStockTransferMapper;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsStockLogService;
import com.ruoyi.wms.service.WmsStockTransferItemService;
import com.ruoyi.wms.service.WmsStockTransferService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import com.ruoyi.wms.service.WmsGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.service.WmsGoodsSkuService;

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
 * 库存调拨单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsStockTransferServiceImpl extends ServiceImpl<WmsStockTransferMapper, WmsStockTransfer> implements WmsStockTransferService {

    private final WmsStockTransferItemService itemService;
    private final WmsStockService stockService;
    private final WmsWarehouseService warehouseService;
    private final WmsGoodsSkuService goodsSkuService;
    private final WmsGoodsService goodsService;
    private final WmsStockLogService stockLogService;
    private final WmsBillApproveFlowService billApproveFlowService;

    public WmsStockTransferServiceImpl(WmsStockTransferItemService itemService,
                                       WmsStockService stockService,
                                       WmsWarehouseService warehouseService,
                                       WmsGoodsSkuService goodsSkuService,
                                       WmsGoodsService goodsService,
                                       WmsStockLogService stockLogService,
                                       WmsBillApproveFlowService billApproveFlowService) {
        this.itemService = itemService;
        this.stockService = stockService;
        this.warehouseService = warehouseService;
        this.goodsSkuService = goodsSkuService;
        this.goodsService = goodsService;
        this.stockLogService = stockLogService;
        this.billApproveFlowService = billApproveFlowService;
    }

    @Override
    public IPage<WmsStockTransferRespVO> page(WmsStockTransferPageReqVO reqVO) {
        LambdaQueryWrapper<WmsStockTransfer> wrapper = new LambdaQueryWrapper<WmsStockTransfer>()
                .like(StringUtils.isNotEmpty(reqVO.getTransferNo()), WmsStockTransfer::getTransferNo, reqVO.getTransferNo())
                .eq(reqVO.getOutWarehouseId() != null, WmsStockTransfer::getOutWarehouseId, reqVO.getOutWarehouseId())
                .eq(reqVO.getInWarehouseId() != null, WmsStockTransfer::getInWarehouseId, reqVO.getInWarehouseId())
                .eq(StringUtils.isNotEmpty(reqVO.getTransferStatus()), WmsStockTransfer::getTransferStatus, reqVO.getTransferStatus())
                .orderByDesc(WmsStockTransfer::getCreateTime)
                .orderByDesc(WmsStockTransfer::getId);

        Page<WmsStockTransfer> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsStockTransferRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<WmsStockTransferRespVO> respVOList = VoUtils.convertList(page.getRecords(), WmsStockTransferRespVO.class);

        // 填充仓库名称
        fillWarehouseName(respVOList);

        resultPage.setRecords(respVOList);
        return resultPage;
    }

    @Override
    public WmsStockTransferRespVO get(Long id) {
        WmsStockTransfer transfer = getById(id);
        WmsStockTransferRespVO respVO = VoUtils.convert(transfer, WmsStockTransferRespVO.class);

        // 填充仓库名称
        if (respVO != null) {
            WmsWarehouse outWarehouse = warehouseService.getById(respVO.getOutWarehouseId());
            if (outWarehouse != null) {
                respVO.setOutWarehouseName(outWarehouse.getWarehouseName());
            }
            WmsWarehouse inWarehouse = warehouseService.getById(respVO.getInWarehouseId());
            if (inWarehouse != null) {
                respVO.setInWarehouseName(inWarehouse.getWarehouseName());
            }

            // 填充明细
            List<WmsStockTransferItem> items = itemService.lambdaQuery()
                    .eq(WmsStockTransferItem::getTransferId, id)
                    .list();
                List<WmsStockTransferItemRespVO> itemVOs = VoUtils.convertList(items, WmsStockTransferItemRespVO.class);
                fillGoodsName(itemVOs);
                fillBarcode(itemVOs);
                respVO.setItems(itemVOs);
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsStockTransferSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        // 校验：调出仓库和调入仓库不能相同
        if (reqVO.getOutWarehouseId() != null && reqVO.getOutWarehouseId().equals(reqVO.getInWarehouseId())) {
            throw new ServiceException("调出仓库和调入仓库不能相同");
        }

        // 生成调拨单号
        String transferNo = generateTransferNo();

        WmsStockTransfer transfer = new WmsStockTransfer();
        transfer.setTransferNo(transferNo);
        transfer.setOutWarehouseId(reqVO.getOutWarehouseId());
        transfer.setInWarehouseId(reqVO.getInWarehouseId());
        transfer.setTransferDate(reqVO.getTransferDate() != null ? reqVO.getTransferDate() : LocalDate.now());
        transfer.setTransferStatus("0");
        transfer.setHandler(reqVO.getHandler());
        transfer.setRemark(reqVO.getRemark());
        transfer.setCreateTime(LocalDateTime.now());
        save(transfer);

        // 保存明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsStockTransferItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsStockTransferItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsStockTransferItem item = new WmsStockTransferItem();
                        item.setTransferId(transfer.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(sku != null ? sku.getUnitId() : null);
                        item.setUnitName(sku != null ? sku.getUnitName() : null);
                        item.setQuantity(vo.getQuantity());
                        item.setRemark(vo.getRemark());
                        return item;
                    })
                    .collect(Collectors.toList());
            itemService.saveBatch(items);
        }

        billApproveFlowService.autoApproveAfterCreate(
                WmsApproveConstants.BILL_TYPE_STOCK_TRANSFER,
                transfer.getId(),
                transfer.getTransferNo(),
                () -> approve(transfer.getId())
        );

        return transfer.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsStockTransferSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        // 校验：调出仓库和调入仓库不能相同
        if (reqVO.getOutWarehouseId() != null && reqVO.getOutWarehouseId().equals(reqVO.getInWarehouseId())) {
            throw new ServiceException("调出仓库和调入仓库不能相同");
        }

        WmsStockTransfer transfer = new WmsStockTransfer();
        transfer.setId(reqVO.getId());
        transfer.setOutWarehouseId(reqVO.getOutWarehouseId());
        transfer.setInWarehouseId(reqVO.getInWarehouseId());
        transfer.setTransferDate(reqVO.getTransferDate());
        transfer.setHandler(reqVO.getHandler());
        transfer.setRemark(reqVO.getRemark());
        updateById(transfer);

        // 删除原有明细
        itemService.lambdaUpdate()
                .eq(WmsStockTransferItem::getTransferId, reqVO.getId())
                .remove();

        // 保存新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsStockTransferItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsStockTransferItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsStockTransferItem item = new WmsStockTransferItem();
                        item.setTransferId(transfer.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(sku != null ? sku.getUnitId() : null);
                        item.setUnitName(sku != null ? sku.getUnitName() : null);
                        item.setQuantity(vo.getQuantity());
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
        WmsStockTransfer transfer = getById(id);
        if (transfer == null) {
            throw new RuntimeException("调拨单不存在");
        }

        // 删除明细
        itemService.lambdaUpdate()
                .eq(WmsStockTransferItem::getTransferId, id)
                .remove();

        // 删除调拨单
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsStockTransfer transfer = getById(id);
        String billNo = transfer != null ? transfer.getTransferNo() : null;
        try {
            if (transfer == null) {
                throw new RuntimeException("调拨单不存在");
            }
            if ("1".equals(transfer.getTransferStatus())) {
                throw new RuntimeException("调拨单已审核");
            }

            // 获取调拨明细
            List<WmsStockTransferItem> items = itemService.lambdaQuery()
                    .eq(WmsStockTransferItem::getTransferId, id)
                    .list();

            if (items != null && !items.isEmpty()) {
                for (WmsStockTransferItem item : items) {
                    // 减少调出仓库库存
                    WmsStock outStock = stockService.lambdaQuery()
                        .eq(WmsStock::getWarehouseId, transfer.getOutWarehouseId())
                        .eq(WmsStock::getSkuId, item.getSkuId())
                        .one();

                    if (outStock == null) {
                        throw new RuntimeException("调出仓库SKU库存不存在");
                    }
                    if (outStock.getQuantity().compareTo(item.getQuantity()) < 0) {
                        throw new RuntimeException("调出仓库SKU库存不足");
                    }
                    BigDecimal outBeforeQty = outStock.getQuantity();
                    BigDecimal outNewQuantity = outStock.getQuantity().subtract(item.getQuantity());
                    outStock.setQuantity(outNewQuantity);
                    stockService.updateById(outStock);

                    // 记录调出仓库减少日志
                    createStockLog(transfer.getOutWarehouseId(), item.getSkuId(), item.getGoodsId(), "6", transfer.getTransferNo(), transfer.getId(),
                                   "2", item.getQuantity(), outBeforeQty, outNewQuantity, null, "调拨出库");

                    // 增加调入仓库库存
                    WmsStock inStock = stockService.lambdaQuery()
                        .eq(WmsStock::getWarehouseId, transfer.getInWarehouseId())
                        .eq(WmsStock::getSkuId, item.getSkuId())
                        .one();

                    if (inStock != null) {
                        BigDecimal inBeforeQty = inStock.getQuantity();
                        BigDecimal inNewQuantity = inStock.getQuantity().add(item.getQuantity());
                        inStock.setQuantity(inNewQuantity);
                        stockService.updateById(inStock);

                        // 记录调入仓库增加日志
                        createStockLog(transfer.getInWarehouseId(), item.getSkuId(), item.getGoodsId(), "6", transfer.getTransferNo(), transfer.getId(),
                                       "1", item.getQuantity(), inBeforeQty, inNewQuantity, null, "调拨入库");
                    } else {
                        BigDecimal inBeforeQty = BigDecimal.ZERO;
                        BigDecimal inNewQuantity = item.getQuantity();
                        inStock = new WmsStock();
                        inStock.setWarehouseId(transfer.getInWarehouseId());
                        inStock.setSkuId(item.getSkuId());
                        inStock.setGoodsId(item.getGoodsId());
                        inStock.setQuantity(inNewQuantity);
                        stockService.save(inStock);

                        // 记录调入仓库增加日志
                        createStockLog(transfer.getInWarehouseId(), item.getSkuId(), item.getGoodsId(), "6", transfer.getTransferNo(), transfer.getId(),
                                       "1", item.getQuantity(), inBeforeQty, inNewQuantity, null, "调拨入库");
                    }
                }
            }

            LoginUser user = SecurityUtils.getLoginUser();
            transfer.setTransferStatus("1");
            transfer.setApproveBy(user.getUsername());
            transfer.setApproveTime(LocalDateTime.now());
            updateById(transfer);
            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_STOCK_TRANSFER, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_STOCK_TRANSFER, id, billNo, ex);
            throw ex;
        }
    }

    /**
     * 生成调拨单号
     */
    private String generateTransferNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "DB" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxNo = baseMapper.selectMaxTransferNoIncludingDeleted(prefix, tenantId);

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
     * 批量填充仓库名称
     */
    private void fillWarehouseName(List<WmsStockTransferRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        list.forEach(item -> {
            if (item.getOutWarehouseId() != null) {
                WmsWarehouse warehouse = warehouseService.getById(item.getOutWarehouseId());
                if (warehouse != null) {
                    item.setOutWarehouseName(warehouse.getWarehouseName());
                }
            }
            if (item.getInWarehouseId() != null) {
                WmsWarehouse warehouse = warehouseService.getById(item.getInWarehouseId());
                if (warehouse != null) {
                    item.setInWarehouseName(warehouse.getWarehouseName());
                }
            }
        });
    }

    private void fillGoodsName(List<WmsStockTransferItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsStockTransferItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillBarcode(List<WmsStockTransferItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsStockTransferItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    private void validateItemsForSave(List<WmsStockTransferItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            throw new ServiceException("调拨明细不能为空");
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
    public List<WmsStockTransferExportVO> exportList(WmsStockTransferPageReqVO reqVO) {
        LambdaQueryWrapper<WmsStockTransfer> wrapper = new LambdaQueryWrapper<WmsStockTransfer>()
                .like(StringUtils.isNotEmpty(reqVO.getTransferNo()), WmsStockTransfer::getTransferNo, reqVO.getTransferNo())
                .eq(reqVO.getOutWarehouseId() != null, WmsStockTransfer::getOutWarehouseId, reqVO.getOutWarehouseId())
                .eq(reqVO.getInWarehouseId() != null, WmsStockTransfer::getInWarehouseId, reqVO.getInWarehouseId())
                .eq(StringUtils.isNotEmpty(reqVO.getTransferStatus()), WmsStockTransfer::getTransferStatus, reqVO.getTransferStatus())
                .orderByDesc(WmsStockTransfer::getCreateTime);
        List<WmsStockTransfer> list = list(wrapper);
        if (list.isEmpty()) return java.util.Collections.emptyList();

        Set<Long> outWhIds = list.stream().map(WmsStockTransfer::getOutWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> inWhIds = list.stream().map(WmsStockTransfer::getInWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> allWhIds = new java.util.HashSet<>();
        allWhIds.addAll(outWhIds); allWhIds.addAll(inWhIds);
        Map<Long, String> whNameMap = allWhIds.isEmpty() ? Collections.emptyMap() :
                warehouseService.listByIds(allWhIds).stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getWarehouseName));

        Set<Long> transferIds = list.stream().map(WmsStockTransfer::getId).collect(Collectors.toSet());
        List<WmsStockTransferItem> allItems = itemService.lambdaQuery()
                .in(WmsStockTransferItem::getTransferId, transferIds).list();
        Map<Long, List<WmsStockTransferItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsStockTransferItem::getTransferId));

        Set<Long> goodsIds = allItems.stream().map(WmsStockTransferItem::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        List<WmsStockTransferExportVO> exportList = new java.util.ArrayList<>();
        for (WmsStockTransfer t : list) {
            List<WmsStockTransferItem> items = itemMap.getOrDefault(t.getId(), Collections.emptyList());
            for (WmsStockTransferItem item : items) {
                WmsStockTransferExportVO vo = new WmsStockTransferExportVO();
                vo.setTransferNo(t.getTransferNo());
                vo.setTransferDate(t.getTransferDate());
                vo.setOutWarehouseName(whNameMap.get(t.getOutWarehouseId()));
                vo.setInWarehouseName(whNameMap.get(t.getInWarehouseId()));
                vo.setGoodsName(goodsNameMap.get(item.getGoodsId()));
                vo.setSkuName(item.getSkuName());
                vo.setSkuCode(item.getSkuCode());
                vo.setQuantity(item.getQuantity());
                vo.setCostPrice(item.getCostPrice());
                vo.setTransferStatus(t.getTransferStatus());
                vo.setHandler(t.getHandler());
                vo.setRemark(t.getRemark());
                vo.setCreateTime(t.getCreateTime());
                exportList.add(vo);
            }
        }
        return exportList;
    }
}
