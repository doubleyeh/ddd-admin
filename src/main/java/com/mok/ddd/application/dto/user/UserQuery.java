package com.mok.ddd.application.dto.user;

import com.mok.ddd.application.dto.BaseQuery;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mok.ddd.domain.entity.QUser.user;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQuery extends BaseQuery {
    private String username;
    private String nickname;
    private Integer state;
    private String tenantId;

    private Long roleId;

    @Override
    public Predicate toPredicate() {
        UserQuery query = this;
        BooleanBuilder builder = new BooleanBuilder();
        if (query.getTenantId() != null && !query.getTenantId().isEmpty()) {
            builder.and(user.tenantId.eq(query.getTenantId()));
        }
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            builder.and(user.username.containsIgnoreCase(query.getUsername()));
        }

        if (query.getNickname() != null && !query.getNickname().isEmpty()) {
            builder.and(user.nickname.containsIgnoreCase(query.getNickname()));
        }

        if (query.getState() != null) {
            builder.and(user.state.eq(query.getState()));
        }

        if (query.getRoleId() != null) {
            builder.and(user.roles.any().id.eq(query.getRoleId()));
        }

        return builder;
    }
}