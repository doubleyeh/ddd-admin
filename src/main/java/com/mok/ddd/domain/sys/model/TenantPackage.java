package com.mok.ddd.domain.sys.model;

import com.mok.ddd.common.Const;
import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

@Entity
@Table(name = "sys_tenant_package")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static TenantPackage create(@NonNull String name, String description) {
        TenantPackage tenantPackage = new TenantPackage();
        tenantPackage.name = name;
        tenantPackage.description = description;
        tenantPackage.state = Const.TenantPackageState.NORMAL;
        return tenantPackage;
    }

    public void updateInfo(@NonNull String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void disable() {
        this.state = Const.TenantPackageState.DISABLED;
    }

    public void enable() {
        this.state = Const.TenantPackageState.NORMAL;
    }

    public void changeMenus(Set<Menu> newMenus) {
        this.menus = newMenus;
    }

    public void changePermissions(Set<Permission> newPermissions) {
        this.permissions = newPermissions;
    }
}
