package com.ruoyi.wms.mapper;

import com.ruoyi.common.core.domain.entity.SysTenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WmsTenantPrintProfileMapper {

    @Select("SELECT tenant_id, tenant_name, print_company_name, print_company_address, print_company_phone, print_logo_path, print_font_scale, print_page_size "
        + "FROM sys_tenant WHERE tenant_id = #{tenantId} AND del_flag = '0' LIMIT 1")
    SysTenant selectByTenantId(@Param("tenantId") Long tenantId);
}