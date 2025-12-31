package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.Menu;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuRepository extends CustomRepository<Menu, Long> {
    List<Menu> findByParentId(Long parentId);

    @Query(value = "SELECT DISTINCT role_id FROM sys_role_menu WHERE menu_id IN :menuIds", nativeQuery = true)
    List<Long> findRoleIdsByMenuIds(@Param("menuIds") List<Long> menuIds);

    @Modifying
    @Query(value = "DELETE FROM sys_role_menu WHERE menu_id IN :menuIds", nativeQuery = true)
    void deleteRoleMenuByMenuIds(@Param("menuIds") List<Long> menuIds);
}