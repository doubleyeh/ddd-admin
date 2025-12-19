package com.mok.ddd.application.dto.role;

import lombok.Data;

import java.util.Set;

@Data
public class RoleGrantDTO {
    private Set<Long> menuIds;
    private Set<Long> permissionIds;
}