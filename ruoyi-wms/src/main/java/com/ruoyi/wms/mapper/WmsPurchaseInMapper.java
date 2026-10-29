package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsPurchaseIn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 采购入库单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsPurchaseInMapper extends BaseMapper<WmsPurchaseIn> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     *
     * @param outNoPrefix 单号前缀，如 "RK20260207"
     * @param tenantId 租户ID
     * @return 最大单号，如 "RK202602070005"
     */
    @Select("SELECT in_no FROM wms_purchase_in WHERE in_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY in_no DESC LIMIT 1")
    String selectMaxInNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

    @Select("SELECT p.id, p.in_no, p.supplier_id, p.warehouse_id, p.in_date, p.total_amount, p.paid_amount, p.handler, p.approve_by, p.return_status, p.remark, "
            + "s.supplier_name, w.warehouse_name "
            + "FROM wms_purchase_in p "
            + "LEFT JOIN wms_supplier s ON p.supplier_id = s.id "
            + "LEFT JOIN wms_warehouse w ON p.warehouse_id = w.id "
            + "WHERE p.del_flag = '0' AND p.approve_by IS NOT NULL "
            + "AND (p.return_status IS NULL OR p.return_status IN ('0','1')) "
            + "AND (p.in_no LIKE CONCAT('%', #{keyword}, '%') OR s.supplier_name LIKE CONCAT('%', #{keyword}, '%')) "
            + "ORDER BY p.approve_time DESC "
            + "LIMIT 20")
    List<Map<String, Object>> searchAvailableForReturn(@Param("keyword") String keyword);

}
