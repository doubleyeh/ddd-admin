package com.mok.ddd.application.sys.dto.log;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginLogDTO {
    private Long id;
    private String username;
    private String ipAddress;
    private String status;
    private String message;
    private String tenantId;
    private String tenantName;
    private LocalDateTime createdTime;
}
