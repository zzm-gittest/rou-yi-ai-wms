package com.ruoyi.wms.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.service.WmsBasicDataImportService;
import com.ruoyi.wms.service.WmsGoodsImportService;
import com.ruoyi.wms.service.basicImport.WmsBasicDataImportHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class WmsBasicDataImportServiceImpl implements WmsBasicDataImportService {

    private final Map<String, WmsBasicDataImportHandler> handlerMap = new HashMap<>();
    private final WmsGoodsImportService goodsImportService;

    public WmsBasicDataImportServiceImpl(List<WmsBasicDataImportHandler> handlers,
                                         WmsGoodsImportService goodsImportService) {
        for (WmsBasicDataImportHandler handler : handlers) {
            handlerMap.put(handler.getType(), handler);
        }
        this.goodsImportService = goodsImportService;
    }

    @Override
    public ImportResult importByType(String type, MultipartFile file, String mappingJson) throws Exception {
        String normalizedType = normalizeType(type);
        WmsBasicDataImportHandler handler = handlerMap.get(normalizedType);
        if (handler == null) {
            throw new ServiceException("不支持的导入类型: " + type);
        }
        return handler.importData(file, mappingJson);
    }

    @Override
    public void downloadTemplate(String type, String goodsTemplateType, HttpServletResponse response) throws Exception {
        String normalizedType = normalizeType(type);

        if ("goods".equals(normalizedType)) {
            goodsImportService.generateImportTemplate(response,
                    StringUtils.isEmpty(goodsTemplateType) ? "simple" : goodsTemplateType);
            return;
        }

        WmsBasicDataImportHandler handler = handlerMap.get(normalizedType);
        if (handler == null || !handler.supportsTemplate()) {
            throw new ServiceException("不支持的模板类型: " + type);
        }

        String fileName = URLEncoder.encode(handler.templateFileName(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

        handler.writeTemplate(response.getOutputStream());
    }

    @Override
    public void downloadTemplatePackage(HttpServletResponse response) throws Exception {
        String fileName = URLEncoder.encode("basic_import_template_package.zip", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            for (WmsBasicDataImportHandler handler : handlerMap.values()) {
                if (!handler.supportsTemplate()) {
                    continue;
                }

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                handler.writeTemplate(bos);

                zos.putNextEntry(new ZipEntry(handler.templateFileName()));
                zos.write(bos.toByteArray());
                zos.closeEntry();
            }

            String readme = "goods模板请使用 /wms/basicImport/template/goods?goodsTemplateType=simple 或 full 下载。";
            zos.putNextEntry(new ZipEntry("README.txt"));
            zos.write(readme.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }

    private String normalizeType(String type) {
        if (StringUtils.isEmpty(type)) {
            return "";
        }
        return type.trim().toLowerCase();
    }
}
