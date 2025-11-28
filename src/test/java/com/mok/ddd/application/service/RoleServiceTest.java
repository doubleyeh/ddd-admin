package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.dto.role.RoleDTO;
import com.mok.ddd.application.dto.role.RoleSaveDTO;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.PermissionMapper;
import com.mok.ddd.application.mapper.RoleMapper;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.entity.Role;
import com.mok.ddd.domain.repository.MenuRepository;
import com.mok.ddd.domain.repository.PermissionRepository;
import com.mok.ddd.domain.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService 单元测试")
class RoleServiceTest {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private MenuMapper menuMapper;

    @Nested
    @DisplayName("createRole 创建角色测试")
    class CreateRoleTests {

        @Test
        @DisplayName("成功创建角色并关联权限和菜单")
        void createRole_Success() {
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setPermissionIds(Set.of(1L, 2L));
            dto.setMenuIds(Set.of(10L, 20L));

            Role newRole = new Role();
            Role savedRole = new Role();
            savedRole.setId(5L);

            Permission p1 = new Permission();
            Permission p2 = new Permission();
            Menu m1 = new Menu();
            Menu m2 = new Menu();

            when(roleMapper.toEntity(dto)).thenReturn(newRole);
            when(permissionRepository.findAllById(dto.getPermissionIds())).thenReturn(List.of(p1, p2));
            when(menuRepository.findAllById(dto.getMenuIds())).thenReturn(List.of(m1, m2));
            when(roleRepository.save(newRole)).thenReturn(savedRole);
            when(roleMapper.toDto(savedRole)).thenReturn(new RoleDTO());

            RoleDTO result = roleService.createRole(dto);

            verify(permissionRepository, times(1)).findAllById(dto.getPermissionIds());
            verify(menuRepository, times(1)).findAllById(dto.getMenuIds());
            verify(roleRepository, times(1)).save(newRole);
            verify(roleMapper, times(1)).toDto(savedRole);

            assertTrue(newRole.getPermissions().containsAll(Set.of(p1, p2)));
            assertTrue(newRole.getMenus().containsAll(Set.of(m1, m2)));
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("updateRole 更新角色测试")
    class UpdateRoleTests {

        private final Long existingId = 1L;

        @Test
        @DisplayName("成功更新角色并关联权限和菜单")
        void updateRole_Success() {
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setId(existingId);
            dto.setPermissionIds(Set.of(3L, 4L));
            dto.setMenuIds(Set.of(30L, 40L));

            Role existingRole = new Role();
            existingRole.setId(existingId);

            Permission p3 = new Permission();
            Permission p4 = new Permission();
            Menu m3 = new Menu();
            Menu m4 = new Menu();

            when(roleRepository.findById(existingId)).thenReturn(Optional.of(existingRole));
            doNothing().when(roleMapper).updateEntityFromDto(dto, existingRole);
            when(permissionRepository.findAllById(dto.getPermissionIds())).thenReturn(List.of(p3, p4));
            when(menuRepository.findAllById(dto.getMenuIds())).thenReturn(List.of(m3, m4));
            when(roleRepository.save(existingRole)).thenReturn(existingRole);
            when(roleMapper.toDto(existingRole)).thenReturn(new RoleDTO());

            RoleDTO result = roleService.updateRole(dto);

            verify(roleRepository, times(1)).findById(existingId);
            verify(roleMapper, times(1)).updateEntityFromDto(any(RoleSaveDTO.class), any(Role.class));
            verify(roleRepository, times(1)).save(existingRole);

            assertTrue(existingRole.getPermissions().containsAll(Set.of(p3, p4)));
            assertTrue(existingRole.getMenus().containsAll(Set.of(m3, m4)));
            assertNotNull(result);
        }

        @Test
        @DisplayName("更新失败：角色不存在抛出NotFoundException")
        void updateRole_RoleNotFound_ThrowsNotFoundException() {
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setId(existingId);

            when(roleRepository.findById(existingId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                roleService.updateRole(dto);
            });

            verify(roleMapper, never()).updateEntityFromDto(any(), any());
            verify(roleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getMenusByRole 获取角色菜单测试")
    class GetMenusByRoleTests {

        private final Long existingId = 1L;

        @Test
        @DisplayName("成功获取角色菜单列表")
        void getMenusByRole_Success() {
            Role existingRole = new Role();
            Menu menu1 = new Menu();
            Menu menu2 = new Menu();
            existingRole.setMenus(Set.of(menu1, menu2));

            MenuDTO dto1 = new MenuDTO();
            dto1.setId(10L);
            MenuDTO dto2 = new MenuDTO();
            dto2.setId(20L);

            when(roleRepository.findById(existingId)).thenReturn(Optional.of(existingRole));
            when(menuMapper.toDto(menu1)).thenReturn(dto1);
            when(menuMapper.toDto(menu2)).thenReturn(dto2);

            Set<MenuDTO> result = roleService.getMenusByRole(existingId);

            verify(roleRepository, times(1)).findById(existingId);
            assertEquals(2, result.size());
            assertTrue(result.containsAll(Set.of(dto1, dto2)));
        }

        @Test
        @DisplayName("获取菜单失败：角色不存在抛出NotFoundException")
        void getMenusByRole_RoleNotFound_ThrowsNotFoundException() {
            when(roleRepository.findById(existingId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                roleService.getMenusByRole(existingId);
            });
        }
    }

    @Nested
    @DisplayName("getPermissionsByRole 获取角色权限测试")
    class GetPermissionsByRoleTests {

        private final Long existingId = 1L;

        @Test
        @DisplayName("成功获取角色权限列表")
        void getPermissionsByRole_Success() {
            Role existingRole = new Role();
            Permission perm1 = new Permission();
            Permission perm2 = new Permission();
            existingRole.setPermissions(Set.of(perm1, perm2));

            PermissionDTO dto1 = new PermissionDTO();
            dto1.setId(100L);
            PermissionDTO dto2 = new PermissionDTO();
            dto2.setId(200L);

            when(roleRepository.findById(existingId)).thenReturn(Optional.of(existingRole));
            when(permissionMapper.toDto(perm1)).thenReturn(dto1);
            when(permissionMapper.toDto(perm2)).thenReturn(dto2);

            Set<PermissionDTO> result = roleService.getPermissionsByRole(existingId);

            verify(roleRepository, times(1)).findById(existingId);
            assertEquals(2, result.size());
            assertTrue(result.containsAll(Set.of(dto1, dto2)));
        }

        @Test
        @DisplayName("获取权限失败：角色不存在抛出NotFoundException")
        void getPermissionsByRole_RoleNotFound_ThrowsNotFoundException() {
            when(roleRepository.findById(existingId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> {
                roleService.getPermissionsByRole(existingId);
            });
        }
    }
}