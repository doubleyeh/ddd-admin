package com.mok.ddd.infrastructure.util;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QSort;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

public final class QuerydslUtils {

    private QuerydslUtils() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Pageable convertToQSortPageable(Pageable pageable, Class<?> entityClass, String alias) {
        if (pageable.getSort().isUnsorted() || Objects.isNull(entityClass) || !StringUtils.hasLength(alias)) {
            return pageable;
        }

        PathBuilder<?> builder = new PathBuilder<>(entityClass, alias);
        List<OrderSpecifier> specifiers = pageable.getSort().stream()
                .map(order -> new OrderSpecifier(
                        order.isAscending() ? Order.ASC : Order.DESC,
                        builder.get(order.getProperty())
                )).toList();

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), new QSort(specifiers.toArray(new OrderSpecifier[0])));
    }
}
