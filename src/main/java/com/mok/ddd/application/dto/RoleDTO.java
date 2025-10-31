package com.mok.ddd.application.dto;

import lombok.Data;

import java.util.Set;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer sort;
    private Boolean enabled;
    private Set<PermissionDTO> permissions;
    private Set<MenuDTO> menus;
}