package com.mok.ddd.application.sys.dto.tenantPackage;

import lombok.Data;

import java.util.Set;

@Data
public class TenantPackageGrantDTO {
    private Set<Long> menuIds;
    private Set<Long> permissionIds;
}
