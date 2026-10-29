package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsSalesOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 销售订单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsSalesOrderMapper extends BaseMapper<WmsSalesOrder> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     *
     * @param outNoPrefix 单号前缀，如 "XS20260207"
     * @param tenantId 租户ID
     * @return 最大单号，如 "XS202602070005"
     */
    @Select("SELECT order_no FROM wms_sales_order WHERE order_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY order_no DESC LIMIT 1")
    String selectMaxOrderNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

    /**
     * 按关键字搜索可出库的已审核销售订单（已审核或部分发货）
     *
     * @param keyword 关键字（匹配订单号或客户名称）
     * @return 最多20条候选记录
     */
    @Select("SELECT o.id, o.order_no, o.customer_id, o.order_date, o.total_amount, o.order_status, "
            + "c.customer_name "
            + "FROM wms_sales_order o "
            + "LEFT JOIN wms_customer c ON o.customer_id = c.id "
            + "WHERE o.del_flag = '0' "
            + "AND o.order_status IN ('1', '2') "
            + "AND (o.order_no LIKE CONCAT('%', #{keyword}, '%') OR c.customer_name LIKE CONCAT('%', #{keyword}, '%')) "
            + "ORDER BY o.create_time DESC "
            + "LIMIT 20")
    List<Map<String, Object>> searchAvailableForOut(@Param("keyword") String keyword);

}
