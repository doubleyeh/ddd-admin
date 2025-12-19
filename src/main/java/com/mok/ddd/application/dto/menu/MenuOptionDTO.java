package com.mok.ddd.application.dto.menu;

import com.mok.ddd.application.dto.permission.PermissionOptionDTO;
import lombok.Data;

import java.util.List;

@Data
public class MenuOptionDTO {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private List<MenuOptionDTO> children;
    private List<PermissionOptionDTO> permissions;
    private Boolean isPermission = false;
}