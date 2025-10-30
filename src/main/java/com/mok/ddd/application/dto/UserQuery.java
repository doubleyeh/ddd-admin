package com.mok.ddd.application.dto;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static com.mok.ddd.domain.entity.QUser.user;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQuery extends BaseQuery{
    private String username;
    private String nickname;
    private Integer state;

    public Predicate toPredicate() {
        UserQuery query = this;
        BooleanBuilder builder = new BooleanBuilder();
        if (query.getUsername() != null && !query.getUsername().isEmpty()) {
            builder.and(user.username.containsIgnoreCase(query.getUsername()));
        }

        if (query.getNickname() != null && !query.getNickname().isEmpty()) {
            builder.and(user.nickname.containsIgnoreCase(query.getNickname()));
        }

        if (query.getState() != null) {
            builder.and(user.state.eq(query.getState()));
        }

        return builder;
    }
}