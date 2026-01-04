package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_tenant")
@Getter
@Setter
public class Tenant extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    private String contactPerson;

    private String contactPhone;

    /**
     * 状态 (1:正常, 0:禁用)
     */
    private Integer state;

    private Long packageId;
}
