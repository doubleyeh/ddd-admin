package com.mok.ddd.application.sys.dto.role;

import com.mok.ddd.application.common.dto.BaseQuery;
import com.mok.ddd.domain.sys.model.QRole;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.util.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQuery extends BaseQuery {

    private String name;
    private String code;
    private String tenantId;
    private Boolean enabled;

    @Override
    public Predicate toPredicate() {
        QRole role = QRole.role;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(this.getName())) {
            builder.and(role.name.containsIgnoreCase(this.getName()));
        }

        if (StringUtils.hasText(this.getCode())) {
            builder.and(role.code.containsIgnoreCase(this.getCode()));
        }

        if (StringUtils.hasText(this.getTenantId())) {
            builder.and(role.tenantId.eq(this.getTenantId()));
        }

        if (this.getEnabled() != null) {
            builder.and(role.enabled.eq(this.getEnabled()));
        }

        return builder;
    }
}