package com.mok.ddd.domain.repository;

import com.mok.ddd.domain.entity.User;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.tenant.TenantFilter;

import java.util.Optional;

public interface UserRepository extends CustomRepository<User, Long> {
    @TenantFilter(TenantFilter.TenantFilterPolicy.FORCE)
    Optional<User> findByUsername(String username);

    Optional<User> findByTenantIdAndUsername(String tenantId, String username);
}