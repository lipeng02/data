package com.sansi.data.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取yml配置
 * @Author LeePong
 */
@Data
@Component
@ConfigurationProperties(prefix = "com.mqtt")
public class MqttConfiguration {
    private String url;
    private String clientId;
    private String[] topics;
    private String username;
    private String password;
    private String timeout;
    private String keepalive;
}
