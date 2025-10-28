package com.mok.ddd.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_user")
@Getter
@Setter
public class User extends TenantBaseEntity {
    private String username;
    private String password;
    private String nickname;
    private Boolean locked;
}