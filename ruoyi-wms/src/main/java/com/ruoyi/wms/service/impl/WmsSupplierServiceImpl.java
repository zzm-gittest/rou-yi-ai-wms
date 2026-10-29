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
import com.ruoyi.wms.domain.WmsPurchaseIn;
import com.ruoyi.wms.domain.WmsPurchaseOrder;
import com.ruoyi.wms.domain.WmsPurchaseReturn;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierPageReqVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierRespVO;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;
import com.ruoyi.wms.mapper.WmsAccountsPayableMapper;
import com.ruoyi.wms.mapper.WmsAccountsReceivableMapper;
import com.ruoyi.wms.mapper.WmsPurchaseInMapper;
import com.ruoyi.wms.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.wms.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.service.WmsSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 供应商表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsSupplierServiceImpl extends ServiceImpl<WmsSupplierMapper, WmsSupplier> implements WmsSupplierService {

    @Autowired
    private WmsPurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private WmsPurchaseInMapper purchaseInMapper;
    @Autowired
    private WmsPurchaseReturnMapper purchaseReturnMapper;
    @Autowired
    private WmsAccountsPayableMapper accountsPayableMapper;
    @Autowired
    private WmsAccountsReceivableMapper accountsReceivableMapper;

    @Override
    public IPage<WmsSupplierRespVO> page(WmsSupplierPageReqVO reqVO) {
        LambdaQueryWrapper<WmsSupplier> wrapper = new LambdaQueryWrapper<WmsSupplier>()
                .like(StringUtils.isNotEmpty(reqVO.getSupplierCode()), WmsSupplier::getSupplierCode, reqVO.getSupplierCode())
                .like(StringUtils.isNotEmpty(reqVO.getSupplierName()), WmsSupplier::getSupplierName, reqVO.getSupplierName())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsSupplier::getStatus, reqVO.getStatus())
                .orderByDesc(WmsSupplier::getCreateTime);

        Page<WmsSupplier> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsSupplierRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(VoUtils.convertList(page.getRecords(), WmsSupplierRespVO.class));
        return resultPage;
    }

    @Override
    public List<WmsSupplierRespVO> selectList() {
        LambdaQueryWrapper<WmsSupplier> wrapper = new LambdaQueryWrapper<WmsSupplier>()
                .eq(WmsSupplier::getStatus, "0")
                .orderByDesc(WmsSupplier::getCreateTime);
        return VoUtils.convertList(list(wrapper), WmsSupplierRespVO.class);
    }

    @Override
    public WmsSupplierRespVO get(Long id) {
        WmsSupplier supplier = getById(id);
        return VoUtils.convert(supplier, WmsSupplierRespVO.class);
    }

    @Override
    public Long create(WmsSupplierSaveReqVO reqVO) {
        WmsSupplier supplier = VoUtils.toEntity(reqVO, WmsSupplier.class);
        if (StringUtils.isEmpty(supplier.getSupplierCode())) {
            supplier.setSupplierCode(generateSupplierCode());
        }
        checkSupplierCodeUnique(null, supplier.getSupplierCode());
        save(supplier);
        return supplier.getId();
    }

    @Override
    public void update(WmsSupplierSaveReqVO reqVO) {
        WmsSupplier existing = getById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("供应商不存在");
        }

        WmsSupplier supplier = VoUtils.toEntity(reqVO, WmsSupplier.class);
        if (StringUtils.isEmpty(supplier.getSupplierCode())) {
            supplier.setSupplierCode(existing.getSupplierCode());
        }
        checkSupplierCodeUnique(reqVO.getId(), supplier.getSupplierCode());
        updateById(supplier);
    }

    @Override
    public void delete(Long id) {
        if (hasHistoricalRelation(id)) {
            throw new ServiceException("供应商已关联历史单据，无法删除。请改为停用供应商。");
        }
        removeById(id);
    }

    private boolean hasHistoricalRelation(Long supplierId) {
        return hasRows(purchaseOrderMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseOrder>()
                .eq(WmsPurchaseOrder::getSupplierId, supplierId)))
                || hasRows(purchaseInMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseIn>()
                .eq(WmsPurchaseIn::getSupplierId, supplierId)))
                || hasRows(purchaseReturnMapper.selectCount(new LambdaQueryWrapper<WmsPurchaseReturn>()
                .eq(WmsPurchaseReturn::getSupplierId, supplierId)))
                || hasRows(accountsPayableMapper.selectCount(new LambdaQueryWrapper<WmsAccountsPayable>()
                .eq(WmsAccountsPayable::getSupplierId, supplierId)))
                || hasRows(accountsReceivableMapper.selectCount(new LambdaQueryWrapper<WmsAccountsReceivable>()
                .eq(WmsAccountsReceivable::getSupplierId, supplierId)));
    }

    private boolean hasRows(Long count) {
        return count != null && count > 0;
    }

    @Override
    public Long findIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        LambdaQueryWrapper<WmsSupplier> wrapper = new LambdaQueryWrapper<WmsSupplier>()
                .eq(WmsSupplier::getSupplierName, name.trim())
                .or()
                .like(WmsSupplier::getSupplierName, name.trim());

        WmsSupplier supplier = getOne(wrapper);
        return supplier != null ? supplier.getId() : null;
    }

    @Override
    public WmsSupplier getDefault() {
        return lambdaQuery()
                .eq(WmsSupplier::getIsDefault, 1)
                .eq(WmsSupplier::getStatus, "0")
                .last("LIMIT 1")
                .one();
    }

    private void checkSupplierCodeUnique(Long id, String supplierCode) {
        if (StringUtils.isEmpty(supplierCode)) {
            return;
        }
        LambdaQueryWrapper<WmsSupplier> wrapper = new LambdaQueryWrapper<WmsSupplier>()
                .eq(WmsSupplier::getSupplierCode, supplierCode);
        if (id != null) {
            wrapper.ne(WmsSupplier::getId, id);
        }
        if (exists(wrapper)) {
            throw new ServiceException("供应商编码已存在");
        }
    }

    private String generateSupplierCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "SY" + datePart;

        WmsSupplier lastSupplier = lambdaQuery()
                .likeRight(WmsSupplier::getSupplierCode, prefix)
                .orderByDesc(WmsSupplier::getSupplierCode)
                .last("LIMIT 1")
                .one();

        int nextSeq = 1;
        if (lastSupplier != null && StringUtils.isNotEmpty(lastSupplier.getSupplierCode())
                && lastSupplier.getSupplierCode().length() >= prefix.length() + 4) {
            String suffix = lastSupplier.getSupplierCode().substring(prefix.length());
            try {
                nextSeq = Integer.parseInt(suffix) + 1;
            } catch (NumberFormatException ignore) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%04d", nextSeq);
    }
}
