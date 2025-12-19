package com.mok.ddd.application.dto.tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantOptionDTO {
    private Long id;
    private String tenantId;
    private String name;
}