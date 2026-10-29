package com.ruoyi.wms.service.basicImport;

import com.ruoyi.wms.domain.vo.goods.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;

/**
 * 基础数据导入处理器
 */
public interface WmsBasicDataImportHandler {

    /**
     * 处理器类型：unit/category/goods/supplier/customer/warehouse
     */
    String getType();

    /**
     * 导入数据
     */
    ImportResult importData(MultipartFile file) throws Exception;

    /**
     * 导入数据（可携带字段映射配置）
     */
    default ImportResult importData(MultipartFile file, String mappingJson) throws Exception {
        return importData(file);
    }

    /**
     * 当前处理器是否支持模板输出
     */
    default boolean supportsTemplate() {
        return true;
    }

    /**
     * 输出模板
     */
    default void writeTemplate(OutputStream outputStream) throws Exception {
        throw new UnsupportedOperationException("当前处理器不支持模板导出");
    }

    /**
     * 模板文件名
     */
    default String templateFileName() {
        return getType() + "_import_template.xlsx";
    }
}
