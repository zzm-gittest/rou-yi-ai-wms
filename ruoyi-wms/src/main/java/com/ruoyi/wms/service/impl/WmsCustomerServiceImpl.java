package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.wms.domain.WmsAccountsPayable;
import com.ruoyi.wms.domain.WmsAccountsReceivable;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.domain.WmsSalesOrder;
import com.ruoyi.wms.domain.WmsSalesOut;
import com.ruoyi.wms.domain.WmsSalesReturn;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerPageReqVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerRespVO;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO;
import com.ruoyi.wms.mapper.WmsAccountsPayableMapper;
import com.ruoyi.wms.mapper.WmsAccountsReceivableMapper;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.mapper.WmsSalesOrderMapper;
import com.ruoyi.wms.mapper.WmsSalesOutMapper;
import com.ruoyi.wms.mapper.WmsSalesReturnMapper;
import com.ruoyi.wms.service.WmsCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 客户表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsCustomerServiceImpl extends ServiceImpl<WmsCustomerMapper, WmsCustomer> implements WmsCustomerService {

    @Autowired
    private WmsSalesOrderMapper salesOrderMapper;
    @Autowired
    private WmsSalesOutMapper salesOutMapper;
    @Autowired
    private WmsSalesReturnMapper salesReturnMapper;
    @Autowired
    private WmsAccountsReceivableMapper accountsReceivableMapper;
    @Autowired
    private WmsAccountsPayableMapper accountsPayableMapper;

    @Override
    public IPage<WmsCustomerRespVO> page(WmsCustomerPageReqVO reqVO) {
        LambdaQueryWrapper<WmsCustomer> wrapper = new LambdaQueryWrapper<WmsCustomer>()
                .like(StringUtils.isNotEmpty(reqVO.getCustomerCode()), WmsCustomer::getCustomerCode, reqVO.getCustomerCode())
                .like(StringUtils.isNotEmpty(reqVO.getCustomerName()), WmsCustomer::getCustomerName, reqVO.getCustomerName())
                .eq(StringUtils.isNotEmpty(reqVO.getCustomerType()), WmsCustomer::getCustomerType, reqVO.getCustomerType())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsCustomer::getStatus, reqVO.getStatus())
                .orderByDesc(WmsCustomer::getCreateTime);

        Page<WmsCustomer> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsCustomerRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(VoUtils.convertList(page.getRecords(), WmsCustomerRespVO.class));
        return resultPage;
    }

    @Override
    public List<WmsCustomerRespVO> selectList() {
        LambdaQueryWrapper<WmsCustomer> wrapper = new LambdaQueryWrapper<WmsCustomer>()
                .eq(WmsCustomer::getStatus, "0")
                .orderByDesc(WmsCustomer::getCreateTime);
        return VoUtils.convertList(list(wrapper), WmsCustomerRespVO.class);
    }

    @Override
    public WmsCustomerRespVO get(Long id) {
        WmsCustomer customer = getById(id);
        return VoUtils.convert(customer, WmsCustomerRespVO.class);
    }

    @Override
    public Long create(WmsCustomerSaveReqVO reqVO) {
        WmsCustomer customer = VoUtils.toEntity(reqVO, WmsCustomer.class);
        if (StringUtils.isEmpty(customer.getCustomerCode())) {
            customer.setCustomerCode(generateCustomerCode());
        }
        checkCustomerCodeUnique(null, customer.getCustomerCode());
        save(customer);
        return customer.getId();
    }

    @Override
    public void update(WmsCustomerSaveReqVO reqVO) {
        WmsCustomer existing = getById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("客户不存在");
        }

        WmsCustomer customer = VoUtils.toEntity(reqVO, WmsCustomer.class);
        if (StringUtils.isEmpty(customer.getCustomerCode())) {
            customer.setCustomerCode(existing.getCustomerCode());
        }
        checkCustomerCodeUnique(reqVO.getId(), customer.getCustomerCode());
        updateById(customer);
    }

    @Override
    public void delete(Long id) {
        if (hasHistoricalRelation(id)) {
            throw new ServiceException("客户已关联历史单据，无法删除。请改为停用客户。");
        }
        removeById(id);
    }

    private boolean hasHistoricalRelation(Long customerId) {
        return hasRows(salesOrderMapper.selectCount(new LambdaQueryWrapper<WmsSalesOrder>()
                .eq(WmsSalesOrder::getCustomerId, customerId)))
                || hasRows(salesOutMapper.selectCount(new LambdaQueryWrapper<WmsSalesOut>()
                .eq(WmsSalesOut::getCustomerId, customerId)))
                || hasRows(salesReturnMapper.selectCount(new LambdaQueryWrapper<WmsSalesReturn>()
                .eq(WmsSalesReturn::getCustomerId, customerId)))
                || hasRows(accountsReceivableMapper.selectCount(new LambdaQueryWrapper<WmsAccountsReceivable>()
                .eq(WmsAccountsReceivable::getCustomerId, customerId)))
                || hasRows(accountsPayableMapper.selectCount(new LambdaQueryWrapper<WmsAccountsPayable>()
                .eq(WmsAccountsPayable::getCustomerId, customerId)));
    }

    private boolean hasRows(Long count) {
        return count != null && count > 0;
    }

    @Override
    public Long findIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<WmsCustomer> wrapper = new LambdaQueryWrapper<WmsCustomer>()
                .eq(WmsCustomer::getCustomerName, name.trim())
                .or()
                .like(WmsCustomer::getCustomerName, name.trim());

        WmsCustomer customer = getOne(wrapper);
        return customer != null ? customer.getId() : null;
    }

    @Override
    public WmsCustomer getDefault() {
        return lambdaQuery()
                .eq(WmsCustomer::getIsDefault, 1)
                .eq(WmsCustomer::getStatus, "0")
                .last("LIMIT 1")
                .one();
    }

    private void checkCustomerCodeUnique(Long id, String customerCode) {
        if (StringUtils.isEmpty(customerCode)) {
            return;
        }
        LambdaQueryWrapper<WmsCustomer> wrapper = new LambdaQueryWrapper<WmsCustomer>()
                .eq(WmsCustomer::getCustomerCode, customerCode);
        if (id != null) {
            wrapper.ne(WmsCustomer::getId, id);
        }
        if (exists(wrapper)) {
            throw new ServiceException("客户编码已存在");
        }
    }

    private String generateCustomerCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "CT" + datePart;

        WmsCustomer lastCustomer = lambdaQuery()
                .likeRight(WmsCustomer::getCustomerCode, prefix)
                .orderByDesc(WmsCustomer::getCustomerCode)
                .last("LIMIT 1")
                .one();

        int nextSeq = 1;
        if (lastCustomer != null && StringUtils.isNotEmpty(lastCustomer.getCustomerCode())
                && lastCustomer.getCustomerCode().length() >= prefix.length() + 4) {
            String suffix = lastCustomer.getCustomerCode().substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignore) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%04d", nextSeq);
    }
}
