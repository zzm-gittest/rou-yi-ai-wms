package com.ruoyi.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.wms.domain.WmsImportTaskError;
import org.apache.ibatis.annotations.Mapper;

/**
 * 导入任务错误明细 Mapper
 */
@Mapper
public interface WmsImportTaskErrorMapper extends BaseMapper<WmsImportTaskError> {
}
