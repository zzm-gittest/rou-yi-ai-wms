package com.ruoyi.wms.service;

import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.domain.vo.goods.WmsGoodsImportVo;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 商品导入服务接口
 *
 * @author ruoyi
 */
public interface WmsGoodsImportService {

    /**
     * Excel导入商品
     *
     * @param file Excel文件
     * @return 导入结果
     */
    ImportResult importGoods(MultipartFile file) throws Exception;

    /**
     * Excel导入商品（支持字段映射）
     *
     * @param file Excel文件
     * @param mappingJson 字段映射配置
     * @return 导入结果
     */
    default ImportResult importGoods(MultipartFile file, String mappingJson) throws Exception {
        return importGoods(file);
    }

    /**
     * 获取导入模板
     *
     * @param response 响应
     * @param templateType 模板类型：simple-简易模板（无规格），full-完整模板（有规格）
     */
    void generateImportTemplate(HttpServletResponse response, String templateType) throws Exception;

    /**
     * 验证Excel数据
     *
     * @param importVo 导入数据
     * @return 错误信息列表
     */
    List<String> validateImportData(WmsGoodsImportVo importVo);
}
