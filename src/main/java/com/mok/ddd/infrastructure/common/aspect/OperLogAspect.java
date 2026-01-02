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
            OperLog operLog = new OperLog();
            operLog.setStatus(1);
            operLog.setCostTime(costTime);
            
            String ip = "127.0.0.1";
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    ip = SysUtil.getIpAddress(request);
                    operLog.setOperUrl(request.getRequestURI());
                    operLog.setRequestMethod(request.getMethod());
                }
            } catch (Exception ignored) {
            }

            operLog.setOperIp(ip);
            String username = TenantContextHolder.getUsername();
            operLog.setOperName(username);
            operLog.setCreateBy(username);
            operLog.setUpdateBy(username);
            operLog.setTenantId(TenantContextHolder.getTenantId());

            if (e != null) {
                operLog.setStatus(0);
                operLog.setErrorMsg(e.getMessage());
            }

            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            operLog.setMethod(className + "." + methodName + "()");

            getControllerMethodDescription(joinPoint, controllerLog, operLog, jsonResult);

            applicationEventPublisher.publishEvent(new OperLogEvent(operLog));
        } catch (Exception exp) {
            log.error("==前置通知异常==");
            log.error("异常信息:{}", exp.getMessage());
        }
    }

    public void getControllerMethodDescription(ProceedingJoinPoint joinPoint, com.mok.ddd.infrastructure.log.annotation.OperLog log, OperLog operLog, Object jsonResult) throws Exception {
        operLog.setBusinessType(log.businessType().ordinal());
        operLog.setTitle(log.title());

        if (log.isSaveRequestData()) {
            setRequestValue(joinPoint, operLog);
        }

        if (log.isSaveResponseData() && jsonResult != null) {
            try {
                operLog.setJsonResult(jsonMapper.writeValueAsString(jsonResult));
            } catch (Exception e) {
                operLog.setJsonResult("error serializing result");
            }
        }
    }

    private void setRequestValue(ProceedingJoinPoint joinPoint, OperLog operLog) throws Exception {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
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
                params = params.substring(0, 2000) + "...";
            }
            operLog.setOperParam(params);
        } catch (Exception e) {
            operLog.setOperParam("error serializing args");
        }
    }
}
