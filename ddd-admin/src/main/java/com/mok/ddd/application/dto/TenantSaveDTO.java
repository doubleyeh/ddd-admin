package com.mok.ddd.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantSaveDTO {
    private Long id;
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
    @NotBlank(message = "租户名称不能为空")
    private String name;
    private String contactPerson;
    private String contactPhone;
    private Boolean enabled;
}