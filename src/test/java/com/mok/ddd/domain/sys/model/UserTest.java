package com.mok.ddd.domain.sys.model;

import com.mok.ddd.common.Const;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UserTest {

    private User createTestUser(String username, Integer state) {
        try {
            var constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();
            setField(user, "username", username);
            setField(user, "state", state);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = User.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {
        @Test
        void create_Success() {
            User user = User.create("testuser", "encodedPass", "Test User", true);

            assertNotNull(user);
            assertEquals("testuser", user.getUsername());
            assertEquals("encodedPass", user.getPassword());
            assertEquals("Test User", user.getNickname());
            assertTrue(user.getIsTenantAdmin());
            assertEquals(Const.UserState.NORMAL, user.getState());
        }
    }

    @Nested
    @DisplayName("updateInfo")
    class UpdateInfoTests {
        @Test
        void updateInfo_ShouldUpdateFields() {
            User user = createTestUser("testuser", Const.UserState.NORMAL);
            Set<Role> roles = new HashSet<>();
            roles.add(mock(Role.class));

            user.updateInfo("New Nickname", roles);

            assertEquals("New Nickname", user.getNickname());
            assertEquals(1, user.getRoles().size());
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePasswordTests {
        @Test
        void changePassword_ShouldUpdatePassword() {
            User user = createTestUser("testuser", Const.UserState.NORMAL);
            user.changePassword("newEncodedPass");
            assertEquals("newEncodedPass", user.getPassword());
        }
    }

    @Nested
    @DisplayName("State Changes")
    class StateTests {
        @Test
        void enable_ShouldSetStateToNormal() {
            User user = createTestUser("testuser", Const.UserState.DISABLED);
            user.enable();
            assertEquals(Const.UserState.NORMAL, user.getState());
        }

        @Test
        void disable_ShouldSetStateToDisabled() {
            User user = createTestUser("testuser", Const.UserState.NORMAL);
            user.disable();
            assertEquals(Const.UserState.DISABLED, user.getState());
        }
    }

    @Nested
    @DisplayName("changeRoles")
    class ChangeRolesTests {
        @Test
        void changeRoles_ShouldUpdateRoles() {
            User user = createTestUser("testuser", Const.UserState.NORMAL);
            Set<Role> newRoles = new HashSet<>();
            newRoles.add(mock(Role.class));
            newRoles.add(mock(Role.class));

            user.changeRoles(newRoles);

            assertEquals(2, user.getRoles().size());
        }
    }
}
