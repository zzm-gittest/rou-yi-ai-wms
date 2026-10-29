package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsSalesReturn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 销售退货单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsSalesReturnMapper extends BaseMapper<WmsSalesReturn> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     *
     * @param outNoPrefix 单号前缀，如 "XT20260207"
     * @param tenantId 租户ID
     * @return 最大单号，如 "XT202602070005"
     */
    @Select("SELECT return_no FROM wms_sales_return WHERE return_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY return_no DESC LIMIT 1")
    String selectMaxReturnNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

}
