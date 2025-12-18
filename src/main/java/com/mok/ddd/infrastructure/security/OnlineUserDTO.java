package com.mok.ddd.infrastructure.security;

import java.util.List;

public record OnlineUserDTO(
        Long userId,
        String username,
        String tenantId,
        String tenantName,
        List<SessionDetail> sessions
) {
    public record SessionDetail(
            String id,
            String ip,
            String browser,
            long loginTime
    ) {}
}