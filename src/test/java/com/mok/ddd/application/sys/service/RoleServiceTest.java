package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import com.mok.ddd.application.sys.dto.role.*;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.application.sys.mapper.PermissionMapper;
import com.mok.ddd.application.sys.mapper.RoleMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.domain.sys.model.Role;
import com.mok.ddd.domain.sys.repository.MenuRepository;
import com.mok.ddd.domain.sys.repository.PermissionRepository;
import com.mok.ddd.domain.sys.repository.RoleRepository;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private StringRedisTemplate redisTemplate;

    private MockedStatic<Role> mockedRole;

    @BeforeEach
    void setUp() {
        mockedRole = mockStatic(Role.class);
    }

    @AfterEach
    void tearDown() {
        mockedRole.close();
    }

    @Nested
    @DisplayName("createRole")
    class CreateRoleTests {

        @Test
        void createRole_Success() {
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setName("Test Role");
            dto.setCode("test_role");
            dto.setDescription("A test role");
            dto.setSort(1);

            Role mockRole = mock(Role.class);
            RoleDTO mockRoleDTO = new RoleDTO();

            mockedRole.when(() -> Role.create(dto.getName(), dto.getCode(), dto.getDescription(), dto.getSort())).thenReturn(mockRole);
            when(roleRepository.save(mockRole)).thenReturn(mockRole);
            when(roleMapper.toDto(mockRole)).thenReturn(mockRoleDTO);

            RoleDTO result = roleService.createRole(dto);

            mockedRole.verify(() -> Role.create(dto.getName(), dto.getCode(), dto.getDescription(), dto.getSort()));
            verify(roleRepository).save(mockRole);
            assertSame(mockRoleDTO, result);
        }
    }

    @Nested
    @DisplayName("updateRole")
    class UpdateRoleTests {

        @Test
        void updateRole_Success() {
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setId(1L);
            dto.setName("New Name");
            dto.setCode("new_code");
            dto.setDescription("New Desc");
            dto.setSort(10);

            Role mockRole = mock(Role.class);
            RoleDTO mockRoleDTO = new RoleDTO();

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleRepository.save(mockRole)).thenReturn(mockRole);
            when(roleMapper.toDto(mockRole)).thenReturn(mockRoleDTO);

            RoleDTO result = roleService.updateRole(dto);

            verify(mockRole).updateInfo(dto.getName(), dto.getCode(), dto.getDescription(), dto.getSort());
            verify(roleRepository).save(mockRole);
            assertSame(mockRoleDTO, result);
        }

        @Test
        void updateRole_NotFound() {
            RoleSaveDTO dto = new RoleSaveDTO();
            dto.setId(1L);
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> roleService.updateRole(dto));
        }
    }

    @Nested
    @DisplayName("updateState")
    class UpdateStateTests {

        @Test
        void updateState_ToNormal() {
            Role mockRole = mock(Role.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleRepository.save(mockRole)).thenReturn(mockRole);

            roleService.updateState(1L, Const.RoleState.NORMAL);

            verify(mockRole).enable();
            verify(mockRole, never()).disable();
            verify(roleRepository).save(mockRole);
        }

        @Test
        void updateState_ToDisabled() {
            Role mockRole = mock(Role.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleRepository.save(mockRole)).thenReturn(mockRole);

            roleService.updateState(1L, Const.RoleState.DISABLED);

            verify(mockRole).disable();
            verify(mockRole, never()).enable();
            verify(roleRepository).save(mockRole);
        }

        @Test
        void updateState_InvalidState() {
            Role mockRole = mock(Role.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleRepository.save(mockRole)).thenReturn(mockRole);

            roleService.updateState(1L, 99);

            verify(mockRole, never()).disable();
            verify(mockRole, never()).enable();
            verify(roleRepository).save(mockRole);
        }

        @Test
        void updateState_NotFound_ThrowsException() {
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> roleService.updateState(1L, Const.RoleState.NORMAL));
        }
    }

    @Nested
    @DisplayName("grant")
    class GrantTests {

        @Test
        void grant_Success_WithBothIds() {
            RoleGrantDTO dto = new RoleGrantDTO();
            dto.setMenuIds(Set.of(10L, 20L));
            dto.setPermissionIds(Set.of(100L, 200L));

            Role mockRole = mock(Role.class);
            Set<Menu> menus = dto.getMenuIds().stream().map(_ -> mock(Menu.class)).collect(Collectors.toSet());
            Set<Permission> permissions = dto.getPermissionIds().stream().map(_ -> mock(Permission.class)).collect(Collectors.toSet());

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(menuRepository.findAllById(dto.getMenuIds())).thenReturn(List.copyOf(menus));
            when(permissionRepository.findAllById(dto.getPermissionIds())).thenReturn(List.copyOf(permissions));

            roleService.grant(1L, dto);

            verify(mockRole).changeMenus(anySet());
            verify(mockRole).changePermissions(anySet());
            verify(roleRepository).save(mockRole);
            verify(redisTemplate).delete(Const.CacheKey.ROLE_PERMS + ":1");
        }

        @Test
        void grant_Success_OnlyMenus() {
            RoleGrantDTO dto = new RoleGrantDTO();
            dto.setMenuIds(Set.of(10L));
            dto.setPermissionIds(null);

            Role mockRole = mock(Role.class);
            Set<Menu> menus = Set.of(mock(Menu.class));

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(menuRepository.findAllById(dto.getMenuIds())).thenReturn(List.copyOf(menus));

            roleService.grant(1L, dto);

            verify(mockRole).changeMenus(eq(new HashSet<>(menus)));
            verify(mockRole, never()).changePermissions(anySet());
            verify(roleRepository).save(mockRole);
            verify(redisTemplate).delete(Const.CacheKey.ROLE_PERMS + ":1");
        }

        @Test
        void grant_Success_OnlyPermissions() {
            RoleGrantDTO dto = new RoleGrantDTO();
            dto.setMenuIds(null);
            dto.setPermissionIds(Set.of(100L));

            Role mockRole = mock(Role.class);
            Set<Permission> permissions = Set.of(mock(Permission.class));

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(permissionRepository.findAllById(dto.getPermissionIds())).thenReturn(List.copyOf(permissions));

            roleService.grant(1L, dto);

            verify(mockRole, never()).changeMenus(anySet());
            verify(mockRole).changePermissions(eq(new HashSet<>(permissions)));
            verify(roleRepository).save(mockRole);
            verify(redisTemplate).delete(Const.CacheKey.ROLE_PERMS + ":1");
        }

        @Test
        void grant_Success_NoIds() {
            RoleGrantDTO dto = new RoleGrantDTO();
            dto.setMenuIds(null);
            dto.setPermissionIds(null);

            Role mockRole = mock(Role.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));

            roleService.grant(1L, dto);

            verify(mockRole, never()).changeMenus(anySet());
            verify(mockRole, never()).changePermissions(anySet());
            verify(roleRepository).save(mockRole);
            verify(redisTemplate).delete(Const.CacheKey.ROLE_PERMS + ":1");
        }


        @Test
        void grant_NotFound_ThrowsException() {
            RoleGrantDTO dto = new RoleGrantDTO();
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> roleService.grant(1L, dto));
            verify(roleRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {

        @Test
        void getById_Success() {
            Role mockRole = mock(Role.class);
            RoleDTO mockDto = new RoleDTO();
            Set<Menu> menus = Set.of(mock(Menu.class));
            Set<Permission> permissions = Set.of(mock(Permission.class));
            Set<MenuDTO> menuDtos = menus.stream().map(_ -> new MenuDTO()).collect(Collectors.toSet());
            Set<PermissionDTO> permissionDtos = permissions.stream().map(_ -> new PermissionDTO()).collect(Collectors.toSet());

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleMapper.toDto(mockRole)).thenReturn(mockDto);
            when(mockRole.getMenus()).thenReturn(menus);
            when(mockRole.getPermissions()).thenReturn(permissions);
            when(menuMapper.toDto(any(Menu.class))).thenReturn(new MenuDTO());
            when(permissionMapper.toDto(any(Permission.class))).thenReturn(new PermissionDTO());

            RoleDTO result = roleService.getById(1L);

            assertSame(mockDto, result);
            when(permissionMapper.toDto(any(Permission.class))).thenReturn(new PermissionDTO());

            result = roleService.getById(1L);

            assertSame(mockDto, result);
            assertEquals(menuDtos.size(), result.getMenus().size());
            assertEquals(permissionDtos.size(), result.getPermissions().size());
        }

        @Test
        void getById_NotFound_ThrowsException() {
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> roleService.getById(1L));
        }

        @Test
        void getById_NoMenusOrPermissions_Success() {
            Role mockRole = mock(Role.class);
            RoleDTO mockDto = new RoleDTO();

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleMapper.toDto(mockRole)).thenReturn(mockDto);
            when(mockRole.getMenus()).thenReturn(Collections.emptySet());
            when(mockRole.getPermissions()).thenReturn(Collections.emptySet());

            RoleDTO result = roleService.getById(1L);

            assertSame(mockDto, result);
            assertTrue(result.getMenus().isEmpty());
            assertTrue(result.getPermissions().isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteRoleBeforeValidation")
    class DeleteRoleTests {
        @Test
        void deleteRole_Success() {
            when(roleRepository.existsUserAssociatedWithRole(1L)).thenReturn(false);
            roleService.deleteRoleBeforeValidation(1L);
            verify(roleRepository).deleteById(1L);
        }

        @Test
        void deleteRole_HasUserAssociated_ThrowsBizException() {
            when(roleRepository.existsUserAssociatedWithRole(1L)).thenReturn(true);
            assertThrows(BizException.class, () -> roleService.deleteRoleBeforeValidation(1L));
            verify(roleRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Query Methods")
    class QueryTests {
        @Mock
        private JPAQueryFactory queryFactory;
        @Mock
        private JPAQuery<RoleOptionDTO> jpaQuery;
        @Mock
        private JPAQuery<RoleDTO> listQuery;
        @Mock
        private JPAQuery<Long> countQuery;
        @Mock
        private JPQLQuery<RoleDTO> paginatedQuery;

        @Test
        @SuppressWarnings("unchecked")
        void getRoleOptions_Success() {
            RoleQuery roleQuery = new RoleQuery();
            when(roleRepository.getJPAQueryFactory()).thenReturn(queryFactory);
            when(queryFactory.select((Expression<RoleOptionDTO>) any())).thenReturn(jpaQuery);
            when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.leftJoin(any(EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.on(any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.where(any(Predicate.class), any(Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.fetch()).thenReturn(Collections.singletonList(new RoleOptionDTO()));

            List<RoleOptionDTO> result = roleService.getRoleOptions(roleQuery);

            assertFalse(result.isEmpty());
        }

        @Test
        @SuppressWarnings("unchecked")
        void findPage_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Predicate predicate = mock(Predicate.class);
            List<RoleDTO> roleDTOs = IntStream.range(0, 10).mapToObj(_ -> new RoleDTO()).collect(Collectors.toList());
            long totalCount = 20L;

            when(roleRepository.getJPAQueryFactory()).thenReturn(queryFactory);

            when(queryFactory.select(any(Expression.class))).thenReturn(listQuery, countQuery);

            when(listQuery.from(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.leftJoin(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.on(any(Predicate.class))).thenReturn(listQuery);
            when(listQuery.where(predicate)).thenReturn(listQuery);

            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);
            when(roleRepository.getQuerydsl()).thenReturn(querydsl);
            when(querydsl.applyPagination(any(), eq(listQuery))).thenReturn(paginatedQuery);
            when(paginatedQuery.fetch()).thenReturn(roleDTOs);

            when(countQuery.from(any(EntityPath.class))).thenReturn(countQuery);
            when(countQuery.leftJoin(any(EntityPath.class))).thenReturn(countQuery);
            when(countQuery.on(any(Predicate.class))).thenReturn(countQuery);
            when(countQuery.where(predicate)).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(totalCount);

            Page<RoleDTO> result = roleService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(10, result.getContent().size());
            assertEquals(totalCount, result.getTotalElements());
        }

        @Test
        @SuppressWarnings("unchecked")
        void findPage_CountQueryReturnsNull() {
            Pageable pageable = PageRequest.of(0, 10);
            Predicate predicate = mock(Predicate.class);

            when(roleRepository.getJPAQueryFactory()).thenReturn(queryFactory);

            when(queryFactory.select(any(Expression.class))).thenReturn(listQuery);
            when(listQuery.from(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.leftJoin(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.on(any(Predicate.class))).thenReturn(listQuery);
            when(listQuery.where(predicate)).thenReturn(listQuery);

            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);
            when(roleRepository.getQuerydsl()).thenReturn(querydsl);
            when(querydsl.applyPagination(any(), eq(listQuery))).thenReturn(paginatedQuery);
            when(paginatedQuery.fetch()).thenReturn(Collections.emptyList());

            Page<RoleDTO> result = roleService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Get Related Entities")
    class GetRelatedEntitiesTests {
        @Test
        void getMenusByRole_Success() {
            Role mockRole = mock(Role.class);
            Menu mockMenu = mock(Menu.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(mockRole.getMenus()).thenReturn(Set.of(mockMenu));
            when(menuMapper.toDto(mockMenu)).thenReturn(new MenuDTO());

            Set<MenuDTO> result = roleService.getMenusByRole(1L);
            assertEquals(1, result.size());
        }

        @Test
        void getMenusByRole_NotFound_ThrowsException() {
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> roleService.getMenusByRole(1L));
        }

        @Test
        void getPermissionsByRole_Success() {
            Role mockRole = mock(Role.class);
            Permission mockPermission = mock(Permission.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(mockRole.getPermissions()).thenReturn(Set.of(mockPermission));
            when(permissionMapper.toDto(mockPermission)).thenReturn(new PermissionDTO());

            Set<PermissionDTO> result = roleService.getPermissionsByRole(1L);
            assertEquals(1, result.size());
        }

        @Test
        void getPermissionsByRole_NotFound_ThrowsException() {
            when(roleRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(NotFoundException.class, () -> roleService.getPermissionsByRole(1L));
        }
    }

    @Test
    void testToDto() {
        Role entity = mock(Role.class);
        RoleDTO dto = new RoleDTO();
        when(roleMapper.toDto(entity)).thenReturn(dto);
        assertSame(dto, roleService.toDto(entity));
    }

    @Test
    void testGetRepository() {
        assertSame(roleRepository, roleService.getRepository());
    }
}
