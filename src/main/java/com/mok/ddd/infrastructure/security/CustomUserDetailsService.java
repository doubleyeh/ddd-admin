package com.mok.ddd.infrastructure.security;

import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.entity.BaseEntity;
import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        String tenantId = TenantContextHolder.getTenantId();

        if (!StringUtils.hasLength(tenantId)) {
            throw new UsernameNotFoundException("Tenant context is missing");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户未找到: " + username));

        boolean isSuperAdmin = SysUtil.isSuperAdmin(tenantId, user.getUsername());
        Set<Long> roleIds = new HashSet<>();
        if(Objects.equals(0, user.getState())){
            throw new BadCredentialsException("用户已被禁用");
        }
        if (isSuperAdmin) {
            roleIds.add(0L);
        } else {
            roleIds = user.getRoles().stream().map(BaseEntity::getId).collect(Collectors.toSet());
        }

        return new CustomUserDetail(user.getId(), user.getUsername(), user.getPassword(), tenantId, roleIds, isSuperAdmin);
    }
}