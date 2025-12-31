package com.mok.ddd.application.sys.dto.permission;

import lombok.Data;

@Data
public class PermissionOptionDTO {
    private Long id;
    private String name;
    private Boolean isPermission = true;
}