package com.mok.ddd.application.sys.dto.tenantPackage;

import com.mok.ddd.domain.sys.model.QTenantPackage;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class TenantPackageQuery {
    private String name;
    private Integer state;

    public Predicate toPredicate() {
        QTenantPackage qTenantPackage = QTenantPackage.tenantPackage;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(name)) {
            builder.and(qTenantPackage.name.containsIgnoreCase(name));
        }
        if (state != null) {
            builder.and(qTenantPackage.state.eq(state));
        }
        return builder;
    }
}
