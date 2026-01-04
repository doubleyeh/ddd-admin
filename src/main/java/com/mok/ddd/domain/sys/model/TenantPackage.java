package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "sys_tenant_package")
@Getter
@Setter
public class TenantPackage extends BaseEntity {
    private String name;
    private String description;
    private Integer state;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_package_menu",
            joinColumns = @JoinColumn(name = "package_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private Set<Menu> menus;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "sys_package_permission",
            joinColumns = @JoinColumn(name = "package_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
