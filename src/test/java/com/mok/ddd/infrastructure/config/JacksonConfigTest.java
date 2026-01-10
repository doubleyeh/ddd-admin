package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonConfigTest {

    private final JacksonConfig jacksonConfig = new JacksonConfig();

    @Test
    void jsonMapper() {
        JsonMapper jsonMapper = jacksonConfig.jsonMapper();
        Long value = 1234567890123456789L;
        String json = jsonMapper.writeValueAsString(value);
        assertEquals("\"1234567890123456789\"", json);
    }

    @Test
    void redisJsonMapper() {
        JsonMapper redisJsonMapper = jacksonConfig.redisJsonMapper();
        Long value = 2876543210987654321L;
        String json = redisJsonMapper.writeValueAsString(value);
        assertEquals("\"2876543210987654321\"", json);
    }
}