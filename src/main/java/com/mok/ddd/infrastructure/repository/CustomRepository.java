package com.mok.ddd.infrastructure.repository;

import com.mok.ddd.domain.common.model.BaseEntity;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.FluentQuery;

import java.util.Optional;
import java.util.function.Function;

@NoRepositoryBean
public interface CustomRepository<T extends BaseEntity, ID> extends JpaRepository<@NonNull T, @NonNull ID>, JpaSpecificationExecutor<@NonNull T>, QuerydslPredicateExecutor<@NonNull T> {
    <U> JPAQuery<U> applyTenantFilter(JPAQuery<U> query, EntityPath<?>... paths);

    JPAQueryFactory getJPAQueryFactory();

    Querydsl getQuerydsl();

    Optional<T> findOne(Predicate predicate);

    Iterable<T> findAll(Predicate predicate);

    Iterable<T> findAll(Predicate predicate, Sort sort);

    Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

    Iterable<T> findAll(OrderSpecifier<?>... orders);

    Page<T> findAll(Predicate predicate, Pageable pageable);

    long count(Predicate predicate);

    boolean exists(Predicate predicate);

    <S extends T, R> R findBy(Predicate predicate, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction);
}
