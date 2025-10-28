package com.mok.ddd.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String token;
    private String username;
    private String tenantId;
}