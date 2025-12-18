package com.mok.ddd.domain.repository;

import com.mok.ddd.domain.entity.Role;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends CustomRepository<Role, Long> {

    @Query("select count(u) > 0 from User u join u.roles r where r.id = :roleId")
    boolean existsUserAssociatedWithRole(@Param("roleId") Long roleId);
}