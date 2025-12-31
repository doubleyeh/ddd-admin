package com.mok.ddd.application.sys.dto.tenantPackage;

import lombok.Data;

@Data
public class TenantPackageSaveDTO {
    private String name;
    private String description;
    private Boolean enabled;
}
