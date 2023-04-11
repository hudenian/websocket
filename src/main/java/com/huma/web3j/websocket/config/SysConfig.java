package com.huma.web3j.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author hudenian
 * @date 2023/4/11
 */
@Data
@Component
@ConfigurationProperties(prefix = "system.config")
public class SysConfig {

    private Long pollingInterval;
    private String wsUrl;

}
