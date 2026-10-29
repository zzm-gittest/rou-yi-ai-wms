package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsGoodsSpec;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecListReqVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecRespVO;
import com.ruoyi.wms.domain.vo.goodsSpec.WmsGoodsSpecSaveReqVO;

import java.util.List;

/**
 * 商品规格表 Service接口
 *
 * @author ruoyi
 */
public interface WmsGoodsSpecService extends IService<WmsGoodsSpec> {

    /**
     * 查询商品规格列表（按商品ID）
     *
     * @param reqVO 查询条件
     * @return 规格列表
     */
    List<WmsGoodsSpecRespVO> listByGoodsId(WmsGoodsSpecListReqVO reqVO);

    /**
     * 获取商品规格详情
     *
     * @param id 规格ID
     * @return 规格详情
     */
    WmsGoodsSpecRespVO get(Long id);

    /**
     * 新增商品规格
     *
     * @param reqVO 规格信息
     * @return 规格ID
     */
    Long create(WmsGoodsSpecSaveReqVO reqVO);

    /**
     * 修改商品规格
     *
     * @param reqVO 规格信息
     */
    void update(WmsGoodsSpecSaveReqVO reqVO);

    /**
     * 删除商品规格
     *
     * @param id 规格ID
     */
    void delete(Long id);
}
