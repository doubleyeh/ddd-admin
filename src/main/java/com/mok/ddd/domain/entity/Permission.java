package com.mok.ddd.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
}