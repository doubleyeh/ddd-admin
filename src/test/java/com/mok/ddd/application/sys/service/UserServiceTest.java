package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.user.UserDTO;
import com.mok.ddd.application.sys.dto.user.UserPasswordDTO;
import com.mok.ddd.application.sys.dto.user.UserPostDTO;
import com.mok.ddd.application.sys.dto.user.UserPutDTO;
import com.mok.ddd.application.sys.mapper.MenuMapper;
import com.mok.ddd.application.sys.mapper.UserMapper;
import com.mok.ddd.common.Const;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.sys.model.*;
import com.mok.ddd.domain.sys.repository.RoleRepository;
import com.mok.ddd.domain.sys.repository.TenantPackageRepository;
import com.mok.ddd.domain.sys.repository.TenantRepository;
import com.mok.ddd.domain.sys.repository.UserRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MenuService menuService;
    @Mock
    private MenuMapper menuMapper;
    @Mock
    private PermissionService permissionService;
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private TenantPackageRepository tenantPackageRepository;

    private MockedStatic<User> mockedUser;
    private MockedStatic<TenantContextHolder> mockedTenantContext;
    private MockedStatic<SysUtil> mockedSysUtil;

    private final String ENCODED_PASSWORD = "encodedPassword123";

    @BeforeEach
    void setUp() {
        mockedUser = mockStatic(User.class);
        mockedTenantContext = mockStatic(TenantContextHolder.class);
        mockedSysUtil = mockStatic(SysUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedUser.close();
        mockedTenantContext.close();
        mockedSysUtil.close();
    }

    @Nested
    @DisplayName("create")
    class CreateTests {
        @Test
        void create_Success() {
            UserPostDTO dto = new UserPostDTO();
            dto.setUsername("test");
            dto.setPassword("raw");
            dto.setNickname("nick");
            dto.setTenantId("tenantA");
            dto.setRoleIds(List.of(1L));

            mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("tenantA");
            when(userRepository.findByTenantIdAndUsername("tenantA", "test")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("raw")).thenReturn(ENCODED_PASSWORD);

            User mockUser = mock(User.class);
            mockedUser.when(() -> User.create("test", ENCODED_PASSWORD, "nick", false)).thenReturn(mockUser);

            Role mockRole = mock(Role.class);
            when(roleRepository.findAllById(anyList())).thenReturn(List.of(mockRole));
            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(userMapper.toDto(mockUser)).thenReturn(new UserDTO());

            userService.create(dto);

            verify(mockUser).changeRoles(new HashSet<>(List.of(mockRole)));
            verify(userRepository).save(mockUser);
        }
        
        @Test
        void create_UsernameExists_ThrowsException() {
            UserPostDTO dto = new UserPostDTO();
            dto.setUsername("test");
            dto.setTenantId("tenantA");
            
            mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("tenantA");
            when(userRepository.findByTenantIdAndUsername("tenantA", "test")).thenReturn(Optional.of(mock(User.class)));
            
            assertThrows(com.mok.ddd.application.exception.BizException.class, () -> userService.create(dto));
        }
        
        @Test
        void create_UnauthorizedTenant_ThrowsException() {
            UserPostDTO dto = new UserPostDTO();
            dto.setTenantId("tenantB");
            
            mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("tenantA");
            mockedSysUtil.when(() -> SysUtil.isSuperTenant("tenantA")).thenReturn(false);
            
            assertThrows(com.mok.ddd.application.exception.BizException.class, () -> userService.create(dto));
        }

        @Test
        void createForTenant_Success() {
            UserPostDTO dto = new UserPostDTO();
            dto.setUsername("testuser");
            dto.setPassword("rawPassword");
            dto.setTenantId("tenantA");
            dto.setNickname("Tenant Admin");

            when(userRepository.findByTenantIdAndUsername("tenantA", "testuser")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn(ENCODED_PASSWORD);

            User mockUser = mock(User.class);
            mockedUser.when(() -> User.create("testuser", ENCODED_PASSWORD, "Tenant Admin", true)).thenReturn(mockUser);

            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(userMapper.toDto(mockUser)).thenReturn(new UserDTO());

            userService.createForTenant(dto);

            mockedUser.verify(() -> User.create("testuser", ENCODED_PASSWORD, "Tenant Admin", true));
            verify(mockUser).assignTenant("tenantA");
            verify(userRepository).save(mockUser);
        }
        
        @Test
        void createForTenant_NullTenantId_ThrowsException() {
            UserPostDTO dto = new UserPostDTO();
            dto.setTenantId(null);
            assertThrows(com.mok.ddd.application.exception.BizException.class, () -> userService.createForTenant(dto));
        }
        
        @Test
        void createForTenant_UsernameExists_ThrowsException() {
            UserPostDTO dto = new UserPostDTO();
            dto.setUsername("test");
            dto.setTenantId("tenantA");
            
            when(userRepository.findByTenantIdAndUsername("tenantA", "test")).thenReturn(Optional.of(mock(User.class)));
            
            assertThrows(com.mok.ddd.application.exception.BizException.class, () -> userService.createForTenant(dto));
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateTests {
        @Test
        void updateUser_Success() {
            UserPutDTO dto = new UserPutDTO();
            dto.setId(1L);
            dto.setNickname("newNick");
            dto.setRoleIds(List.of(2L));

            User mockUser = mock(User.class);
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            Role mockRole = mock(Role.class);
            when(roleRepository.findAllById(anyList())).thenReturn(List.of(mockRole));

            userService.updateUser(dto);

            verify(mockUser).updateInfo("newNick", new HashSet<>(List.of(mockRole)));
            verify(userRepository).save(mockUser);
        }
        
        @Test
        void updateUser_NotFound_ThrowsException() {
            UserPutDTO dto = new UserPutDTO();
            dto.setId(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.updateUser(dto));
        }
        
        @Test
        void updateNickname_Success() {
            Long id = 1L;
            String newNickname = "New Nick";
            User mockUser = mock(User.class);
            when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));
            
            userService.updateNickname(id, newNickname);
            
            verify(mockUser).updateInfo(eq(newNickname), any());
            verify(userRepository).save(mockUser);
        }
        
        @Test
        void updateNickname_NotFound_ThrowsException() {
            Long id = 1L;
            when(userRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.updateNickname(id, "nick"));
        }
    }

    @Nested
    @DisplayName("updateUserState")
    class UpdateStateTests {
        @Test
        void updateUserState_ToNormal() {
            User mockUser = mock(User.class);
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            userService.updateUserState(1L, Const.UserState.NORMAL);
            verify(mockUser).enable();
            verify(userRepository).save(mockUser);
        }

        @Test
        void updateUserState_ToDisabled() {
            User mockUser = mock(User.class);
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            userService.updateUserState(1L, Const.UserState.DISABLED);
            verify(mockUser).disable();
            verify(userRepository).save(mockUser);
        }
        
        @Test
        void updateUserState_NotFound_ThrowsException() {
            Long id = 1L;
            when(userRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.updateUserState(id, 1));
        }
    }

    @Nested
    @DisplayName("updatePassword")
    class UpdatePasswordTests {
        @Test
        void updatePassword_Success() {
            UserPasswordDTO dto = new UserPasswordDTO();
            dto.setId(1L);
            dto.setPassword("newRaw");

            User mockUser = mock(User.class);
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.encode("newRaw")).thenReturn("newEncoded");

            userService.updatePassword(dto);

            verify(mockUser).changePassword("newEncoded");
            verify(userRepository).save(mockUser);
        }
        
        @Test
        void updatePassword_NotFound_ThrowsException() {
            UserPasswordDTO dto = new UserPasswordDTO();
            dto.setId(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.updatePassword(dto));
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteTests {
        @Test
        void deleteById_Success() {
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("tenantA");
            when(mockUser.getUsername()).thenReturn("userA");
            when(mockUser.getIsTenantAdmin()).thenReturn(false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("tenantA", "userA")).thenReturn(false);

            userService.deleteById(1L);

            verify(userRepository).deleteById(1L);
        }
        
        @Test
        void deleteById_NotFound_ThrowsException() {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.deleteById(1L));
        }
        
        @Test
        void deleteById_SuperAdmin_ThrowsException() {
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("tenantA");
            when(mockUser.getUsername()).thenReturn("userA");
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("tenantA", "userA")).thenReturn(true);
            
            assertThrows(com.mok.ddd.application.exception.BizException.class, () -> userService.deleteById(1L));
        }
        
        @Test
        void deleteById_TenantAdmin_ThrowsException() {
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("tenantA");
            when(mockUser.getUsername()).thenReturn("userA");
            when(mockUser.getIsTenantAdmin()).thenReturn(true);
            
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("tenantA", "userA")).thenReturn(false);
            
            assertThrows(com.mok.ddd.application.exception.BizException.class, () -> userService.deleteById(1L));
        }
    }

    @Nested
    @DisplayName("Read Operations")
    class ReadTests {
        @Mock
        private JPAQueryFactory queryFactory;
        @Mock
        private JPAQuery<UserDTO> listQuery;
        @Mock
        private JPAQuery<Long> countQuery;
        @Mock
        private JPQLQuery<UserDTO> paginatedQuery;

        @Test
        @SuppressWarnings("unchecked")
        void findPage_Success() {
            Pageable pageable = PageRequest.of(0, 10);
            Predicate predicate = mock(Predicate.class);
            List<UserDTO> userDTOs = IntStream.range(0, 10).mapToObj(_ -> new UserDTO()).collect(Collectors.toList());
            long totalCount = 20L;

            when(userRepository.getJPAQueryFactory()).thenReturn(queryFactory);
            
            when(queryFactory.select(any(Expression.class))).thenReturn(listQuery, countQuery);

            when(listQuery.from(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.leftJoin(any(EntityPath.class))).thenReturn(listQuery);
            when(listQuery.on(any(Predicate.class))).thenReturn(listQuery);
            when(listQuery.where(any(Predicate.class))).thenReturn(listQuery);

            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);
            when(userRepository.getQuerydsl()).thenReturn(querydsl);
            when(querydsl.applyPagination(any(), eq(listQuery))).thenReturn(paginatedQuery);
            when(paginatedQuery.fetch()).thenReturn(userDTOs);

            when(countQuery.from(any(EntityPath.class))).thenReturn(countQuery);
            when(countQuery.leftJoin(any(EntityPath.class))).thenReturn(countQuery);
            when(countQuery.on(any(Predicate.class))).thenReturn(countQuery);
            when(countQuery.where(any(Predicate.class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(totalCount);

            Page<UserDTO> result = userService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(10, result.getContent().size());
            assertEquals(totalCount, result.getTotalElements());
        }

        @Test
        void findByUsername_Success() {
            String username = "test";
            User mockUser = mock(User.class);
            UserDTO mockDto = new UserDTO();
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            when(userMapper.toDto(mockUser)).thenReturn(mockDto);

            UserDTO result = userService.findByUsername(username);
            assertSame(mockDto, result);
        }
        
        @Test
        void findByUsername_NotFound_ThrowsException() {
            String username = "test";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.findByUsername(username));
        }

        @Test
        void findAccountInfoByUsername_SuperAdmin() {
            String username = "root";
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("000000");
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("000000", username)).thenReturn(true);

            when(menuService.findAll()).thenReturn(List.of(new MenuDTO()));
            when(permissionService.getAllPermissionCodes()).thenReturn(Set.of("perm1"));
            when(menuService.buildMenuTree(anyList())).thenReturn(List.of(new MenuDTO()));

            AccountInfoDTO result = userService.findAccountInfoByUsername(username);

            assertNotNull(result);
            assertTrue(result.getPermissions().contains(Const.SUPER_ADMIN_ROLE_CODE));
        }
        
        @Test
        void findAccountInfoByUsername_TenantAdmin() {
            String username = "admin";
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("tenant1");
            when(mockUser.getIsTenantAdmin()).thenReturn(true);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("tenant1", username)).thenReturn(false);
            
            Tenant mockTenant = mock(Tenant.class);
            when(mockTenant.getPackageId()).thenReturn(1L);
            when(tenantRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mockTenant));
            
            TenantPackage mockPackage = mock(TenantPackage.class);
            when(tenantPackageRepository.findById(1L)).thenReturn(Optional.of(mockPackage));
            when(mockPackage.getMenus()).thenReturn(Set.of(mock(Menu.class)));
            when(mockPackage.getPermissions()).thenReturn(Set.of(mock(Permission.class)));
            
            when(menuMapper.toDtoList(any())).thenReturn(List.of(new MenuDTO()));
            when(menuService.buildMenuTree(anyList())).thenReturn(List.of(new MenuDTO()));

            AccountInfoDTO result = userService.findAccountInfoByUsername(username);

            assertNotNull(result);
        }
        
        @Test
        void findAccountInfoByUsername_TenantAdmin_NoPackage() {
            String username = "admin";
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("tenant1");
            when(mockUser.getIsTenantAdmin()).thenReturn(true);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("tenant1", username)).thenReturn(false);
            
            Tenant mockTenant = mock(Tenant.class);
            when(mockTenant.getPackageId()).thenReturn(null);
            when(tenantRepository.findByTenantId("tenant1")).thenReturn(Optional.of(mockTenant));
            
            AccountInfoDTO result = userService.findAccountInfoByUsername(username);
            
            assertNotNull(result);
            assertTrue(result.getMenus().isEmpty());
            assertTrue(result.getPermissions().isEmpty());
        }
        
        @Test
        void findAccountInfoByUsername_NormalUser() {
            String username = "user";
            User mockUser = mock(User.class);
            when(mockUser.getTenantId()).thenReturn("tenant1");
            when(mockUser.getIsTenantAdmin()).thenReturn(false);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
            mockedSysUtil.when(() -> SysUtil.isSuperAdmin("tenant1", username)).thenReturn(false);
            
            Role mockRole = mock(Role.class);
            when(mockUser.getRoles()).thenReturn(Set.of(mockRole));
            when(mockRole.getMenus()).thenReturn(Set.of(mock(Menu.class)));
            when(mockRole.getPermissions()).thenReturn(Set.of(mock(Permission.class)));
            
            when(menuMapper.toDtoList(any())).thenReturn(List.of(new MenuDTO()));
            when(menuService.buildMenuTree(anyList())).thenReturn(List.of(new MenuDTO()));

            AccountInfoDTO result = userService.findAccountInfoByUsername(username);

            assertNotNull(result);
        }
        
        @Test
        void findAccountInfoByUsername_NotFound_ThrowsException() {
            String username = "test";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
            assertThrows(com.mok.ddd.application.exception.NotFoundException.class, () -> userService.findAccountInfoByUsername(username));
        }
    }
}
