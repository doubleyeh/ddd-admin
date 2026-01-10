package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = RateLimitProperties.class)
@TestPropertySource(properties = {
        "rate.limit.enabled=false",
        "rate.limit.time=10",
        "rate.limit.count=100"
})
class RateLimitPropertiesTest {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Test
    void propertiesAreMapped() {
        assertFalse(rateLimitProperties.isEnabled());
        assertEquals(10, rateLimitProperties.getTime());
        assertEquals(100, rateLimitProperties.getCount());
    }
}