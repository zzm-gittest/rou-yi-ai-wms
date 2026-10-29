package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsSalesReturn;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnExportVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnPageReqVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnRespVO;
import com.ruoyi.wms.domain.vo.salesReturn.WmsSalesReturnSaveReqVO;
import java.util.List;

/**
 * 销售退货单表 Service接口
 *
 * @author ruoyi
 */
public interface WmsSalesReturnService extends IService<WmsSalesReturn> {

    /**
     * 分页查询销售退货单列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsSalesReturnRespVO> page(WmsSalesReturnPageReqVO reqVO);

    /**
     * 获取销售退货单详情
     *
     * @param id 退货单ID
     * @return 退货单详情
     */
    WmsSalesReturnRespVO get(Long id);

    /**
     * 新增销售退货单
     *
     * @param reqVO 退货单信息
     * @return 退货单ID
     */
    Long create(WmsSalesReturnSaveReqVO reqVO);

    /**
     * 修改销售退货单
     *
     * @param reqVO 退货单信息
     */
    void update(WmsSalesReturnSaveReqVO reqVO);

    /**
     * 删除销售退货单
     *
     * @param id 退货单ID
     */
    void delete(Long id);

    /**
     * 审核销售退货单
     *
     * @param id 退货单ID
     */
    void approve(Long id);

    /**
     * 导出销售退货列表（按明细行展开）
     */
    List<WmsSalesReturnExportVO> exportList(WmsSalesReturnPageReqVO reqVO);
}
