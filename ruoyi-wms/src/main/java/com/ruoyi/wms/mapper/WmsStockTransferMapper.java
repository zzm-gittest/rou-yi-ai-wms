package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsStockTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 库存调拨单表 Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface WmsStockTransferMapper extends BaseMapper<WmsStockTransfer> {

    /**
     * 查询指定前缀的最大单号（包括已删除记录）
     *
     * @param outNoPrefix 单号前缀，如 "DB20260207"
     * @param tenantId 租户ID
     * @return 最大单号
     */
    @Select("SELECT transfer_no FROM wms_stock_transfer WHERE transfer_no LIKE CONCAT(#{prefix}, '%') AND tenant_id = #{tenantId} ORDER BY transfer_no DESC LIMIT 1")
    String selectMaxTransferNoIncludingDeleted(@Param("prefix") String outNoPrefix, @Param("tenantId") Long tenantId);

}
