package com.ruoyi.wms.service.ai.function;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 函数调用结果
 *
 * @author ruoyi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCallResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 返回消息（给用户的友好回复）
     */
    private String message;

    /**
     * 函数名称
     */
    private String functionName;

    /**
     * 调用的参数
     */
    private Map<String, Object> parameters;

    /**
     * 返回的数据
     */
    private Object data;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 是否需要继续收集信息
     */
    private boolean needMoreInfo;

    /**
     * 缺失的参数列表
     */
    private java.util.List<String> missingFields;

    /**
     * 下一个问题（用于引导用户）
     */
    private String nextQuestion;

    /**
     * 创建成功结果
     */
    public static FunctionCallResult success(String functionName, String message, Object data) {
        return FunctionCallResult.builder()
                .success(true)
                .functionName(functionName)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 创建需要更多信息的结果
     */
    public static FunctionCallResult needMoreInfo(String functionName, String question, java.util.List<String> missingFields, Map<String, Object> collectedData) {
        return FunctionCallResult.builder()
                .success(false)
                .functionName(functionName)
                .needMoreInfo(true)
                .nextQuestion(question)
                .missingFields(missingFields)
                .parameters(collectedData)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static FunctionCallResult error(String functionName, String error) {
        return FunctionCallResult.builder()
                .success(false)
                .functionName(functionName)
                .error(error)
                .message("操作失败: " + error)
                .build();
    }
}
