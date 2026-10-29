package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsAccountsPayable;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayablePageReqVO;
import com.ruoyi.wms.domain.vo.accountsPayable.WmsAccountsPayableRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 应付账款表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsAccountsPayableMapper extends BaseMapper<WmsAccountsPayable> {

    /**
     * 分页查询应付账款
     */
    IPage<WmsAccountsPayableRespVO> selectApPage(Page<?> page, @Param("reqVO") WmsAccountsPayablePageReqVO reqVO);

    /**
     * 根据ID查询应付账款详情
     */
    WmsAccountsPayableRespVO selectApById(@Param("id") Long id);
}
