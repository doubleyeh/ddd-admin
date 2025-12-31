package com.mok.ddd.application.sys.dto.tenant;

import lombok.Data;

@Data
public class TenantDTO {
    private Long id;
    private String tenantId;
    private String name;
    private String contactPerson;
    private String contactPhone;
    private Boolean enabled;
    private Long packageId;
    private String packageName;
}
