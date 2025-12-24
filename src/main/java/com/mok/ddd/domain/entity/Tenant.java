package com.mok.ddd.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "sys_tenant")
public class Tenant extends BaseEntity {
    private String tenantId;
    private String name;
    private String contactPerson;
    private String contactPhone;
    private Boolean enabled;
    private Long packageId;
}