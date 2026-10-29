package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsExpense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 费用单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsExpenseMapper extends BaseMapper<WmsExpense> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     *
     * @param outNoPrefix 单号前缀，如 "FY20260207"
     * @param tenantId 租户ID
     * @return 最大单号
     */
    @Select("SELECT expense_no FROM wms_expense WHERE expense_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY expense_no DESC LIMIT 1")
    String selectMaxExpenseNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

}
