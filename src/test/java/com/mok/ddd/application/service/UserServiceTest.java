package com.mok.ddd.application.service;

import com.mok.ddd.application.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.dto.user.UserPasswordDTO;
import com.mok.ddd.application.dto.user.UserPostDTO;
import com.mok.ddd.application.dto.user.UserPutDTO;
import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import com.mok.ddd.application.mapper.MenuMapper;
import com.mok.ddd.application.mapper.UserMapper;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.Menu;
import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.domain.entity.Role;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 单元测试")
class UserServiceTest {

    @InjectMocks
    @Spy
    private UserService userService;

    @Mock
    private UserRepository userRepository;

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

    private final String ENCODED_PASSWORD = "encodedPassword123";
    private final String MOCK_SUPER_ADMIN_TENANT_ID = "SUPER_ADMIN_TENANT";

    @Nested
    @DisplayName("用户创建测试")
    class CreateTests {
        private UserPostDTO dto;
        private User savedUser;
        private UserDTO userDTO;

        @BeforeEach
        void setup() {
            dto = new UserPostDTO();
            dto.setUsername("testuser");
            dto.setPassword("rawPassword");
            dto.setTenantId("000000");

            savedUser = new User();
            savedUser.setId(1L);
            savedUser.setUsername("testuser");
            savedUser.setTenantId("000000");
            savedUser.setPassword(ENCODED_PASSWORD);

            userDTO = new UserDTO();
            userDTO.setId(1L);
        }

        @Test
        @DisplayName("创建用户成功")
        void create_Success() {
            try (MockedStatic<TenantContextHolder> tenantContext = mockStatic(TenantContextHolder.class)) {
                tenantContext.when(TenantContextHolder::getTenantId).thenReturn("000000");

                User newUser = mock(User.class);
                when(userRepository.findByTenantIdAndUsername("000000", dto.getUsername())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
                when(userMapper.postToEntity(dto)).thenReturn(newUser);
                when(userRepository.save(newUser)).thenReturn(savedUser);
                doReturn(userDTO).when(userService).toDto(savedUser);

                UserDTO result = userService.create(dto);

                verify(userRepository).findByTenantIdAndUsername("000000", dto.getUsername());
                assertEquals(userDTO, result);
            }
        }

        @Test
        @DisplayName("创建用户失败：用户名已存在")
        void create_UsernameExists_ThrowsBizException() {
            try (MockedStatic<TenantContextHolder> tenantContext = mockStatic(TenantContextHolder.class)) {
                tenantContext.when(TenantContextHolder::getTenantId).thenReturn("000000");

                when(userRepository.findByTenantIdAndUsername("000000", dto.getUsername()))
                        .thenReturn(Optional.of(new User()));

                assertThrows(BizException.class, () -> userService.create(dto));
            }
        }

        @Test
        @DisplayName("为特定租户创建管理员成功")
        void createForTenant_Success() {
            User newUser = mock(User.class);
            String tenantId = "tenantA";

            when(userRepository.findByTenantIdAndUsername(tenantId, dto.getUsername())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(any())).thenReturn(ENCODED_PASSWORD);
            when(userMapper.postToEntity(dto)).thenReturn(newUser);
            when(userRepository.save(newUser)).thenReturn(savedUser);
            doReturn(userDTO).when(userService).toDto(savedUser);

            UserDTO result = userService.createForTenant(dto, tenantId);

            verify(userRepository, times(1)).findByTenantIdAndUsername(tenantId, dto.getUsername());
            verify(newUser).setTenantId(tenantId);
            assertEquals(userDTO, result);
        }

        @Test
        @DisplayName("为特定租户创建失败：用户名已存在")
        void createForTenant_UsernameExists_ThrowsBizException() {
            String tenantId = "tenantA";
            when(userRepository.findByTenantIdAndUsername(tenantId, dto.getUsername())).thenReturn(Optional.of(new User()));

            assertThrows(BizException.class, () -> userService.createForTenant(dto, tenantId));

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("用户更新测试")
    class UpdateTests {
        private final Long userId = 1L;
        private UserPutDTO putDTO;
        private UserDTO userDTO;

        @BeforeEach
        void setup() {
            putDTO = new UserPutDTO();
            putDTO.setId(userId);
            userDTO = new UserDTO();
        }

        @Test
        @DisplayName("更新用户信息成功")
        void updateUser_Success() {
            User existingUser = mock(User.class);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenReturn(existingUser);
            doReturn(userDTO).when(userService).toDto(any());

            UserDTO result = userService.updateUser(putDTO);

            verify(userRepository, times(1)).findById(userId);
            verify(userMapper, times(1)).putToEntity(putDTO, existingUser);
            verify(userRepository, times(1)).save(existingUser);
            assertNotNull(result);
        }

        @Test
        @DisplayName("更新用户信息失败：用户不存在")
        void updateUser_NotFound_ThrowsNotFoundException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.updateUser(putDTO));
        }

        @Test
        @DisplayName("更新密码成功")
        void updatePassword_Success() {
            User existingUser = mock(User.class);

            UserPasswordDTO passwordDTO = new UserPasswordDTO();
            passwordDTO.setId(userId);
            passwordDTO.setPassword("newRawPassword");

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.encode(any())).thenReturn("newEncodedPassword");

            userService.updatePassword(passwordDTO);

            verify(userRepository, times(1)).findById(userId);
            verify(existingUser).setPassword("newEncodedPassword");
            verify(userRepository, times(1)).save(existingUser);
        }

        @Test
        @DisplayName("更新密码失败：用户不存在")
        void updatePassword_NotFound_ThrowsNotFoundException() {
            UserPasswordDTO passwordDTO = new UserPasswordDTO();
            passwordDTO.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.updatePassword(passwordDTO));
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("用户删除测试")
    class DeleteTests {
        private final Long userId = 1L;
        private User user;

