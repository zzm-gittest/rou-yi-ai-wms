package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsGoodsCategory;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryListReqVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategoryRespVO;
import com.ruoyi.wms.domain.vo.goodsCategory.WmsGoodsCategorySaveReqVO;

import java.util.List;

/**
 * 商品分类表 Service接口
 *
 * @author ruoyi
 */
public interface WmsGoodsCategoryService extends IService<WmsGoodsCategory> {

    /**
     * 查询商品分类树列表
     *
     * @param reqVO 查询条件
     * @return 分类树列表
     */
    List<WmsGoodsCategoryRespVO> listTree(WmsGoodsCategoryListReqVO reqVO);

    /**
     * 查询商品分类列表（不分页）
     *
     * @return 分类列表
     */
    List<WmsGoodsCategoryRespVO> selectList();

    /**
     * 获取商品分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    WmsGoodsCategoryRespVO get(Long id);

    /**
     * 新增商品分类
     *
     * @param reqVO 分类信息
     * @return 分类ID
     */
    Long create(WmsGoodsCategorySaveReqVO reqVO);

    /**
     * 修改商品分类
     *
     * @param reqVO 分类信息
     */
    void update(WmsGoodsCategorySaveReqVO reqVO);

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     */
    void delete(Long id);
}
