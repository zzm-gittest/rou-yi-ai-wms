package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.wms.domain.WmsGoods;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsExportVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsPageReqVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsRespVO;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsSaveReqVO;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 商品表 Service接口
 *
 * @author ruoyi
 */
public interface WmsGoodsService extends IService<WmsGoods> {

    /**
     * 分页查询商品列表
     *
     * @param reqVO 查询条件
     * @return 分页结果
     */
    IPage<WmsGoodsRespVO> page(WmsGoodsPageReqVO reqVO);

    /**
     * 查询商品列表（不分页，仅正常状态）
     *
     * @return 商品列表
     */
    List<WmsGoodsRespVO> selectList();

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    WmsGoodsRespVO get(Long id);

    /**
     * 获取商品详情（含SKU列表）
     *
     * @param id 商品ID
     * @return 商品详情（含SKU列表）
     */
    WmsGoodsRespVO getDetailWithSkus(Long id);

    /**
     * 新增商品
     *
     * @param reqVO 商品信息
     * @return 商品ID
     */
    Long create(WmsGoodsSaveReqVO reqVO);

    /**
     * 修改商品
     *
     * @param reqVO 商品信息
     */
    void update(WmsGoodsSaveReqVO reqVO);

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void delete(Long id);

    /**
     * 生成商品编码
     *
     * @param categoryId 分类ID
     * @return 商品编码
     */
    String generateGoodsCode(String categoryId);

    /**
     * 生成SKU编码
     *
     * @param goodsId 商品ID
     * @return SKU编码
     */
    String generateSkuCode(Long goodsId);

    /**
     * Excel导入商品
     *
     * @param file Excel文件
     * @return 导入结果
     */
    ImportResult importGoods(MultipartFile file) throws Exception;

    /**
     * 获取导入模板
     *
     * @param response 响应
     * @param templateType 模板类型：simple-简易模板（无规格），full-完整模板（有规格）
     */
    void generateImportTemplate(HttpServletResponse response, String templateType) throws Exception;

    /**
     * 导出商品列表（按SKU展开）
     *
     * @param reqVO 查询条件
     * @return 导出数据列表
     */
    List<WmsGoodsExportVO> exportList(WmsGoodsPageReqVO reqVO);
}
