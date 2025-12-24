package com.mok.ddd.domain.repository;

import com.mok.ddd.domain.entity.Tenant;
import com.mok.ddd.infrastructure.repository.CustomRepository;

import java.util.Optional;

public interface TenantRepository extends CustomRepository<Tenant, Long> {
    Optional<Tenant> findByTenantId(String tenantId);

    long countByPackageId(Long packageId);
}