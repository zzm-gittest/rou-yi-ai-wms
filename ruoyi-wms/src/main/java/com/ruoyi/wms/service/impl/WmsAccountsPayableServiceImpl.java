package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.WmsAccountsPayable;
import com.ruoyi.wms.domain.WmsFinanceLog;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePayReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayableRespVO;
import com.ruoyi.wms.mapper.WmsAccountsPayableMapper;
import com.ruoyi.wms.service.WmsAccountService;
import com.ruoyi.wms.service.WmsAccountsPayableService;
import com.ruoyi.wms.service.WmsFinanceLogService;
import com.ruoyi.wms.service.WmsPurchaseInService;
import com.ruoyi.wms.service.WmsPurchaseOrderService;
import com.ruoyi.wms.service.WmsSalesReturnService;
import com.ruoyi.wms.domain.WmsSalesReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应付账款表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsAccountsPayableServiceImpl extends ServiceImpl<WmsAccountsPayableMapper, WmsAccountsPayable> implements WmsAccountsPayableService {

    @Autowired
    private WmsAccountService accountService;

    @Autowired
    private WmsFinanceLogService financeLogService;

    @Autowired
    private WmsPurchaseInService purchaseInService;

    @Autowired
    private WmsPurchaseOrderService purchaseOrderService;

    @Autowired
    private WmsSalesReturnService salesReturnService;

    @Override
    public IPage<WmsAccountsPayableRespVO> page(WmsAccountsPayablePageReqVO reqVO) {
        Page<WmsAccountsPayableRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsAccountsPayableRespVO> resultPage = baseMapper.selectApPage(page, reqVO);
        return resultPage;
    }

    @Override
    public WmsAccountsPayableRespVO get(Long id) {
        return baseMapper.selectApById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(WmsAccountsPayablePayReqVO reqVO) {
        WmsAccountsPayable ap = getById(reqVO.getId());
        if (ap == null) {
            throw new ServiceException("应付账款记录不存在");
        }
        if ("1".equals(ap.getApStatus())) {
            throw new ServiceException("该应付账款已结清");
        }
        if (reqVO.getPayAmount().compareTo(ap.getApAmount().subtract(ap.getPaidAmount() != null ? ap.getPaidAmount() : BigDecimal.ZERO)) > 0) {
            throw new ServiceException("付款金额不能超过未付金额");
        }

        // 判断是否为销售退货退款（我们退款给客户）
        boolean isSalesReturnRefund = "3".equals(ap.getBillType());

        // 更新应付账款
        BigDecimal newPaidAmount = ap.getPaidAmount() != null ? ap.getPaidAmount() : BigDecimal.ZERO;
        newPaidAmount = newPaidAmount.add(reqVO.getPayAmount());
        ap.setPaidAmount(newPaidAmount);
        if (newPaidAmount.compareTo(ap.getApAmount()) >= 0) {
            ap.setApStatus("1"); // 已结清
        }
        // 保存付款凭证
        if (reqVO.getPaymentProof() != null && !reqVO.getPaymentProof().isEmpty()) {
            ap.setPaymentProof(reqVO.getPaymentProof());
        }
        updateById(ap);

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

        // 销售退货退款（我们退款给客户）是减少账户余额，其他付款是减少账户余额
        // 实际上无论是付款还是退款给客户，都是从账户扣款
        if (account.getBalance().compareTo(reqVO.getPayAmount()) < 0) {
            throw new ServiceException("账户余额不足");
        }
        account.setBalance(account.getBalance().subtract(reqVO.getPayAmount()));
        accountService.updateById(account);

        // 记录交易后余额
        BigDecimal afterBalance = account.getBalance();

        // 确定原始单据类型
        String originalBillType;
        switch (ap.getBillType()) {
            case "1":
                originalBillType = "采购订单";
                break;
            case "2":
                originalBillType = "采购入库";
                break;
            case "3":
                originalBillType = "销售退货";
                break;
            default:
                originalBillType = "未知";
        }

        // 记录财务日志
        WmsFinanceLog financeLog = new WmsFinanceLog();
        financeLog.setVoucherNo(generateVoucherNo());
        financeLog.setBillType("2"); // 付款
        financeLog.setDirection("2"); // 支出
        financeLog.setBillNo(ap.getBillNo());
        financeLog.setBillId(ap.getId());
        financeLog.setOriginalBillType(originalBillType);
        // 业务类型：6=应付付款，8=销售退货退款
        financeLog.setBusinessType(isSalesReturnRefund ? "8" : "6");
        // 往来单位：销售退货是客户，其他是供应商
        if (isSalesReturnRefund) {
            financeLog.setPartnerId(ap.getCustomerId());
            financeLog.setPartnerType("2"); // 客户
        } else {
            financeLog.setPartnerId(ap.getSupplierId());
            financeLog.setPartnerType("1"); // 供应商
        }
        financeLog.setAccountId(reqVO.getAccountId());
        financeLog.setBeforeBalance(beforeBalance);
        financeLog.setAfterBalance(afterBalance);
        financeLog.setPaymentMethod(account.getAccountType());
        financeLog.setAmount(reqVO.getPayAmount());
        financeLog.setBillDate(ap.getBillDate());
        // 生成摘要
        String partnerName = isSalesReturnRefund ? "客户" : "供应商";
        financeLog.setSummary(String.format("支付%s%s-%s款项", partnerName, ap.getBillNo(), originalBillType));
        financeLog.setRemark(reqVO.getRemark() != null ? reqVO.getRemark() : (isSalesReturnRefund ? "销售退货退款" : "应付账款付款"));
        financeLogService.save(financeLog);

        // 同步更新原单据的已付金额/退款金额
        if ("1".equals(ap.getBillType())) {
            // 采购订单
            WmsPurchaseOrder order = purchaseOrderService.getById(ap.getBillId());
            if (order != null) {
                BigDecimal currentPaid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
                order.setPaidAmount(currentPaid.add(reqVO.getPayAmount()));
                purchaseOrderService.updateById(order);
            }
        } else if ("2".equals(ap.getBillType())) {
            // 采购入库 - 需要同时更新采购入库单和关联的采购订单
            WmsPurchaseIn purchaseIn = purchaseInService.getById(ap.getBillId());
            if (purchaseIn != null) {
                // 更新采购入库单的已付金额
                BigDecimal currentPaid = purchaseIn.getPaidAmount() != null ? purchaseIn.getPaidAmount() : BigDecimal.ZERO;
                purchaseIn.setPaidAmount(currentPaid.add(reqVO.getPayAmount()));
                purchaseInService.updateById(purchaseIn);

                // 如果入库单关联了采购订单，同时更新采购订单的已付金额
                if (purchaseIn.getOrderId() != null) {
                    WmsPurchaseOrder order = purchaseOrderService.getById(purchaseIn.getOrderId());
                    if (order != null) {
                        BigDecimal orderPaid = order.getPaidAmount() != null ? order.getPaidAmount() : BigDecimal.ZERO;
                        order.setPaidAmount(orderPaid.add(reqVO.getPayAmount()));
                        purchaseOrderService.updateById(order);
                    }
                }
            }
        } else if ("3".equals(ap.getBillType())) {
            // 销售退货 - 更新销售退货单的退款金额
            WmsSalesReturn salesReturn = salesReturnService.getById(ap.getBillId());
            if (salesReturn != null) {
                BigDecimal currentRefund = salesReturn.getRefundAmount() != null ? salesReturn.getRefundAmount() : BigDecimal.ZERO;
                salesReturn.setRefundAmount(currentRefund.add(reqVO.getPayAmount()));

                // 更新退款状态
                BigDecimal totalAmount = salesReturn.getTotalAmount() != null ? salesReturn.getTotalAmount() : BigDecimal.ZERO;
                if (salesReturn.getRefundAmount().compareTo(BigDecimal.ZERO) == 0) {
                    salesReturn.setPaymentStatus("0"); // 未退
                } else if (salesReturn.getRefundAmount().compareTo(totalAmount) >= 0) {
                    salesReturn.setPaymentStatus("2"); // 已退
                } else {
                    salesReturn.setPaymentStatus("1"); // 部分
                }
                salesReturnService.updateById(salesReturn);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createByBill(Long supplierId, Long customerId, String billType, String billNo, Long billId,
                              BigDecimal billAmount, BigDecimal paidAmount, LocalDate billDate) {
        // 检查是否已存在
        WmsAccountsPayable existing = lambdaQuery()
                .eq(WmsAccountsPayable::getBillId, billId)
                .eq(WmsAccountsPayable::getBillType, billType)
                .one();
        if (existing != null) {
            return existing.getId();
        }

        WmsAccountsPayable ap = new WmsAccountsPayable();
        ap.setSupplierId(supplierId);
        ap.setCustomerId(customerId);
        ap.setBillType(billType);
        ap.setBillNo(billNo);
        ap.setBillId(billId);
        ap.setBillAmount(billAmount);
        ap.setPaidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO);
        ap.setApAmount(billAmount);
        // 如果已付金额 >= 单据金额，则已结清
        if (ap.getPaidAmount().compareTo(billAmount) >= 0) {
            ap.setApStatus("1");
        } else {
            ap.setApStatus("0");
        }
        ap.setBillDate(billDate);
        ap.setCreateTime(LocalDateTime.now());
        save(ap);

        return ap.getId();
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
