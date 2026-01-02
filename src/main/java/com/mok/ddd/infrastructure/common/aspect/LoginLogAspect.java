package com.mok.ddd.infrastructure.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoginLogAspect {

    private final JsonMapper jsonMapper;

    @Pointcut("execution(public * com.mok.ddd.adapter.web..*.*(..))")
    public void webLog() {
    }

    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long endTime = System.currentTimeMillis();

        try {
            log.info("Request End: {}, Response: {}, Cost: {}ms",
                    proceedingJoinPoint.getSignature().toShortString(),
                    jsonMapper.writeValueAsString(result),
                    endTime - startTime);
        } catch (JacksonException e) {
            log.warn("Failed to serialize response for logging", e);
        }

        return result;
    }

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        log.info("Request Start: {} {}. Class Method: {}.{}. Args: {}",
                request.getMethod(),
                request.getRequestURL().toString(),
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                getArgsAsJson(joinPoint.getArgs()));
    }

    @AfterThrowing(pointcut = "webLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause = '{}'",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getMessage() != null ? e.getMessage() : "NULL",
                e);
    }

    private String getArgsAsJson(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return jsonMapper.writeValueAsString(args);
        } catch (JacksonException e) {
            return "[Failed to serialize args]";
        }
    }
}
