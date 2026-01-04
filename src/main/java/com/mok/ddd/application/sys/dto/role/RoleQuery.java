package com.mok.ddd.application.sys.dto.role;

import com.mok.ddd.domain.sys.model.QRole;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class RoleQuery {
    private String name;
    private String code;
    private Integer state;

    public Predicate toPredicate() {
        QRole role = QRole.role;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(name)) {
            builder.and(role.name.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(code)) {
            builder.and(role.code.containsIgnoreCase(code));
        }
        if (state != null) {
            builder.and(role.state.eq(state));
        }
        return builder;
    }
}
