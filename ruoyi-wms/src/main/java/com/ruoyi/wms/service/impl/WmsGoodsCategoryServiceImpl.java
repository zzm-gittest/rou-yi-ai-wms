package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.VoUtils;
import com.ruoyi.wms.domain.WmsGoodsCategory;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryListReqVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryRespVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;
import com.ruoyi.wms.mapper.WmsGoodsCategoryMapper;
import com.ruoyi.wms.service.WmsGoodsCategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类表 Service实现
 *
 * @author ruoyi
 */
@Service
public class WmsGoodsCategoryServiceImpl extends ServiceImpl<WmsGoodsCategoryMapper, WmsGoodsCategory> implements WmsGoodsCategoryService {

    @Override
    public List<WmsGoodsCategoryRespVO> listTree(WmsGoodsCategoryListReqVO reqVO) {
        LambdaQueryWrapper<WmsGoodsCategory> wrapper = new LambdaQueryWrapper<WmsGoodsCategory>()
                .like(StringUtils.isNotEmpty(reqVO.getCategoryName()), WmsGoodsCategory::getCategoryName, reqVO.getCategoryName())
                .eq(StringUtils.isNotEmpty(reqVO.getStatus()), WmsGoodsCategory::getStatus, reqVO.getStatus())
                .orderByAsc(WmsGoodsCategory::getOrderNum);

        List<WmsGoodsCategory> categoryList = list(wrapper);
        List<WmsGoodsCategoryRespVO> respVOList = VoUtils.convertList(categoryList, WmsGoodsCategoryRespVO.class);

        return buildTree(respVOList);
    }

    @Override
    public List<WmsGoodsCategoryRespVO> selectList() {
        LambdaQueryWrapper<WmsGoodsCategory> wrapper = new LambdaQueryWrapper<WmsGoodsCategory>()
                .eq(WmsGoodsCategory::getStatus, "0")
                .orderByAsc(WmsGoodsCategory::getOrderNum);
        return VoUtils.convertList(list(wrapper), WmsGoodsCategoryRespVO.class);
    }

    @Override
    public WmsGoodsCategoryRespVO get(Long id) {
        WmsGoodsCategory category = getById(id);
        return VoUtils.convert(category, WmsGoodsCategoryRespVO.class);
    }

    @Override
    public Long create(WmsGoodsCategorySaveReqVO reqVO) {
        WmsGoodsCategory category = VoUtils.toEntity(reqVO, WmsGoodsCategory.class);

        // 检查同一父分类下是否存在相同名称的分类
        LambdaQueryWrapper<WmsGoodsCategory> nameWrapper = new LambdaQueryWrapper<WmsGoodsCategory>()
                .eq(WmsGoodsCategory::getParentId, reqVO.getParentId() != null ? reqVO.getParentId() : 0L)
                .eq(WmsGoodsCategory::getCategoryName, reqVO.getCategoryName().trim());
        long nameCount = count(nameWrapper);
        if (nameCount > 0) {
            throw new RuntimeException("该父分类下已存在同名分类：" + reqVO.getCategoryName());
        }

        // 设置祖级列表
        if (category.getParentId() == null || category.getParentId() == 0L) {
            category.setParentId(0L);
            category.setAncestors("0");
        } else {
            WmsGoodsCategory parent = getById(category.getParentId());
            if (parent != null) {
                category.setAncestors(parent.getAncestors() + "," + parent.getId());
            } else {
                category.setAncestors("0");
            }
        }

        save(category);
        return category.getId();
    }

    @Override
    public void update(WmsGoodsCategorySaveReqVO reqVO) {
        WmsGoodsCategory category = VoUtils.toEntity(reqVO, WmsGoodsCategory.class);

        // 检查同一父分类下是否存在相同名称的分类（排除自己）
        Long parentId = reqVO.getParentId() != null ? reqVO.getParentId() : 0L;
        LambdaQueryWrapper<WmsGoodsCategory> nameWrapper = new LambdaQueryWrapper<WmsGoodsCategory>()
                .eq(WmsGoodsCategory::getParentId, parentId)
                .eq(WmsGoodsCategory::getCategoryName, reqVO.getCategoryName().trim())
                .ne(WmsGoodsCategory::getId, reqVO.getId());
        long nameCount = count(nameWrapper);
        if (nameCount > 0) {
            throw new RuntimeException("该父分类下已存在同名分类：" + reqVO.getCategoryName());
        }

        WmsGoodsCategory oldCategory = getById(category.getId());
        if (oldCategory != null && !oldCategory.getParentId().equals(category.getParentId())) {
            // 父分类发生变化，更新祖级列表
            if (category.getParentId() == null || category.getParentId() == 0L) {
                category.setParentId(0L);
                category.setAncestors("0");
            } else {
                WmsGoodsCategory newParent = getById(category.getParentId());
                if (newParent != null) {
                    category.setAncestors(newParent.getAncestors() + "," + newParent.getId());
                }
            }
        }

        updateById(category);
    }

    @Override
    public void delete(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<WmsGoodsCategory> wrapper = new LambdaQueryWrapper<WmsGoodsCategory>()
                .eq(WmsGoodsCategory::getParentId, id);
        long count = count(wrapper);
        if (count > 0) {
            throw new RuntimeException("存在子分类，不允许删除");
        }
        removeById(id);
    }

    /**
     * 构建树形结构
     */
    private List<WmsGoodsCategoryRespVO> buildTree(List<WmsGoodsCategoryRespVO> categories) {
        List<WmsGoodsCategoryRespVO> returnList = new ArrayList<>();
        List<Long> tempList = categories.stream().map(WmsGoodsCategoryRespVO::getId).collect(Collectors.toList());

        for (WmsGoodsCategoryRespVO category : categories) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (category.getParentId() == null || category.getParentId() == 0L || !tempList.contains(category.getParentId())) {
                recursionFn(categories, category);
                returnList.add(category);
            }
        }

        if (returnList.isEmpty()) {
            returnList = categories;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<WmsGoodsCategoryRespVO> list, WmsGoodsCategoryRespVO t) {
        // 得到子节点列表
        List<WmsGoodsCategoryRespVO> childList = getChildList(list, t);
        t.setChildren(childList);
        for (WmsGoodsCategoryRespVO tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<WmsGoodsCategoryRespVO> getChildList(List<WmsGoodsCategoryRespVO> list, WmsGoodsCategoryRespVO t) {
        List<WmsGoodsCategoryRespVO> tlist = new ArrayList<>();
        Iterator<WmsGoodsCategoryRespVO> it = list.iterator();
        while (it.hasNext()) {
            WmsGoodsCategoryRespVO n = it.next();
            if (n.getParentId() != null && n.getParentId().equals(t.getId())) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<WmsGoodsCategoryRespVO> list, WmsGoodsCategoryRespVO t) {
        return !getChildList(list, t).isEmpty();
    }
}
