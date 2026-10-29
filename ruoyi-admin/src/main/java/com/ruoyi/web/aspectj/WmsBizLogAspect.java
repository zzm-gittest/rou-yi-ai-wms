package com.ruoyi.web.aspectj;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.tenant.TenantHolder;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.wms.domain.WmsBizOperLog;
import com.ruoyi.wms.service.IWmsBizOperLogService;
import io.swagger.annotations.Api;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * WMS业务操作日志切面
 * 自动拦截所有 com.ruoyi.web.controller.wms 包下的写操作(POST/PUT/DELETE)
 */
@Aspect
@Component
public class WmsBizLogAspect {

    private static final Logger log = LoggerFactory.getLogger(WmsBizLogAspect.class);

    private static final ThreadLocal<Long> COST_TIME = new NamedThreadLocal<>("WmsBizLog-CostTime");

    private static final int PARAM_MAX_LENGTH = 2000;
    private static final int RESULT_MAX_LENGTH = 2000;
    private static final String[] BIZ_ID_FIELDS = {
        "id", "bizId", "billId", "orderId", "inId", "outId", "returnId", "transferId", "checkId"
    };
    private static final String[] BIZ_NO_FIELDS = {
        "bizNo", "billNo", "orderNo", "inNo", "outNo", "returnNo", "transferNo", "checkNo",
        "expenseNo", "taskNo", "warehouseCode", "goodsCode", "skuCode", "supplierCode",
        "customerCode", "accountCode", "reportCode", "taskCode", "conversationId", "sessionId",
        "operationType", "billType", "warehouseName", "goodsName", "skuName", "supplierName",
        "customerName", "accountName", "unitName", "categoryName", "typeName", "barcode"
    };

    /** 控制器类名 → 业务模块名 映射 */
    private static final Map<String, String[]> MODULE_MAP = new HashMap<>();
    static {
        MODULE_MAP.put("WmsPurchaseOrderController",   new String[]{"采购订单",      "wms_purchase_order"});
        MODULE_MAP.put("WmsPurchaseInController",       new String[]{"采购入库",      "wms_purchase_in"});
        MODULE_MAP.put("WmsPurchaseReturnController",   new String[]{"采购退货",      "wms_purchase_return"});
        MODULE_MAP.put("WmsSalesOrderController",       new String[]{"销售订单",      "wms_sales_order"});
        MODULE_MAP.put("WmsSalesOutController",         new String[]{"销售出库",      "wms_sales_out"});
        MODULE_MAP.put("WmsSalesReturnController",      new String[]{"销售退货",      "wms_sales_return"});
        MODULE_MAP.put("WmsStockTransferController",    new String[]{"调拨单",        "wms_stock_transfer"});
        MODULE_MAP.put("WmsStockCheckController",       new String[]{"盘点单",        "wms_stock_check"});
        MODULE_MAP.put("WmsWarehouseController",        new String[]{"仓库管理",      "wms_warehouse"});
        MODULE_MAP.put("WmsGoodsController",            new String[]{"商品管理",      "wms_goods"});
        MODULE_MAP.put("WmsGoodsSkuController",         new String[]{"SKU管理",       "wms_goods_sku"});
        MODULE_MAP.put("WmsGoodsCategoryController",    new String[]{"商品分类",      "wms_goods_category"});
        MODULE_MAP.put("WmsSupplierController",         new String[]{"供应商管理",    "wms_supplier"});
        MODULE_MAP.put("WmsCustomerController",         new String[]{"客户管理",      "wms_customer"});
        MODULE_MAP.put("WmsUnitController",             new String[]{"单位管理",      "wms_unit"});
        MODULE_MAP.put("WmsAccountController",          new String[]{"账户管理",      "wms_account"});
        MODULE_MAP.put("WmsExpenseController",          new String[]{"费用管理",      "wms_expense"});
        MODULE_MAP.put("WmsExpenseTypeController",      new String[]{"费用类型",      "wms_expense_type"});
        MODULE_MAP.put("WmsImportTaskController",       new String[]{"数据导入",      "wms_import_task"});
        MODULE_MAP.put("WmsBillApprovePolicyController", new String[]{"审核策略",     "wms_bill_approve_policy"});
        MODULE_MAP.put("WmsPcAiAssistantController",    new String[]{"PC端AI助手",    "wms_ai_chat_log"});
        MODULE_MAP.put("WmsAccountsReceivableController", new String[]{"应收账款",    "wms_accounts_receivable"});
        MODULE_MAP.put("WmsAccountsPayableController",  new String[]{"应付账款",      "wms_accounts_payable"});
        MODULE_MAP.put("WmsMobileAiController",         new String[]{"移动端AI助手",  "wms_ai_mobile"});
        MODULE_MAP.put("WmsAiController",               new String[]{"AI智能助手",    "wms_ai"});
        MODULE_MAP.put("WmsReportController",           new String[]{"报表中心",      "wms_report"});
        MODULE_MAP.put("WmsUserOnboardingController",   new String[]{"新手引导",      "wms_user_onboarding"});
        MODULE_MAP.put("WmsBasicImportController",      new String[]{"基础资料导入",  "wms_basic_import"});
        MODULE_MAP.put("WmsGoodsSpecController",        new String[]{"商品规格",      "wms_goods_spec"});
        MODULE_MAP.put("WmsStockController",            new String[]{"库存管理",      "wms_stock"});
        MODULE_MAP.put("WmsStockLogController",         new String[]{"库存流水",      "wms_stock_log"});
        MODULE_MAP.put("WmsStatisticsController",       new String[]{"经营统计",      "wms_statistics"});
        MODULE_MAP.put("WmsOcrController",              new String[]{"OCR识别",       "wms_ocr"});
        MODULE_MAP.put("WmsBizOperLogController",       new String[]{"业务操作日志",  "wms_biz_oper_log"});
        MODULE_MAP.put("WmsApproveLogController",       new String[]{"审批日志",      "wms_approve_log"});
        MODULE_MAP.put("WmsFinanceLogController",       new String[]{"财务流水",      "wms_finance_log"});
    }

