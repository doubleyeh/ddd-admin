package com.mok.ddd.application.sys.dto.tenantPackage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantPackageSaveDTO {
    @NotBlank(message = "套餐名称不能为空")
    private String name;
    private String description;
    private Integer state;
}
