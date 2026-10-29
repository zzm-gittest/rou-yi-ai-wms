package com.ruoyi.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.wms.domain.vo.importTask.CreateImportTaskReqVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskPageReqVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskReportRespVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskRespVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 导入任务 Service
 */
public interface WmsImportTaskService {

    ImportTaskRespVO createTask(CreateImportTaskReqVO reqVO);

    IPage<ImportTaskRespVO> page(ImportTaskPageReqVO reqVO);

    ImportTaskReportRespVO preview(Long taskId, MultipartFile file, String goodsTemplateType, String mappingJson) throws Exception;

    ImportTaskReportRespVO execute(Long taskId) throws Exception;

    ImportTaskReportRespVO retry(Long taskId, MultipartFile file, String goodsTemplateType, String mappingJson) throws Exception;

    List<String> parseHeaders(MultipartFile file) throws Exception;

    ImportTaskReportRespVO report(Long taskId);
}
