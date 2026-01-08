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
    }

    @Nested
    @DisplayName("grant")
    class GrantTests {

        @Test
        void grant_Success() {
            RoleGrantDTO dto = new RoleGrantDTO();
            dto.setMenuIds(Set.of(10L, 20L));
            dto.setPermissionIds(Set.of(100L, 200L));

            Role mockRole = mock(Role.class);
            Set<Menu> menus = dto.getMenuIds().stream().map(id -> mock(Menu.class)).collect(Collectors.toSet());
            Set<Permission> permissions = dto.getPermissionIds().stream().map(id -> mock(Permission.class)).collect(Collectors.toSet());

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(menuRepository.findAllById(dto.getMenuIds())).thenReturn(List.copyOf(menus));
            when(permissionRepository.findAllById(dto.getPermissionIds())).thenReturn(List.copyOf(permissions));

            roleService.grant(1L, dto);

            verify(mockRole).changeMenus(eq(new HashSet<>(menus)));
            verify(mockRole).changePermissions(eq(new HashSet<>(permissions)));
            verify(roleRepository).save(mockRole);
            verify(redisTemplate).delete(anyString());
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
            Set<MenuDTO> menuDtos = menus.stream().map(m -> new MenuDTO()).collect(Collectors.toSet());
            Set<PermissionDTO> permissionDtos = permissions.stream().map(p -> new PermissionDTO()).collect(Collectors.toSet());

            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(roleMapper.toDto(mockRole)).thenReturn(mockDto);
            when(mockRole.getMenus()).thenReturn(menus);
            when(mockRole.getPermissions()).thenReturn(permissions);
            when(menuMapper.toDto(any(Menu.class))).thenReturn(new MenuDTO());
            when(permissionMapper.toDto(any(Permission.class))).thenReturn(new PermissionDTO());

            RoleDTO result = roleService.getById(1L);

            assertSame(mockDto, result);
            assertEquals(menuDtos.size(), result.getMenus().size());
            assertEquals(permissionDtos.size(), result.getPermissions().size());
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
            RoleDTO roleDTO = new RoleDTO();

            when(roleRepository.getJPAQueryFactory()).thenReturn(queryFactory);

            when(queryFactory.select((Expression<RoleDTO>) any())).thenReturn(listQuery);

            when(listQuery.from(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.leftJoin(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.on(any(Predicate.class))).thenReturn(listQuery);
            when(listQuery.where(any(Predicate.class))).thenReturn(listQuery);

            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);
            when(roleRepository.getQuerydsl()).thenReturn(querydsl);
            when(querydsl.applyPagination(any(), eq(listQuery))).thenReturn(paginatedQuery);
            when(paginatedQuery.fetch()).thenReturn(List.of(roleDTO));

            Page<RoleDTO> result = roleService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
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
        void getPermissionsByRole_Success() {
            Role mockRole = mock(Role.class);
            Permission mockPermission = mock(Permission.class);
            when(roleRepository.findById(1L)).thenReturn(Optional.of(mockRole));
            when(mockRole.getPermissions()).thenReturn(Set.of(mockPermission));
            when(permissionMapper.toDto(mockPermission)).thenReturn(new PermissionDTO());

            Set<PermissionDTO> result = roleService.getPermissionsByRole(1L);
            assertEquals(1, result.size());
        }
    }

    @Test
    void testToDto() {
        Role entity = mock(Role.class);
        RoleDTO dto = new RoleDTO();
        when(roleMapper.toDto(entity)).thenReturn(dto);
        assertSame(dto, roleService.toDto(entity));
    }

}
