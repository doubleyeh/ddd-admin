package com.mok.ddd.infrastructure.common.aspect;

import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.OperLog;
import com.mok.ddd.infrastructure.log.event.OperLogEvent;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperLogAspect {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final JsonMapper jsonMapper;

    @Around("@annotation(controllerLog)")
    public Object doAround(ProceedingJoinPoint joinPoint, com.mok.ddd.infrastructure.log.annotation.OperLog controllerLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            handleLog(joinPoint, controllerLog, exception, result, costTime);
        }
    }

    protected void handleLog(final ProceedingJoinPoint joinPoint, com.mok.ddd.infrastructure.log.annotation.OperLog controllerLog, final Exception e, Object jsonResult, long costTime) {
        try {
            int status = 1;
            String errorMsg = null;
            if (e != null) {
                status = 0;
                errorMsg = e.getMessage();
            }

            String operUrl = "";
            String requestMethod = "";
            String ip = "127.0.0.1";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = SysUtil.getIpAddress(request);
                    operUrl = request.getRequestURI();
                    requestMethod = request.getMethod();
                }
            } catch (Exception ignored) {
            }

            String username = TenantContextHolder.getUsername();
            String tenantId = TenantContextHolder.getTenantId();

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            String method = className + "." + methodName + "()";

            String title = controllerLog.title();
            Integer businessType = controllerLog.businessType().ordinal();

            String operParam = null;
            if (controllerLog.isSaveRequestData()) {
                operParam = getRequestValue(joinPoint);
            }

            String resultJson = null;
            if (controllerLog.isSaveResponseData() && jsonResult != null) {
                try {
                    resultJson = jsonMapper.writeValueAsString(jsonResult);
                } catch (Exception ex) {
                    resultJson = "error serializing result";
                }
            }

            OperLog operLog = OperLog.create(title, businessType, method, requestMethod, username, operUrl, ip, operParam, resultJson, status, errorMsg, costTime);
            operLog.setTenantId(tenantId);
            operLog.setCreateBy(username);
            operLog.setUpdateBy(username);

            applicationEventPublisher.publishEvent(new OperLogEvent(operLog));
        } catch (Exception exp) {
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
        }
    }

    private String getRequestValue(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        Object[] arguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest || args[i] instanceof HttpServletResponse || args[i] instanceof MultipartFile) {
                continue;
            }
            arguments[i] = args[i];
        }

        try {
            String params = jsonMapper.writeValueAsString(arguments);
            if (params.length() > 2000) {
                return params.substring(0, 2000) + "...";
            }
            return params;
        } catch (Exception e) {
            return "error serializing args";
        }
    }
}
