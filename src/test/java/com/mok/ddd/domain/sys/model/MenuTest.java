package com.mok.ddd.domain.sys.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("Menu 领域实体测试")
class MenuTest {

    @Test
    @DisplayName("创建菜单成功")
    void create_Success() {
        Menu parent = mock(Menu.class);
        Menu menu = Menu.create(parent, "Test Menu", "/test", "TestComponent", "icon", 1, false);

        assertNotNull(menu);
        assertEquals(parent, menu.getParent());
        assertEquals("Test Menu", menu.getName());
        assertEquals("/test", menu.getPath());
        assertEquals("TestComponent", menu.getComponent());
        assertEquals("icon", menu.getIcon());
        assertEquals(1, menu.getSort());
        assertFalse(menu.getIsHidden());
    }

    @Test
    @DisplayName("更新菜单信息成功")
    void updateInfo_Success() {
        Menu menu = Menu.create(null, "Old Name", "/old", "OldComp", "old-icon", 1, false);
        Menu newParent = mock(Menu.class);
        menu.updateInfo(newParent, "New Name", "/new", "NewComp", "new-icon", 2, true);

        assertEquals(newParent, menu.getParent());
        assertEquals("New Name", menu.getName());
        assertEquals("/new", menu.getPath());
        assertEquals("NewComp", menu.getComponent());
        assertEquals("new-icon", menu.getIcon());
        assertEquals(2, menu.getSort());
        assertTrue(menu.getIsHidden());
    }

    @Test
    @DisplayName("更换菜单权限 - 验证双向关系")
    void changePermissions_ShouldUpdateBidirectionalRelationship() {
        // 1. Setup
        Menu menu = Menu.create(null, "Test", "/test", "Comp", "icon", 1, false);
        Permission p1 = Permission.create("p1", "p1", null, null, null, null);
        Permission p2 = Permission.create("p2", "p2", null, null, null, null);
        
        Set<Permission> initialPermissions = new HashSet<>();
        initialPermissions.add(p1);

        // 2. Assign initial permissions
        menu.changePermissions(initialPermissions);

        // 3. Verify initial state
        assertEquals(1, menu.getPermissions().size());
        assertTrue(menu.getPermissions().contains(p1));
        assertEquals(menu, p1.getMenu(), "p1的menu引用应该指向当前menu");

        // 4. Change permissions
        Permission p3 = Permission.create("p3", "p3", null, null, null, null);
        Set<Permission> newPermissions = new HashSet<>();
        newPermissions.add(p2);
        newPermissions.add(p3);
        menu.changePermissions(newPermissions);

        // 5. Verify final state
        assertEquals(2, menu.getPermissions().size());
        assertFalse(menu.getPermissions().contains(p1));
        assertTrue(menu.getPermissions().contains(p2));
        assertTrue(menu.getPermissions().contains(p3));
        
        assertNull(p1.getMenu(), "p1的menu引用应该被置为null");
        assertEquals(menu, p2.getMenu(), "p2的menu引用应该指向当前menu");
        assertEquals(menu, p3.getMenu(), "p3的menu引用应该指向当前menu");
    }
}
