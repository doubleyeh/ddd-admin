package com.mok.ddd.application.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    @NotBlank(message = "登录用户不允许为空")
    private String username;

    @NotBlank(message = "登录密码不允许为空")
    private String password;

    @NotBlank(message = "租户不允许为空")
    private String tenantId;
}