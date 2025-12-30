package com.mok.ddd.application.dto.tenant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantSaveDTO {
    private Long id;
    private String tenantId;
    @NotBlank(message = "租户名称不能为空")
    @Size(min = 2, message = "租户名称最少2个字符")
    private String name;
    private String contactPerson;
    private String contactPhone;
    private Boolean enabled;
    private Long packageId;
}
