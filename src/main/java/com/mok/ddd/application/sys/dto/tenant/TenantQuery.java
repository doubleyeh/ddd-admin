package com.mok.ddd.application.sys.dto.tenant;

import com.mok.ddd.application.common.dto.BaseQuery;
import com.mok.ddd.domain.sys.model.QTenant;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class TenantQuery extends BaseQuery {
    private String tenantId;
    private String name;
    private Integer state;

    public Predicate toPredicate() {
        QTenant tenant = QTenant.tenant;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(tenantId)) {
            builder.and(tenant.tenantId.containsIgnoreCase(tenantId));
        }
        if (StringUtils.hasText(name)) {
            builder.and(tenant.name.containsIgnoreCase(name));
        }
        if (state != null) {
            builder.and(tenant.state.eq(state));
        }
        return builder;
    }
}
