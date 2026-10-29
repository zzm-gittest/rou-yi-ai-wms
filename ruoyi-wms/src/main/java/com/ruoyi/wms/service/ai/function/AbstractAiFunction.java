package com.ruoyi.wms.service.ai.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * AI函数抽象基类
 *
 * @author ruoyi
 */
@Slf4j
public abstract class AbstractAiFunction {

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 获取函数定义
     */
    public abstract AiFunction getFunctionDefinition();

    /**
     * 执行函数
     *
     * @param parameters 参数
     * @param userId     用户ID
     * @return 执行结果
     */
    public abstract FunctionCallResult execute(Map<String, Object> parameters, Long userId);

    /**
     * 验证参数
     *
     * @param parameters 参数
     * @return 验证结果
     */
    public ToolCallService.ParameterValidationResult validate(Map<String, Object> parameters) {
        AiFunction definition = getFunctionDefinition();
        List<String> required = definition.getParameters().getRequired();

        if (required == null || required.isEmpty()) {
            return ToolCallService.ParameterValidationResult.valid();
        }

        List<String> missing = new ArrayList<>();
        for (String field : required) {
            Object value = parameters.get(field);
            if (value == null || (value instanceof String && ((String) value).trim().isEmpty())) {
                missing.add(field);
            } else if (value instanceof List && ((List<?>) value).isEmpty()) {
                missing.add(field);
            }
        }

        if (!missing.isEmpty()) {
            String nextQuestion = generateNextQuestion(missing);
            return ToolCallService.ParameterValidationResult.invalid(missing, nextQuestion);
        }

        return ToolCallService.ParameterValidationResult.valid();
    }

    /**
     * 生成下一个问题
     */
    protected String generateNextQuestion(List<String> missingFields) {
        AiFunction definition = getFunctionDefinition();
        Map<String, AiFunction.PropertySchema> properties = definition.getParameters().getProperties();

        StringBuilder sb = new StringBuilder();
        sb.append("好的，");

        if (missingFields.size() == 1) {
            String field = missingFields.get(0);
            AiFunction.PropertySchema prop = properties.get(field);
            String desc = prop != null ? prop.getDescription() : field;
            sb.append("请告诉我").append(desc).append("是什么？");
        } else {
            sb.append("还需要以下信息：");
            for (int i = 0; i < missingFields.size(); i++) {
                String field = missingFields.get(i);
                AiFunction.PropertySchema prop = properties.get(field);
                String desc = prop != null ? prop.getDescription() : field;
                sb.append("\n").append(i + 1).append(". ").append(desc);
            }
        }

        return sb.toString();
    }

    /**
     * 获取参数字符串
     */
    protected String getParamString(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取参数整数
     */
    protected Integer getParamInt(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取参数长整数
     */
    protected Long getParamLong(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取参数列表
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> getParamList(Map<String, Object> parameters, String key, Class<T> clazz) {
        Object value = parameters.get(key);
        if (value == null) return new ArrayList<>();
        if (value instanceof List) {
            return (List<T>) value;
        }
        return new ArrayList<>();
    }
}
