package com.mok.ddd.infrastructure.security;

import com.mok.ddd.application.sys.service.LoginLogService;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.LoginLog;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Component
@RequiredArgsConstructor
public class AuthenticationEventListener {

    private final LoginLogService loginLogService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        Authentication authentication = success.getAuthentication();
        String username = authentication.getName();
        String tenantId = TenantContextHolder.getTenantId();
        String ipAddress = getIpAddress();

        LoginLog loginLog = LoginLog.create(username, ipAddress, "SUCCESS", "Login successful");
        loginLog.assignTenant(tenantId);
        loginLogService.createLoginLog(loginLog);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent failure) {
        String username = TenantContextHolder.getUsername();
        String tenantId = TenantContextHolder.getTenantId();
        String ipAddress = getIpAddress();

        Exception ex = failure.getException();
        String message = ex.getMessage();
        if(ex.getCause() != null && ex.getCause().getMessage() != null){
            message = ex.getCause().getMessage();
        }

        LoginLog loginLog = LoginLog.create(username, ipAddress, "FAILURE", message);
        loginLog.assignTenant(tenantId);
        loginLogService.createLoginLog(loginLog);
    }

    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return SysUtil.getIpAddress(request);
        }
        return "Unknown";
    }
}
