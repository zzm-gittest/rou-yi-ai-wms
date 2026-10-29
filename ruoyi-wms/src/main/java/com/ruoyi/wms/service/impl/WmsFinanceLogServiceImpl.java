package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsFinanceLog;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.vo.financeLog.WmsFinanceLogPageReqVO;
import com.ruoyi.wms.domain.vo.financeLog.WmsFinanceLogRespVO;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsAccountMapper;
import com.ruoyi.wms.mapper.WmsFinanceLogMapper;
import com.ruoyi.wms.service.WmsFinanceLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 财务日志表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsFinanceLogServiceImpl extends ServiceImpl<WmsFinanceLogMapper, WmsFinanceLog> implements WmsFinanceLogService {

    @Autowired
    private WmsSupplierMapper supplierMapper;

    @Autowired
    private WmsCustomerMapper customerMapper;

    @Autowired
    private WmsAccountMapper accountMapper;

    @Override
    public IPage<WmsFinanceLogRespVO> page(WmsFinanceLogPageReqVO reqVO) {
        LambdaQueryWrapper<WmsFinanceLog> wrapper = new LambdaQueryWrapper<WmsFinanceLog>()
                .like(StringUtils.isNotEmpty(reqVO.getVoucherNo()), WmsFinanceLog::getVoucherNo, reqVO.getVoucherNo())
                .like(StringUtils.isNotEmpty(reqVO.getBillNo()), WmsFinanceLog::getBillNo, reqVO.getBillNo())
                .eq(StringUtils.isNotEmpty(reqVO.getBillType()), WmsFinanceLog::getBillType, reqVO.getBillType())
                .eq(StringUtils.isNotEmpty(reqVO.getDirection()), WmsFinanceLog::getDirection, reqVO.getDirection())
                .eq(reqVO.getAccountId() != null, WmsFinanceLog::getAccountId, reqVO.getAccountId())
                .ge(reqVO.getStartDate() != null, WmsFinanceLog::getBillDate, reqVO.getStartDate())
                .le(reqVO.getEndDate() != null, WmsFinanceLog::getBillDate, reqVO.getEndDate())
                .orderByDesc(WmsFinanceLog::getId);

        Page<WmsFinanceLog> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsFinanceLog> result = baseMapper.selectPage(page, wrapper);

        // 转换为RespVO
        return result.convert(this::convertToRespVO);
    }

    @Override
    public WmsFinanceLogRespVO get(Long id) {
        WmsFinanceLog financeLog = baseMapper.selectById(id);
        if (financeLog == null) {
            throw new ServiceException("日志不存在");
        }
        return convertToRespVO(financeLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLog(String billType, String billNo, Long billId, String businessType,
                          Long partnerId, String partnerType, Long accountId,
                          BigDecimal amount, String direction, LocalDate billDate, String remark) {
        WmsFinanceLog log = new WmsFinanceLog();
        log.setBillType(billType);
        log.setBillNo(billNo);
        log.setBillId(billId);
        log.setBusinessType(businessType);
        log.setPartnerId(partnerId);
        log.setPartnerType(partnerType);
        log.setAccountId(accountId);
        log.setAmount(amount);
        log.setDirection(direction);
        log.setBillDate(billDate);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());

        // 如果指定了账户，设置支付方式
        if (accountId != null) {
            WmsAccount account = accountMapper.selectById(accountId);
            if (account != null) {
                log.setPaymentMethod(account.getAccountType());
            }
        }

        save(log);
        return log.getId();
    }

    /**
     * 转换为RespVO
     */
    private WmsFinanceLogRespVO convertToRespVO(WmsFinanceLog log) {
        WmsFinanceLogRespVO vo = new WmsFinanceLogRespVO();
        BeanUtils.copyProperties(log, vo);

        // 设置单据类型名称
        String billType = log.getBillType();
        if ("1".equals(billType)) {
            vo.setBillTypeName("收款");
        } else if ("2".equals(billType)) {
            vo.setBillTypeName("付款");
        } else if ("3".equals(billType)) {
            vo.setBillTypeName("费用");
        }

        // 设置业务类型名称
        String businessType = log.getBusinessType();
        if ("1".equals(businessType)) {
            vo.setBusinessTypeName("采购付款");
        } else if ("2".equals(businessType)) {
            vo.setBusinessTypeName("销售收款");
        } else if ("3".equals(businessType)) {
            vo.setBusinessTypeName("采购退货收款");
        } else if ("4".equals(businessType)) {
            vo.setBusinessTypeName("销售退货付款");
        } else if ("5".equals(businessType)) {
            vo.setBusinessTypeName("应收账款收款");
        } else if ("6".equals(businessType)) {
            vo.setBusinessTypeName("应付账款付款");
        } else if ("7".equals(businessType)) {
            vo.setBusinessTypeName("采购退货退款");
        } else if ("8".equals(businessType)) {
            vo.setBusinessTypeName("销售退货退款");
        } else {
            vo.setBusinessTypeName("未知");
        }

        // 设置原始单据类型名称
        String originalBillType = log.getOriginalBillType();
        if (originalBillType != null) {
            switch (originalBillType) {
                case "销售订单":
                    vo.setOriginalBillTypeName("销售订单");
                    break;
                case "销售出库":
                    vo.setOriginalBillTypeName("销售出库");
                    break;
                case "采购订单":
                    vo.setOriginalBillTypeName("采购订单");
                    break;
                case "采购入库":
                    vo.setOriginalBillTypeName("采购入库");
                    break;
                case "采购退货":
                    vo.setOriginalBillTypeName("采购退货");
                    break;
                case "销售退货":
                    vo.setOriginalBillTypeName("销售退货");
                    break;
                default:
                    vo.setOriginalBillTypeName(originalBillType);
            }
        }

        // 填充往来单位名称
        if (log.getPartnerId() != null) {
            if ("1".equals(log.getPartnerType())) {
                WmsSupplier supplier = supplierMapper.selectById(log.getPartnerId());
                if (supplier != null) {
                    vo.setPartnerName(supplier.getSupplierName());
                }
            } else if ("2".equals(log.getPartnerType())) {
                WmsCustomer customer = customerMapper.selectById(log.getPartnerId());
                if (customer != null) {
                    vo.setPartnerName(customer.getCustomerName());
                }
            }
        }

        // 填充账户名称
        if (log.getAccountId() != null) {
            WmsAccount account = accountMapper.selectById(log.getAccountId());
            if (account != null) {
                vo.setAccountName(account.getAccountName());
            }
        }

        // 设置方向名称
        String direction = log.getDirection();
        if ("1".equals(direction)) {
            vo.setDirectionName("收入");
        } else if ("2".equals(direction)) {
            vo.setDirectionName("支出");
        }

        // 设置支付方式名称
        String paymentMethod = log.getPaymentMethod();
        if ("1".equals(paymentMethod)) {
            vo.setPaymentMethodName("现金");
        } else if ("2".equals(paymentMethod)) {
            vo.setPaymentMethodName("银行");
        } else if ("3".equals(paymentMethod)) {
            vo.setPaymentMethodName("支付宝");
        } else if ("4".equals(paymentMethod)) {
            vo.setPaymentMethodName("微信");
        }

        return vo;
    }
}
