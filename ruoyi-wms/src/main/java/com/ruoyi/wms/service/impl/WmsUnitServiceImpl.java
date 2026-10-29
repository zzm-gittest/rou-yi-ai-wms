package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.wms.domain.WmsUnit;
import com.ruoyi.wms.domain.vo.unit.WmsUnitPageReqVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitRespVO;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;
import com.ruoyi.wms.mapper.WmsUnitMapper;
import com.ruoyi.wms.service.WmsUnitService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 计量单位表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsUnitServiceImpl extends ServiceImpl<WmsUnitMapper, WmsUnit> implements WmsUnitService {

    @Override
    public IPage<WmsUnitRespVO> page(WmsUnitPageReqVO reqVO) {
        LambdaQueryWrapper<WmsUnit> wrapper = new LambdaQueryWrapper<WmsUnit>()
                .like(StringUtils.isNotEmpty(reqVO.getUnitName()), WmsUnit::getUnitName, reqVO.getUnitName())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsUnit::getStatus, reqVO.getStatus())
                .orderByDesc(WmsUnit::getCreateTime);

        Page<WmsUnit> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        // 转换为 RespVO
        Page<WmsUnitRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        resultPage.setRecords(VoUtils.convertList(page.getRecords(), WmsUnitRespVO.class));
        return resultPage;
    }

    @Override
    public List<WmsUnitRespVO> selectList() {
        LambdaQueryWrapper<WmsUnit> wrapper = new LambdaQueryWrapper<WmsUnit>()
                .eq(WmsUnit::getStatus, "0")
                .orderByDesc(WmsUnit::getCreateTime);
        return VoUtils.convertList(list(wrapper), WmsUnitRespVO.class);
    }

    @Override
    public WmsUnitRespVO get(Long id) {
        WmsUnit unit = getById(id);
        return VoUtils.convert(unit, WmsUnitRespVO.class);
    }

    @Override
    public Long create(WmsUnitSaveReqVO reqVO) {
        WmsUnit unit = VoUtils.toEntity(reqVO, WmsUnit.class);
        save(unit);
        return unit.getId();
    }

    @Override
    public void update(WmsUnitSaveReqVO reqVO) {
        WmsUnit unit = VoUtils.toEntity(reqVO, WmsUnit.class);
        updateById(unit);
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }
}
