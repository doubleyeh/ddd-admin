package com.mok.ddd.infrastructure.security;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class TokenSessionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String token;
    private String username;
    private String tenantId;
    private CustomUserDetail principal;
    private String ip;
    private String browser;
    private long loginTime;

    public TokenSessionDTO() {
    }

    public TokenSessionDTO(String username, String tenantId, CustomUserDetail principal, String ip, String browser, long loginTime) {
        this.username = username;
        this.tenantId = tenantId;
        this.principal = principal;
        this.ip = ip;
        this.browser = browser;
        this.loginTime = loginTime;
    }
}