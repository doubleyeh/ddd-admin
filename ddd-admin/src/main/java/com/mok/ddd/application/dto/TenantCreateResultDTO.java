package com.mok.ddd.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantCreateResultDTO extends TenantDTO {
    private String initialAdminPassword;
}