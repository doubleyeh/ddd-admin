package com.mok.ddd.application.sys.dto.tenantPackage;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import lombok.Data;

import java.util.Set;

@Data
public class TenantPackageDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean enabled;
    private Set<MenuDTO> menus;
    private Set<PermissionDTO> permissions;
}
