package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.WmsBizOperLog;

/**
 * WMS业务操作日志 Service
 */
public interface IWmsBizOperLogService {

    /**
     * 异步插入日志
     */
    void asyncInsertLog(WmsBizOperLog log);

    /**
     * 根据业务表和业务ID解析业务标识
     */
    String resolveBizNo(String bizTable, Long bizId);

    /**
     * 分页查询日志
     */
    IPage<WmsBizOperLog> page(Long tenantId, String bizModule, String bizType,
                               String operUserName, String bizNo,
                               String beginTime, String endTime,
                               int pageNo, int pageSize);
}
