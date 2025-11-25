package com.mok.ddd.application.dto.permission;

import lombok.Data;

@Data
public class PermissionDTO {
    private Long id;
    private String name;
    private String code;
    private String url;
    private String method;
    private String description;
}