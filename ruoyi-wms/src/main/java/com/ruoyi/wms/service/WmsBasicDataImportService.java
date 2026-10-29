package com.ruoyi.wms.service;

import com.ruoyi.wms.domain.vo.goods.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

public interface WmsBasicDataImportService {

    ImportResult importByType(String type, MultipartFile file, String mappingJson) throws Exception;

    void downloadTemplate(String type, String goodsTemplateType, HttpServletResponse response) throws Exception;

    void downloadTemplatePackage(HttpServletResponse response) throws Exception;
}
