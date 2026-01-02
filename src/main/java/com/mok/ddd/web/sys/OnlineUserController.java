package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.sys.service.TenantService;
import com.mok.ddd.infrastructure.log.annotation.OperLog;
import com.mok.ddd.infrastructure.log.enums.BusinessType;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.security.OnlineUserDTO;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.web.common.RestResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/online-user")
public class OnlineUserController {

    private final JwtTokenProvider tokenProvider;
    private final TenantService tenantService;

    public OnlineUserController(JwtTokenProvider tokenProvider, TenantService tenantService) {
        this.tokenProvider = tokenProvider;
        this.tenantService = tenantService;
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('admin:online-user')")
    public RestResponse<List<OnlineUserDTO>> list() {
        Map<String, String> tenantMap = tenantService.findOptions(null).stream()
                .collect(Collectors.toMap(TenantOptionDTO::getTenantId, TenantOptionDTO::getName, (a, b) -> a));
        String currentTenantId = TenantContextHolder.getTenantId();
        boolean isSuper = TenantContextHolder.isSuperTenant();

        return RestResponse.success(tokenProvider.getAllOnlineUsers(tenantMap, currentTenantId, isSuper));
    }

    @PostMapping("/kickout")
    @PreAuthorize("hasAuthority('admin:online-user:kickout')")
    @OperLog(title = "在线用户", businessType = BusinessType.FORCE)
    public RestResponse<Void> kickout(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null) {
            tokenProvider.removeToken(token);
        }
        return RestResponse.success();
    }
}