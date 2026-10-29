package com.ruoyi.wms.service.basicImport.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.service.basicImport.ImportFieldMeta;
import com.ruoyi.wms.domain.vo.warehouse.WmsWarehouseSaveReqVO;
import com.ruoyi.wms.mapper.WmsWarehouseMapper;
import com.ruoyi.wms.service.WmsWarehouseService;
import com.ruoyi.wms.service.basicImport.AbstractSimpleExcelImportHandler;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class WmsWarehouseImportHandler extends AbstractSimpleExcelImportHandler {

    private final WmsWarehouseService warehouseService;
    private final WmsWarehouseMapper warehouseMapper;

    public WmsWarehouseImportHandler(WmsWarehouseService warehouseService, WmsWarehouseMapper warehouseMapper) {
        this.warehouseService = warehouseService;
        this.warehouseMapper = warehouseMapper;
    }

    @Override
    public String getType() {
        return "warehouse";
    }

    @Override
    protected String validateAndSave(Row row, int excelRowNum) {
        String warehouseCode = cellString(row, "warehouseCode");
        String warehouseName = cellString(row, "warehouseName");
        String contact = cellString(row, "contact");
        String phone = cellString(row, "phone");
        String address = cellString(row, "address");
        String status = normalizeStatus(cellString(row, "status"));
        Integer sort = cellInteger(row, "sort", 0);
        String remark = cellString(row, "remark");

        if (StringUtils.isEmpty(warehouseName)) {
            return "仓库名称不能为空";
        }

        if (StringUtils.isNotEmpty(warehouseCode)) {
            Long codeCount = warehouseMapper.selectCount(new LambdaQueryWrapper<WmsWarehouse>()
                    .eq(WmsWarehouse::getWarehouseCode, warehouseCode.trim()));
            if (codeCount != null && codeCount > 0) {
                return "仓库编码已存在: " + warehouseCode;
            }
        }

        Long nameCount = warehouseMapper.selectCount(new LambdaQueryWrapper<WmsWarehouse>()
                .eq(WmsWarehouse::getWarehouseName, warehouseName.trim()));
        if (nameCount != null && nameCount > 0) {
            return "仓库名称已存在: " + warehouseName;
        }

        WmsWarehouseSaveReqVO reqVO = new WmsWarehouseSaveReqVO();
        reqVO.setWarehouseCode(StringUtils.isEmpty(warehouseCode) ? null : warehouseCode.trim());
        reqVO.setWarehouseName(warehouseName.trim());
        reqVO.setContact(contact);
        reqVO.setPhone(phone);
        reqVO.setAddress(address);
        reqVO.setStatus(status);
        reqVO.setSort(sort == null ? 0 : sort);
        reqVO.setRemark(remark);
        warehouseService.create(reqVO);
        return null;
    }

    @Override
    protected String[] headers() {
        return new String[] {"仓库编码", "仓库名称*", "联系人", "联系电话", "仓库地址", "状态(正常/停用)", "排序", "备注"};
    }

    @Override
    protected String[] sampleRow() {
        return new String[] {"WH001", "广州一号仓", "王五", "020-999999", "广州市白云区", "正常", "1", "示例仓库"};
    }

    @Override
    protected List<ImportFieldMeta> fieldMetas() {
        return Arrays.asList(
                ImportFieldMeta.of("warehouseCode", "仓库编码", false),
                ImportFieldMeta.of("warehouseName", "仓库名称", true, "名称"),
                ImportFieldMeta.of("contact", "联系人", false),
                ImportFieldMeta.of("phone", "联系电话", false, "电话"),
                ImportFieldMeta.of("address", "仓库地址", false, "地址"),
                ImportFieldMeta.of("status", "状态", false, "状态(正常/停用)"),
                ImportFieldMeta.of("sort", "排序", false),
                ImportFieldMeta.of("remark", "备注", false)
        );
    }

    @Override
    public String templateFileName() {
        return "warehouse_import_template.xlsx";
    }
}
