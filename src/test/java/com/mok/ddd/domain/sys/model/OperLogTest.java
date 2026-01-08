package com.mok.ddd.domain.sys.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OperLog 领域实体测试")
class OperLogTest {

    @Test
    @DisplayName("创建操作日志成功")
    void create_Success() {
        OperLog log = OperLog.create("Test Title", 1, "testMethod", "POST", "user", "/test", "127.0.0.1", "params", "result", 1, null, 100L);

        assertNotNull(log);
        assertEquals("Test Title", log.getTitle());
        assertEquals(1, log.getBusinessType());
        assertEquals("testMethod", log.getMethod());
        assertEquals("POST", log.getRequestMethod());
        assertEquals("user", log.getOperName());
        assertEquals("/test", log.getOperUrl());
        assertEquals("127.0.0.1", log.getOperIp());
        assertEquals("params", log.getOperParam());
        assertEquals("result", log.getJsonResult());
        assertEquals(1, log.getStatus());
        assertNull(log.getErrorMsg());
        assertEquals(100L, log.getCostTime());
    }

    @Test
    @DisplayName("分配租户ID - 首次分配成功")
    void assignTenant_FirstTime_Success() {
        OperLog log = OperLog.create(null, null, null, null, null, null, null, null, null, null, null, null);
        log.assignTenant("tenant123");
        assertEquals("tenant123", log.getTenantId());
    }

    @Test
    @DisplayName("分配租户ID - 已有ID时不再分配")
    void assignTenant_AlreadyExists_DoesNotChange() {
        OperLog log = OperLog.create(null, null, null, null, null, null, null, null, null, null, null, null);
        log.assignTenant("tenant123");
        log.assignTenant("newTenant");
        assertEquals("tenant123", log.getTenantId());
    }

    @Test
    @DisplayName("分配创建者 - 首次分配成功")
    void assignCreator_FirstTime_Success() {
        OperLog log = OperLog.create(null, null, null, null, null, null, null, null, null, null, null, null);
        log.assignCreator("creatorUser");
        assertEquals("creatorUser", log.getCreateBy());
        assertEquals("creatorUser", log.getUpdateBy());
    }

    @Test
    @DisplayName("分配创建者 - 已有创建者时不再分配")
    void assignCreator_AlreadyExists_DoesNotChange() {
        OperLog log = OperLog.create(null, null, null, null, null, null, null, null, null, null, null, null);
        log.assignCreator("creatorUser");
        log.assignCreator("newUser");
        assertEquals("creatorUser", log.getCreateBy());
        assertEquals("creatorUser", log.getUpdateBy());
    }
}
