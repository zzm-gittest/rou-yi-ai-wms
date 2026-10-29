package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsAccountsReceivable;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsReceivable.WmsAccountsReceivableRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 应收账款表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsAccountsReceivableMapper extends BaseMapper<WmsAccountsReceivable> {

    /**
     * 分页查询应收账款
     */
    IPage<WmsAccountsReceivableRespVO> selectArPage(Page<?> page, @Param("reqVO") WmsAccountsReceivablePageReqVO reqVO);

    /**
     * 根据ID查询应收账款详情
     */
    WmsAccountsReceivableRespVO selectArById(@Param("id") Long id);
}
