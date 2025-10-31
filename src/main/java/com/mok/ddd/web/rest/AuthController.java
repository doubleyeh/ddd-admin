package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.LoginRequest;
import com.mok.ddd.application.dto.LoginResDTO;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.security.TenantContextHolder;
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
    public RestResponse<LoginResDTO> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        TenantContextHolder.setTenantId(loginRequest.getTenantId());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(
                loginRequest.getUsername(),
                loginRequest.getTenantId()
        );

        return RestResponse.success(new LoginResDTO(
                jwt,
                loginRequest.getUsername(),
                loginRequest.getTenantId()
        ));
    }
}