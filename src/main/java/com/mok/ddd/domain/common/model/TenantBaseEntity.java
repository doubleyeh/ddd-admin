package com.mok.ddd.domain.common.model;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Getter
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = {
        @ParamDef(name = "tenantId", type = String.class)
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantBaseEntity extends BaseEntity {

    @Column(name = "tenant_id", updatable = false)
    private String tenantId;

    protected TenantBaseEntity() {
    }

    @Override
    @PrePersist
    protected void prePersist() {
        if (this.tenantId == null) {
            this.tenantId = TenantContextHolder.getTenantId();
        }
        super.prePersist();
    }

    protected void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