        @BeforeEach
        void setup() {
            user = new User();
            user.setId(userId);
            user.setTenantId("tenantA");
            user.setUsername("regularUser");
        }

        @Test
        @DisplayName("删除普通用户成功")
        void deleteById_Success() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperAdmin(user.getTenantId(), user.getUsername())).thenReturn(false);

                doNothing().when(userRepository).deleteById(userId);

                userService.deleteById(userId);

                verify(userRepository, times(1)).findById(userId);
                mockedSysUtil.verify(() -> SysUtil.isSuperAdmin(user.getTenantId(), user.getUsername()), times(1));
                verify(userRepository, times(1)).deleteById(userId);
            }
        }

        @Test
        @DisplayName("删除用户失败：用户不存在")
        void deleteById_NotFound_ThrowsNotFoundException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.deleteById(userId));
        }

        @Test
        @DisplayName("删除用户失败：尝试删除超级管理员")
        void deleteById_IsSuperAdmin_ThrowsBizException() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperAdmin(user.getTenantId(), user.getUsername())).thenReturn(true);

                assertThrows(BizException.class, () -> userService.deleteById(userId));

                verify(userRepository, never()).deleteById(any());
            }
        }
    }

    @Nested
    @DisplayName("用户查询测试")
    class QueryTests {
        private final String username = "testuser";
        private User user;
        private UserDTO userDTO;

        @BeforeEach
        void setup() {
            user = new User();
            userDTO = new UserDTO();
        }

        @Test
        @DisplayName("根据用户名查找成功")
        void findByUsername_Success() {
            doReturn(userDTO).when(userService).toDto(user);
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

            UserDTO result = userService.findByUsername(username);

            assertEquals(userDTO, result);
        }

        @Test
        @DisplayName("根据用户名查找失败：用户不存在")
        void findByUsername_NotFound_ThrowsNotFoundException() {
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.findByUsername(username));
        }
    }

    @Nested
    @DisplayName("findAccountInfoByUsername 账号信息测试")
    class AccountInfoTests {
        private final String username = "testuser";
        private User user;
        private UserDTO userDTO;

        @BeforeEach
        void setup() {
            user = new User();
            user.setTenantId(MOCK_SUPER_ADMIN_TENANT_ID);
            user.setUsername(username);
            userDTO = new UserDTO();
        }

        @Test
        @DisplayName("查找超级管理员账号信息成功")
        void findAccountInfoByUsername_SuperAdmin_Success() {
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            doReturn(userDTO).when(userService).toDto(user);

            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperAdmin(anyString(), anyString())).thenReturn(true);

                MenuDTO menu1 = new MenuDTO();
                menu1.setSort(1);
                MenuDTO menu2 = new MenuDTO();
                menu2.setSort(2);

                List<MenuDTO> flatMenus = new ArrayList<>(List.of(menu1, menu2));

                Set<String> allPermissions = Set.of("P1", "P2");
                List<MenuDTO> menuTree = List.of(new MenuDTO());

                when(menuService.findAll()).thenReturn(flatMenus);
                when(permissionService.getAllPermissionCodes()).thenReturn(allPermissions);
                when(menuService.buildMenuTree(any())).thenReturn(menuTree);

                AccountInfoDTO result = userService.findAccountInfoByUsername(username);

                verify(menuService, times(1)).findAll();
                verify(permissionService, times(1)).getAllPermissionCodes();
                verify(menuService, times(1)).buildMenuTree(flatMenus);
                assertEquals(menuTree, result.getMenus());
                assertEquals(allPermissions, result.getPermissions());
            }
        }

        @Test
        @DisplayName("查找普通用户账号信息成功")
        void findAccountInfoByUsername_RegularUser_Success() {
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            doReturn(userDTO).when(userService).toDto(user);

            try (MockedStatic<SysUtil> mockedSysUtil = mockStatic(SysUtil.class)) {
                mockedSysUtil.when(() -> SysUtil.isSuperAdmin(anyString(), anyString())).thenReturn(false);

                Menu m1 = new Menu();
                Menu m2 = new Menu();
                Permission p1 = new Permission();
                p1.setCode("R_P1");
                Permission p2 = new Permission();
                p2.setCode("R_P2");

                Role r1 = new Role();
                r1.setMenus(Set.of(m1));
                r1.setPermissions(Set.of(p1));
                Role r2 = new Role();
                r2.setMenus(Set.of(m2));
                r2.setPermissions(Set.of(p2));
                user.setRoles(Set.of(r1, r2));

                MenuDTO dto1 = new MenuDTO();
                MenuDTO dto2 = new MenuDTO();

                List<MenuDTO> flatMenus = new ArrayList<>(List.of(dto1, dto2));
                List<MenuDTO> menuTree = List.of(new MenuDTO());

                when(menuMapper.toDtoList(anySet())).thenReturn(flatMenus);
                when(menuService.buildMenuTree(any())).thenReturn(menuTree);

                AccountInfoDTO result = userService.findAccountInfoByUsername(username);

                verify(menuMapper, times(1)).toDtoList(Set.of(m1, m2));
                verify(menuService, times(1)).buildMenuTree(flatMenus);
                assertEquals(menuTree, result.getMenus());
                assertEquals(Set.of("R_P1", "R_P2"), result.getPermissions());
            }
        }

        @Test
        @DisplayName("查找账号信息失败：用户不存在")
        void findAccountInfoByUsername_UserNotFound_ThrowsNotFoundException() {
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> userService.findAccountInfoByUsername(username));
        }
    }
}