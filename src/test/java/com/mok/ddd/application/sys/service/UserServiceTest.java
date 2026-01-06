package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    }
}
