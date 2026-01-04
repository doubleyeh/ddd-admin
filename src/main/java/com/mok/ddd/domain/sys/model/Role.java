package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "sys_role")
@Getter
@Setter
public class Role extends TenantBaseEntity {
    private String name;
    private String code;
    private String description;
    private Integer sort;
    
    /**
     * 状态 (1:正常, 0:禁用)
     */
    private Integer state;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_role_menu",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private Set<Menu> menus;
}
