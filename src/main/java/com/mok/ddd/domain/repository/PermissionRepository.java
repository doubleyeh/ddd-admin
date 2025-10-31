package com.mok.ddd.domain.repository;

import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.infrastructure.repository.CustomRepository;

public interface PermissionRepository extends CustomRepository<Permission, Long> {
}