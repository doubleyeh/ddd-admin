package com.mok.ddd.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${auth.expiration-ms}")
    private long jwtExpirationInMs;

    @Value("${auth.allow-multi-device:true}")
    private boolean allowMultiDevice;

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;

    public JwtTokenProvider(StringRedisTemplate redisTemplate, JsonMapper jsonMapper) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
    }

    public String createToken(String username, String tenantId, CustomUserDetail principal, String ipAddress, String browser) {
        String userKey = "user:tokens:" + tenantId + ":" + username;
        if (!allowMultiDevice) {
            Object oldToken = redisTemplate.opsForValue().get(userKey);
            if (oldToken != null) {
                redisTemplate.delete("auth:token:" + oldToken.toString());
            }
        }

        String token = UUID.randomUUID().toString();
        String tokenKey = "auth:token:" + token;
        TokenSessionDTO session = new TokenSessionDTO(username, tenantId, principal, ipAddress, browser, System.currentTimeMillis());
        session.setToken(token);
        String sessionJson = jsonMapper.writeValueAsString(session);

        redisTemplate.opsForValue().set(tokenKey, sessionJson, jwtExpirationInMs, TimeUnit.MILLISECONDS);
        if (allowMultiDevice) {
            redisTemplate.opsForSet().add(userKey, token);
        } else {
            redisTemplate.opsForValue().set(userKey, token);
        }
        redisTemplate.expire(userKey, jwtExpirationInMs, TimeUnit.MILLISECONDS);

        return token;
    }

    public TokenSessionDTO getSession(String token) {
        String tokenKey = "auth:token:" + token;
        String data = redisTemplate.opsForValue().get(tokenKey);
        if (StringUtils.hasText(data)) {
            try {
                TokenSessionDTO session = jsonMapper.readValue(data, TokenSessionDTO.class);
                if(session == null){
                    return null;
                }

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

    public List<OnlineUserDTO> getAllOnlineUsers(Map<String, String> tenantMap, String currentTenantId, boolean isSuper) {
        Set<String> keys = redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keySet = new HashSet<>();
            ScanOptions options = ScanOptions.scanOptions().match("auth:token:*").count(1000).build();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
                while (cursor.hasNext()) {
                    keySet.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                return keySet;
            }
            return keySet;
        });

        List<TokenSessionDTO> sessions = new ArrayList<>();
        if (keys != null) {
            for (String fullKey : keys) {
                String token = fullKey.replace("auth:token:", "");
                TokenSessionDTO session = getSession(token);
                if (session != null) {
                    session.setToken(token);
                    sessions.add(session);
                }
            }
        }

        Map<String, List<TokenSessionDTO>> grouped = sessions.stream()
                .filter(s -> s.getPrincipal() != null)
                .filter(s -> isSuper || s.getTenantId().equals(currentTenantId))
                .collect(Collectors.groupingBy(s -> s.getTenantId() + ":" + s.getPrincipal().getUserId()));

        return grouped.values().stream().map(userSessions -> {
            TokenSessionDTO first = userSessions.getFirst();
            List<OnlineUserDTO.SessionDetail> details = userSessions.stream()
                    .map(s -> new OnlineUserDTO.SessionDetail(
                            s.getToken(),
                            s.getIp(),
                            s.getBrowser(),
                            s.getLoginTime()
                    )).toList();

            return new OnlineUserDTO(
                    first.getPrincipal().getUserId(),
                    first.getUsername(),
                    first.getTenantId(),
                    tenantMap.getOrDefault(first.getTenantId(), first.getTenantId()),
                    details
            );
        }).toList();
    }

    public void removeToken(String token) {
        TokenSessionDTO session = getSession(token);
        if (session != null) {
            String tokenKey = "auth:token:" + token;
            String userKey = "user:tokens:" + session.getTenantId() + ":" + session.getUsername();

            redisTemplate.delete(tokenKey);

            if (allowMultiDevice) {
                redisTemplate.opsForSet().remove(userKey, token);
            } else {
                redisTemplate.delete(userKey);
            }
        }
    }
}