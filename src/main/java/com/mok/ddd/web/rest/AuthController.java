package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.auth.LoginRequest;
import com.mok.ddd.application.dto.auth.LoginResDTO;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.infrastructure.security.CustomUserDetail;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
            this.authenticationManager = authenticationManager;
            this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public RestResponse<LoginResDTO> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String tenantId = loginRequest.getTenantId();
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String ipAddress = SysUtil.getIpAddress(request);

        return ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .where(TenantContextHolder.USERNAME, username)
                .call(() -> {
                    Authentication authentication = authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    username,
                                    password));

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    String jwt = tokenProvider.createToken(
                            username,
                            tenantId, (CustomUserDetail)authentication.getPrincipal(), ipAddress, SysUtil.getBrowser(request.getHeader("User-Agent")));

                    return RestResponse.success(new LoginResDTO(
                            jwt,
                            username,
                            tenantId, SysUtil.isSuperTenant(tenantId)));
                });
    }

    @PostMapping("/logout")
    public RestResponse<Void> logout(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (org.springframework.util.StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String jwt = bearerToken.substring(7);
            tokenProvider.removeToken(jwt);
        }
        SecurityContextHolder.clearContext();
        return RestResponse.success();
    }
}