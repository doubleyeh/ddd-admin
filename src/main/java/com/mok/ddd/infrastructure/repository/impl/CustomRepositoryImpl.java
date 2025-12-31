package com.mok.ddd.infrastructure.repository.impl;

import com.mok.ddd.common.Const;
import com.mok.ddd.domain.common.model.BaseEntity;
import com.mok.ddd.domain.common.model.TenantBaseEntity;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.data.support.PageableExecutionUtils;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@SuppressWarnings({"NullableProblems"})
public class CustomRepositoryImpl<T extends BaseEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements CustomRepository<T, ID> {

    private final JPAQueryFactory dslQueryFactory;
    private final Querydsl querydsl;
    private final PathBuilder<T> pathBuilder;

    protected final static String TENANT_VARIABLE = "tenantId";

    public CustomRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.dslQueryFactory = new JPAQueryFactory(entityManager);
        this.pathBuilder = new PathBuilder<>(entityInformation.getJavaType(), entityInformation.getEntityName());
        this.querydsl = new Querydsl(entityManager, pathBuilder);
    }

    // --- 核心 QueryDSL 构建和多租户过滤 ---
    protected <U extends T> JPAQuery<U> createQuery(EntityPath<U> entityPath, Predicate... predicates) {
        JPAQuery<U> query = dslQueryFactory.selectFrom(entityPath);

        if (TenantBaseEntity.class.isAssignableFrom(getDomainClass())) {
            try {
                StringPath tenantPath = pathBuilder.getString(TENANT_VARIABLE);
                String currentTenantId = TenantContextHolder.getTenantId();
                BooleanExpression tenantFilter;

                if (currentTenantId == null
                        || currentTenantId.isEmpty()
                        || Const.DEFAULT_TENANT_ID.equals(currentTenantId.trim())
                        || TenantContextHolder.isSuperAdmin()) {

                    tenantFilter = Expressions.TRUE.isTrue();
                } else {
                    tenantFilter = tenantPath.eq(currentTenantId);
                }

                query.where(tenantFilter);
            } catch (Exception e) {
                log.error("Error applying tenant filter using QueryDSL Path API", e);
            }
        }

        if (predicates.length > 0) {
            query.where(predicates);
        }
        return query;
    }

    @Override
    public <U> JPAQuery<U> applyTenantFilter(JPAQuery<U> query, EntityPath<?>... paths) {
        if (TenantContextHolder.isSuperAdmin()) {
            return query;
        }

        String currentTenantId = TenantContextHolder.getTenantId();
        if (currentTenantId == null || currentTenantId.isEmpty()) {
            return query;
        }

        for (EntityPath<?> path : paths) {
            if (TenantBaseEntity.class.isAssignableFrom(path.getType())) {
                StringPath tp = Expressions.stringPath(path, "tenantId");
                query.where(tp.eq(currentTenantId));
            }
        }
        return query;
    }

    @Override
    public JPAQueryFactory getJPAQueryFactory() {
        return dslQueryFactory;
    }

    @Override
    public Querydsl getQuerydsl(){
        return querydsl;
    }

    // --- 接口方法实现 ---

    @Override
    public Optional<T> findOne(Predicate predicate) {
        JPAQuery<T> query = createQuery(pathBuilder, predicate);
        return Optional.ofNullable(query.fetchOne());
    }

    @Override
    public Iterable<T> findAll(Predicate predicate) {
        JPAQuery<T> query = createQuery(pathBuilder, predicate);
        return query.fetch();
    }

    @Override
    public Iterable<T> findAll(Predicate predicate, Sort sort) {
        JPAQuery<T> query = createQuery(pathBuilder, predicate);
        return querydsl.applySorting(sort, query).fetch();
    }

    @Override
    public Iterable<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        JPAQuery<T> query = createQuery(pathBuilder, predicate);
        return query.orderBy(orders).fetch();
    }

    @Override
    public Iterable<T> findAll(OrderSpecifier<?>... orders) {
        JPAQuery<T> query = createQuery(pathBuilder);
        return query.orderBy(orders).fetch();
    }

    @Override
    public Page<T> findAll(Predicate predicate, Pageable pageable) {
        JPAQuery<T> query = createQuery(pathBuilder, predicate);
        JPQLQuery<T> paginatedQuery = querydsl.applyPagination(pageable, query);

        return PageableExecutionUtils.getPage(paginatedQuery.fetch(), pageable, () -> {
            JPAQuery<Long> countQuery = createQuery(pathBuilder, predicate).select(pathBuilder.count());
            return Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        });
    }

    @Override
    public long count(Predicate predicate) {
        JPAQuery<Long> countQuery = createQuery(pathBuilder, predicate).select(pathBuilder.count());
        return Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
    }

    @Override
    public boolean exists(Predicate predicate) {
        JPAQuery<T> query = createQuery(pathBuilder, predicate);
        return query.fetchFirst() != null;
    }

    @Override
    public <S extends T, R> R findBy(Predicate predicate, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery API is not fully implemented in CustomRepositoryImpl.");
    }

    // getPath 方法保持原样
    protected Path<?> getPath(Expression<?> expression) {
        if (expression instanceof EntityPath<?> entityPath) {
            return entityPath;
        } else if (expression instanceof Path<?> propertyPath) {
            return propertyPath.getRoot();
        }
        throw new UnsupportedOperationException("expression require EntityPath(Table) or StringPath(field)");
    }
}