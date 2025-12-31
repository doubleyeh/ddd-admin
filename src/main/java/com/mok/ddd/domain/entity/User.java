package com.mok.ddd.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "sys_user")
@Getter
@Setter
public class User extends TenantBaseEntity {
    private String username;
    private String password;
    private String nickname;
    /**
     * 1正常， 0禁用
     */
    private Integer state;
    
    /**
     * 是否为租户管理员（仅租户创建时初始化的用户为true）
     */
    @Column(name = "is_tenant_admin")
    private Boolean isTenantAdmin = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
}
