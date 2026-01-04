package com.mok.ddd.application.sys.dto.role;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.dto.permission.PermissionDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer sort;
    private Integer state;
    private String tenantId;
    private String tenantName;
    private LocalDateTime createTime;
    private Set<MenuDTO> menus;
    private Set<PermissionDTO> permissions;
}
