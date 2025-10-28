package com.mok.ddd.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import com.mok.ddd.infrastructure.security.TenantContextHolder;

@MappedSuperclass
@Getter
@Setter
@FilterDef(name = "tenantFilter", parameters = {
        @ParamDef(name = "tenantId", type = String.class)
})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantBaseEntity extends BaseEntity {

    @Column(name = "tenant_id", updatable = false)
    private String tenantId;

    @Override
    @PrePersist
    public void prePersist() {
        if (this.tenantId == null) {
            // 插入时自动从上下文填充 tenantId
            this.tenantId = TenantContextHolder.getTenantId();
        }
        super.prePersist();
    }
}