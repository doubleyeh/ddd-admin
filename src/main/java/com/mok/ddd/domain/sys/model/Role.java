package com.mok.ddd.domain.sys.model;

import com.mok.ddd.common.Const;
import com.mok.ddd.domain.common.model.TenantBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Set;

@Entity
@Table(name = "sys_role")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public static Role create(@NonNull String name, @NonNull String code, String description, Integer sort) {
        Role role = new Role();
        role.name = name;
        role.code = code;
        role.description = description;
        role.sort = sort;
        role.state = Const.RoleState.NORMAL;
        return role;
    }

    public void updateInfo(String name, String code, String description, Integer sort) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.sort = sort;
    }

    public void disable() {
        if (this.state.equals(Const.RoleState.DISABLED)) {
            return;
        }
        this.state = Const.RoleState.DISABLED;
    }

    public void enable() {
        if (this.state.equals(Const.RoleState.NORMAL)) {
            return;
        }
        this.state = Const.RoleState.NORMAL;
    }

    public void changePermissions(Set<Permission> newPermissions) {
        this.permissions = newPermissions;
    }

    public void changeMenus(Set<Menu> newMenus) {
        this.menus = newMenus;
    }
}
