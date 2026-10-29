package com.ruoyi.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.PageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.wms.domain.WmsImportTask;
import com.ruoyi.wms.domain.WmsImportTaskError;
import com.ruoyi.wms.domain.vo.goods.ImportError;
import com.ruoyi.wms.domain.vo.goods.ImportResult;
import com.ruoyi.wms.domain.vo.importTask.CreateImportTaskReqVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskErrorRespVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskPageReqVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskReportRespVO;
import com.ruoyi.wms.domain.vo.importTask.ImportTaskRespVO;
import com.ruoyi.wms.mapper.WmsImportTaskErrorMapper;
import com.ruoyi.wms.mapper.WmsImportTaskMapper;
import com.ruoyi.wms.service.WmsBasicDataImportService;
import com.ruoyi.wms.service.WmsImportTaskService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 导入任务 Service实现
 */
@Service
public class WmsImportTaskServiceImpl extends ServiceImpl<WmsImportTaskMapper, WmsImportTask> implements WmsImportTaskService {

    private static final Set<String> SUPPORTED_TYPES = new HashSet<>(Arrays.asList(
            "unit", "category", "goods", "supplier", "customer", "warehouse", "purchase_in", "sales_out"
    ));
    private static final int ERROR_MSG_DB_MAX_LEN = 255;
    private static final String ERROR_MSG_TRUNCATE_SUFFIX = "...(已截断)";

    private final WmsImportTaskErrorMapper taskErrorMapper;
    private final WmsBasicDataImportService basicDataImportService;

    public WmsImportTaskServiceImpl(WmsImportTaskErrorMapper taskErrorMapper,
                                    WmsBasicDataImportService basicDataImportService) {
        this.taskErrorMapper = taskErrorMapper;
        this.basicDataImportService = basicDataImportService;
    }

    @Override
    public ImportTaskRespVO createTask(CreateImportTaskReqVO reqVO) {
        String taskType = normalizeType(reqVO.getTaskType());
        if (!SUPPORTED_TYPES.contains(taskType)) {
            throw new ServiceException("不支持的任务类型: " + reqVO.getTaskType());
        }

        WmsImportTask task = new WmsImportTask();
        task.setTenantId(getCurrentTenantId());
        task.setTaskNo(generateTaskNo());
        task.setTaskType(taskType);
        task.setStatus(0);
        task.setFileUrl(null);
        task.setTotalRows(0);
        task.setSuccessRows(0);
        task.setFailRows(0);
        task.setStrategyJson(reqVO.getStrategyJson());
        task.setCreateBy(getCurrentUsername());
        task.setCreateTime(LocalDateTime.now());
        task.setEndedTime(null);

        save(task);
        return toResp(task);
    }

    @Override
    public IPage<ImportTaskRespVO> page(ImportTaskPageReqVO reqVO) {
        LambdaQueryWrapper<WmsImportTask> wrapper = new LambdaQueryWrapper<WmsImportTask>()
                .like(StringUtils.isNotEmpty(reqVO.getTaskNo()), WmsImportTask::getTaskNo, reqVO.getTaskNo())
                .eq(StringUtils.isNotEmpty(reqVO.getTaskType()), WmsImportTask::getTaskType, normalizeType(reqVO.getTaskType()))
                .eq(reqVO.getStatus() != null, WmsImportTask::getStatus, reqVO.getStatus())
                .orderByDesc(WmsImportTask::getCreateTime);

        Page<WmsImportTask> page = page(PageUtils.getPage(reqVO.getPageNo(), reqVO.getPageSize()), wrapper);

        Page<ImportTaskRespVO> resultPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ImportTaskRespVO> rows = new ArrayList<>();
        for (WmsImportTask task : page.getRecords()) {
            rows.add(toResp(task));
        }
        resultPage.setRecords(rows);
        return resultPage;
    }

