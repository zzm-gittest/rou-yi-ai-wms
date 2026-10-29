package com.ruoyi.wms.service.basicImport.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.wms.domain.WmsCustomer;
import com.ruoyi.wms.service.basicImport.ImportFieldMeta;
import com.ruoyi.wms.domain.vo.customer.WmsCustomerSaveReqVO;
import com.ruoyi.wms.mapper.WmsCustomerMapper;
import com.ruoyi.wms.service.WmsCustomerService;
import com.ruoyi.wms.service.basicImport.AbstractSimpleExcelImportHandler;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class WmsCustomerImportHandler extends AbstractSimpleExcelImportHandler {

    private final WmsCustomerService customerService;
    private final WmsCustomerMapper customerMapper;

    public WmsCustomerImportHandler(WmsCustomerService customerService, WmsCustomerMapper customerMapper) {
        this.customerService = customerService;
        this.customerMapper = customerMapper;
    }

    @Override
    public String getType() {
        return "customer";
    }

    @Override
    protected String validateAndSave(Row row, int excelRowNum) {
        String customerCode = cellString(row, "customerCode");
        String customerName = cellString(row, "customerName");
        String customerType = cellString(row, "customerType");
        String contact = cellString(row, "contact");
        String phone = cellString(row, "phone");
        String mobile = cellString(row, "mobile");
        String address = cellString(row, "address");
        String status = normalizeStatus(cellString(row, "status"));
        String remark = cellString(row, "remark");

        if (StringUtils.isEmpty(customerName)) {
            return "客户名称不能为空";
        }

        if (StringUtils.isNotEmpty(customerCode)) {
            Long codeCount = customerMapper.selectCount(new LambdaQueryWrapper<WmsCustomer>()
                    .eq(WmsCustomer::getCustomerCode, customerCode.trim()));
            if (codeCount != null && codeCount > 0) {
                return "客户编码已存在: " + customerCode;
            }
        }

        Long nameCount = customerMapper.selectCount(new LambdaQueryWrapper<WmsCustomer>()
                .eq(WmsCustomer::getCustomerName, customerName.trim()));
        if (nameCount != null && nameCount > 0) {
            return "客户名称已存在: " + customerName;
        }

        String normalizedType = "1".equals(customerType) || "批发".equals(customerType) ? "1" : "2";

        WmsCustomerSaveReqVO reqVO = new WmsCustomerSaveReqVO();
        reqVO.setCustomerCode(StringUtils.isEmpty(customerCode) ? null : customerCode.trim());
        reqVO.setCustomerName(customerName.trim());
        reqVO.setCustomerType(normalizedType);
        reqVO.setContact(contact);
        reqVO.setPhone(phone);
        reqVO.setMobile(mobile);
        reqVO.setAddress(address);
        reqVO.setStatus(status);
        reqVO.setRemark(remark);
        reqVO.setCreditLimit(BigDecimal.ZERO);
        reqVO.setSettlementType("1");
        reqVO.setPaymentDays(0);
        customerService.create(reqVO);
        return null;
    }

    @Override
    protected String[] headers() {
        return new String[] {"客户编码", "客户名称*", "客户类型(批发/零售)", "联系人", "联系电话", "手机号", "地址", "状态(正常/停用)", "备注"};
    }

    @Override
    protected String[] sampleRow() {
        return new String[] {"CUS001", "华东门店", "零售", "李四", "021-666666", "13900139000", "上海市浦东新区", "正常", "示例客户"};
    }

    @Override
    protected List<ImportFieldMeta> fieldMetas() {
        return Arrays.asList(
                ImportFieldMeta.of("customerCode", "客户编码", false),
                ImportFieldMeta.of("customerName", "客户名称", true, "名称"),
                ImportFieldMeta.of("customerType", "客户类型", false, "客户类型(批发/零售)"),
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
        return "customer_import_template.xlsx";
    }
}
