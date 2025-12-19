package com.mok.ddd.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "sys_menu")
@Getter
@Setter
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
    private Set<Permission> permissions;
}