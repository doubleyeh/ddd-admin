package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.dto.role.RoleDTO;
import com.mok.ddd.application.dto.role.RoleOptionDTO;
import com.mok.ddd.application.dto.role.RoleSaveDTO;
import com.mok.ddd.application.exception.BizException;
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
import org.jspecify.annotations.NonNull;
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

    @Nested
    @DisplayName("findPage 分页查询测试")
    class FindPageTests {
        @Test
        @DisplayName("分页查询角色成功")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void findPage_Success() {
            com.querydsl.core.types.Predicate predicate = mock(com.querydsl.core.types.Predicate.class);
            org.springframework.data.domain.Pageable pageable = mock(org.springframework.data.domain.Pageable.class);
            com.querydsl.jpa.impl.JPAQueryFactory queryFactory = mock(com.querydsl.jpa.impl.JPAQueryFactory.class);
            com.querydsl.jpa.impl.JPAQuery jpaQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            com.querydsl.jpa.impl.JPAQuery countQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            com.querydsl.jpa.JPQLQuery jpqlQuery = mock(com.querydsl.jpa.JPQLQuery.class);
            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);

            when(roleRepository.getJPAQueryFactory()).thenReturn(queryFactory);
            when(roleRepository.getQuerydsl()).thenReturn(querydsl);

            when(queryFactory.select(any(com.querydsl.core.types.Expression.class)))
                    .thenReturn(jpaQuery)
                    .thenReturn(countQuery);

            when(jpaQuery.from(any(com.querydsl.core.types.EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.leftJoin(any(com.querydsl.core.types.EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.on(any(com.querydsl.core.types.Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(jpaQuery);

            when(querydsl.applyPagination(eq(pageable), any())).thenReturn(jpqlQuery);
            when(jpqlQuery.fetch()).thenReturn(List.of(new RoleDTO()));

            when(countQuery.from(any(com.querydsl.core.types.EntityPath.class))).thenReturn(countQuery);
            when(countQuery.leftJoin(any(com.querydsl.core.types.EntityPath.class))).thenReturn(countQuery);
            when(countQuery.on(any(com.querydsl.core.types.Predicate.class))).thenReturn(countQuery);
            when(countQuery.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(1L);

            org.springframework.data.domain.Page<@NonNull RoleDTO> result = roleService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(roleRepository).applyTenantFilter(any(), any());
        }
    }

    @Nested
    @DisplayName("updateState 状态更新测试")
    class UpdateStateTests {
        @Test
        @DisplayName("更新角色状态成功")
        void updateState_Success() {
            Long id = 1L;
            Boolean enabled = true;
            Role role = new Role();
            role.setId(id);
            RoleDTO roleDTO = new RoleDTO();

            when(roleRepository.findById(id)).thenReturn(Optional.of(role));
            when(roleRepository.save(role)).thenReturn(role);
            when(roleMapper.toDto(role)).thenReturn(roleDTO);

            RoleDTO result = roleService.updateState(id, enabled);

            assertTrue(role.getEnabled());
            assertEquals(roleDTO, result);
        }
    }

    @Nested
    @DisplayName("deleteRoleBeforeValidation 删除校验测试")
    class DeleteRoleTests {
        @Test
        @DisplayName("删除角色成功")
        void deleteRole_Success() {
            Long id = 1L;
            when(roleRepository.existsUserAssociatedWithRole(id)).thenReturn(false);

            roleService.deleteRoleBeforeValidation(id);

            verify(roleRepository).deleteById(id);
        }

        @Test
        @DisplayName("删除角色失败：存在关联用户")
        void deleteRole_HasUserAssociated_ThrowsBizException() {
            Long id = 1L;
            when(roleRepository.existsUserAssociatedWithRole(id)).thenReturn(true);

            BizException exception = assertThrows(BizException.class, () ->
                    roleService.deleteRoleBeforeValidation(id)
            );

            assertEquals("该角色下存在用户，请先删除用户关联该角色", exception.getMessage());
            verify(roleRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getRoleOptions 选项查询测试")
    class GetRoleOptionsTests {
        @Test
        @DisplayName("获取角色选项列表成功")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void getRoleOptions_Success() {
            com.mok.ddd.application.dto.role.RoleQuery roleQuery = mock(com.mok.ddd.application.dto.role.RoleQuery.class);
            com.querydsl.jpa.impl.JPAQueryFactory queryFactory = mock(com.querydsl.jpa.impl.JPAQueryFactory.class);
            com.querydsl.jpa.impl.JPAQuery jpaQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            List<RoleOptionDTO> options = List.of(new RoleOptionDTO());

            when(roleQuery.toPredicate()).thenReturn(mock(com.querydsl.core.types.Predicate.class));
            when(roleRepository.getJPAQueryFactory()).thenReturn(queryFactory);

            doReturn(jpaQuery).when(queryFactory).select(any(com.querydsl.core.types.Expression.class));
            doReturn(jpaQuery).when(jpaQuery).from(any(com.querydsl.core.types.EntityPath.class));
            doReturn(jpaQuery).when(jpaQuery).leftJoin(any(com.querydsl.core.types.EntityPath.class));

            doReturn(jpaQuery).when(jpaQuery).on(any(com.querydsl.core.types.Predicate.class));
            doReturn(jpaQuery).when(jpaQuery).where(any(com.querydsl.core.types.Predicate.class));

            when(jpaQuery.fetch()).thenReturn(options);

            List<RoleOptionDTO> result = roleService.getRoleOptions(roleQuery);

            assertNotNull(result);
            assertEquals(options, result);
        }
    }

    @Test
    @DisplayName("基础转换方法测试")
    void testBaseMethods() {
        RoleDTO dto = new RoleDTO();
        Role entity = new Role();

        when(roleMapper.toEntity(dto)).thenReturn(entity);
        assertEquals(entity, roleService.toEntity(dto));

        when(roleMapper.toDto(entity)).thenReturn(dto);
        assertEquals(dto, roleService.toDto(entity));

        assertEquals(roleRepository, roleService.getRepository());
    }
}