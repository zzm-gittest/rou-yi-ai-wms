package com.ruoyi.wms.service.ai.function;

import lombok.Data;

import java.util.List;

/**
 * AI函数定义（用于Function Calling）
 *
 * @author ruoyi
 */
@Data
public class AiFunction {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 函数描述
     */
    private String description;

    /**
     * 参数定义
     */
    private AiParameter parameters;

    /**
     * 是否需要用户确认后执行
     */
    private boolean requireConfirmation = true;

    /**
     * 参数定义
     */
    @Data
    public static class AiParameter {
        /**
         * 参数类型: object
         */
        private String type = "object";

        /**
         * 必需参数列表
         */
        private List<String> required;

        /**
         * 属性定义
         */
        private java.util.Map<String, PropertySchema> properties;
    }

    /**
     * 属性Schema
     */
    @Data
    public static class PropertySchema {
        /**
         * 类型: string, number, array, integer, boolean
         */
        private String type;

        /**
         * 描述
         */
        private String description;

        /**
         * 枚举值
         */
        private List<String> enumValues;

        /**
         * 数组项的Schema
         */
        private PropertySchema items;

        public static PropertySchema string(String description) {
            PropertySchema schema = new PropertySchema();
            schema.setType("string");
            schema.setDescription(description);
            return schema;
        }

        public static PropertySchema integer(String description) {
            PropertySchema schema = new PropertySchema();
            schema.setType("integer");
            schema.setDescription(description);
            return schema;
        }

        public static PropertySchema number(String description) {
            PropertySchema schema = new PropertySchema();
            schema.setType("number");
            schema.setDescription(description);
            return schema;
        }

        public static PropertySchema array(String description, PropertySchema items) {
            PropertySchema schema = new PropertySchema();
            schema.setType("array");
            schema.setDescription(description);
            schema.setItems(items);
            return schema;
        }
    }
}
