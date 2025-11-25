package com.mok.ddd.application.dto.role;

import java.util.Set;

import com.mok.ddd.application.dto.menu.MenuDTO;
import com.mok.ddd.application.dto.permission.PermissionDTO;

import lombok.Data;

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