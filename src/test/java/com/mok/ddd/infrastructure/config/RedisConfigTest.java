package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private JsonMapper redisJsonMapper;

    @InjectMocks
    private RedisConfig redisConfig;

    @Test
    void redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(connectionFactory, redisJsonMapper);
        assertNotNull(redisTemplate);
        assertEquals(connectionFactory, redisTemplate.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, redisTemplate.getKeySerializer());
        assertInstanceOf(GenericJacksonJsonRedisSerializer.class, redisTemplate.getValueSerializer());
        assertInstanceOf(GenericJacksonJsonRedisSerializer.class, redisTemplate.getHashValueSerializer());
    }

    @Test
    void stringRedisTemplate() {
        StringRedisTemplate stringRedisTemplate = redisConfig.stringRedisTemplate(connectionFactory);
        assertNotNull(stringRedisTemplate);
        assertEquals(connectionFactory, stringRedisTemplate.getConnectionFactory());
    }
}