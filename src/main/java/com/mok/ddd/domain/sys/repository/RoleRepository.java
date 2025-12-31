package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.Role;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends CustomRepository<Role, Long> {

    @Query("select count(u) > 0 from com.mok.ddd.domain.sys.model.User u join u.roles r where r.id = :roleId")
    boolean existsUserAssociatedWithRole(@Param("roleId") Long roleId);
}