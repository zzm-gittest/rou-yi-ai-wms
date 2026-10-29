package com.ruoyi.wms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cloud.print")
public class CloudPrintProperties {

    /**
     * 云打印开关
     */
    private Boolean enabled = false;

    /**
     * 云打印供应商提交接口
     */
    private String endpoint;

    /**
     * 云打印应用标识
     */
    private String appId;

    /**
     * 云打印设备编码
     */
    private String printerCode;

    /**
     * API Key 认证值
     */
    private String apiKey;

    /**
     * API Key 头名称
     */
    private String apiKeyHeader = "X-API-Key";

    /**
     * 默认打印份数
     */
    private Integer copies = 1;

    /**
     * 连接超时（毫秒）
     */
    private Integer connectTimeoutMs = 5000;

    /**
     * 读取超时（毫秒）
     */
    private Integer readTimeoutMs = 10000;
}
