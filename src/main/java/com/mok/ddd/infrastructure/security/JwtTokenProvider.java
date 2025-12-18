package com.mok.ddd.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationInMs;

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public JwtTokenProvider(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public String createToken(String username, String tenantId, CustomUserDetail principal, String ipAddress) {
        String token = UUID.randomUUID().toString();
        String tokenKey = "auth:token:" + token;
        String userKey = "user:tokens:" + tenantId + ":" + username;

        TokenSessionDTO session = new TokenSessionDTO(username, tenantId, principal, ipAddress, System.currentTimeMillis());
        String sessionJson = jsonMapper.writeValueAsString(session);

        redisTemplate.opsForValue().set(tokenKey, sessionJson, jwtExpirationInMs, TimeUnit.MILLISECONDS);
        redisTemplate.opsForSet().add(userKey, token);
        redisTemplate.expire(userKey, jwtExpirationInMs, TimeUnit.MILLISECONDS);

        return token;
    }

    public TokenSessionDTO getSession(String token) {
        String tokenKey = "auth:token:" + token;
        String data = redisTemplate.opsForValue().get(tokenKey);
        if (StringUtils.hasText(data)) {
            try {
                TokenSessionDTO session = jsonMapper.readValue(data, TokenSessionDTO.class);
                Long expire = redisTemplate.getExpire(tokenKey, TimeUnit.MILLISECONDS);

                // 剩余时间小于10分钟，刷新
                if (expire != null && expire > 0 && expire < 600000) {
                    redisTemplate.expire(tokenKey, jwtExpirationInMs, TimeUnit.MILLISECONDS);
                    String userKey = "user:tokens:" + session.getTenantId() + ":" + session.getUsername();
                    redisTemplate.expire(userKey, jwtExpirationInMs, TimeUnit.MILLISECONDS);
                }
                return session;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public void removeToken(String token) {
        TokenSessionDTO session = getSession(token);
        if (session != null) {
            redisTemplate.delete("auth:token:" + token);
            redisTemplate.opsForSet().remove("user:tokens:" + session.getTenantId() + ":" + session.getUsername(), token);
        }
    }
}