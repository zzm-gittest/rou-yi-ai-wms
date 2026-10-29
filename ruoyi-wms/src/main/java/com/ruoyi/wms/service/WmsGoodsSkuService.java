package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsGoodsSku;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuPageReqVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuRespVO;
import com.ruoyi.wms.domain.vo.goodsSku.WmsGoodsSkuSaveReqVO;

import java.util.List;
import java.util.Map;

/**
 * 商品SKU表 Service接口
 *
 * @author ruoyi
 */
public interface WmsGoodsSkuService extends IService<WmsGoodsSku> {

    /**
     * 分页查询商品SKU
     *
     * @param reqVO 查询条件
     * @return SKU分页列表
     */
    IPage<WmsGoodsSkuRespVO> page(WmsGoodsSkuPageReqVO reqVO);

    /**
     * 查询商品SKU列表（按商品ID）
     *
     * @param goodsId 商品ID
     * @return SKU列表
     */
    List<WmsGoodsSkuRespVO> listByGoodsId(Long goodsId);

    /**
     * 获取商品SKU详情
     *
     * @param id SKU ID
     * @return SKU详情
     */
    WmsGoodsSkuRespVO get(Long id);

    /**
     * 新增商品SKU
     *
     * @param reqVO SKU信息
     * @return SKU ID
     */
    Long create(WmsGoodsSkuSaveReqVO reqVO);

    /**
     * 修改商品SKU
     *
     * @param reqVO SKU信息
     */
    void update(WmsGoodsSkuSaveReqVO reqVO);

    /**
     * 删除商品SKU
     *
     * @param id SKU ID
     */
    void delete(Long id);

    /**
     * 批量删除商品SKU
     *
     * @param ids SKU ID列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据条形码查询商品SKU（本地数据库）
     * 扫码录入时使用，如果返回null表示该条码未录入过
     *
     * @param barcode 条形码
     * @return SKU详情，不存在返回null
     */
    WmsGoodsSkuRespVO getByBarcode(String barcode);

    /**
     * 根据商品名称搜索SKU（用于AI/语音/截图OCR/文字识别匹配）。
     *
     * <p>先做精确 LIKE 子串匹配；若归一化后长度 &gt;= 2，再叠加 bigram 模糊召回 +
     * 相似度精排，覆盖空格、缺字、乱序等 LIKE 无法命中的输入。精确结果始终优先。</p>
     *
     * @param name 商品名称
     * @param limit 返回数量限制
     * @return SKU列表
     */
    List<WmsGoodsSkuRespVO> searchByName(String name, int limit);

    /**
     * 按ID列表获取启用状态的SKU映射（仅允许status=0）。
     *
     * @param skuIds SKU ID列表
     * @return 以SKU ID为key的映射
     */
    Map<Long, WmsGoodsSku> getEnabledSkuMap(List<Long> skuIds);

}
