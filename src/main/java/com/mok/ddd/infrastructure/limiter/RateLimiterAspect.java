package com.mok.ddd.infrastructure.limiter;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.infrastructure.config.RateLimitProperties;
import com.mok.ddd.infrastructure.util.IpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitProperties rateLimitProperties;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void rateLimiterPointCut() {
    }

    @Around("rateLimiterPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!rateLimitProperties.isEnabled()) {
            return point.proceed();
        }

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);

        int time = rateLimitProperties.getTime();
        int count = rateLimitProperties.getCount();
        String keyPrefix = "rate_limit:";
        LimitType limitType = LimitType.DEFAULT;

        if (rateLimiter != null) {
            time = rateLimiter.time();
            count = rateLimiter.count();
            keyPrefix = rateLimiter.key();
            limitType = rateLimiter.limitType();
        }

        String key = keyPrefix;
        if (limitType == LimitType.IP) {
            key += IpUtil.getIpAddr() + ":";
        }
        key += method.getName();

        List<String> keys = Collections.singletonList(key);
        String luaScript = buildLuaScript();
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long currentCount = redisTemplate.execute(redisScript, keys, count, time);

        if (currentCount != null && currentCount.intValue() <= count) {
            log.info("第{}次访问key为 {}，描述为 [{}] 的接口", currentCount, keys, keyPrefix);
            return point.proceed();
        } else {
            throw new BizException("系统繁忙，请稍后重试");
        }
    }

    private String buildLuaScript() {
        return """
                local c\
                
                c = redis.call('get',KEYS[1])\
                
                if c and tonumber(c) > tonumber(ARGV[1]) then\
                
                return c;\
                
                end\
                
                c = redis.call('incr',KEYS[1])\
                
                if tonumber(c) == 1 then\
                
                redis.call('expire',KEYS[1],ARGV[2])\
                
                end\
                
                return c;""";
    }
}
