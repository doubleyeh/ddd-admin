package com.mok.ddd.infrastructure.security;

import com.mok.ddd.application.service.PermissionService;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String tenantId = TenantContextHolder.getTenantId();

        if (StringUtils.isBlank(tenantId)) {
            throw new UsernameNotFoundException("Tenant context is missing");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到: " + username));

        boolean isSuperAdmin = SysUtil.isSuperAdmin(tenantId, user.getUsername());
        Set<SimpleGrantedAuthority> authorities;
        if(Objects.equals(0, user.getState())){
            throw new BadCredentialsException("用户已被禁用");
        }
        if (isSuperAdmin) {
            Set<String> allCodes = permissionService.getAllPermissionCodes();
            authorities = allCodes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        } else {
            authorities = user.getRoles().stream()
                    .flatMap(role -> role.getPermissions().stream())
                    .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                    .collect(Collectors.toSet());
        }

        return new CustomUserDetail(user.getId(), user.getUsername(), user.getPassword(), tenantId, authorities, isSuperAdmin);
    }
}