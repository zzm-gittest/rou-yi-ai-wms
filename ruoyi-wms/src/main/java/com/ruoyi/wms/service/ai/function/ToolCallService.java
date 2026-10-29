package com.ruoyi.wms.service.ai.function;

import java.util.List;
import java.util.Map;

/**
 * AI工具调用服务接口
 *
 * @author ruoyi
 */
public interface ToolCallService {

    /**
     * 获取所有可用的函数定义
     *
     * @return 函数定义列表
     */
    List<AiFunction> getAvailableFunctions();

    /**
     * 获取指定函数的定义
     *
     * @param functionName 函数名称
     * @return 函数定义，不存在返回null
     */
    AiFunction getFunction(String functionName);

    /**
     * 执行函数调用
     *
     * @param functionName 函数名称
     * @param parameters   参数
     * @param userId       用户ID
     * @return 调用结果
     */
    FunctionCallResult executeFunction(String functionName, Map<String, Object> parameters, Long userId);

    /**
     * 验证函数参数是否完整
     *
     * @param functionName 函数名称
     * @param parameters   当前参数
     * @return 验证结果，包含缺失字段信息
     */
    ParameterValidationResult validateParameters(String functionName, Map<String, Object> parameters);

    /**
     * 参数验证结果
     */
    class ParameterValidationResult {
        private boolean valid;
        private java.util.List<String> missingFields;
        private String nextQuestion;

        public ParameterValidationResult(boolean valid, java.util.List<String> missingFields, String nextQuestion) {
            this.valid = valid;
            this.missingFields = missingFields;
            this.nextQuestion = nextQuestion;
        }

        public boolean isValid() {
            return valid;
        }

        public java.util.List<String> getMissingFields() {
            return missingFields;
        }

        public String getNextQuestion() {
            return nextQuestion;
        }

        public static ParameterValidationResult valid() {
            return new ParameterValidationResult(true, java.util.Collections.emptyList(), null);
        }

        public static ParameterValidationResult invalid(java.util.List<String> missingFields, String nextQuestion) {
            return new ParameterValidationResult(false, missingFields, nextQuestion);
        }
    }
}
