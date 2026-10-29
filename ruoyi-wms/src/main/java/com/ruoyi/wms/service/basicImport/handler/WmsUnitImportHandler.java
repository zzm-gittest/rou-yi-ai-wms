package com.ruoyi.wms.service.basicImport.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsUnit;
import com.ruoyi.wms.service.basicImport.ImportFieldMeta;
import com.ruoyi.wms.domain.vo.unit.WmsUnitSaveReqVO;
import com.ruoyi.wms.mapper.WmsUnitMapper;
import com.ruoyi.wms.service.WmsUnitService;
import com.ruoyi.wms.service.basicImport.AbstractSimpleExcelImportHandler;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WmsUnitImportHandler extends AbstractSimpleExcelImportHandler {

    private final WmsUnitService unitService;
    private final WmsUnitMapper unitMapper;

    public WmsUnitImportHandler(WmsUnitService unitService, WmsUnitMapper unitMapper) {
        this.unitService = unitService;
        this.unitMapper = unitMapper;
    }

    @Override
    public String getType() {
        return "unit";
    }

    @Override
    protected String validateAndSave(Row row, int excelRowNum) {
        String unitName = cellString(row, "unitName");
        String status = normalizeStatus(cellString(row, "status"));
        String remark = cellString(row, "remark");

        if (StringUtils.isEmpty(unitName)) {
            return "单位名称不能为空";
        }

        Long count = unitMapper.selectCount(new LambdaQueryWrapper<WmsUnit>()
                .eq(WmsUnit::getUnitName, unitName.trim()));
        if (count != null && count > 0) {
            return "单位已存在: " + unitName;
        }

        WmsUnitSaveReqVO reqVO = new WmsUnitSaveReqVO();
        reqVO.setUnitName(unitName.trim());
        reqVO.setStatus(status);
        reqVO.setRemark(remark);
        unitService.create(reqVO);
        return null;
    }

    @Override
    protected String[] headers() {
        return new String[] {"单位名称*", "状态(正常/停用)", "备注"};
    }

    @Override
    protected String[] sampleRow() {
        return new String[] {"箱", "正常", "默认计量单位"};
    }

    @Override
    protected List<ImportFieldMeta> fieldMetas() {
        return Arrays.asList(
                ImportFieldMeta.of("unitName", "单位名称", true, "单位", "名称"),
                ImportFieldMeta.of("status", "状态", false, "状态(正常/停用)"),
                ImportFieldMeta.of("remark", "备注", false)
        );
    }

    @Override
    public String templateFileName() {
        return "unit_import_template.xlsx";
    }
}
