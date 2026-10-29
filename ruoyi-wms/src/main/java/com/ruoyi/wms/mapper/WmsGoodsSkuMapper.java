package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuPageReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 商品SKU表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsGoodsSkuMapper extends BaseMapper<WmsGoodsSku> {

    /**
     * 分页查询SKU列表（关联商品和库存）
     *
     * @param page 分页参数
     * @param reqVO 查询条件
     * @return SKU列表
     */
    IPage<WmsGoodsSkuRespVO> selectSkuPage(Page<WmsGoodsSkuPageReqVO> page, @Param("reqVO") WmsGoodsSkuPageReqVO reqVO);

    /**
     * 按名称搜索SKU（同时搜索SKU名称、规格和商品名称）
     *
     * @param name 搜索关键词
     * @param limit 返回数量限制
     * @return SKU列表
     */
    List<WmsGoodsSkuRespVO> searchByNameWithGoods(@Param("name") String name, @Param("limit") int limit);

    /**
     * 模糊召回：只要 SKU名称/规格/商品名称 命中任意一个字符片段（bigram/单字）即召回，
     * 用于空格、缺字、乱序等场景的候选粗召回，后续由应用层做相似度精排。
     *
     * @param tokens 归一化后的字符片段列表（bigram 或单字）
     * @param limit 召回数量上限
     * @return 候选SKU列表
     */
    List<WmsGoodsSkuRespVO> recallByTokens(@Param("tokens") List<String> tokens, @Param("limit") int limit);

    /**
     * 按ID查询SKU详情（关联商品和单位名称）
     *
     * @param id SKU ID
     * @return SKU详情
     */
    WmsGoodsSkuRespVO selectRespById(@Param("id") Long id);

    /**
     * 查询某商品SKU编码前缀下的最大SKU编码（包含逻辑删除记录）。
     *
     * @param goodsId 商品ID
     * @param prefix SKU编码前缀
     * @return 最大SKU编码
     */
    String selectMaxSkuCodeIncludingDeleted(@Param("goodsId") Long goodsId, @Param("prefix") String prefix);

    /**
     * 统计指定SKU编码数量（包含逻辑删除记录，可排除指定ID）。
     *
     * @param skuCode SKU编码
     * @param excludeId 排除的SKU ID
     * @return 数量
     */
    Long countBySkuCodeIncludingDeleted(@Param("skuCode") String skuCode, @Param("excludeId") Long excludeId);

}
