package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsSalesOut;
import com.ruoyi.wms.domain.vo.print.WmsBillPrintRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutExportVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutItemRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutPageReqVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutRespVO;
import com.ruoyi.wms.domain.vo.salesOut.WmsSalesOutSaveReqVO;

import java.util.List;

/**
 * 销售出库单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsSalesOutService extends IService<WmsSalesOut> {

    /**
     * 分页查询销售出库单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsSalesOutRespVO> page(WmsSalesOutPageReqVO reqVO);

    /**
     * 获取销售出库单详情
     *
     * @param id 出库单ID
     * @return 出库单详情
     */
    WmsSalesOutRespVO get(Long id);

    /**
     * 获取销售出库打印数据
     *
     * @param id 出库单ID
     * @return 打印数据
     */
    WmsBillPrintRespVO getPrintData(Long id);

    /**
     * 新增销售出库单
     *
     * @param reqVO 出库单信息
     * @return 出库单ID
     */
    Long create(WmsSalesOutSaveReqVO reqVO);

    /**
     * 按飞书/导入来源单据号查询已生成的 WMS 出库单号（备注前缀「导入来源单号:」），用于防重复导入
     *
     * @param sourceBillNo 飞书单据号（单据号*）
     * @return 已存在的出库单号，无则 null
     */
    String findOutNoByImportSourceBillNo(String sourceBillNo);

    /**
     * 修改销售出库单
     *
     * @param reqVO 出库单信息
     */
    void update(WmsSalesOutSaveReqVO reqVO);

    /**
     * 删除销售出库单
     *
     * @param id 出库单ID
     */
    void delete(Long id);

    /**
     * 审核销售出库单
     *
     * @param id 出库单ID
     */
    void approve(Long id);

    /**
     * 反审核销售出库单
     *
     * @param id 出库单ID
     */
    void cancelApprove(Long id);

    /**
     * 查询已审核的销售出库单列表
     *
     * @return 出库单列表
     */
    List<WmsSalesOutRespVO> listApproved();

    /**
     * 获取销售出库单明细
     *
     * @param outId 出库单ID
     * @return 明细列表
     */
    List<WmsSalesOutItemRespVO> getItems(Long outId);

    /**
     * 按关键字搜索可退货的销售出库单（已审核且未全部退货）
     *
     * @param keyword 搜索关键字（出库单号或客户名称）
     * @return 出库单列表
     */
    List<WmsSalesOutRespVO> searchAvailableForReturn(String keyword);

    /**
     * 导出销售出库列表（按明细行展开）
     *
     * @param reqVO 查询条件
     * @return 导出数据列表
     */
    List<WmsSalesOutExportVO> exportList(WmsSalesOutPageReqVO reqVO);
}
