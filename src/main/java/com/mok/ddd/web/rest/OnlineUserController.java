package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.tenant.TenantOptionsDTO;
import com.mok.ddd.application.service.TenantService;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.security.OnlineUserDTO;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
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
                .collect(Collectors.toMap(TenantOptionsDTO::getTenantId, TenantOptionsDTO::getName, (a, b) -> a));
        String currentTenantId = TenantContextHolder.getTenantId();
        boolean isSuper = TenantContextHolder.isSuperTenant();

        return RestResponse.success(tokenProvider.getAllOnlineUsers(tenantMap, currentTenantId, isSuper));
    }

    @PostMapping("/kickout")
    @PreAuthorize("hasAuthority('admin:online-user:kickout')")
    public RestResponse<Void> kickout(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        if (token != null) {
            tokenProvider.removeToken(token);
        }
        return RestResponse.success();
    }
}