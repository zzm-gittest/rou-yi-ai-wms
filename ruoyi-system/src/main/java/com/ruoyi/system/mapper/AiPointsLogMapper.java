package com.ruoyi.system.mapper;

import com.ruoyi.common.core.domain.entity.AiPointsLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * AI点数流水 数据层
 *
 * @author ruoyi
 */
public interface AiPointsLogMapper
{
    /**
     * 插入点数流水记录
     *
     * @param log 流水记录
     * @return 结果
     */
    public int insertLog(AiPointsLog log);

    /**
     * 查询租户点数流水
     *
     * @param tenantId 租户ID
     * @param limit 返回条数
     * @return 流水列表
     */
    public List<AiPointsLog> selectLogsByTenantId(@Param("tenantId") Long tenantId, @Param("limit") int limit);

    /**
     * 按业务ID查询记录数（用于防重复交易）
     *
     * @param bizId 业务ID
     * @return 记录数
     */
    public int countByBizId(@Param("bizId") String bizId);
}
