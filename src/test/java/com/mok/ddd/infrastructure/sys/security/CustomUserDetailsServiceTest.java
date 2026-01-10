package com.mok.ddd.infrastructure.sys.security;

import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.service.TenantCacheService;
import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.User;
import com.mok.ddd.domain.sys.repository.UserRepository;
import com.mok.ddd.infrastructure.security.CustomUserDetail;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantCacheService tenantCacheService;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_Success() {
        String username = "testuser";
        String tenantId = "test-tenant";

        ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .run(() -> {
                    TenantDTO tenant = new TenantDTO();
                    tenant.setState(Const.TenantState.NORMAL);

                    User user = User.create(username, "password", "test", false);
                    user.assignTenant(tenantId);
                    user.changeRoles(new HashSet<>());

                    when(tenantCacheService.findByTenantId(tenantId)).thenReturn(tenant);
                    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    assertNotNull(userDetails);
                    assertEquals(username, userDetails.getUsername());
                });
    }

    @Test
    void loadUserByUsername_TenantNotSet() {
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("testuser");
        });
    }

    @Test
    void loadUserByUsername_TenantNotFound() {
        String tenantId = "test-tenant";
        ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .run(() -> {
                    when(tenantCacheService.findByTenantId(tenantId)).thenReturn(null);

                    assertThrows(UsernameNotFoundException.class, () -> {
                        customUserDetailsService.loadUserByUsername("testuser");
                    });
                });
    }

    @Test
    void loadUserByUsername_TenantDisabled() {
        String tenantId = "test-tenant";
        ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .run(() -> {
                    TenantDTO tenant = new TenantDTO();
                    tenant.setState(Const.TenantState.DISABLED);

                    when(tenantCacheService.findByTenantId(tenantId)).thenReturn(tenant);

                    assertThrows(UsernameNotFoundException.class, () -> {
                        customUserDetailsService.loadUserByUsername("testuser");
                    });
                });
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        String username = "testuser";
        String tenantId = "test-tenant";
        ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .run(() -> {
                    TenantDTO tenant = new TenantDTO();
                    tenant.setState(Const.TenantState.NORMAL);

                    when(tenantCacheService.findByTenantId(tenantId)).thenReturn(tenant);
                    when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

                    assertThrows(UsernameNotFoundException.class, () -> {
                        customUserDetailsService.loadUserByUsername(username);
                    });
                });
    }

    @Test
    void loadUserByUsername_UserDisabled() {
        String username = "testuser";
        String tenantId = "test-tenant";
        ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .run(() -> {
                    TenantDTO tenant = new TenantDTO();
                    tenant.setState(Const.TenantState.NORMAL);

                    User user = User.create(username, "password", "test", false);
                    user.disable();
                    user.changeRoles(new HashSet<>());

                    when(tenantCacheService.findByTenantId(tenantId)).thenReturn(tenant);
                    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

                    assertThrows(UsernameNotFoundException.class, () -> {
                        customUserDetailsService.loadUserByUsername(username);
                    });
                });
    }

    @Test
    void loadUserByUsername_SuperAdmin() {
        String username = Const.SUPER_ADMIN_USERNAME;
        String tenantId = Const.DEFAULT_TENANT_ID;
        ScopedValue.where(TenantContextHolder.TENANT_ID, tenantId)
                .run(() -> {
                    TenantDTO tenant = new TenantDTO();
                    tenant.setState(Const.TenantState.NORMAL);

                    User user = User.create(username, "password", "super", false);
                    user.changeRoles(new HashSet<>());

                    when(tenantCacheService.findByTenantId(tenantId)).thenReturn(tenant);
                    when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                    assertNotNull(userDetails);
                    assertInstanceOf(CustomUserDetail.class, userDetails);
                    CustomUserDetail customUserDetail = (CustomUserDetail) userDetails;
                    assertTrue(customUserDetail.isSuperAdmin());
                    assertTrue(customUserDetail.getRoleIds().contains(0L));
                });
    }
}