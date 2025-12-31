package com.mok.ddd.application.sys.dto.permission;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;
    private Long menuId;
    private String name;
    private String code;
    private String url;
    private String method;
    private String description;
}