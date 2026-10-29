package com.ruoyi.wms.service.basicImport.handler;

import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.service.WmsGoodsImportService;
import com.ruoyi.wms.service.basicImport.WmsBasicDataImportHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class WmsGoodsImportHandler implements WmsBasicDataImportHandler {

    private final WmsGoodsImportService goodsImportService;

    public WmsGoodsImportHandler(WmsGoodsImportService goodsImportService) {
        this.goodsImportService = goodsImportService;
    }

    @Override
    public String getType() {
        return "goods";
    }

    @Override
    public ImportResult importData(MultipartFile file) throws Exception {
        return goodsImportService.importGoods(file);
    }

    @Override
    public ImportResult importData(MultipartFile file, String mappingJson) throws Exception {
        return goodsImportService.importGoods(file, mappingJson);
    }

    @Override
    public boolean supportsTemplate() {
        return false;
    }
}
