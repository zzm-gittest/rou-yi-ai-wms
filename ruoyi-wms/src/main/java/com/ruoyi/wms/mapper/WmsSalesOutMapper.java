package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsSalesOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 销售出库单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsSalesOutMapper extends BaseMapper<WmsSalesOut> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     * 用于生成新单号时避免与已删除记录的单号冲突
     *
     * @param outNoPrefix 单号前缀，如 "CK20260207"
     * @param tenantId 租户ID
     * @return 最大单号，如 "CK202602070005"
     */
    @Select("SELECT out_no FROM wms_sales_out WHERE out_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY out_no DESC LIMIT 1")
    String selectMaxOutNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

    /**
     * 按关键字搜索可退货的销售出库单（已审核且未全部退货）
     *
     * @param keyword 搜索关键字（出库单号或客户名称）
     * @return 匹配的出库单列表
     */
    @Select("SELECT o.id, o.out_no, o.customer_id, o.warehouse_id, o.out_date, o.total_amount, "
            + "o.received_amount, o.handler, o.approve_by, o.return_status, o.remark, "
            + "c.customer_name, w.warehouse_name "
            + "FROM wms_sales_out o "
            + "LEFT JOIN wms_customer c ON o.customer_id = c.id "
            + "LEFT JOIN wms_warehouse w ON o.warehouse_id = w.id "
            + "WHERE o.del_flag = '0' AND o.approve_by IS NOT NULL "
            + "AND (o.return_status IS NULL OR o.return_status IN ('0','1')) "
            + "AND (o.out_no LIKE CONCAT('%', #{keyword}, '%') OR c.customer_name LIKE CONCAT('%', #{keyword}, '%')) "
            + "ORDER BY o.approve_time DESC "
            + "LIMIT 20")
    List<Map<String, Object>> searchAvailableForReturn(@Param("keyword") String keyword);

}
