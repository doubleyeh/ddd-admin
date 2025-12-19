package com.mok.ddd.domain.repository;

import com.mok.ddd.domain.entity.Permission;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface PermissionRepository extends CustomRepository<Permission, Long> {
    @Modifying
    @Query("update Permission p set p.menu.id = ?1 where p.id in ?2")
    void bindMenuId(Long menuId, Set<Long> ids);
}