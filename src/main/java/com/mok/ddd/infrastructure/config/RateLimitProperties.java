package com.mok.ddd.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {

    /**
     * Whether to enable rate limiting
     */
    private boolean enabled = true;

    /**
     * Limit time, in seconds
     */
    private int time = 1;

    /**
     * Limit count
     */
    private int count = 30;
}
