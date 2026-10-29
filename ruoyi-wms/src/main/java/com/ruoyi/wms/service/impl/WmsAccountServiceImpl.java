package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsAccount;
import com.ruoyi.wms.domain.vo.account.WmsAccountPageReqVO;
import com.ruoyi.wms.domain.vo.account.WmsAccountRespVO;
import com.ruoyi.wms.domain.vo.account.WmsAccountSaveReqVO;
import com.ruoyi.wms.mapper.WmsAccountMapper;
import com.ruoyi.wms.service.WmsAccountService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收款账户表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsAccountServiceImpl extends ServiceImpl<WmsAccountMapper, WmsAccount> implements WmsAccountService {

    @Override
    public IPage<WmsAccountRespVO> page(WmsAccountPageReqVO reqVO) {
        LambdaQueryWrapper<WmsAccount> wrapper = new LambdaQueryWrapper<WmsAccount>()
                .like(StringUtils.isNotEmpty(reqVO.getAccountCode()), WmsAccount::getAccountCode, reqVO.getAccountCode())
                .like(StringUtils.isNotEmpty(reqVO.getAccountName()), WmsAccount::getAccountName, reqVO.getAccountName())
                .eq(StringUtils.isNotEmpty(reqVO.getAccountType()), WmsAccount::getAccountType, reqVO.getAccountType())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsAccount::getStatus, reqVO.getStatus())
                .orderByDesc(WmsAccount::getCreateTime);

        Page<WmsAccount> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsAccount> result = baseMapper.selectPage(page, wrapper);

        // 转换为RespVO
        return result.convert(this::convertToRespVO);
    }

    @Override
    public List<WmsAccountRespVO> listEnabled() {
        LambdaQueryWrapper<WmsAccount> wrapper = new LambdaQueryWrapper<WmsAccount>()
                .eq(WmsAccount::getStatus, "0")
                .orderByAsc(WmsAccount::getAccountCode);
        List<WmsAccount> list = baseMapper.selectList(wrapper);
        return list.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public WmsAccountRespVO get(Long id) {
        WmsAccount account = baseMapper.selectById(id);
        if (account == null) {
            throw new ServiceException("账户不存在");
        }
        return convertToRespVO(account);
    }

    @Override
    public Long create(WmsAccountSaveReqVO reqVO) {
        WmsAccount account = new WmsAccount();
        BeanUtils.copyProperties(reqVO, account);
        if (StringUtils.isEmpty(account.getAccountCode())) {
            account.setAccountCode(generateAccountCode(account.getAccountType()));
        }
        checkAccountCodeUnique(null, account.getAccountCode());

        account.setBalance(reqVO.getInitBalance() != null ? reqVO.getInitBalance() : BigDecimal.ZERO);
        if (StringUtils.isEmpty(account.getStatus())) {
            account.setStatus("0");
        }

        baseMapper.insert(account);
        return account.getId();
    }

    @Override
    public void update(WmsAccountSaveReqVO reqVO) {
        WmsAccount existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("账户不存在");
        }

        String originalAccountCode = existing.getAccountCode();

        BeanUtils.copyProperties(reqVO, existing);
        if (StringUtils.isEmpty(existing.getAccountCode())) {
            existing.setAccountCode(StringUtils.isEmpty(originalAccountCode)
                    ? generateAccountCode(existing.getAccountType())
                    : originalAccountCode);
        }
        checkAccountCodeUnique(reqVO.getId(), existing.getAccountCode());

        // 不允许直接修改余额
        existing.setBalance(existing.getBalance());

        baseMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        WmsAccount account = baseMapper.selectById(id);
        if (account == null) {
            throw new ServiceException("账户不存在");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ServiceException("账户余额不为零，不能删除");
        }

        baseMapper.deleteById(id);
    }

    /**
     * 转换为RespVO
     */
    private WmsAccountRespVO convertToRespVO(WmsAccount account) {
        WmsAccountRespVO vo = new WmsAccountRespVO();
        BeanUtils.copyProperties(account, vo);

        // 设置账户类型名称
        String accountType = account.getAccountType();
        if ("1".equals(accountType)) {
            vo.setAccountTypeName("现金");
        } else if ("2".equals(accountType)) {
            vo.setAccountTypeName("银行");
        } else if ("3".equals(accountType)) {
            vo.setAccountTypeName("支付宝");
        } else if ("4".equals(accountType)) {
            vo.setAccountTypeName("微信");
        } else {
            vo.setAccountTypeName("其他");
        }

        return vo;
    }

    private void checkAccountCodeUnique(Long id, String accountCode) {
        if (StringUtils.isEmpty(accountCode)) {
            return;
        }
        LambdaQueryWrapper<WmsAccount> wrapper = new LambdaQueryWrapper<WmsAccount>()
                .eq(WmsAccount::getAccountCode, accountCode);
        if (id != null) {
            wrapper.ne(WmsAccount::getId, id);
        }
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("账户编码已存在");
        }
    }

    private String generateAccountCode(String accountType) {
        String safeType = StringUtils.isEmpty(accountType) ? "1" : accountType;
        String prefix = "AC" + safeType;

        WmsAccount lastAccount = lambdaQuery()
                .likeRight(WmsAccount::getAccountCode, prefix)
                .orderByDesc(WmsAccount::getAccountCode)
                .last("LIMIT 1")
                .one();

        int nextSeq = 1;
        if (lastAccount != null && StringUtils.isNotEmpty(lastAccount.getAccountCode())
                && lastAccount.getAccountCode().length() > prefix.length()) {
            String suffix = lastAccount.getAccountCode().substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignore) {
                nextSeq = 1;
            }
        }

        return prefix + String.format("%06d", nextSeq);
    }
}
