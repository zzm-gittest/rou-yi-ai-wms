package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsStockCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存盘点单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsStockCheckMapper extends BaseMapper<WmsStockCheck> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     *
     * @param outNoPrefix 单号前缀，如 "PD20260207"
     * @param tenantId 租户ID
     * @return 最大单号
     */
    @Select("SELECT check_no FROM wms_stock_check WHERE check_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY check_no DESC LIMIT 1")
    String selectMaxCheckNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

}
