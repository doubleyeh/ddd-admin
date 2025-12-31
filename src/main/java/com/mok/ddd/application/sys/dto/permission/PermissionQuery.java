package com.mok.ddd.application.sys.dto.permission;

import com.mok.ddd.application.common.dto.BaseQuery;
import com.mok.ddd.domain.sys.model.QPermission;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionQuery extends BaseQuery {

    private Long menuId;

    @Override
    public Predicate toPredicate() {
        BooleanBuilder builder = new BooleanBuilder();

        if (menuId != null) {
            builder.and(QPermission.permission.menu.id.eq(menuId));
        }

        return builder;
    }
}