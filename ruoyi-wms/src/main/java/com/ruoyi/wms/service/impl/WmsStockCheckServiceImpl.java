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
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsStockCheck;
import com.ruoyi.wms.domain.WmsStockCheckItem;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckItemRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckExportVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckPageReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckRespVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckSaveReqVO;
import com.ruoyi.wms.domain.vo.stockCheck.WmsStockCheckItemSaveReqVO;
import com.ruoyi.wms.mapper.WmsStockCheckMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsStockCheckItemService;
import com.ruoyi.wms.service.WmsStockCheckService;
import com.ruoyi.wms.service.WmsStockLogService;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * 库存盘点单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsStockCheckServiceImpl extends ServiceImpl<WmsStockCheckMapper, WmsStockCheck> implements WmsStockCheckService {

    private static final String CHECK_TYPE_FULL = "FULL";
    private static final String CHECK_TYPE_SAMPLE = "SAMPLE";
    private static final String CHECK_TYPE_DYNAMIC = "DYNAMIC";

    private final WmsStockCheckItemService itemService;
    private final WmsStockService stockService;
    private final WmsWarehouseService warehouseService;
    private final WmsGoodsSkuService goodsSkuService;
    private final WmsGoodsService goodsService;
    private final WmsStockLogService stockLogService;
    private final WmsBillApproveFlowService billApproveFlowService;

    public WmsStockCheckServiceImpl(WmsStockCheckItemService itemService,
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
    public IPage<WmsStockCheckRespVO> page(WmsStockCheckPageReqVO reqVO) {
        LambdaQueryWrapper<WmsStockCheck> wrapper = new LambdaQueryWrapper<WmsStockCheck>()
                .like(StringUtils.isNotEmpty(reqVO.getCheckNo()), WmsStockCheck::getCheckNo, reqVO.getCheckNo())
                .eq(reqVO.getWarehouseId() != null, WmsStockCheck::getWarehouseId, reqVO.getWarehouseId())
            .eq(StringUtils.isNotEmpty(reqVO.getCheckType()), WmsStockCheck::getCheckType, reqVO.getCheckType())
                .eq(StringUtils.isNotEmpty(reqVO.getCheckStatus()), WmsStockCheck::getCheckStatus, reqVO.getCheckStatus())
                .orderByDesc(WmsStockCheck::getCreateTime)
                .orderByDesc(WmsStockCheck::getId);

        Page<WmsStockCheck> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsStockCheckRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<WmsStockCheckRespVO> respVOList = VoUtils.convertList(page.getRecords(), WmsStockCheckRespVO.class);

        // 填充仓库名称
        fillWarehouseName(respVOList);

        resultPage.setRecords(respVOList);
        return resultPage;
    }

    @Override
    public WmsStockCheckRespVO get(Long id) {
        WmsStockCheck check = getById(id);
        WmsStockCheckRespVO respVO = VoUtils.convert(check, WmsStockCheckRespVO.class);

        // 填充仓库名称
        if (respVO != null) {
            WmsWarehouse warehouse = warehouseService.getById(respVO.getWarehouseId());
            if (warehouse != null) {
                respVO.setWarehouseName(warehouse.getWarehouseName());
            }

            // 填充明细
            List<WmsStockCheckItem> items = itemService.lambdaQuery()
                    .eq(WmsStockCheckItem::getCheckId, id)
                    .list();
                List<WmsStockCheckItemRespVO> itemVOs = VoUtils.convertList(items, WmsStockCheckItemRespVO.class);
                fillGoodsName(itemVOs);
                fillBarcode(itemVOs);
                respVO.setItems(itemVOs);
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsStockCheckSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        // 生成盘点单号
        String checkNo = generateCheckNo();

        WmsStockCheck check = new WmsStockCheck();
        check.setCheckNo(checkNo);
        check.setWarehouseId(reqVO.getWarehouseId());
        check.setCheckDate(reqVO.getCheckDate() != null ? reqVO.getCheckDate() : LocalDate.now());
        check.setCheckType(normalizeCheckType(reqVO.getCheckType()));
        check.setCheckStatus("0");
        check.setHandler(reqVO.getHandler());
        check.setRemark(reqVO.getRemark());
        save(check);

        // 保存明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsStockCheckItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsStockCheckItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsStockCheckItem item = new WmsStockCheckItem();
                        item.setCheckId(check.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(sku != null ? sku.getUnitId() : null);
                        item.setUnitName(sku != null ? sku.getUnitName() : null);
                        item.setBeforeQty(vo.getBeforeQty() != null ? vo.getBeforeQty() : BigDecimal.ZERO);
                        item.setCheckQty(vo.getCheckQty() != null ? vo.getCheckQty() : BigDecimal.ZERO);
                        BigDecimal beforeQty = item.getBeforeQty();
                        BigDecimal checkQty = item.getCheckQty();
                        item.setDiffQty(normalizeDiffQty(checkQty.subtract(beforeQty)));
                        item.setCostPrice(vo.getCostPrice() == null ? BigDecimal.ZERO : vo.getCostPrice());
                        item.setDiffAmount(item.getDiffQty().multiply(item.getCostPrice()));
                        item.setRemark(vo.getRemark());
                        return item;
                    })
                    .collect(Collectors.toList());
            itemService.saveBatch(items);
        }

        billApproveFlowService.autoApproveAfterCreate(
                WmsApproveConstants.BILL_TYPE_STOCK_CHECK,
                check.getId(),
                check.getCheckNo(),
                () -> approve(check.getId())
        );

        return check.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsStockCheckSaveReqVO reqVO) {
        validateItemsForSave(reqVO.getItems());

        WmsStockCheck check = new WmsStockCheck();
        check.setId(reqVO.getId());
        check.setWarehouseId(reqVO.getWarehouseId());
        check.setCheckDate(reqVO.getCheckDate());
        check.setCheckType(normalizeCheckType(reqVO.getCheckType()));
        check.setHandler(reqVO.getHandler());
        check.setRemark(reqVO.getRemark());
        updateById(check);

        // 删除原有明细
        itemService.lambdaUpdate()
                .eq(WmsStockCheckItem::getCheckId, reqVO.getId())
                .remove();

        // 保存新明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsStockCheckItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            List<WmsStockCheckItem> items = reqVO.getItems().stream()
                    .map(vo -> {
                        WmsGoodsSku sku = skuMap.get(vo.getSkuId());
                        WmsStockCheckItem item = new WmsStockCheckItem();
                        item.setCheckId(check.getId());
                        item.setSkuId(vo.getSkuId());
                        item.setGoodsId(sku != null ? sku.getGoodsId() : null);
                        item.setSkuCode(sku != null ? sku.getSkuCode() : null);
                        item.setSkuName(sku != null ? sku.getSkuName() : null);
                        item.setSpecInfo(sku != null ? sku.getSpecInfo() : null);
                        item.setUnitId(sku != null ? sku.getUnitId() : null);
                        item.setUnitName(sku != null ? sku.getUnitName() : null);
                        item.setBeforeQty(vo.getBeforeQty() != null ? vo.getBeforeQty() : BigDecimal.ZERO);
                        item.setCheckQty(vo.getCheckQty() != null ? vo.getCheckQty() : BigDecimal.ZERO);
                        BigDecimal beforeQty = item.getBeforeQty();
                        BigDecimal checkQty = item.getCheckQty();
                        item.setDiffQty(normalizeDiffQty(checkQty.subtract(beforeQty)));
                        item.setCostPrice(vo.getCostPrice() == null ? BigDecimal.ZERO : vo.getCostPrice());
                        item.setDiffAmount(item.getDiffQty().multiply(item.getCostPrice()));
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
        WmsStockCheck check = getById(id);
        if (check == null) {
            throw new RuntimeException("盘点单不存在");
        }

        // 删除明细
        itemService.lambdaUpdate()
                .eq(WmsStockCheckItem::getCheckId, id)
                .remove();

        // 删除盘点单
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsStockCheck check = getById(id);
        String billNo = check != null ? check.getCheckNo() : null;
        try {
            if (check == null) {
                throw new RuntimeException("盘点单不存在");
            }
            if ("1".equals(check.getCheckStatus())) {
                throw new RuntimeException("盘点单已审核");
            }

            // 获取盘点明细
            List<WmsStockCheckItem> items = itemService.lambdaQuery()
                .eq(WmsStockCheckItem::getCheckId, id)
                .list();

            if (items != null && !items.isEmpty()) {
                for (WmsStockCheckItem item : items) {
                    // 使用保存的账面数量和实盘数量
                    BigDecimal beforeQty = item.getBeforeQty() != null ? item.getBeforeQty() : BigDecimal.ZERO;
                    BigDecimal checkQty = item.getCheckQty() != null ? item.getCheckQty() : BigDecimal.ZERO;
                    // 使用保存的差异数量
                        BigDecimal diffQty = item.getDiffQty() != null
                            ? normalizeDiffQty(item.getDiffQty())
                            : normalizeDiffQty(checkQty.subtract(beforeQty));

                    // 只有有差异时才调整库存
                    if (diffQty.compareTo(BigDecimal.ZERO) != 0) {
                        // 查询当前库存
                        WmsStock stock = stockService.lambdaQuery()
                            .eq(WmsStock::getWarehouseId, check.getWarehouseId())
                            .eq(WmsStock::getSkuId, item.getSkuId())
                            .one();

                        BigDecimal currentStock = stock != null ? stock.getQuantity() : BigDecimal.ZERO;
                        BigDecimal afterQty = currentStock.add(diffQty);

                        // 检查调整后库存不能为负
                        if (afterQty.compareTo(BigDecimal.ZERO) < 0) {
                            throw new ServiceException(String.format(
                                "商品[%s]盘点后库存不能为负数：账面=%s，盘点=%s，差异=%s",
                                item.getSkuName() != null ? item.getSkuName() : item.getSkuId(),
                                beforeQty, checkQty, diffQty
                            ));
                        }

                        if (stock != null) {
                            // 更新现有库存记录
                            stock.setQuantity(afterQty);
                            stockService.updateById(stock);
                        } else {
                            // 创建新的库存记录（只有盘盈才会到这里）
                            stock = new WmsStock();
                            stock.setWarehouseId(check.getWarehouseId());
                            stock.setSkuId(item.getSkuId());
                            stock.setGoodsId(item.getGoodsId());
                            stock.setQuantity(afterQty);
                            stockService.save(stock);
                        }

                        // 记录库存日志
                        String changeType = diffQty.compareTo(BigDecimal.ZERO) > 0 ? "1" : "2"; // 1=增加，2=减少
                        createStockLog(check.getWarehouseId(), item.getSkuId(), item.getGoodsId(),
                                "7", check.getCheckNo(), check.getId(),
                                changeType, diffQty.abs(), currentStock, afterQty, null, "盘点调整");
                    }
                }
            }

            LoginUser user = SecurityUtils.getLoginUser();
            check.setCheckStatus("1");
            check.setApproveBy(user.getUsername());
            check.setApproveTime(LocalDateTime.now());
            updateById(check);
            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_STOCK_CHECK, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_STOCK_CHECK, id, billNo, ex);
            throw ex;
        }
    }

    private String normalizeCheckType(String checkType) {
        if (StringUtils.isBlank(checkType)) {
            return CHECK_TYPE_FULL;
        }
        if (CHECK_TYPE_DYNAMIC.equals(checkType)) {
            return CHECK_TYPE_SAMPLE;
        }
        if (!CHECK_TYPE_FULL.equals(checkType) && !CHECK_TYPE_SAMPLE.equals(checkType)) {
            throw new ServiceException("盘点类型仅支持FULL或SAMPLE");
        }
        return checkType;
    }

    private BigDecimal normalizeDiffQty(BigDecimal diffQty) {
        if (diffQty == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return diffQty.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 创建库存日志
     */
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

    /**
     * 生成盘点单号
     */
    private String generateCheckNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "PD" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxNo = baseMapper.selectMaxCheckNoIncludingDeleted(prefix, tenantId);

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
    private void fillWarehouseName(List<WmsStockCheckRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        list.forEach(item -> {
            if (item.getWarehouseId() != null) {
                WmsWarehouse warehouse = warehouseService.getById(item.getWarehouseId());
                if (warehouse != null) {
                    item.setWarehouseName(warehouse.getWarehouseName());
                }
            }
        });
    }

    private void fillGoodsName(List<WmsStockCheckItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsStockCheckItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillBarcode(List<WmsStockCheckItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsStockCheckItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    private void validateItemsForSave(List<WmsStockCheckItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            throw new ServiceException("盘点明细不能为空");
        }
        for (WmsStockCheckItemSaveReqVO item : items) {
            if (item == null || item.getSkuId() == null) {
                throw new ServiceException("盘点明细商品不能为空");
            }
            if (item.getCheckQty() == null || item.getCheckQty().compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("盘点数量不能小于0");
            }
            if (item.getCostPrice() != null && item.getCostPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ServiceException("成本价不能小于0");
            }
        }
    }

    @Override
    public List<WmsStockCheckExportVO> exportList(WmsStockCheckPageReqVO reqVO) {
        LambdaQueryWrapper<WmsStockCheck> wrapper = new LambdaQueryWrapper<WmsStockCheck>()
                .like(StringUtils.isNotEmpty(reqVO.getCheckNo()), WmsStockCheck::getCheckNo, reqVO.getCheckNo())
                .eq(reqVO.getWarehouseId() != null, WmsStockCheck::getWarehouseId, reqVO.getWarehouseId())
                .eq(StringUtils.isNotEmpty(reqVO.getCheckType()), WmsStockCheck::getCheckType, reqVO.getCheckType())
                .eq(StringUtils.isNotEmpty(reqVO.getCheckStatus()), WmsStockCheck::getCheckStatus, reqVO.getCheckStatus())
                .orderByDesc(WmsStockCheck::getCreateTime);
        List<WmsStockCheck> list = list(wrapper);
        if (list.isEmpty()) return java.util.Collections.emptyList();

        Set<Long> warehouseIds = list.stream().map(WmsStockCheck::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> whNameMap = warehouseIds.isEmpty() ? Collections.emptyMap() :
                warehouseService.listByIds(warehouseIds).stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getWarehouseName));

        Set<Long> checkIds = list.stream().map(WmsStockCheck::getId).collect(Collectors.toSet());
        List<WmsStockCheckItem> allItems = itemService.lambdaQuery()
                .in(WmsStockCheckItem::getCheckId, checkIds).list();
        Map<Long, List<WmsStockCheckItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsStockCheckItem::getCheckId));

        Set<Long> goodsIds = allItems.stream().map(WmsStockCheckItem::getGoodsId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream().collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        List<WmsStockCheckExportVO> exportList = new java.util.ArrayList<>();
        for (WmsStockCheck c : list) {
            List<WmsStockCheckItem> items = itemMap.getOrDefault(c.getId(), Collections.emptyList());
            for (WmsStockCheckItem item : items) {
                WmsStockCheckExportVO vo = new WmsStockCheckExportVO();
                vo.setCheckNo(c.getCheckNo());
                vo.setCheckDate(c.getCheckDate());
                vo.setWarehouseName(whNameMap.get(c.getWarehouseId()));
                vo.setCheckType(c.getCheckType());
                vo.setGoodsName(goodsNameMap.get(item.getGoodsId()));
                vo.setSkuName(item.getSkuName());
                vo.setSkuCode(item.getSkuCode());
                vo.setBeforeQty(item.getBeforeQty());
                vo.setCheckQty(item.getCheckQty());
                vo.setDiffQty(item.getDiffQty());
                vo.setCheckStatus(c.getCheckStatus());
                vo.setHandler(c.getHandler());
                vo.setRemark(c.getRemark());
                vo.setCreateTime(c.getCreateTime());
                exportList.add(vo);
            }
        }
        return exportList;
    }
}