    @Override
    public ImportTaskReportRespVO preview(Long taskId, MultipartFile file, String goodsTemplateType, String mappingJson) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }

        WmsImportTask task = getByIdWithCheck(taskId);
        task.setStatus(1);
        updateById(task);

        String uploadDir = RuoYiConfig.getUploadPath() + "/import-task";
        String fileUrl = FileUploadUtils.upload(uploadDir, file);
        task.setFileUrl(fileUrl);
        task.setStrategyJson(mappingJson);

        ImportResult importResult = executeImport(task, file, goodsTemplateType);
        ImportTaskReportRespVO report = report(taskId);
        if (importResult != null && importResult.getBillOutNoMap() != null) {
            report.setBillOutNoMap(importResult.getBillOutNoMap());
        }
        return report;
    }

    @Override
    public ImportTaskReportRespVO execute(Long taskId) {
        WmsImportTask task = getByIdWithCheck(taskId);
        if (task.getStatus() != null && task.getStatus() == 0) {
            throw new ServiceException("请先上传文件并执行预览");
        }
        return report(taskId);
    }

    @Override
    public ImportTaskReportRespVO retry(Long taskId, MultipartFile file, String goodsTemplateType, String mappingJson) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("重跑文件不能为空");
        }

        WmsImportTask task = getByIdWithCheck(taskId);
        task.setStatus(1);
        updateById(task);

        String uploadDir = RuoYiConfig.getUploadPath() + "/import-task";
        String fileUrl = FileUploadUtils.upload(uploadDir, file);
        task.setFileUrl(fileUrl);
        task.setStrategyJson(mappingJson);

        ImportResult importResult = executeImport(task, file, goodsTemplateType);
        ImportTaskReportRespVO report = report(taskId);
        if (importResult != null && importResult.getBillOutNoMap() != null) {
            report.setBillOutNoMap(importResult.getBillOutNoMap());
        }
        return report;
    }

    @Override
    public List<String> parseHeaders(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }

        List<String> headers = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return headers;
            }
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return headers;
            }
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                String value = StringUtils.trimToEmpty(headerRow.getCell(i) == null ? "" : headerRow.getCell(i).toString());
                if (StringUtils.isNotEmpty(value)) {
                    headers.add(value);
                }
            }
        }
        return headers;
    }

    @Override
    public ImportTaskReportRespVO report(Long taskId) {
        WmsImportTask task = getByIdWithCheck(taskId);

        List<WmsImportTaskError> taskErrors = taskErrorMapper.selectList(new LambdaQueryWrapper<WmsImportTaskError>()
                .eq(WmsImportTaskError::getTaskId, taskId)
                .orderByAsc(WmsImportTaskError::getRowNum));

        List<ImportTaskErrorRespVO> errors = new ArrayList<>();
        for (WmsImportTaskError taskError : taskErrors) {
            ImportTaskErrorRespVO item = new ImportTaskErrorRespVO();
            item.setRowNum(taskError.getRowNum());
            item.setErrorMsg(taskError.getErrorMsg());
            item.setRawDataJson(taskError.getRawDataJson());
            errors.add(item);
        }

        ImportTaskReportRespVO report = new ImportTaskReportRespVO();
        report.setTask(toResp(task));
        report.setErrors(errors);
        report.setSuggestFix(buildSuggestFix(task));
        return report;
    }

    private ImportResult executeImport(WmsImportTask task, MultipartFile file, String goodsTemplateType) throws Exception {
        String type = normalizeType(task.getTaskType());
        ImportResult result;

        if (!"goods".equals(type) && StringUtils.isNotEmpty(goodsTemplateType)) {
            goodsTemplateType = null;
        }

        result = basicDataImportService.importByType(type, file, task.getStrategyJson());

        int total = result.getTotal();
        int success = result.getSuccessCount();
        int fail = result.getFailCount();

        task.setTotalRows(total);
        task.setSuccessRows(success);
        task.setFailRows(fail);
        task.setEndedTime(LocalDateTime.now());
        task.setStatus(calcStatus(success, fail));
        updateById(task);

        replaceErrors(task, result.getErrors());
        return result;
    }

    private void replaceErrors(WmsImportTask task, List<ImportError> errors) {
        taskErrorMapper.delete(new LambdaQueryWrapper<WmsImportTaskError>()
                .eq(WmsImportTaskError::getTaskId, task.getId()));

        if (errors == null || errors.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (ImportError error : errors) {
            WmsImportTaskError entity = new WmsImportTaskError();
            entity.setTenantId(task.getTenantId());
            entity.setTaskId(task.getId());
            entity.setRowNum(error.getRowNumber());
            entity.setErrorCode("IMPORT_ERROR");
            entity.setErrorMsg(safeErrorMessage(error.getErrorMessage()));
            entity.setRawDataJson(error.getRawData());
            entity.setCreateTime(now);
            taskErrorMapper.insert(entity);
        }
    }

    private String safeErrorMessage(String errorMessage) {
        String msg = StringUtils.defaultIfEmpty(errorMessage, "导入失败").trim();
        if (msg.length() <= ERROR_MSG_DB_MAX_LEN) {
            return msg;
        }

        int keepLen = ERROR_MSG_DB_MAX_LEN - ERROR_MSG_TRUNCATE_SUFFIX.length();
        if (keepLen <= 0) {
            return msg.substring(0, ERROR_MSG_DB_MAX_LEN);
        }
        return msg.substring(0, keepLen) + ERROR_MSG_TRUNCATE_SUFFIX;
    }

    private Integer calcStatus(int success, int fail) {
        if (fail <= 0) {
            return 2;
        }
        if (success > 0) {
            return 3;
        }
        return 4;
    }

    private String buildSuggestFix(WmsImportTask task) {
        if (task.getStatus() == null) {
            return "请先上传文件执行预览";
        }
        if (task.getStatus() == 2) {
            return "导入完成，无需修复";
        }
        if (task.getStatus() == 3) {
            return "存在失败行，请根据错误明细修正后使用失败重跑";
        }
        if (task.getStatus() == 4) {
            return "全部行失败，请检查模板、必填字段及主数据依赖后重跑";
        }
        return "任务处理中";
    }

    private String generateTaskNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prefix = "IMP" + datePart;

        long count = lambdaQuery()
                .likeRight(WmsImportTask::getTaskNo, prefix)
                .count();

        return prefix + String.format("%04d", count + 1);
    }

    private WmsImportTask getByIdWithCheck(Long taskId) {
        WmsImportTask task = getById(taskId);
        if (task == null) {
            throw new ServiceException("导入任务不存在");
        }

        Long tenantId = getCurrentTenantId();
        if (tenantId != null && task.getTenantId() != null && !tenantId.equals(task.getTenantId())) {
            throw new ServiceException("无权访问该导入任务");
        }
        return task;
    }

    private ImportTaskRespVO toResp(WmsImportTask task) {
        ImportTaskRespVO resp = new ImportTaskRespVO();
        resp.setId(task.getId());
        resp.setTaskNo(task.getTaskNo());
        resp.setTaskType(task.getTaskType());
        resp.setStatus(task.getStatus());
        resp.setStatusLabel(statusLabel(task.getStatus()));
        resp.setFileUrl(task.getFileUrl());
        resp.setTotalRows(defaultInt(task.getTotalRows()));
        resp.setSuccessRows(defaultInt(task.getSuccessRows()));
        resp.setFailRows(defaultInt(task.getFailRows()));
        resp.setStrategyJson(task.getStrategyJson());
        resp.setCreateBy(task.getCreateBy());
        resp.setCreateTime(task.getCreateTime());
        resp.setEndedTime(task.getEndedTime());
        return resp;
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String statusLabel(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待执行";
            case 1:
                return "执行中";
            case 2:
                return "完成";
            case 3:
                return "部分失败";
            case 4:
                return "失败";
            default:
                return "未知";
        }
    }

    private Long getCurrentTenantId() {
        try {
            return SecurityUtils.getTenantId();
        } catch (Exception ex) {
            return null;
        }
    }

    private String getCurrentUsername() {
        try {
            return SecurityUtils.getUsername();
        } catch (Exception ex) {
            return "system";
        }
    }

    private String normalizeType(String type) {
        if (StringUtils.isEmpty(type)) {
            return "";
        }
        return type.trim().toLowerCase();
    }
}
