package com.mok.ddd.application.dto.role;

import static com.mok.ddd.domain.entity.QRole.role;

import org.springframework.util.StringUtils;

import com.mok.ddd.application.dto.BaseQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQuery extends BaseQuery {

    private String name;
    private String code;

    @Override
    public Predicate toPredicate() {
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(this.getName())) {
            builder.and(role.name.containsIgnoreCase(this.getName()));
        }

        if (StringUtils.hasText(this.getCode())) {
            builder.and(role.code.containsIgnoreCase(this.getCode()));
        }

        return builder;
    }
}
