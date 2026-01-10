package com.mok.ddd.infrastructure.sys.security;

import com.mok.ddd.application.sys.dto.tenant.TenantDTO;
import com.mok.ddd.application.sys.service.TenantCacheService;
import com.mok.ddd.common.SysUtil;
import com.mok.ddd.domain.common.model.BaseEntity;
import com.mok.ddd.domain.sys.model.User;
import com.mok.ddd.domain.sys.repository.UserRepository;
import com.mok.ddd.infrastructure.security.CustomUserDetail;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Primary;
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
    private final TenantCacheService tenantCacheService;

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        String tenantId = TenantContextHolder.getTenantId();

        if (!StringUtils.hasLength(tenantId)) {
            throw new UsernameNotFoundException("租户不能为空");
        }

        TenantDTO tenant = tenantCacheService.findByTenantId(tenantId);
        if (tenant == null || !tenant.isEnabled()) {
            throw new UsernameNotFoundException("租户不存在或已被禁用");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在或密码错误"));

        boolean isSuperAdmin = SysUtil.isSuperAdmin(tenantId, user.getUsername());
        Set<Long> roleIds = new HashSet<>();
        if(Objects.equals(0, user.getState())){
            throw new UsernameNotFoundException("用户已被禁用");
        }
        if (user.getRoles() != null) {
            roleIds = user.getRoles().stream().map(BaseEntity::getId).collect(Collectors.toSet());
        }
        if (isSuperAdmin) {
            roleIds.add(0L);
        }

        return new CustomUserDetail(user.getId(), user.getUsername(), user.getPassword(), tenantId, roleIds, isSuperAdmin);
    }
}