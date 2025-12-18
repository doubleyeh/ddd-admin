package com.mok.ddd.application.dto.role;

import com.mok.ddd.application.dto.BaseQuery;
import com.mok.ddd.domain.entity.QRole;
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

        return builder;
    }
}
