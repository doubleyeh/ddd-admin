package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "sys_menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Boolean isHidden;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Permission> permissions = new HashSet<>();

    public static Menu create(Menu parent, String name, String path, String component, String icon, Integer sort, Boolean isHidden) {
        Menu menu = new Menu();
        menu.parent = parent;
        menu.name = name;
        menu.path = path;
        menu.component = component;
        menu.icon = icon;
        menu.sort = sort;
        menu.isHidden = isHidden;
        return menu;
    }

    public void updateInfo(Menu parent, String name, String path, String component, String icon, Integer sort, Boolean isHidden) {
        this.parent = parent;
        this.name = name;
        this.path = path;
        this.component = component;
        this.icon = icon;
        this.sort = sort;
        this.isHidden = isHidden;
    }

    public void changePermissions(Set<Permission> newPermissions) {
        this.permissions.forEach(p -> p.setMenu(null));
        this.permissions.clear();
        newPermissions.forEach(p -> p.setMenu(this));
        this.permissions.addAll(newPermissions);
    }
}
