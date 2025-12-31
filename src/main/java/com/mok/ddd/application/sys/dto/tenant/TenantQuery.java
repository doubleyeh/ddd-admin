package com.mok.ddd.application.sys.dto.tenant;

import com.mok.ddd.application.common.dto.BaseQuery;
import com.mok.ddd.domain.sys.model.QTenant;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantQuery extends BaseQuery {
    private String tenantId;
    private String name;

    public Predicate toPredicate() {
        TenantQuery query = this;
        QTenant tenant = QTenant.tenant;
        BooleanBuilder builder = new BooleanBuilder();
        if (query.getTenantId() != null && !query.getTenantId().isEmpty()) {
            builder.and(tenant.tenantId.containsIgnoreCase(query.getTenantId()));
        }
        if (query.getName() != null && !query.getName().isEmpty()) {
            builder.and(tenant.name.containsIgnoreCase(query.getName()));
        }
        return builder;
    }
}