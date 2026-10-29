package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.WmsAccountsReceivable;
import com.ruoyi.wms.domain.WmsFinanceLog;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.WmsSalesOut;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableCollectReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableRespVO;
import com.ruoyi.wms.mapper.WmsAccountsReceivableMapper;
import com.ruoyi.wms.service.WmsAccountService;
import com.ruoyi.wms.service.WmsAccountsReceivableService;
import com.ruoyi.wms.service.WmsFinanceLogService;
import com.ruoyi.wms.service.WmsSalesOrderService;
import com.ruoyi.wms.service.WmsSalesOutService;
import com.ruoyi.wms.service.WmsSalesReturnService;
import com.ruoyi.wms.service.WmsPurchaseReturnService;
import com.ruoyi.wms.domain.WmsSalesReturn;
import com.ruoyi.wms.domain.WmsPurchaseReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应收账款表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsAccountsReceivableServiceImpl extends ServiceImpl<WmsAccountsReceivableMapper, WmsAccountsReceivable> implements WmsAccountsReceivableService {

    @Autowired
    private WmsAccountService accountService;

    @Autowired
    private WmsFinanceLogService financeLogService;

    @Autowired
    private WmsSalesOrderService salesOrderService;

    @Autowired
    private WmsSalesOutService salesOutService;

    @Autowired
    private WmsSalesReturnService salesReturnService;

    @Autowired
    private WmsPurchaseReturnService purchaseReturnService;

    @Override
    public IPage<WmsAccountsReceivableRespVO> page(WmsAccountsReceivablePageReqVO reqVO) {
        Page<WmsAccountsReceivableRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsAccountsReceivableRespVO> resultPage = baseMapper.selectArPage(page, reqVO);
        return resultPage;
    }

    @Override
    public WmsAccountsReceivableRespVO get(Long id) {
        return baseMapper.selectArById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void collect(WmsAccountsReceivableCollectReqVO reqVO) {
        WmsAccountsReceivable ar = getById(reqVO.getId());
        if (ar == null) {
            throw new ServiceException("应收账款记录不存在");
        }
        if ("1".equals(ar.getArStatus())) {
            throw new ServiceException("该应收账款已结清");
        }
        if (reqVO.getCollectAmount().compareTo(ar.getArAmount().subtract(ar.getReceivedAmount() != null ? ar.getReceivedAmount() : BigDecimal.ZERO)) > 0) {
            throw new ServiceException("收款金额不能超过未收金额");
        }

        // 更新应收账款
        BigDecimal newReceivedAmount = ar.getReceivedAmount() != null ? ar.getReceivedAmount() : BigDecimal.ZERO;
        newReceivedAmount = newReceivedAmount.add(reqVO.getCollectAmount());
        ar.setReceivedAmount(newReceivedAmount);
        if (newReceivedAmount.compareTo(ar.getArAmount()) >= 0) {
            ar.setArStatus("1"); // 已结清
        }
        // 保存收款凭证
        if (reqVO.getCollectionProof() != null && !reqVO.getCollectionProof().isEmpty()) {
            ar.setCollectionProof(reqVO.getCollectionProof());
        }
        updateById(ar);

        // 更新账户余额
        WmsAccount account = accountService.getById(reqVO.getAccountId());
        if (account == null) {
            throw new ServiceException("结算账户不存在");
        }
        if ("1".equals(account.getStatus())) {
            throw new ServiceException("结算账户已停用");
        }

        // 记录交易前余额
        BigDecimal beforeBalance = account.getBalance();

        // 采购退货退款（供应商退钱给我们）增加账户余额，销售收款也增加账户余额
        account.setBalance(account.getBalance().add(reqVO.getCollectAmount()));
        accountService.updateById(account);

        // 记录交易后余额
        BigDecimal afterBalance = account.getBalance();

        // 确定原始单据类型
        String originalBillType;
        switch (ar.getBillType()) {
            case "1":
                originalBillType = "销售订单";
                break;
            case "2":
                originalBillType = "销售出库";
                break;
            case "3":
                originalBillType = "采购退货";
                break;
            default:
                originalBillType = "未知";
        }

        // 记录财务日志
        WmsFinanceLog financeLog = new WmsFinanceLog();
        financeLog.setVoucherNo(generateVoucherNo());
        financeLog.setBillType("1"); // 收款
        financeLog.setDirection("1"); // 收入
        financeLog.setBillNo(ar.getBillNo());
        financeLog.setBillId(ar.getId());
        financeLog.setOriginalBillType(originalBillType);
        // 业务类型：5=应收收款，7=采购退货退款
        financeLog.setBusinessType("3".equals(ar.getBillType()) ? "7" : "5");
        // 往来单位：采购退货是供应商，其他是客户
        if ("3".equals(ar.getBillType())) {
            financeLog.setPartnerId(ar.getSupplierId());
            financeLog.setPartnerType("1"); // 供应商
        } else {
            financeLog.setPartnerId(ar.getCustomerId());
            financeLog.setPartnerType("2"); // 客户
        }
        financeLog.setAccountId(reqVO.getAccountId());
        financeLog.setBeforeBalance(beforeBalance);
        financeLog.setAfterBalance(afterBalance);
        financeLog.setPaymentMethod(account.getAccountType());
        financeLog.setAmount(reqVO.getCollectAmount());
        financeLog.setBillDate(ar.getBillDate());
        // 生成摘要
        String partnerName = "3".equals(ar.getBillType()) ? "供应商" : "客户";
        financeLog.setSummary(String.format("收到%s%s-%s款项", partnerName, ar.getBillNo(), originalBillType));
        financeLog.setRemark(reqVO.getRemark() != null ? reqVO.getRemark() : ("3".equals(ar.getBillType()) ? "采购退货退款收款" : "应收账款收款"));
        financeLogService.save(financeLog);

        // 同步更新原单据的已收金额/退款金额
        if ("1".equals(ar.getBillType())) {
            // 销售订单
            WmsSalesOrder order = salesOrderService.getById(ar.getBillId());
            if (order != null) {
                BigDecimal currentReceived = order.getReceivedAmount() != null ? order.getReceivedAmount() : BigDecimal.ZERO;
                order.setReceivedAmount(currentReceived.add(reqVO.getCollectAmount()));
                salesOrderService.updateById(order);
            }
        } else if ("2".equals(ar.getBillType())) {
            // 销售出库 - 需要同时更新销售出库单和关联的销售订单
            WmsSalesOut salesOut = salesOutService.getById(ar.getBillId());
            if (salesOut != null) {
                // 更新销售出库单的已收金额
                BigDecimal currentReceived = salesOut.getReceivedAmount() != null ? salesOut.getReceivedAmount() : BigDecimal.ZERO;
                salesOut.setReceivedAmount(currentReceived.add(reqVO.getCollectAmount()));
                salesOutService.updateById(salesOut);

                // 如果出库单关联了销售订单，同时更新销售订单的已收金额
                if (salesOut.getOrderId() != null) {
                    WmsSalesOrder order = salesOrderService.getById(salesOut.getOrderId());
                    if (order != null) {
                        BigDecimal orderReceived = order.getReceivedAmount() != null ? order.getReceivedAmount() : BigDecimal.ZERO;
                        order.setReceivedAmount(orderReceived.add(reqVO.getCollectAmount()));
                        salesOrderService.updateById(order);
                    }
                }
            }
        } else if ("3".equals(ar.getBillType())) {
            // 采购退货 - 更新采购退货单的退款金额
            WmsPurchaseReturn purchaseReturn = purchaseReturnService.getById(ar.getBillId());
            if (purchaseReturn != null) {
                BigDecimal currentRefund = purchaseReturn.getRefundAmount() != null ? purchaseReturn.getRefundAmount() : BigDecimal.ZERO;
                purchaseReturn.setRefundAmount(currentRefund.add(reqVO.getCollectAmount()));

                // 更新退款状态
                BigDecimal totalAmount = purchaseReturn.getTotalAmount() != null ? purchaseReturn.getTotalAmount() : BigDecimal.ZERO;
                if (purchaseReturn.getRefundAmount().compareTo(BigDecimal.ZERO) == 0) {
                    purchaseReturn.setPaymentStatus("0"); // 未退
                } else if (purchaseReturn.getRefundAmount().compareTo(totalAmount) >= 0) {
                    purchaseReturn.setPaymentStatus("2"); // 已退
                } else {
                    purchaseReturn.setPaymentStatus("1"); // 部分
                }
                purchaseReturnService.updateById(purchaseReturn);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createByBill(Long customerId, Long supplierId, String billType, String billNo, Long billId,
                              BigDecimal billAmount, BigDecimal receivedAmount, LocalDate billDate) {
        // 检查是否已存在
        WmsAccountsReceivable existing = lambdaQuery()
                .eq(WmsAccountsReceivable::getBillId, billId)
                .eq(WmsAccountsReceivable::getBillType, billType)
                .one();
        if (existing != null) {
            return existing.getId();
        }

        WmsAccountsReceivable ar = new WmsAccountsReceivable();
        ar.setCustomerId(customerId);
        ar.setSupplierId(supplierId);
        ar.setBillType(billType);
        ar.setBillNo(billNo);
        ar.setBillId(billId);
        ar.setBillAmount(billAmount);
        ar.setReceivedAmount(receivedAmount != null ? receivedAmount : BigDecimal.ZERO);
        ar.setArAmount(billAmount);
        // 如果已收金额 >= 单据金额，则已结清
        if (ar.getReceivedAmount().compareTo(billAmount) >= 0) {
            ar.setArStatus("1");
        } else {
            ar.setArStatus("0");
        }
        ar.setBillDate(billDate);
        ar.setCreateTime(LocalDateTime.now());
        save(ar);

        return ar.getId();
    }

    /**
     * 生成凭证号
     */
    private String generateVoucherNo() {
        String dateStr = LocalDate.now().toString().replace("-", "");
        Long count = financeLogService.lambdaQuery()
                .likeRight(WmsFinanceLog::getVoucherNo, "PZ" + dateStr)
                .count();
        int seq = count != null ? count.intValue() + 1 : 1;
        return String.format("PZ%s%04d", dateStr, seq);
    }
}
