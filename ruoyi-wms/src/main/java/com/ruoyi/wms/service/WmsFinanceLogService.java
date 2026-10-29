package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsFinanceLog;
import com.ruoyi.wms.domain.vo.financeLog.WmsFinanceLogPageReqVO;
import com.ruoyi.wms.domain.vo.financeLog.WmsFinanceLogRespVO;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 财务日志表 Service接口
 *
 * @author ruoyi
 */
public interface WmsFinanceLogService extends IService<WmsFinanceLog> {

    /**
     * 分页查询财务日志列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsFinanceLogRespVO> page(WmsFinanceLogPageReqVO reqVO);

    /**
     * 获取财务日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    WmsFinanceLogRespVO get(Long id);

    /**
     * 创建财务日志（用于收款/付款）
     *
     * @param billType 单据类型（1收款 2付款 3费用）
     * @param billNo 单据编号
     * @param billId 单据ID
     * @param businessType 业务类型（1采购 2销售 3退货 4费用）
     * @param partnerId 往来单位ID
     * @param partnerType 往来单位类型（1供应商 2客户）
     * @param accountId 账户ID
     * @param amount 金额
     * @param direction 方向（1收入 2支出）
     * @param billDate 单据日期
     * @param remark 备注
     * @return 财务日志ID
     */
    Long createLog(String billType, String billNo, Long billId, String businessType,
                   Long partnerId, String partnerType, Long accountId,
                   BigDecimal amount, String direction, LocalDate billDate, String remark);
}
