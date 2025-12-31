package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.Permission;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PermissionRepository extends CustomRepository<Permission, Long> {
    @Modifying
    @Query("update com.mok.ddd.domain.sys.model.Permission p set p.menu.id = ?1 where p.id in ?2")
    void bindMenuId(Long menuId, Set<Long> ids);

    @Query(value = "SELECT DISTINCT role_id FROM sys_role_permission WHERE permission_id = :id", nativeQuery = true)
    List<Long> findRoleIdsByPermissionId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM sys_role_permission WHERE permission_id = :id", nativeQuery = true)
    void deleteRolePermissionsByPermissionId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM sys_role_permission WHERE permission_id IN (SELECT id FROM sys_permission WHERE menu_id IN :menuIds)", nativeQuery = true)
    void deleteRolePermissionsByMenuIds(@Param("menuIds") List<Long> menuIds);

    @Modifying
    @Query("DELETE FROM com.mok.ddd.domain.sys.model.Permission p WHERE p.menu.id IN :menuIds")
    void deleteByMenuIds(@Param("menuIds") List<Long> menuIds);

    @Query(value = "SELECT p.code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = :roleId", nativeQuery = true)
    List<String> findCodesByRoleId(@Param("roleId") Long roleId);
}