package com.ruoyi.wms.service.basicImport.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.service.basicImport.ImportFieldMeta;
import com.ruoyi.wms.domain.vo.supplier.WmsSupplierSaveReqVO;
import com.ruoyi.wms.mapper.WmsSupplierMapper;
import com.ruoyi.wms.service.WmsSupplierService;
import com.ruoyi.wms.service.basicImport.AbstractSimpleExcelImportHandler;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class WmsSupplierImportHandler extends AbstractSimpleExcelImportHandler {

    private final WmsSupplierService supplierService;
    private final WmsSupplierMapper supplierMapper;

    public WmsSupplierImportHandler(WmsSupplierService supplierService, WmsSupplierMapper supplierMapper) {
        this.supplierService = supplierService;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public String getType() {
        return "supplier";
    }

    @Override
    protected String validateAndSave(Row row, int excelRowNum) {
        String supplierCode = cellString(row, "supplierCode");
        String supplierName = cellString(row, "supplierName");
        String contact = cellString(row, "contact");
        String phone = cellString(row, "phone");
        String mobile = cellString(row, "mobile");
        String address = cellString(row, "address");
        String status = normalizeStatus(cellString(row, "status"));
        String remark = cellString(row, "remark");

        if (StringUtils.isEmpty(supplierName)) {
            return "供应商名称不能为空";
        }

        if (StringUtils.isNotEmpty(supplierCode)) {
            Long codeCount = supplierMapper.selectCount(new LambdaQueryWrapper<WmsSupplier>()
                    .eq(WmsSupplier::getSupplierCode, supplierCode.trim()));
            if (codeCount != null && codeCount > 0) {
                return "供应商编码已存在: " + supplierCode;
            }
        }

        Long nameCount = supplierMapper.selectCount(new LambdaQueryWrapper<WmsSupplier>()
                .eq(WmsSupplier::getSupplierName, supplierName.trim()));
        if (nameCount != null && nameCount > 0) {
            return "供应商名称已存在: " + supplierName;
        }

        WmsSupplierSaveReqVO reqVO = new WmsSupplierSaveReqVO();
        reqVO.setSupplierCode(StringUtils.isEmpty(supplierCode) ? null : supplierCode.trim());
        reqVO.setSupplierName(supplierName.trim());
        reqVO.setContact(contact);
        reqVO.setPhone(phone);
        reqVO.setMobile(mobile);
        reqVO.setAddress(address);
        reqVO.setStatus(status);
        reqVO.setRemark(remark);
        reqVO.setCreditLimit(BigDecimal.ZERO);
        reqVO.setSettlementType("1");
        reqVO.setPaymentDays(0);
        supplierService.create(reqVO);
        return null;
    }

    @Override
    protected String[] headers() {
        return new String[] {"供应商编码", "供应商名称*", "联系人", "联系电话", "手机号", "地址", "状态(正常/停用)", "备注"};
    }

    @Override
    protected String[] sampleRow() {
        return new String[] {"SUP001", "华南供应链", "张三", "020-888888", "13800138000", "广州市天河区", "正常", "示例供应商"};
    }

    @Override
    protected List<ImportFieldMeta> fieldMetas() {
        return Arrays.asList(
                ImportFieldMeta.of("supplierCode", "供应商编码", false),
                ImportFieldMeta.of("supplierName", "供应商名称", true, "名称"),
                ImportFieldMeta.of("contact", "联系人", false),
                ImportFieldMeta.of("phone", "联系电话", false, "电话"),
                ImportFieldMeta.of("mobile", "手机号", false, "手机"),
                ImportFieldMeta.of("address", "地址", false),
                ImportFieldMeta.of("status", "状态", false, "状态(正常/停用)"),
                ImportFieldMeta.of("remark", "备注", false)
        );
    }

    @Override
    public String templateFileName() {
        return "supplier_import_template.xlsx";
    }
}
