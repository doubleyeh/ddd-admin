package com.mok.ddd.application.dto.tenantPackage;

import lombok.Data;

import java.util.Set;

@Data
public class TenantPackageDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean enabled;
    private Set<Long> menuIds;
    private Set<Long> permissionIds;
}
