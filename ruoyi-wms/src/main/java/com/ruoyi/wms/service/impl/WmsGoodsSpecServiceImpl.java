package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.wms.domain.WmsGoodsSpec;
import com.ruoyi.wms.domain.WmsGoodsSpecValue;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecListReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecRespVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecSaveReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecValueRespVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecValueSaveReqVO;
import com.ruoyi.wms.mapper.WmsGoodsSpecMapper;
import com.ruoyi.wms.service.WmsGoodsSpecService;
import com.ruoyi.wms.service.WmsGoodsSpecValueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品规格表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsGoodsSpecServiceImpl extends ServiceImpl<WmsGoodsSpecMapper, WmsGoodsSpec> implements WmsGoodsSpecService {

    private final WmsGoodsSpecValueService specValueService;

    public WmsGoodsSpecServiceImpl(WmsGoodsSpecValueService specValueService) {
        this.specValueService = specValueService;
    }

    @Override
    public List<WmsGoodsSpecRespVO> listByGoodsId(WmsGoodsSpecListReqVO reqVO) {
        LambdaQueryWrapper<WmsGoodsSpec> wrapper = new LambdaQueryWrapper<WmsGoodsSpec>()
                .eq(reqVO.getGoodsId() != null, WmsGoodsSpec::getGoodsId, reqVO.getGoodsId())
                .orderByAsc(WmsGoodsSpec::getSort);

        List<WmsGoodsSpec> specList = list(wrapper);
        List<WmsGoodsSpecRespVO> respVOList = VoUtils.convertList(specList, WmsGoodsSpecRespVO.class);

        // 填充规格值
        fillSpecValues(respVOList);

        return respVOList;
    }

    @Override
    public WmsGoodsSpecRespVO get(Long id) {
        WmsGoodsSpec spec = getById(id);
        WmsGoodsSpecRespVO respVO = VoUtils.convert(spec, WmsGoodsSpecRespVO.class);

        // 填充规格值
        if (respVO != null) {
            List<WmsGoodsSpecValue> values = specValueService.lambdaQuery()
                    .eq(WmsGoodsSpecValue::getSpecId, id)
                    .orderByAsc(WmsGoodsSpecValue::getSort)
                    .list();
            respVO.setValues(VoUtils.convertList(values, WmsGoodsSpecValueRespVO.class));
        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(WmsGoodsSpecSaveReqVO reqVO) {
        // 检查同一商品下是否已存在同名规格
        long count = lambdaQuery()
                .eq(WmsGoodsSpec::getGoodsId, reqVO.getGoodsId())
                .eq(WmsGoodsSpec::getSpecName, reqVO.getSpecName())
                .count();
        if (count > 0) {
            throw new ServiceException("商品规格\"" + reqVO.getSpecName() + "\"已存在");
        }

        WmsGoodsSpec spec = new WmsGoodsSpec();
        spec.setGoodsId(reqVO.getGoodsId());
        spec.setSpecName(reqVO.getSpecName());
        spec.setSort(reqVO.getSort());
        save(spec);

        // 保存规格值（去重）
        if (reqVO.getValues() != null && !reqVO.getValues().isEmpty()) {
            Set<String> valueSet = new HashSet<>();
            List<WmsGoodsSpecValue> values = reqVO.getValues().stream()
                    .filter(vo -> {
                        // 检查是否有重复的规格值
                        if (!valueSet.add(vo.getSpecValue())) {
                            return false; // 过滤掉重复值
                        }
                        return true;
                    })
                    .map(vo -> {
                        WmsGoodsSpecValue value = new WmsGoodsSpecValue();
                        value.setSpecId(spec.getId());
                        value.setSpecValue(vo.getSpecValue());
                        value.setSort(vo.getSort());
                        return value;
                    })
                    .collect(Collectors.toList());
            specValueService.saveBatch(values);
        }

        return spec.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WmsGoodsSpecSaveReqVO reqVO) {
        // 检查同一商品下是否已存在同名规格（排除自己）
        long count = lambdaQuery()
                .eq(WmsGoodsSpec::getGoodsId, reqVO.getGoodsId())
                .eq(WmsGoodsSpec::getSpecName, reqVO.getSpecName())
                .ne(WmsGoodsSpec::getId, reqVO.getId())
                .count();
        if (count > 0) {
            throw new ServiceException("商品规格\"" + reqVO.getSpecName() + "\"已存在");
        }

        WmsGoodsSpec spec = new WmsGoodsSpec();
        spec.setId(reqVO.getId());
        spec.setGoodsId(reqVO.getGoodsId());
        spec.setSpecName(reqVO.getSpecName());
        spec.setSort(reqVO.getSort());
        updateById(spec);

        // 删除原有规格值
        specValueService.lambdaUpdate()
                .eq(WmsGoodsSpecValue::getSpecId, reqVO.getId())
                .remove();

        // 保存新规格值（去重）
        if (reqVO.getValues() != null && !reqVO.getValues().isEmpty()) {
            Set<String> valueSet = new HashSet<>();
            List<WmsGoodsSpecValue> values = reqVO.getValues().stream()
                    .filter(vo -> {
                        // 检查是否有重复的规格值
                        if (!valueSet.add(vo.getSpecValue())) {
                            return false; // 过滤掉重复值
                        }
                        return true;
                    })
                    .map(vo -> {
                        WmsGoodsSpecValue value = new WmsGoodsSpecValue();
                        value.setSpecId(spec.getId());
                        value.setSpecValue(vo.getSpecValue());
                        value.setSort(vo.getSort());
                        return value;
                    })
                    .collect(Collectors.toList());
            specValueService.saveBatch(values);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 删除规格值
        specValueService.lambdaUpdate()
                .eq(WmsGoodsSpecValue::getSpecId, id)
                .remove();

        // 删除规格
        removeById(id);
    }

    /**
     * 批量填充规格值
     */
    private void fillSpecValues(List<WmsGoodsSpecRespVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 获取所有规格ID
        List<Long> specIds = list.stream()
                .map(WmsGoodsSpecRespVO::getId)
                .collect(Collectors.toList());

        // 批量查询规格值
        List<WmsGoodsSpecValue> values = specValueService.lambdaQuery()
                .in(WmsGoodsSpecValue::getSpecId, specIds)
                .orderByAsc(WmsGoodsSpecValue::getSort)
                .list();

        // 按规格ID分组
        values.stream().collect(Collectors.groupingBy(WmsGoodsSpecValue::getSpecId))
                .forEach((specId, valueList) -> {
                    list.stream()
                            .filter(spec -> spec.getId().equals(specId))
                            .findFirst()
                            .ifPresent(spec -> spec.setValues(
                                    VoUtils.convertList(valueList, WmsGoodsSpecValueRespVO.class)
                            ));
                });
    }
}
