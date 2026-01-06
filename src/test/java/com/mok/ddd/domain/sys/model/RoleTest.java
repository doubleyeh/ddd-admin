package com.mok.ddd.domain.sys.model;

import com.mok.ddd.common.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleTest {

    private Role createTestRole(String name, Integer state) {
        try {
            var constructor = Role.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Role role = constructor.newInstance();
            setField(role, "name", name);
            setField(role, "state", state);
            return role;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = Role.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("create 方法测试")
    class CreateTests {
        @Test
        void create_Success() {
            Role role = Role.create("Test Role", "test_role", "A test role", 1);

            assertNotNull(role);
            assertEquals("Test Role", role.getName());
            assertEquals("test_role", role.getCode());
            assertEquals(1, role.getSort());
            assertEquals(Const.RoleState.NORMAL, role.getState());
        }
    }

    @Nested
    @DisplayName("disable/enable 方法测试")
    class StateTests {
        @Test
        void disable_NormalRole_ShouldSetStateToDisabled() {
            Role role = createTestRole("Normal Role", Const.RoleState.NORMAL);
            role.disable();
            assertEquals(Const.RoleState.DISABLED, role.getState());
        }

        @Test
        void disable_AlreadyDisabledRole_ShouldRemainDisabled() {
            Role role = createTestRole("Disabled Role", Const.RoleState.DISABLED);
            role.disable();
            assertEquals(Const.RoleState.DISABLED, role.getState());
        }

        @Test
        void enable_DisabledRole_ShouldSetStateToNormal() {
            Role role = createTestRole("Disabled Role", Const.RoleState.DISABLED);
            role.enable();
            assertEquals(Const.RoleState.NORMAL, role.getState());
        }

        @Test
        void enable_AlreadyEnabledRole_ShouldRemainNormal() {
            Role role = createTestRole("Normal Role", Const.RoleState.NORMAL);
            role.enable();
            assertEquals(Const.RoleState.NORMAL, role.getState());
        }
    }

    @Nested
    @DisplayName("updateInfo 方法测试")
    class UpdateInfoTests {
        @Test
        void updateInfo_ShouldUpdateFields() {
            Role role = createTestRole("Old Name", Const.RoleState.NORMAL);
            role.updateInfo("New Name", "new_code", "New Desc", 2);
            assertEquals("New Name", role.getName());
            assertEquals("new_code", role.getCode());
            assertEquals("New Desc", role.getDescription());
            assertEquals(2, role.getSort());
        }
    }

    @Nested
    @DisplayName("changePermissions/Menus 方法测试")
    class AssociationTests {
        @Test
        void changePermissions_ShouldUpdatePermissions() {
            Role role = createTestRole("Test Role", Const.RoleState.NORMAL);
            Set<Permission> newPermissions = new HashSet<>();
            newPermissions.add(new Permission());
            role.changePermissions(newPermissions);
            assertEquals(1, role.getPermissions().size());
        }

        @Test
        void changeMenus_ShouldUpdateMenus() {
            Role role = createTestRole("Test Role", Const.RoleState.NORMAL);
            Set<Menu> newMenus = new HashSet<>();
            newMenus.add(new Menu());
            role.changeMenus(newMenus);
            assertEquals(1, role.getMenus().size());
        }
    }
}
