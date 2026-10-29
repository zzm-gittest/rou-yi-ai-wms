package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsAccountsPayable;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePayReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayableRespVO;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应付账款表 Service接口
 *
 * @author ruoyi
 */
public interface WmsAccountsPayableService extends IService<WmsAccountsPayable> {

    /**
     * 分页查询应付账款
     */
    IPage<WmsAccountsPayableRespVO> page(WmsAccountsPayablePageReqVO reqVO);

    /**
     * 根据ID查询应付账款详情
     */
    WmsAccountsPayableRespVO get(Long id);

    /**
     * 付款
     */
    void pay(WmsAccountsPayablePayReqVO reqVO);

    /**
     * 根据业务单据创建应付账款
     *
     * @param supplierId 供应商ID（采购相关单据）
     * @param customerId 客户ID（销售退货单据）
     * @param billType 单据类型（1采购订单 2采购入库 3销售退货）
     * @param billNo 单据编号
     * @param billId 单据ID
     * @param billAmount 单据金额
     * @param paidAmount 已付金额
     * @param billDate 单据日期
     * @return 应付账款ID
     */
    Long createByBill(Long supplierId, Long customerId, String billType, String billNo, Long billId,
                      BigDecimal billAmount, BigDecimal paidAmount, LocalDate billDate);
}
