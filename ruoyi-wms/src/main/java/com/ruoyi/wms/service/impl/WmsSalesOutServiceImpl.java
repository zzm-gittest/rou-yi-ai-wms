package com.ruoyi.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.core.domain.entity.SysTenant;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.constant.WmsApproveConstants;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.WmsSalesOrderItem;
import com.ruoyi.wms.domain.WmsSalesOut;
import com.ruoyi.wms.domain.WmsSalesOutItem;
import com.ruoyi.wms.domain.WmsAccountsReceivable;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.WmsStock;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.domain.WmsStockLog;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintItemRespVO;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutExportVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemSaveReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutPageReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderMapper;
import com.ruoyi.wms.mapper.WmsSalesOutItemMapper;
import com.ruoyi.wms.mapper.WmsSalesOutMapper;
import com.ruoyi.wms.mapper.WmsTenantPrintProfileMapper;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderItemMapper;
import com.ruoyi.wms.service.WmsGoodsSkuService;
import com.ruoyi.wms.service.WmsGoodsService;
import com.ruoyi.wms.service.WmsSalesOutService;
import com.ruoyi.wms.service.WmsSalesOrderService;
import com.ruoyi.wms.service.WmsStockService;
import com.ruoyi.wms.service.WmsStockLogService;
import com.ruoyi.wms.service.WmsAccountsReceivableService;
import com.ruoyi.wms.service.WmsFinanceLogService;
import com.ruoyi.wms.service.WmsAccountService;
import com.ruoyi.wms.service.WmsUserOnboardingService;
import com.ruoyi.wms.service.WmsBillApproveFlowService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.WmsAccount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 销售出库单表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsSalesOutServiceImpl extends ServiceImpl<WmsSalesOutMapper, WmsSalesOut> implements WmsSalesOutService {

    private static final Logger log = LoggerFactory.getLogger(WmsSalesOutServiceImpl.class);

    @Autowired
    private WmsSalesOutItemMapper salesOutItemMapper;

    @Autowired
    private WmsCustomerMapper customerMapper;

    @Autowired
    private WmsWarehouseMapper warehouseMapper;

    @Autowired
    private WmsSalesOrderMapper salesOrderMapper;

    @Autowired
    private WmsSalesOrderItemMapper salesOrderItemMapper;

    @Autowired
    private WmsSalesOrderService salesOrderService;

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
    private WmsFinanceLogService financeLogService;

    @Autowired
    private WmsAccountService accountService;

    @Autowired
    private WmsUserOnboardingService onboardingService;

    @Autowired
    private WmsBillApproveFlowService billApproveFlowService;

    @Autowired
    private WmsTenantPrintProfileMapper tenantPrintProfileMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String ERR_FIELD_LOCKED = "SALES_OUT_FIELD_LOCKED";
    private static final String ERR_CUSTOMER_INVALID = "SALES_OUT_CUSTOMER_INVALID";
    private static final String ERR_RECALC_FAILED = "SALES_OUT_RECALC_FAILED";

    @Override
    public IPage<WmsSalesOutRespVO> page(WmsSalesOutPageReqVO reqVO) {
        log.info("=== 销售出库分页查询, approveStatus参数: {}", reqVO.getApproveStatus());
        LambdaQueryWrapper<WmsSalesOut> wrapper = new LambdaQueryWrapper<WmsSalesOut>()
                .like(StringUtils.isNotEmpty(reqVO.getOutNo()), WmsSalesOut::getOutNo, reqVO.getOutNo())
                .eq(reqVO.getCustomerId() != null, WmsSalesOut::getCustomerId, reqVO.getCustomerId())
                .eq(reqVO.getWarehouseId() != null, WmsSalesOut::getWarehouseId, reqVO.getWarehouseId())
                // 根据审核状态过滤：0-待审核(approveBy为空)，1-已审核(approveBy不为空)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY IS NULL OR TRIM(APPROVE_BY) = ''")
                .isNotNull(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsSalesOut::getApproveBy)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    "TRIM(APPROVE_BY) != ''")
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsSalesOut::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsSalesOut::getCreateTime)
                .orderByDesc(WmsSalesOut::getId);

        Page<WmsSalesOut> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsSalesOut> result = baseMapper.selectPage(page, wrapper);

        // 转换为RespVO
        return result.convert(item -> {
            WmsSalesOutRespVO vo = new WmsSalesOutRespVO();
            BeanUtils.copyProperties(item, vo);

            // 计算应收金额
            BigDecimal receivableAmount = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
            vo.setReceivableAmount(receivableAmount);

            // 计算收款状态
            BigDecimal receivedAmount = item.getReceivedAmount() != null ? item.getReceivedAmount() : BigDecimal.ZERO;
            if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
                vo.setPaymentStatus("0"); // 未收
            } else if (receivedAmount.compareTo(receivableAmount) < 0) {
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

            // 填充仓库名称
            if (item.getWarehouseId() != null) {
                var warehouse = warehouseMapper.selectById(item.getWarehouseId());
                if (warehouse != null) {
                    vo.setWarehouseName(warehouse.getWarehouseName());
                }
            }

            // 填充订单号
            if (item.getOrderId() != null) {
                var order = salesOrderMapper.selectById(item.getOrderId());
                if (order != null) {
                    vo.setOrderNo(order.getOrderNo());
                }
            }

            // 设置审核状态
            vo.setApproveStatus(StringUtils.isNotBlank(item.getApproveBy()) ? "1" : "0");

            return vo;
        });
    }

    @Override
    public WmsSalesOutRespVO get(Long id) {
        WmsSalesOut salesOut = baseMapper.selectById(id);
        if (salesOut == null) {
            throw new ServiceException("出库单不存在");
        }

        WmsSalesOutRespVO vo = new WmsSalesOutRespVO();
            BeanUtils.copyProperties(salesOut, vo);

        // 计算应收金额
        BigDecimal receivableAmount = salesOut.getTotalAmount() != null ? salesOut.getTotalAmount() : BigDecimal.ZERO;
        vo.setReceivableAmount(receivableAmount);

        // 计算收款状态
        BigDecimal receivedAmount = salesOut.getReceivedAmount() != null ? salesOut.getReceivedAmount() : BigDecimal.ZERO;
        if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
            vo.setPaymentStatus("0"); // 未收
        } else if (receivedAmount.compareTo(receivableAmount) < 0) {
            vo.setPaymentStatus("1"); // 部分
        } else {
            vo.setPaymentStatus("2"); // 已收
        }

        // 填充客户名称
        if (salesOut.getCustomerId() != null) {
            var customer = customerMapper.selectById(salesOut.getCustomerId());
            if (customer != null) {
                vo.setCustomerName(customer.getCustomerName());
            }
        }

        // 填充仓库名称
        if (salesOut.getWarehouseId() != null) {
            var warehouse = warehouseMapper.selectById(salesOut.getWarehouseId());
            if (warehouse != null) {
                vo.setWarehouseName(warehouse.getWarehouseName());
            }
        }

        // 填充订单号
        if (salesOut.getOrderId() != null) {
            var order = salesOrderMapper.selectById(salesOut.getOrderId());
            if (order != null) {
                vo.setOrderNo(order.getOrderNo());
            }
        }

        // 查询明细
        LambdaQueryWrapper<WmsSalesOutItem> itemWrapper = new LambdaQueryWrapper<WmsSalesOutItem>()
                .eq(WmsSalesOutItem::getOutId, id);
        List<WmsSalesOutItem> items = salesOutItemMapper.selectList(itemWrapper);
        List<WmsSalesOutItemRespVO> itemVOs = items.stream()
                .map(item -> {
                WmsSalesOutItemRespVO itemVO = new WmsSalesOutItemRespVO();
                BeanUtils.copyProperties(item, itemVO);
                return itemVO;
            })
                .collect(Collectors.toList());
        fillGoodsName(itemVOs);
        fillBarcode(itemVOs);

        // 如果是订单出库，填充订单相关字段
        if (salesOut.getOrderId() != null) {
            // 获取订单明细
            List<WmsSalesOrderItem> orderItems = salesOrderItemMapper.selectList(
                    new LambdaQueryWrapper<WmsSalesOrderItem>()
                            .eq(WmsSalesOrderItem::getOrderId, salesOut.getOrderId())
            );
            // 创建订单item的映射（用Long类型的skuId作为键）
            Map<Long, WmsSalesOrderItem> orderItemMap = orderItems.stream()
                    .collect(Collectors.toMap(WmsSalesOrderItem::getSkuId, item -> item));

            // 为每个出库单明细填充订单相关字段
            for (WmsSalesOutItemRespVO itemVO : itemVOs) {
                if (itemVO.getSkuId() != null) {
                    WmsSalesOrderItem orderItem = orderItemMap.get(itemVO.getSkuId());
                    if (orderItem != null) {
                        BigDecimal orderQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : BigDecimal.ZERO;
                        BigDecimal totalDeliveredQty = orderItem.getDeliveredQty() != null ? orderItem.getDeliveredQty() : BigDecimal.ZERO;
                        BigDecimal currentOutQty = itemVO.getQuantity() != null ? itemVO.getQuantity() : BigDecimal.ZERO;

                        // 判断出库单是否已审核
                        boolean isApproved = StringUtils.isNotBlank(salesOut.getApproveBy());

                        // 计算本次出库之前的已发货数量
                        BigDecimal deliveredQtyBeforeThis = isApproved
                            ? totalDeliveredQty.subtract(currentOutQty).max(BigDecimal.ZERO)
                            : totalDeliveredQty;

                        // 设置订单相关字段
                        itemVO.setOrderQuantity(orderQty);
                        itemVO.setDeliveredQty(deliveredQtyBeforeThis);
                        // 可发货数量 = 订单数量 - 本次出库之前的已发货数量
                        itemVO.setAvailableQty(orderQty.subtract(deliveredQtyBeforeThis).max(BigDecimal.ZERO));
                    } else {
                        // 没有找到对应的订单明细
                        itemVO.setOrderQuantity(BigDecimal.ZERO);
                        itemVO.setDeliveredQty(BigDecimal.ZERO);
                        itemVO.setAvailableQty(BigDecimal.ZERO);
                    }
                }
            }
        }

        vo.setItems(itemVOs);

        // 设置审核状态
        String approveStatus = StringUtils.isNotBlank(salesOut.getApproveBy()) ? "1" : "0";
        vo.setApproveStatus(approveStatus);
        log.info("=== 销售出库详情查询 id={}, approveBy={}, approveStatus={}",
            id, salesOut.getApproveBy(), approveStatus);

        return vo;
    }

    @Override
    public WmsBillPrintRespVO getPrintData(Long id) {
        WmsSalesOut bill = baseMapper.selectById(id);
        if (bill == null) {
            throw new ServiceException("销售出库单不存在");
        }

        WmsSalesOutRespVO detail = get(id);
        SysTenant tenant = getTenantPrintProfile();

        WmsBillPrintRespVO respVO = new WmsBillPrintRespVO();
        respVO.setTitle("销售出库单");
        respVO.setCompanyName(resolvePrintCompanyName(tenant));
        respVO.setCompanyAddress(tenant != null ? tenant.getPrintCompanyAddress() : null);
        respVO.setCompanyPhone(tenant != null ? tenant.getPrintCompanyPhone() : null);
        respVO.setLogoPath(tenant != null ? tenant.getPrintLogoPath() : null);
        respVO.setPrintFontScale(tenant != null ? tenant.getPrintFontScale() : java.math.BigDecimal.ONE);
        respVO.setPrintPageSize(tenant != null ? tenant.getPrintPageSize() : "A4");
        respVO.setBillNo(detail.getOutNo());
        respVO.setBillDate(detail.getOutDate() != null ? detail.getOutDate().toString() : null);
        respVO.setWarehouseName(detail.getWarehouseName());
        respVO.setCounterpartName(detail.getCustomerName());
        respVO.setHandler(detail.getHandler());
        respVO.setApproveBy(detail.getApproveBy());
        respVO.setApproveTime(detail.getApproveTime());
        respVO.setTotalAmount(detail.getTotalAmount());
        respVO.setRemark(detail.getRemark());
        respVO.setPrintTime(LocalDateTime.now());
        respVO.setItems(buildPrintItems(detail.getItems()));
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsSalesOutSaveReqVO reqVO) {
        validateItemsPositive(reqVO.getItems());
        validateCustomerAvailable(reqVO.getCustomerId());
        assertSourceBillNoNotDuplicate(reqVO.getSourceBillNo(), null);

        WmsSalesOut salesOut = new WmsSalesOut();
        BeanUtils.copyProperties(reqVO, salesOut);
        salesOut.setOutNo(generateOutNo());
        salesOut.setTotalAmount(BigDecimal.ZERO);
        salesOut.setReceivedAmount(reqVO.getReceivedAmount() != null ? reqVO.getReceivedAmount() : BigDecimal.ZERO);
        salesOut.setPaymentStatus("0");
        // 设置来源类型：1直接出库 2订单出库
        salesOut.setSourceType(reqVO.getSourceType() != null ? reqVO.getSourceType() :
                              (reqVO.getOrderId() != null ? 2 : 1));
        salesOut.setCreateTime(LocalDateTime.now());

        // 插入主表
        baseMapper.insert(salesOut);

        // 插入明细
        if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
            // 批量查询SKU信息
            List<Long> skuIds = reqVO.getItems().stream()
                    .map(WmsSalesOutItemSaveReqVO::getSkuId)
                    .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (WmsSalesOutItemSaveReqVO itemReq : reqVO.getItems()) {
                WmsSalesOutItem item = new WmsSalesOutItem();
                item.setOutId(salesOut.getId());
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

                salesOutItemMapper.insert(item);
            }
            salesOut.setTotalAmount(totalAmount);
            baseMapper.updateById(salesOut);

            // 更新收款状态
            updatePaymentStatus(salesOut);
        }

        // 新手任务要求是“创建销售出库单”，创建成功即标记完成。
        tryCompleteSalesOutOnboardingOnCreate(resolveLoginUserOrNull());

        billApproveFlowService.autoApproveAfterCreate(
            WmsApproveConstants.BILL_TYPE_SALES_OUT,
            salesOut.getId(),
            salesOut.getOutNo(),
            () -> approve(salesOut.getId())
        );

        return salesOut.getId();
    }

    /** 历史数据：备注前缀（新单请用 source_bill_no 字段） */
    private static final String LEGACY_IMPORT_REMARK_PREFIX = "导入来源单号:";

    @Override
    public String findOutNoByImportSourceBillNo(String sourceBillNo) {
        if (StringUtils.isEmpty(sourceBillNo)) {
            return null;
        }
        String billNo = sourceBillNo.trim();
        WmsSalesOut found = lambdaQuery()
            .eq(WmsSalesOut::getSourceBillNo, billNo)
            .orderByDesc(WmsSalesOut::getCreateTime)
            .last("limit 1")
            .one();
        if (found != null) {
            return found.getOutNo();
        }
        String remarkPrefix = LEGACY_IMPORT_REMARK_PREFIX + billNo;
        found = lambdaQuery()
            .likeRight(WmsSalesOut::getRemark, remarkPrefix)
            .orderByDesc(WmsSalesOut::getCreateTime)
            .last("limit 1")
            .one();
        return found != null ? found.getOutNo() : null;
    }

    private void assertSourceBillNoNotDuplicate(String sourceBillNo, Long excludeId) {
        if (StringUtils.isEmpty(sourceBillNo)) {
            return;
        }
        String existingOutNo = findOutNoByImportSourceBillNo(sourceBillNo);
        if (StringUtils.isEmpty(existingOutNo)) {
            return;
        }
        if (excludeId != null) {
            WmsSalesOut current = baseMapper.selectById(excludeId);
            if (current != null && sourceBillNo.trim().equals(StringUtils.trimToEmpty(current.getSourceBillNo()))) {
                return;
            }
        }
        throw new ServiceException("来源单号已存在，WMS出库单号: " + existingOutNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsSalesOutSaveReqVO reqVO) {
        validateItemsPositive(reqVO.getItems());
        validateCustomerAvailable(reqVO.getCustomerId());

        WmsSalesOut existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("出库单不存在");
        }

        boolean lockCriticalFields = shouldLockCriticalFields(existing);
        validateCriticalFieldsForAutoApprovedBill(existing, reqVO, lockCriticalFields);
        Long oldCustomerId = existing.getCustomerId();

        BeanUtils.copyProperties(reqVO, existing);
        if (reqVO.getReceivedAmount() != null) {
            existing.setReceivedAmount(reqVO.getReceivedAmount());
        }

        try {
            // 删除原明细
            LambdaQueryWrapper<WmsSalesOutItem> wrapper = new LambdaQueryWrapper<WmsSalesOutItem>()
                    .eq(WmsSalesOutItem::getOutId, reqVO.getId());
            salesOutItemMapper.delete(wrapper);

            BigDecimal totalAmount = BigDecimal.ZERO;
            // 插入新明细
            if (reqVO.getItems() != null && !reqVO.getItems().isEmpty()) {
                // 批量查询SKU信息
                List<Long> skuIds = reqVO.getItems().stream()
                        .map(WmsSalesOutItemSaveReqVO::getSkuId)
                        .collect(Collectors.toList());
                Map<Long, WmsGoodsSku> skuMap = goodsSkuService.getEnabledSkuMap(skuIds);

                for (WmsSalesOutItemSaveReqVO itemReq : reqVO.getItems()) {
                    WmsSalesOutItem item = new WmsSalesOutItem();
                    item.setOutId(existing.getId());
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
                    BigDecimal unitPrice = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
                    BigDecimal amount = itemReq.getQuantity().multiply(unitPrice);
                    item.setAmount(amount);
                    totalAmount = totalAmount.add(amount);

                    salesOutItemMapper.insert(item);
                }
            }
            // 客户改动后由服务端覆盖重算结果，防止前端绕过金额计算。
            existing.setTotalAmount(totalAmount);
        } catch (RuntimeException ex) {
            throw new ServiceException(buildBizError(ERR_RECALC_FAILED, "金额重算失败，请重试"));
        }

        // 更新收款状态
        updatePaymentStatus(existing);

        baseMapper.updateById(existing);

        // 自动审核且已审核单据允许改客户时，需要同步应收账款中的客户归属。
        if (lockCriticalFields && !Objects.equals(oldCustomerId, existing.getCustomerId())) {
            syncAccountsReceivableCustomer(existing.getId(), existing.getCustomerId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsSalesOut existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("出库单不存在");
        }

        // 删除明细
        LambdaQueryWrapper<WmsSalesOutItem> wrapper = new LambdaQueryWrapper<WmsSalesOutItem>()
                .eq(WmsSalesOutItem::getOutId, id);
        salesOutItemMapper.delete(wrapper);

        // 删除主表
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsSalesOut salesOut = baseMapper.selectById(id);
        String billNo = salesOut != null ? salesOut.getOutNo() : null;
        try {
            if (salesOut == null) {
                throw new ServiceException("出库单不存在");
            }
            if (StringUtils.isNotEmpty(salesOut.getApproveBy())) {
                throw new ServiceException("出库单已审核");
            }

            LoginUser user = resolveLoginUserOrNull();
            salesOut.setApproveBy(user != null ? user.getUsername() : "system");
            salesOut.setApproveTime(LocalDateTime.now());
            salesOut.setStatus("1");
            baseMapper.updateById(salesOut);

            // 获取出库明细
            List<WmsSalesOutItem> items = salesOutItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOutItem>()
                    .eq(WmsSalesOutItem::getOutId, id)
            );

            if (items != null && !items.isEmpty()) {
                for (WmsSalesOutItem item : items) {
                    // 如果是订单出库，更新销售订单明细的已发货数量
                    if (salesOut.getOrderId() != null && item.getSkuId() != null) {
                        WmsSalesOrderItem orderItem = salesOrderItemMapper.selectOne(
                                new LambdaQueryWrapper<WmsSalesOrderItem>()
                                        .eq(WmsSalesOrderItem::getOrderId, salesOut.getOrderId())
                                        .eq(WmsSalesOrderItem::getSkuId, item.getSkuId())
                        );
                        if (orderItem != null) {
                            BigDecimal orderQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : BigDecimal.ZERO;
                            BigDecimal currentDelivered = orderItem.getDeliveredQty() != null ? orderItem.getDeliveredQty() : BigDecimal.ZERO;
                            BigDecimal newDelivered = currentDelivered.add(item.getQuantity());

                            // 校验：已发货数量 + 本次出库数量不能超过订单数量
                            if (newDelivered.compareTo(orderQty) > 0) {
                                throw new ServiceException(String.format(
                                    "商品[%s]出库数量超限：订单数量=%s，已发货数量=%s，本次出库数量=%s",
                                    item.getSkuName() != null ? item.getSkuName() : item.getSkuId(),
                                    orderQty, currentDelivered, item.getQuantity()
                                ));
                            }

                            orderItem.setDeliveredQty(newDelivered);
                            salesOrderItemMapper.updateById(orderItem);
                        }
                    }

                    WmsStock stock = stockService.lambdaQuery()
                            .eq(WmsStock::getWarehouseId, salesOut.getWarehouseId())
                            .eq(WmsStock::getSkuId, item.getSkuId())
                            .one();

                    BigDecimal beforeQty = BigDecimal.ZERO;
                    BigDecimal costPriceSnapshot = BigDecimal.ZERO;
                    if (stock == null) {
                        // 无库存记录时，从SKU采购价获取成本参考价
                        WmsGoodsSku sku = goodsSkuService.getById(item.getSkuId());
                        BigDecimal fallbackCost = (sku != null && sku.getPurchasePrice() != null) ? sku.getPurchasePrice() : BigDecimal.ZERO;
                        stock = new WmsStock();
                        stock.setWarehouseId(salesOut.getWarehouseId());
                        stock.setSkuId(item.getSkuId());
                        stock.setGoodsId(item.getGoodsId());
                        stock.setQuantity(BigDecimal.ZERO);
                        stock.setCostPrice(fallbackCost);
                        costPriceSnapshot = fallbackCost;
                        stockService.save(stock);
                    } else {
                        beforeQty = stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO;
                        costPriceSnapshot = stock.getCostPrice() != null ? stock.getCostPrice() : BigDecimal.ZERO;
                    }

                    BigDecimal newQuantity = beforeQty.subtract(item.getQuantity());
                    stock.setQuantity(newQuantity);
                    stockService.updateById(stock);

                    createStockLog(salesOut.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "2", salesOut.getOutNo(), salesOut.getId(),
                                   "2", item.getQuantity(), beforeQty, newQuantity, costPriceSnapshot, "销售出库");
                }

                // 销售出库审核后，根据客户类型回写价格：批发客户→更新批发价，零售客户→更新销售价
                WmsCustomer customer = salesOut.getCustomerId() != null ? customerMapper.selectById(salesOut.getCustomerId()) : null;
                String customerType = customer != null ? customer.getCustomerType() : null;
                syncSkuPriceFromApprovedItems(items, customerType);
            }

            // 如果是订单出库，更新订单状态
            if (salesOut.getOrderId() != null) {
                updateOrderStatusAfterOut(salesOut.getOrderId());
            }

            // 生成应收账款
            accountsReceivableService.createByBill(
                    salesOut.getCustomerId(),
                    null, // supplierId - 销售出库不涉及供应商
                    "2", // 2=销售出库
                    salesOut.getOutNo(),
                    salesOut.getId(),
                    salesOut.getTotalAmount(),
                    salesOut.getReceivedAmount(),
                    salesOut.getOutDate()
            );

            tryCompleteSalesOutOnboarding(user);
            billApproveFlowService.recordApproveSuccess(WmsApproveConstants.BILL_TYPE_SALES_OUT, id, billNo);
        } catch (RuntimeException ex) {
            billApproveFlowService.recordApproveFail(WmsApproveConstants.BILL_TYPE_SALES_OUT, id, billNo, ex);
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApprove(Long id) {
        WmsSalesOut salesOut = baseMapper.selectById(id);
        if (salesOut == null) {
            throw new ServiceException("出库单不存在");
        }
        if (StringUtils.isEmpty(salesOut.getApproveBy())) {
            throw new ServiceException("出库单未审核，无需反审核");
        }

        List<WmsSalesOutItem> items = salesOutItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOutItem>()
                        .eq(WmsSalesOutItem::getOutId, id)
        );

        if (items != null && !items.isEmpty()) {
            for (WmsSalesOutItem item : items) {
                WmsStock stock = stockService.lambdaQuery()
                        .eq(WmsStock::getWarehouseId, salesOut.getWarehouseId())
                        .eq(WmsStock::getSkuId, item.getSkuId())
                        .one();

                BigDecimal rollbackQty = item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO;
                if (rollbackQty.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ServiceException("反审核数量异常");
                }

                if (stock == null) {
                    BigDecimal beforeQty = BigDecimal.ZERO;
                    WmsStock newStock = new WmsStock();
                    newStock.setWarehouseId(salesOut.getWarehouseId());
                    newStock.setSkuId(item.getSkuId());
                    newStock.setGoodsId(item.getGoodsId());
                    newStock.setQuantity(rollbackQty);
                    newStock.setCostPrice(item.getPrice());
                    stockService.save(newStock);

                    createStockLog(salesOut.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "2", salesOut.getOutNo(), salesOut.getId(),
                            "1", rollbackQty, beforeQty, rollbackQty, item.getPrice(), "销售出库反审核");
                } else {
                    BigDecimal beforeQty = stock.getQuantity() != null ? stock.getQuantity() : BigDecimal.ZERO;
                    BigDecimal newQty = beforeQty.add(rollbackQty);
                    stock.setQuantity(newQty);
                    stockService.updateById(stock);

                    BigDecimal costPriceSnapshot = stock.getCostPrice() != null ? stock.getCostPrice() : item.getPrice();
                    createStockLog(salesOut.getWarehouseId(), item.getSkuId(), item.getGoodsId(), "2", salesOut.getOutNo(), salesOut.getId(),
                            "1", rollbackQty, beforeQty, newQty, costPriceSnapshot, "销售出库反审核");
                }

                if (salesOut.getOrderId() != null && item.getSkuId() != null) {
                    WmsSalesOrderItem orderItem = salesOrderItemMapper.selectOne(
                            new LambdaQueryWrapper<WmsSalesOrderItem>()
                                    .eq(WmsSalesOrderItem::getOrderId, salesOut.getOrderId())
                                    .eq(WmsSalesOrderItem::getSkuId, item.getSkuId())
                    );
                    if (orderItem != null) {
                        BigDecimal currentDelivered = orderItem.getDeliveredQty() != null ? orderItem.getDeliveredQty() : BigDecimal.ZERO;
                        BigDecimal newDelivered = currentDelivered.subtract(rollbackQty);
                        if (newDelivered.compareTo(BigDecimal.ZERO) < 0) {
                            throw new ServiceException("订单已发货数量异常，无法反审核");
                        }
                        orderItem.setDeliveredQty(newDelivered);
                        salesOrderItemMapper.updateById(orderItem);
                    }
                }
            }
        }

        // 反审核后强制回写NULL审核字段，避免更新策略跳过null导致页面状态未刷新。
        lambdaUpdate()
            .eq(WmsSalesOut::getId, id)
            .set(WmsSalesOut::getApproveBy, null)
            .set(WmsSalesOut::getApproveTime, null)
            .set(WmsSalesOut::getStatus, "0")
            .update();

        if (salesOut.getOrderId() != null) {
            updateOrderStatusAfterOut(salesOut.getOrderId());
        }

        accountsReceivableService.lambdaQuery()
                .eq(WmsAccountsReceivable::getBillId, id)
                .eq(WmsAccountsReceivable::getBillType, "2")
                .list()
                .forEach(ar -> accountsReceivableService.removeById(ar.getId()));
    }

    /**
     * 出库后更新订单状态
     * 如果所有商品都已发货，订单状态改为"全部发货(3)"，否则改为"部分发货(2)"
     */
    private void updateOrderStatusAfterOut(Long orderId) {
        // 获取订单所有明细
        List<WmsSalesOrderItem> orderItems = salesOrderItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOrderItem>()
                        .eq(WmsSalesOrderItem::getOrderId, orderId)
        );

        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        // 检查是否所有商品都已全部发货
        boolean allDelivered = true;
        for (WmsSalesOrderItem orderItem : orderItems) {
            BigDecimal orderQty = orderItem.getQuantity() != null ? orderItem.getQuantity() : BigDecimal.ZERO;
            BigDecimal deliveredQty = orderItem.getDeliveredQty() != null ? orderItem.getDeliveredQty() : BigDecimal.ZERO;
            if (deliveredQty.compareTo(orderQty) < 0) {
                allDelivered = false;
                break;
            }
        }

        // 更新订单状态
        WmsSalesOrder order = salesOrderMapper.selectById(orderId);
        if (order != null) {
            if (allDelivered) {
                order.setOrderStatus("3"); // 全部发货
            } else {
                order.setOrderStatus("2"); // 部分发货
            }
            salesOrderMapper.updateById(order);
        }
    }

    /**
     * 生成出库单号（带重试机制）
     * 使用原生SQL查询包括已删除记录，避免与已删除记录的单号冲突
     */
    private String generateOutNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "CK" + dateStr;
        Long tenantId = SecurityUtils.getTenantIdSafe();
        log.warn("=== generateOutNo DEBUG: prefix={}, tenantId={}", prefix, tenantId);

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxOutNo = baseMapper.selectMaxOutNoIncludingDeleted(prefix, tenantId);
        log.warn("=== generateOutNo DEBUG: maxOutNo={}", maxOutNo);

        int serial = 1;
        if (maxOutNo != null) {
            try {
                String serialStr = maxOutNo.substring(prefix.length());
                serial = Integer.parseInt(serialStr) + 1;
                log.warn("=== generateOutNo DEBUG: parsed serial={}", serial);
            } catch (Exception e) {
                log.warn("=== generateOutNo DEBUG: parse failed, using={}", serial);
            }
        }

        String outNo = prefix + String.format("%04d", serial);
        log.warn("=== generateOutNo DEBUG: generated outNo={}", outNo);
        return outNo;
    }

    /**
     * 更新收款状态
     */
    private void updatePaymentStatus(WmsSalesOut salesOut) {
        BigDecimal totalAmount = salesOut.getTotalAmount() != null ? salesOut.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal receivedAmount = salesOut.getReceivedAmount() != null ? salesOut.getReceivedAmount() : BigDecimal.ZERO;

        if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
            salesOut.setPaymentStatus("0"); // 未收
        } else if (receivedAmount.compareTo(totalAmount) >= 0) {
            salesOut.setPaymentStatus("2"); // 已收
        } else {
            salesOut.setPaymentStatus("1"); // 部分
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
    public List<WmsSalesOutRespVO> listApproved() {
        // 查询已审核且未全部退货的出库单（用于退货单选择）
        // returnStatus: 0-未退货, 1-部分退货, 2-全部退货, NULL-未退货
        LambdaQueryWrapper<WmsSalesOut> wrapper = new LambdaQueryWrapper<WmsSalesOut>()
                .isNotNull(WmsSalesOut::getApproveBy)
                .and(w -> w.isNull(WmsSalesOut::getReturnStatus)
                        .or()
                        .ne(WmsSalesOut::getReturnStatus, "2"))
                .orderByDesc(WmsSalesOut::getCreateTime);
        List<WmsSalesOut> list = baseMapper.selectList(wrapper);

        return list.stream().map(item -> {
            WmsSalesOutRespVO vo = new WmsSalesOutRespVO();
            BeanUtils.copyProperties(item, vo);

            // 计算应收金额
            BigDecimal receivableAmount = item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO;
            vo.setReceivableAmount(receivableAmount);

            // 计算收款状态
            BigDecimal receivedAmount = item.getReceivedAmount() != null ? item.getReceivedAmount() : BigDecimal.ZERO;
            if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
                vo.setPaymentStatus("0"); // 未收
            } else if (receivedAmount.compareTo(receivableAmount) < 0) {
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

            // 填充仓库名称
            if (item.getWarehouseId() != null) {
                var warehouse = warehouseMapper.selectById(item.getWarehouseId());
                if (warehouse != null) {
                    vo.setWarehouseName(warehouse.getWarehouseName());
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WmsSalesOutItemRespVO> getItems(Long outId) {
        LambdaQueryWrapper<WmsSalesOutItem> wrapper = new LambdaQueryWrapper<WmsSalesOutItem>()
                .eq(WmsSalesOutItem::getOutId, outId);
        List<WmsSalesOutItem> items = salesOutItemMapper.selectList(wrapper);

        List<WmsSalesOutItemRespVO> result = items.stream().map(item -> {
            WmsSalesOutItemRespVO itemVO = new WmsSalesOutItemRespVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());
        fillGoodsName(result);
        fillBarcode(result);
        return result;
    }

    private void fillGoodsName(List<WmsSalesOutItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> goodsIds = items.stream()
                .map(WmsSalesOutItemRespVO::getGoodsId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (goodsIds.isEmpty()) {
            return;
        }
        Map<Long, String> goodsNameMap = goodsService.listByIds(goodsIds).stream()
                .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName, (a, b) -> a));
        items.forEach(item -> item.setGoodsName(goodsNameMap.get(item.getGoodsId())));
    }

    private void fillBarcode(List<WmsSalesOutItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> skuIds = items.stream()
                .map(WmsSalesOutItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, String> barcodeMap = goodsSkuService.listByIds(skuIds).stream()
                .collect(Collectors.toMap(WmsGoodsSku::getId, sku -> StringUtils.trimToEmpty(sku.getBarcode()), (a, b) -> a));
        items.forEach(item -> item.setBarcode(barcodeMap.getOrDefault(item.getSkuId(), "")));
    }

    private void tryCompleteSalesOutOnboarding(LoginUser user) {
        try {
            if (user == null || user.getUserId() == null || StringUtils.isEmpty(user.getUsername())) {
                return;
            }

            Long approvedCount = lambdaQuery()
                    .eq(WmsSalesOut::getCreateBy, user.getUsername())
                    .isNotNull(WmsSalesOut::getApproveBy)
                    .count();
            if (approvedCount != null && approvedCount == 1L) {
                onboardingService.completeTask(user.getUserId(), "sales_out");
            }
        } catch (Exception ex) {
            // 新手引导失败不应阻塞销售出库主流程
            log.warn("complete onboarding sales_out failed", ex);
        }
    }

    private LoginUser resolveLoginUserOrNull() {
        try {
            return SecurityUtils.getLoginUser();
        } catch (Exception ex) {
            return null;
        }
    }

    private void tryCompleteSalesOutOnboardingOnCreate(LoginUser user) {
        try {
            if (user == null || user.getUserId() == null) {
                return;
            }
            onboardingService.completeTask(user.getUserId(), "sales_out");
        } catch (Exception ex) {
            // 新手引导失败不应阻塞销售出库主流程
            log.warn("complete onboarding sales_out on create failed", ex);
        }
    }

    private void validateItemsPositive(List<WmsSalesOutItemSaveReqVO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        for (WmsSalesOutItemSaveReqVO item : items) {
            if (item == null) {
                throw new ServiceException("出库明细不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("出库明细数量必须大于0");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ServiceException("出库明细单价必须大于0");
            }
        }
    }

    /**
     * 自动审核且已审核单据，锁定关键字段（仓库/日期/数量/单价），允许客户改动。
     */
    private void validateCriticalFieldsForAutoApprovedBill(WmsSalesOut existing,
                                                           WmsSalesOutSaveReqVO reqVO,
                                                           boolean lockCriticalFields) {
        if (!lockCriticalFields) {
            return;
        }

        if (!Objects.equals(existing.getWarehouseId(), reqVO.getWarehouseId())) {
            throw new ServiceException(buildBizError(ERR_FIELD_LOCKED, "关键字段不可修改：仓库"));
        }

        if (!Objects.equals(existing.getOutDate(), reqVO.getOutDate())) {
            throw new ServiceException(buildBizError(ERR_FIELD_LOCKED, "关键字段不可修改：出库日期"));
        }

        List<WmsSalesOutItem> existedItems = salesOutItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOutItem>()
                        .eq(WmsSalesOutItem::getOutId, existing.getId())
        );

        List<WmsSalesOutItemSaveReqVO> newItems = reqVO.getItems() == null
                ? Collections.emptyList()
                : reqVO.getItems();

        if (existedItems.size() != newItems.size()) {
            throw new ServiceException(buildBizError(ERR_FIELD_LOCKED, "关键字段不可修改：明细行数"));
        }

        Map<Long, WmsSalesOutItem> existedMap = existedItems.stream()
                .collect(Collectors.toMap(WmsSalesOutItem::getSkuId, item -> item, (a, b) -> a));

        for (WmsSalesOutItemSaveReqVO item : newItems) {
            WmsSalesOutItem old = existedMap.get(item.getSkuId());
            if (old == null) {
                throw new ServiceException(buildBizError(ERR_FIELD_LOCKED, "关键字段不可修改：明细商品"));
            }

            BigDecimal oldQty = old.getQuantity() == null ? BigDecimal.ZERO : old.getQuantity();
            BigDecimal newQty = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            if (oldQty.compareTo(newQty) != 0) {
                throw new ServiceException(buildBizError(ERR_FIELD_LOCKED, "关键字段不可修改：数量"));
            }

            BigDecimal oldPrice = old.getPrice() == null ? BigDecimal.ZERO : old.getPrice();
            BigDecimal newPrice = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            if (oldPrice.compareTo(newPrice) != 0) {
                throw new ServiceException(buildBizError(ERR_FIELD_LOCKED, "关键字段不可修改：单价"));
            }
        }
    }

    private boolean shouldLockCriticalFields(WmsSalesOut existing) {
        if (existing == null || StringUtils.isEmpty(existing.getApproveBy())) {
            return false;
        }
        return !billApproveFlowService.needApprove(WmsApproveConstants.BILL_TYPE_SALES_OUT);
    }

    private void validateCustomerAvailable(Long customerId) {
        if (customerId == null) {
            throw new ServiceException(buildBizError(ERR_CUSTOMER_INVALID, "客户不能为空"));
        }
        // 使用条件查询由租户/逻辑删除拦截器统一兜底，避免越权选择他租户客户。
        WmsCustomer customer = customerMapper.selectOne(
                new LambdaQueryWrapper<WmsCustomer>()
                        .eq(WmsCustomer::getId, customerId)
        );
        if (customer == null) {
            throw new ServiceException(buildBizError(ERR_CUSTOMER_INVALID, "客户不存在或不属于当前租户"));
        }
        if (!"0".equals(customer.getStatus())) {
            throw new ServiceException(buildBizError(ERR_CUSTOMER_INVALID, "客户已停用，不能用于销售出库"));
        }
    }

    private String buildBizError(String code, String message) {
        return code + ":" + message;
    }

    private void syncAccountsReceivableCustomer(Long salesOutId, Long customerId) {
        if (salesOutId == null || customerId == null) {
            return;
        }
        accountsReceivableService.lambdaUpdate()
                .eq(WmsAccountsReceivable::getBillType, "2")
                .eq(WmsAccountsReceivable::getBillId, salesOutId)
                .set(WmsAccountsReceivable::getCustomerId, customerId)
                .update();
    }

    private void syncSkuPriceFromApprovedItems(List<WmsSalesOutItem> items, String customerType) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Map<Long, BigDecimal> qtyBySku = new java.util.HashMap<>();
        Map<Long, BigDecimal> amountBySku = new java.util.HashMap<>();

        for (WmsSalesOutItem item : items) {
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

        boolean isWholesale = "1".equals(customerType);

        for (WmsGoodsSku sku : skuList) {
            if (sku == null || sku.getId() == null) {
                continue;
            }
            BigDecimal qty = qtyBySku.get(sku.getId());
            BigDecimal amount = amountBySku.get(sku.getId());
            if (qty == null || amount == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal newPrice = amount.divide(qty, 2, BigDecimal.ROUND_HALF_UP);
            if (isWholesale) {
                BigDecimal oldPrice = sku.getWholesalePrice();
                if (oldPrice == null || oldPrice.compareTo(newPrice) != 0) {
                    sku.setWholesalePrice(newPrice);
                    goodsSkuService.updateById(sku);
                }
            } else {
                BigDecimal oldPrice = sku.getSalePrice();
                if (oldPrice == null || oldPrice.compareTo(newPrice) != 0) {
                    sku.setSalePrice(newPrice);
                    goodsSkuService.updateById(sku);
                }
            }
        }
    }

    private List<WmsBillPrintItemRespVO> buildPrintItems(List<WmsSalesOutItemRespVO> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        // 批量查询条码，构建 skuId -> barcode 映射
        Set<Long> skuIds = items.stream()
                .map(WmsSalesOutItemRespVO::getSkuId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> skuBarcodeMap = skuIds.isEmpty()
                ? Collections.emptyMap()
                : goodsSkuService.listByIds(skuIds).stream()
                        .filter(sku -> StringUtils.isNotBlank(sku.getBarcode()))
                        .collect(Collectors.toMap(WmsGoodsSku::getId, WmsGoodsSku::getBarcode, (a, b) -> a));
        Set<Long> goodsIds = items.stream()
            .map(WmsSalesOutItemRespVO::getGoodsId)
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
    public List<WmsSalesOutRespVO> searchAvailableForReturn(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return listApproved();
        }
        List<Map<String, Object>> rows = baseMapper.searchAvailableForReturn(keyword.trim());
        return rows.stream().map(row -> {
            WmsSalesOutRespVO vo = new WmsSalesOutRespVO();
            vo.setId(toLong(row.get("id")));
            vo.setOutNo(toStr(row.get("out_no")));
            vo.setCustomerId(toLong(row.get("customer_id")));
            vo.setWarehouseId(toLong(row.get("warehouse_id")));
            vo.setCustomerName(toStr(row.get("customer_name")));
            vo.setWarehouseName(toStr(row.get("warehouse_name")));
            vo.setApproveBy(toStr(row.get("approve_by")));
            vo.setReturnStatus(toStr(row.get("return_status")));
            vo.setRemark(toStr(row.get("remark")));
            return vo;
        }).collect(Collectors.toList());
    }

    private Long toLong(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        try {
            return Long.valueOf(String.valueOf(val));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String toStr(Object val) {
        return val == null ? null : String.valueOf(val);
    }

    @Override
    public List<WmsSalesOutExportVO> exportList(WmsSalesOutPageReqVO reqVO) {
        // 1. 按查询条件查询所有出库单（不分页）
        LambdaQueryWrapper<WmsSalesOut> wrapper = new LambdaQueryWrapper<WmsSalesOut>()
                .like(StringUtils.isNotEmpty(reqVO.getOutNo()), WmsSalesOut::getOutNo, reqVO.getOutNo())
                .eq(reqVO.getCustomerId() != null, WmsSalesOut::getCustomerId, reqVO.getCustomerId())
                .eq(reqVO.getWarehouseId() != null, WmsSalesOut::getWarehouseId, reqVO.getWarehouseId())
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "0".equals(reqVO.getApproveStatus()),
                    "APPROVE_BY IS NULL OR TRIM(APPROVE_BY) = ''")
                .isNotNull(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    WmsSalesOut::getApproveBy)
                .apply(StringUtils.isNotEmpty(reqVO.getApproveStatus()) && "1".equals(reqVO.getApproveStatus()),
                    "TRIM(APPROVE_BY) != ''")
                .eq(StringUtils.isNotEmpty(reqVO.getPaymentStatus()), WmsSalesOut::getPaymentStatus, reqVO.getPaymentStatus())
                .orderByDesc(WmsSalesOut::getCreateTime)
                .orderByDesc(WmsSalesOut::getId);

        List<WmsSalesOut> list = baseMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. 批量查询客户名称
        Set<Long> customerIds = list.stream().map(WmsSalesOut::getCustomerId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> customerNameMap = customerIds.isEmpty() ? java.util.Collections.emptyMap() :
                customerMapper.selectBatchIds(customerIds).stream()
                        .collect(Collectors.toMap(WmsCustomer::getId, WmsCustomer::getCustomerName));

        // 3. 批量查询仓库名称
        Set<Long> warehouseIds = list.stream().map(WmsSalesOut::getWarehouseId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> warehouseNameMap = warehouseIds.isEmpty() ? java.util.Collections.emptyMap() :
                warehouseMapper.selectBatchIds(warehouseIds).stream()
                        .collect(Collectors.toMap(WmsWarehouse::getId, WmsWarehouse::getWarehouseName));

        // 4. 批量查询所有明细
        Set<Long> outIds = list.stream().map(WmsSalesOut::getId).collect(Collectors.toSet());
        List<WmsSalesOutItem> allItems = salesOutItemMapper.selectList(
                new LambdaQueryWrapper<WmsSalesOutItem>().in(WmsSalesOutItem::getOutId, outIds));
        Map<Long, List<WmsSalesOutItem>> itemMap = allItems.stream()
                .collect(Collectors.groupingBy(WmsSalesOutItem::getOutId));

        // 5. 批量查询商品名称
        Set<Long> goodsIds = allItems.stream().map(WmsSalesOutItem::getGoodsId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> goodsNameMap = goodsIds.isEmpty() ? java.util.Collections.emptyMap() :
                goodsService.listByIds(goodsIds).stream()
                        .collect(Collectors.toMap(WmsGoods::getId, WmsGoods::getGoodsName));

        // 5b. 批量查询销售订单号
        Set<Long> orderIds = list.stream().map(WmsSalesOut::getOrderId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> orderNoMap = orderIds.isEmpty() ? java.util.Collections.emptyMap() :
                salesOrderService.listByIds(orderIds).stream()
                        .collect(Collectors.toMap(WmsSalesOrder::getId, WmsSalesOrder::getOrderNo));

        // 6. 组装导出数据
        List<WmsSalesOutExportVO> exportList = new java.util.ArrayList<>();
        for (WmsSalesOut out : list) {
            String approveStatus = StringUtils.isNotBlank(out.getApproveBy()) ? "1" : "0";
            String customerName = customerNameMap.get(out.getCustomerId());
            String warehouseName = warehouseNameMap.get(out.getWarehouseId());

            // 计算收款状态
            BigDecimal receivableAmount = out.getTotalAmount() != null ? out.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal receivedAmount = out.getReceivedAmount() != null ? out.getReceivedAmount() : BigDecimal.ZERO;
            String paymentStatus;
            if (receivedAmount.compareTo(BigDecimal.ZERO) == 0) {
                paymentStatus = "0";
            } else if (receivedAmount.compareTo(receivableAmount) < 0) {
                paymentStatus = "1";
            } else {
                paymentStatus = "2";
            }

            List<WmsSalesOutItem> items = itemMap.getOrDefault(out.getId(), java.util.Collections.emptyList());
            String orderNo = orderNoMap.get(out.getOrderId());
            if (items.isEmpty()) {
                WmsSalesOutExportVO vo = buildSalesOutExportRow(out, null, customerName, warehouseName,
                        paymentStatus, approveStatus, goodsNameMap, orderNo);
                exportList.add(vo);
            } else {
                for (WmsSalesOutItem item : items) {
                    WmsSalesOutExportVO vo = buildSalesOutExportRow(out, item, customerName, warehouseName,
                            paymentStatus, approveStatus, goodsNameMap, orderNo);
                    exportList.add(vo);
                }
            }
        }
        return exportList;
    }

    private WmsSalesOutExportVO buildSalesOutExportRow(WmsSalesOut out, WmsSalesOutItem item,
            String customerName, String warehouseName, String paymentStatus, String approveStatus,
            Map<Long, String> goodsNameMap, String orderNo) {
        WmsSalesOutExportVO vo = new WmsSalesOutExportVO();
        vo.setOutNo(out.getOutNo());
        vo.setOutDate(out.getOutDate());
        vo.setCustomerName(customerName);
        vo.setWarehouseName(warehouseName);
        vo.setOrderNo(orderNo);
        vo.setSourceType(out.getSourceType());
        vo.setTotalAmount(out.getTotalAmount());
        vo.setPaymentStatus(paymentStatus);
        vo.setApproveStatus(approveStatus);
        vo.setRemark(out.getRemark());
        vo.setCreateTime(out.getCreateTime());

        if (item != null) {
            vo.setSkuCode(item.getSkuCode());
            vo.setSkuName(item.getSkuName());
            vo.setGoodsName(goodsNameMap.get(item.getGoodsId()));
            vo.setQuantity(item.getQuantity());
            vo.setPrice(item.getPrice());
            vo.setAmount(item.getAmount());
        }
        return vo;
    }
}
