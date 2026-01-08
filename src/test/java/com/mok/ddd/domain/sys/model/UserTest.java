package com.mok.ddd.domain.sys.model;

import com.mok.ddd.common.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("User 领域实体测试")
class UserTest {

    @Test
    @DisplayName("创建用户成功")
    void create_Success() {
        User user = User.create("testuser", "password", "Test User", true);

        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("Test User", user.getNickname());
        assertTrue(user.getIsTenantAdmin());
        assertEquals(Const.UserState.NORMAL, user.getState());
    }

    @Test
    @DisplayName("分配租户ID成功")
    void assignTenant_Success() {
        User user = User.create("u", "p", "n", false);
        user.assignTenant("tenant1");
        assertEquals("tenant1", user.getTenantId());
        
        // 再次分配不应覆盖
        user.assignTenant("tenant2");
        assertEquals("tenant1", user.getTenantId());
    }

    @Test
    @DisplayName("更新用户信息成功")
    void updateInfo_Success() {
        User user = User.create("u", "p", "n", false);
        Role role = mock(Role.class);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        
        user.updateInfo("New Nickname", roles);
        
        assertEquals("New Nickname", user.getNickname());
        assertEquals(roles, user.getRoles());
    }

    @Test
    @DisplayName("修改密码成功")
    void changePassword_Success() {
        User user = User.create("u", "oldpass", "n", false);
        user.changePassword("newpass");
        assertEquals("newpass", user.getPassword());
    }

    @Test
    @DisplayName("禁用用户成功")
    void disable_Success() {
        User user = User.create("u", "p", "n", false);
        user.disable();
        assertEquals(Const.UserState.DISABLED, user.getState());
    }

    @Test
    @DisplayName("启用用户成功")
    void enable_Success() {
        User user = User.create("u", "p", "n", false);
        user.disable(); // 先禁用
        user.enable();
        assertEquals(Const.UserState.NORMAL, user.getState());
    }

    @Test
    @DisplayName("更换角色成功")
    void changeRoles_Success() {
        User user = User.create("u", "p", "n", false);
        Role r1 = mock(Role.class);
        Set<Role> roles = new HashSet<>();
        roles.add(r1);
        
        user.changeRoles(roles);
        assertEquals(roles, user.getRoles());
    }
}
