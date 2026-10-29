package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsAccountsReceivable;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableCollectReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableRespVO;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 应收账款表 Service接口
 *
 * @author ruoyi
 */
public interface WmsAccountsReceivableService extends IService<WmsAccountsReceivable> {

    /**
     * 分页查询应收账款
     */
    IPage<WmsAccountsReceivableRespVO> page(WmsAccountsReceivablePageReqVO reqVO);

    /**
     * 根据ID查询应收账款详情
     */
    WmsAccountsReceivableRespVO get(Long id);

    /**
     * 收款
     */
    void collect(WmsAccountsReceivableCollectReqVO reqVO);

    /**
     * 根据业务单据创建应收账款
     *
     * @param customerId 客户ID（销售相关单据）
     * @param supplierId 供应商ID（采购退货单据）
     * @param billType 单据类型（1销售订单 2销售出库 3采购退货）
     * @param billNo 单据编号
     * @param billId 单据ID
     * @param billAmount 单据金额
     * @param receivedAmount 已收金额
     * @param billDate 单据日期
     * @return 应收账款ID
     */
    Long createByBill(Long customerId, Long supplierId, String billType, String billNo, Long billId,
                      BigDecimal billAmount, BigDecimal receivedAmount, LocalDate billDate);
}
