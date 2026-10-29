package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsExpenseType;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypePageReqVO;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypeRespVO;
import com.ruoyi.wms.domain.vo.expenseType.WmsExpenseTypeSaveReqVO;
import com.ruoyi.wms.mapper.WmsExpenseTypeMapper;
import com.ruoyi.wms.service.WmsExpenseTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 费用类型表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsExpenseTypeServiceImpl extends ServiceImpl<WmsExpenseTypeMapper, WmsExpenseType> implements WmsExpenseTypeService {

    @Override
    public IPage<WmsExpenseTypeRespVO> page(WmsExpenseTypePageReqVO reqVO) {
        LambdaQueryWrapper<WmsExpenseType> wrapper = new LambdaQueryWrapper<WmsExpenseType>()
                .like(StringUtils.isNotEmpty(reqVO.getTypeName()), WmsExpenseType::getTypeName, reqVO.getTypeName())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsExpenseType::getStatus, reqVO.getStatus())
                .orderByDesc(WmsExpenseType::getCreateTime);

        Page<WmsExpenseType> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        IPage<WmsExpenseType> result = baseMapper.selectPage(page, wrapper);

        return result.convert(item -> {
            WmsExpenseTypeRespVO vo = new WmsExpenseTypeRespVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        });
    }

    @Override
    public List<WmsExpenseTypeRespVO> listEnabled() {
        LambdaQueryWrapper<WmsExpenseType> wrapper = new LambdaQueryWrapper<WmsExpenseType>()
                .eq(WmsExpenseType::getStatus, "0")
                .orderByAsc(WmsExpenseType::getTypeName);
        List<WmsExpenseType> list = baseMapper.selectList(wrapper);
        return list.stream()
                .map(item -> {
                    WmsExpenseTypeRespVO vo = new WmsExpenseTypeRespVO();
                    BeanUtils.copyProperties(item, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public WmsExpenseTypeRespVO get(Long id) {
        WmsExpenseType expenseType = baseMapper.selectById(id);
        if (expenseType == null) {
            throw new ServiceException("费用类型不存在");
        }
        WmsExpenseTypeRespVO vo = new WmsExpenseTypeRespVO();
        BeanUtils.copyProperties(expenseType, vo);
        return vo;
    }

    @Override
    public Long create(WmsExpenseTypeSaveReqVO reqVO) {
        // 检查名称唯一性
        LambdaQueryWrapper<WmsExpenseType> wrapper = new LambdaQueryWrapper<WmsExpenseType>()
                .eq(WmsExpenseType::getTypeName, reqVO.getTypeName());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("费用类型名称已存在");
        }

        WmsExpenseType expenseType = new WmsExpenseType();
        BeanUtils.copyProperties(reqVO, expenseType);
        if (StringUtils.isEmpty(expenseType.getStatus())) {
            expenseType.setStatus("0");
        }

        baseMapper.insert(expenseType);
        return expenseType.getId();
    }

    @Override
    public void update(WmsExpenseTypeSaveReqVO reqVO) {
        WmsExpenseType existing = baseMapper.selectById(reqVO.getId());
        if (existing == null) {
            throw new ServiceException("费用类型不存在");
        }

        // 检查名称唯一性
        LambdaQueryWrapper<WmsExpenseType> wrapper = new LambdaQueryWrapper<WmsExpenseType>()
                .eq(WmsExpenseType::getTypeName, reqVO.getTypeName())
                .ne(WmsExpenseType::getId, reqVO.getId());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new ServiceException("费用类型名称已存在");
        }

        BeanUtils.copyProperties(reqVO, existing);
        baseMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }
}