    @Autowired
    private IWmsBizOperLogService wmsBizOperLogService;

    /**
     * 切入点：所有 wms controller 包下的 public 方法
     */
    @Pointcut("execution(public * com.ruoyi.web.controller.wms..*.*(..))")
    public void wmsControllerPointcut() {}

    @Before("wmsControllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        COST_TIME.set(System.currentTimeMillis());
    }

    @AfterReturning(pointcut = "wmsControllerPointcut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    @AfterThrowing(pointcut = "wmsControllerPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    private void handleLog(JoinPoint joinPoint, Exception e, Object jsonResult) {
        try {
            HttpServletRequest request = ServletUtils.getRequest();
            String httpMethod = request.getMethod();

            // 只记录写操作(POST/PUT/DELETE)，GET 查询不记录
            if ("GET".equalsIgnoreCase(httpMethod)) {
                return;
            }

            Class<?> targetClass = ClassUtils.getUserClass(joinPoint.getTarget());
            String className = targetClass.getSimpleName();
            String methodName = joinPoint.getSignature().getName();

            // 排除列表/分页等查询方法
            if (isQueryMethod(methodName)) {
                return;
            }

            WmsBizOperLog operLog = new WmsBizOperLog();

            // 获取业务模块和表名
            String[] moduleInfo = resolveModuleInfo(targetClass);
            operLog.setBizModule(moduleInfo[0]);
            operLog.setBizTable(moduleInfo[1]);

            // 推断操作类型
            operLog.setBizType(inferBizType(httpMethod, methodName));

            // 操作人信息
            try {
                LoginUser loginUser = SecurityUtils.getLoginUser();
                if (loginUser != null) {
                    operLog.setOperUserId(loginUser.getUserId());
                    operLog.setOperUserName(loginUser.getUsername());
                    operLog.setTenantId(TenantHolder.getTenantId());
                }
            } catch (Exception ignored) {
                operLog.setOperUserName("anonymous");
                operLog.setTenantId(TenantHolder.getTenantId());
            }

            operLog.setOperIp(IpUtils.getIpAddr());
            operLog.setOperUrl(StringUtils.substring(request.getRequestURI(), 0, 500));
            operLog.setOperMethod(httpMethod);
            operLog.setCreateBy(operLog.getOperUserName());
            operLog.setCreateTime(LocalDateTime.now());

            // 提取业务数据ID和单号
            extractBizIdAndNo(operLog, joinPoint, jsonResult, request);

            // 请求参数
            String params = getRequestParams(joinPoint, request);
            operLog.setOperParam(StringUtils.substring(params, 0, PARAM_MAX_LENGTH));

            // 返回结果
            if (jsonResult != null) {
                String result = JSON.toJSONString(jsonResult);
                operLog.setOperResult(StringUtils.substring(result, 0, RESULT_MAX_LENGTH));
            }

            // 状态
            if (e != null) {
                operLog.setStatus(1);
                operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            } else {
                operLog.setStatus(0);
            }

            // 耗时
            Long startTime = COST_TIME.get();
            if (startTime != null) {
                operLog.setCostTime(System.currentTimeMillis() - startTime);
            }

            // 异步写入日志
            wmsBizOperLogService.asyncInsertLog(operLog);
        } catch (Exception ex) {
            log.error("WMS业务日志记录异常", ex);
        } finally {
            COST_TIME.remove();
        }
    }

    /**
     * 推断业务操作类型
     */
    private String inferBizType(String httpMethod, String methodName) {
        String lowerMethod = methodName.toLowerCase();
        if (lowerMethod.contains("approve") && !lowerMethod.contains("cancel")) {
            return "APPROVE";
        }
        if (lowerMethod.contains("cancelapprove") || lowerMethod.contains("unapprove")) {
            return "CANCEL_APPROVE";
        }
        if (lowerMethod.contains("export")) {
            return "EXPORT";
        }
        if (lowerMethod.contains("import")) {
            return "IMPORT";
        }
        switch (httpMethod.toUpperCase()) {
            case "POST":
                return "CREATE";
            case "PUT":
                return "UPDATE";
            case "DELETE":
                return "DELETE";
            default:
                return "OTHER";
        }
    }

    /**
     * 是否是查询方法（排除记录）
     */
    private boolean isQueryMethod(String methodName) {
        String lower = methodName.toLowerCase();
        return lower.startsWith("get") || lower.startsWith("list") || lower.startsWith("page")
            || lower.startsWith("query") || lower.startsWith("search") || lower.startsWith("find")
            || lower.startsWith("count") || lower.startsWith("check") || lower.equals("detail")
            || lower.equals("info") || lower.startsWith("download") || lower.startsWith("template");
    }

    /**
     * 提取业务数据ID和业务单号
     */
    private void extractBizIdAndNo(WmsBizOperLog operLog, JoinPoint joinPoint, Object jsonResult,
                                   HttpServletRequest request) {
        Object[] args = joinPoint.getArgs();
        if (args != null) {
            for (Object arg : args) {
                if (shouldSkipArg(arg)) {
                    continue;
                }
                extractFromSource(operLog, arg);
                if (operLog.getBizId() != null && StringUtils.isNotEmpty(operLog.getBizNo())) {
                    break;
                }
            }
        }

        if (jsonResult instanceof AjaxResult) {
            Object data = ((AjaxResult) jsonResult).get(AjaxResult.DATA_TAG);
            if (data instanceof Number && operLog.getBizId() == null) {
                operLog.setBizId(((Number) data).longValue());
            } else {
                extractFromSource(operLog, data);
            }
        }

        if (StringUtils.isEmpty(operLog.getBizNo()) && operLog.getBizId() != null) {
            String resolvedBizNo = wmsBizOperLogService.resolveBizNo(operLog.getBizTable(), operLog.getBizId());
            if (StringUtils.isNotEmpty(resolvedBizNo)) {
                operLog.setBizNo(resolvedBizNo);
            }
        }

        if (StringUtils.isEmpty(operLog.getBizNo())) {
            String urlBizNo = extractBizNoFromUrl(request == null ? null : request.getRequestURI());
            if (StringUtils.isNotEmpty(urlBizNo)) {
                operLog.setBizNo(urlBizNo);
            }
        }

        if (StringUtils.isEmpty(operLog.getBizNo())) {
            operLog.setBizNo(buildFallbackBizNo(operLog, request, joinPoint.getSignature().getName()));
        }
    }

    /**
     * 获取控制器模块和业务表标识
     */
    private String[] resolveModuleInfo(Class<?> targetClass) {
        String[] mapped = MODULE_MAP.get(targetClass.getSimpleName());
        if (mapped != null) {
            return mapped;
        }

        String moduleName = resolveModuleName(targetClass);
        String bizTable = resolveBizTable(targetClass);
        if (StringUtils.isEmpty(moduleName)) {
            moduleName = targetClass.getSimpleName().replace("Controller", "");
        }
        if (StringUtils.isEmpty(bizTable)) {
            bizTable = toSnakeCase(targetClass.getSimpleName().replace("Controller", ""));
        }
        return new String[]{moduleName, bizTable};
    }

    private String resolveModuleName(Class<?> targetClass) {
        Api api = targetClass.getAnnotation(Api.class);
        if (api != null) {
            for (String tag : api.tags()) {
                if (StringUtils.isNotEmpty(tag)) {
                    return tag;
                }
            }
            if (StringUtils.isNotEmpty(api.value())) {
                return api.value();
            }
        }
        return null;
    }

    private String resolveBizTable(Class<?> targetClass) {
        RequestMapping requestMapping = targetClass.getAnnotation(RequestMapping.class);
        if (requestMapping != null) {
            String[] paths = requestMapping.path().length > 0 ? requestMapping.path() : requestMapping.value();
            for (String path : paths) {
                if (StringUtils.isNotEmpty(path)) {
                    String normalized = path.trim().replace('\\', '/');
                    while (normalized.startsWith("/")) {
                        normalized = normalized.substring(1);
                    }
                    while (normalized.endsWith("/")) {
                        normalized = normalized.substring(0, normalized.length() - 1);
                    }
                    normalized = normalized.replace('/', '_').replace('-', '_');
                    normalized = normalized.replaceAll("_+", "_");
                    if (StringUtils.isNotEmpty(normalized)) {
                        return normalized.toLowerCase(Locale.ROOT);
                    }
                }
            }
        }
        return null;
    }

    private void extractFromSource(WmsBizOperLog operLog, Object source) {
        if (source == null) {
            return;
        }

        if (source instanceof Number) {
            if (operLog.getBizId() == null) {
                operLog.setBizId(((Number) source).longValue());
            }
            return;
        }

        if (source instanceof Map<?, ?> map) {
            extractFromMap(operLog, map);
            return;
        }

        if (source instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                extractFromSource(operLog, item);
                if (operLog.getBizId() != null && StringUtils.isNotEmpty(operLog.getBizNo())) {
                    return;
                }
            }
            return;
        }

        if (operLog.getBizId() == null) {
            for (String field : BIZ_ID_FIELDS) {
                Long value = toLong(getPropertyValue(source, field));
                if (value != null) {
                    operLog.setBizId(value);
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(operLog.getBizNo())) {
            for (String field : BIZ_NO_FIELDS) {
                String value = toText(getPropertyValue(source, field));
                if (StringUtils.isNotEmpty(value)) {
                    operLog.setBizNo(value);
                    break;
                }
            }
        }
    }

    private void extractFromMap(WmsBizOperLog operLog, Map<?, ?> map) {
        if (operLog.getBizId() == null) {
            for (String field : BIZ_ID_FIELDS) {
                Long value = toLong(map.get(field));
                if (value != null) {
                    operLog.setBizId(value);
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(operLog.getBizNo())) {
            for (String field : BIZ_NO_FIELDS) {
                String value = toText(map.get(field));
                if (StringUtils.isNotEmpty(value)) {
                    operLog.setBizNo(value);
                    break;
                }
            }
        }
    }

    private boolean shouldSkipArg(Object arg) {
        return arg == null || arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
            || arg instanceof BindingResult || arg instanceof MultipartFile;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String text && text.matches("^-?\\d+$")) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String toText(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Object getPropertyValue(Object obj, String fieldName) {
        try {
            String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
            Method getter = obj.getClass().getMethod(getterName);
            return getter.invoke(obj);
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractBizNoFromUrl(String uri) {
        if (StringUtils.isEmpty(uri)) {
            return null;
        }
        String normalized = uri;
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String lastSegment = StringUtils.substringAfterLast(normalized, "/");
        if (StringUtils.isEmpty(lastSegment)) {
            return null;
        }
        String lower = lastSegment.toLowerCase(Locale.ROOT);
        if ("wms".equals(lower) || "ai".equals(lower) || "mobile".equals(lower)
            || "chat".equals(lower) || "start".equals(lower) || "approve".equals(lower)
            || "query".equals(lower) || "complete".equals(lower) || "context".equals(lower)
            || "history".equals(lower) || "voicetotext".equals(lower)) {
            return null;
        }
        return lastSegment;
    }

    private String buildFallbackBizNo(WmsBizOperLog operLog, HttpServletRequest request, String methodName) {
        if (operLog.getBizId() != null) {
            return operLog.getBizTable() + "#" + operLog.getBizId();
        }
        String urlBizNo = extractBizNoFromUrl(request == null ? null : request.getRequestURI());
        if (StringUtils.isNotEmpty(urlBizNo)) {
            return operLog.getBizTable() + "#" + urlBizNo;
        }
        return operLog.getBizTable() + "#" + methodName;
    }

    private String toSnakeCase(String text) {
        if (StringUtils.isEmpty(text)) {
            return text;
        }
        String value = text.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
            .toLowerCase(Locale.ROOT);
        return value.startsWith("wms_") ? value : "wms_" + value;
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint, HttpServletRequest request) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg == null || arg instanceof HttpServletRequest || arg instanceof HttpServletResponse
                || arg instanceof BindingResult || arg instanceof MultipartFile) {
                continue;
            }
            try {
                String json = JSON.toJSONString(arg);
                if (sb.length() > 0) {
                    sb.append(" | ");
                }
                sb.append(json);
            } catch (Exception ignored) {
            }
        }
        return sb.toString();
    }
}
