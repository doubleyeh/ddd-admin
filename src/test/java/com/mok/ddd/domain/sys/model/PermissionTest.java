package com.mok.ddd.domain.sys.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@DisplayName("Permission 领域实体测试")
class PermissionTest {

    @Test
    @DisplayName("创建权限成功")
    void create_Success() {
        Menu menu = mock(Menu.class);
        Permission permission = Permission.create("Test Perm", "test:perm", "/api/test", "GET", "desc", menu);

        assertNotNull(permission);
        assertEquals("Test Perm", permission.getName());
        assertEquals("test:perm", permission.getCode());
        assertEquals("/api/test", permission.getUrl());
        assertEquals("GET", permission.getMethod());
        assertEquals("desc", permission.getDescription());
        assertEquals(menu, permission.getMenu());
    }

    @Test
    @DisplayName("更新权限信息成功")
    void updateInfo_Success() {
        Permission permission = Permission.create("Old", "old:code", "/old", "POST", "old desc", null);
        Menu newMenu = mock(Menu.class);
        permission.updateInfo("New", "new:code", "/new", "PUT", "new desc", newMenu);

        assertEquals("New", permission.getName());
        assertEquals("new:code", permission.getCode());
        assertEquals("/new", permission.getUrl());
        assertEquals("PUT", permission.getMethod());
        assertEquals("new desc", permission.getDescription());
        assertEquals(newMenu, permission.getMenu());
    }
}
