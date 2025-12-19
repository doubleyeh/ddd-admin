package com.mok.ddd.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_permission")
@Getter
@Setter
public class Permission extends BaseEntity {
    private String name;
    private String code;
    private String url;
    private String method;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id")
    private Menu menu;
}