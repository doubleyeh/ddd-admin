package com.mok.ddd.application.dto.tenantPackage;

import com.mok.ddd.application.dto.BaseQuery;
import com.mok.ddd.domain.entity.QTenantPackage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantPackageQuery extends BaseQuery {
    private Boolean enabled;
    private String name;

    public Predicate toPredicate() {
        TenantPackageQuery query = this;
        QTenantPackage tp = QTenantPackage.tenantPackage;
        BooleanBuilder builder = new BooleanBuilder();
        if (query.getName() != null && !query.getName().isEmpty()) {
            builder.and(tp.name.containsIgnoreCase(query.getName()));
        }
        if (query.getEnabled() != null) {
            builder.and(tp.enabled.eq(query.getEnabled()));
        }
        return builder;
    }
}