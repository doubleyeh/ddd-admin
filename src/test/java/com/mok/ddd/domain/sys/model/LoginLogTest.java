package com.mok.ddd.domain.sys.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("LoginLog 领域实体测试")
class LoginLogTest {

    @Test
    @DisplayName("创建登录日志成功")
    void create_Success() {
        LoginLog log = LoginLog.create("user", "127.0.0.1", "SUCCESS", "Login successful");

        assertNotNull(log);
        assertEquals("user", log.getUsername());
        assertEquals("127.0.0.1", log.getIpAddress());
        assertEquals("SUCCESS", log.getStatus());
        assertEquals("Login successful", log.getMessage());
    }

    @Test
    @DisplayName("分配租户ID - 首次分配成功")
    void assignTenant_FirstTime_Success() {
        LoginLog log = LoginLog.create("user", "127.0.0.1", "SUCCESS", "msg");
        log.assignTenant("tenant123");
        assertEquals("tenant123", log.getTenantId());
    }

    @Test
    @DisplayName("分配租户ID - 已有ID时不再分配")
    void assignTenant_AlreadyExists_DoesNotChange() {
        LoginLog log = LoginLog.create("user", "127.0.0.1", "SUCCESS", "msg");
        log.assignTenant("tenant123");
        log.assignTenant("newTenant");
        assertEquals("tenant123", log.getTenantId());
    }
}
