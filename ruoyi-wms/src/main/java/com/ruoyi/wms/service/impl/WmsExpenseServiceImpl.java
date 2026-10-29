package com.ruoyi.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsExpense;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.WmsExpenseType;
import com.ruoyi.wms.domain.WmsFinanceLog;
import com.ruoyi.wms.domain.vo.expense.WmsExpensePageReqVO;
import com.ruoyi.wms.domain.vo.expense.WmsExpenseRespVO;
import com.ruoyi.wms.domain.vo.expense.WmsExpenseSaveReqVO;
import com.ruoyi.wms.mapper.WmsAccountMapper;
import com.ruoyi.wms.mapper.WmsExpenseTypeMapper;
import com.ruoyi.wms.mapper.WmsExpenseMapper;
import com.ruoyi.wms.service.WmsAccountService;
import com.ruoyi.wms.service.WmsExpenseService;
import com.ruoyi.wms.service.WmsFinanceLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 费用表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsExpenseServiceImpl extends ServiceImpl<WmsExpenseMapper, WmsExpense> implements WmsExpenseService {

    @Autowired
    private WmsExpenseTypeMapper expenseTypeMapper;

    @Autowired
    private WmsAccountMapper accountMapper;

    @Autowired
    private WmsAccountService accountService;

    @Autowired
    private WmsFinanceLogService financeLogService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public IPage<WmsExpenseRespVO> page(WmsExpensePageReqVO reqVO) {
        LambdaQueryWrapper<WmsExpense> wrapper = new LambdaQueryWrapper<WmsExpense>()
                .like(StringUtils.isNotEmpty(reqVO.getExpenseNo()), WmsExpense::getExpenseNo, reqVO.getExpenseNo())
                .eq(reqVO.getExpenseTypeId() != null, WmsExpense::getExpenseType, reqVO.getExpenseTypeId())
                .ge(reqVO.getStartDate() != null, WmsExpense::getExpenseDate, reqVO.getStartDate())
                .le(reqVO.getEndDate() != null, WmsExpense::getExpenseDate, reqVO.getEndDate())
                .orderByDesc(WmsExpense::getExpenseDate)
                .orderByDesc(WmsExpense::getCreateTime);

        Page<WmsExpense> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsExpense> result = baseMapper.selectPage(page, wrapper);

        return result.convert(this::convertToRespVO);
    }

    @Override
    public WmsExpenseRespVO get(Long id) {
        WmsExpense expense = baseMapper.selectById(id);
        if (expense == null) {
            throw new ServiceException("费用单不存在");
        }
        return convertToRespVO(expense);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsExpenseSaveReqVO reqVO) {
        WmsExpense expense = new WmsExpense();
        BeanUtils.copyProperties(reqVO, expense);
        // 手动设置字段名不一致的属性
        expense.setExpenseType(reqVO.getExpenseTypeId());
        expense.setExpenseNo(generateExpenseNo());
        expense.setApproveStatus("0");

        baseMapper.insert(expense);
        return expense.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsExpenseSaveReqVO reqVO) {
        WmsExpense existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("费用单不存在");
        }
        if ("1".equals(existing.getApproveStatus())) {
            throw new ServiceException("已审核的费用单不能修改");
        }

        BeanUtils.copyProperties(reqVO, existing);
        // 手动设置字段名不一致的属性
        existing.setExpenseType(reqVO.getExpenseTypeId());
        baseMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        WmsExpense existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new ServiceException("费用单不存在");
        }
        if ("1".equals(existing.getApproveStatus())) {
            throw new ServiceException("已审核的费用单不能删除");
        }

        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        WmsExpense expense = baseMapper.selectById(id);
        if (expense == null) {
            throw new ServiceException("费用单不存在");
        }
        if ("1".equals(expense.getApproveStatus())) {
            throw new ServiceException("费用单已审核");
        }

        // 获取费用类型名称
        String expenseTypeName = "";
        if (expense.getExpenseType() != null) {
            WmsExpenseType expenseType = expenseTypeMapper.selectById(expense.getExpenseType());
            if (expenseType != null) {
                expenseTypeName = expenseType.getTypeName();
            }
        }

        // 获取账户并扣减余额
        WmsAccount account = accountService.getById(expense.getAccountId());
        if (account == null) {
            throw new ServiceException("结算账户不存在");
        }
        if ("1".equals(account.getStatus())) {
            throw new ServiceException("结算账户已停用");
        }
        if (account.getBalance().compareTo(expense.getAmount()) < 0) {
            throw new ServiceException("账户余额不足");
        }

        // 记录交易前余额
        BigDecimal beforeBalance = account.getBalance();
        // 扣减账户余额
        account.setBalance(account.getBalance().subtract(expense.getAmount()));
        accountService.updateById(account);
        // 记录交易后余额
        BigDecimal afterBalance = account.getBalance();

        // 更新审核状态
        expense.setApproveStatus("1");
        expense.setApproveBy(SecurityUtils.getUsername());
        expense.setApproveTime(LocalDateTime.now());
        baseMapper.updateById(expense);

        // 生成财务日志
        WmsFinanceLog financeLog = new WmsFinanceLog();
        financeLog.setVoucherNo(generateVoucherNo());
        financeLog.setBillType("3"); // 费用
        financeLog.setDirection("2"); // 支出
        financeLog.setBillNo(expense.getExpenseNo());
        financeLog.setBillId(expense.getId());
        financeLog.setOriginalBillType(expenseTypeName);
        financeLog.setBusinessType("4"); // 费用
        financeLog.setAccountId(expense.getAccountId());
        financeLog.setBeforeBalance(beforeBalance);
        financeLog.setAfterBalance(afterBalance);
        financeLog.setPaymentMethod(account.getAccountType());
        financeLog.setAmount(expense.getAmount());
        financeLog.setBillDate(expense.getExpenseDate());
        financeLog.setSummary("支付" + expenseTypeName + "-" + expense.getExpenseNo());
        financeLog.setRemark(expense.getRemark());
        financeLogService.save(financeLog);
    }

    /**
     * 生成费用单号
     */
    private String generateExpenseNo() {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String prefix = "FY" + dateStr;
        Long tenantId = SecurityUtils.getTenantId();

        // 使用原生SQL查询最大单号（包括已删除记录）
        String maxNo = baseMapper.selectMaxExpenseNoIncludingDeleted(prefix, tenantId);

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
     * 转换为RespVO
     */
    private WmsExpenseRespVO convertToRespVO(WmsExpense expense) {
        WmsExpenseRespVO vo = new WmsExpenseRespVO();
        BeanUtils.copyProperties(expense, vo);
        vo.setExpenseTypeId(expense.getExpenseType());

        // 填充费用类型名称
        if (expense.getExpenseType() != null) {
            WmsExpenseType expenseType = expenseTypeMapper.selectById(expense.getExpenseType());
            if (expenseType != null) {
                vo.setExpenseTypeName(expenseType.getTypeName());
            }
        }

        // 填充账户名称
        if (expense.getAccountId() != null) {
            WmsAccount account = accountMapper.selectById(expense.getAccountId());
            if (account != null) {
                vo.setAccountName(account.getAccountName());
            }
        }

        return vo;
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
