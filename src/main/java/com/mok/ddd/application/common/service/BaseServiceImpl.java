package com.mok.ddd.application.common.service;

import com.mok.ddd.domain.common.model.BaseEntity;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.mok.ddd.infrastructure.util.QuerydslUtils;
import com.querydsl.core.types.Predicate;
import org.jspecify.annotations.NonNull;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class BaseServiceImpl<E extends BaseEntity, ID, DTO> implements BaseService<E, ID, DTO> {
    protected final Class<E> entityClass;

    @SuppressWarnings("unchecked")
    protected BaseServiceImpl() {
        Class<?>[] clazzArr = GenericTypeResolver.resolveTypeArguments(getClass(), BaseServiceImpl.class);
        this.entityClass = Objects.nonNull(clazzArr) ? (Class<E>) clazzArr[0] : null;
    }
    protected abstract CustomRepository<E, ID> getRepository();

    protected abstract E toEntity(@NonNull DTO dto);
    protected abstract DTO toDto(@NonNull E entity);

    protected String getEntityAlias() {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public DTO getById(ID id) {
        Optional<E> entity = getRepository().findById(id);
        return entity.map(this::toDto).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DTO> findAll() {
        return getRepository().findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DTO> findAll(Predicate predicate) {
            return StreamSupport.stream(getRepository().findAll(predicate).spliterator(), false)
                    .map(this::toDto)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull DTO> findPage(Predicate predicate, Pageable pageable) {
        Pageable qSortPageable = convertToQSortPageable(pageable);
        Page<@NonNull E> entityPage = getRepository().findAll(predicate, qSortPageable);

        List<DTO> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    @Override
    @Transactional
    public DTO save(@NonNull DTO dto) {
        E entity = toEntity(dto);
        return toDto(getRepository().save(entity));
    }

    @Override
    @Transactional
    public DTO update(@NonNull DTO dto) {
        E entity = toEntity(dto);
        return toDto(getRepository().save(entity));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    @Override
    public Pageable convertToQSortPageable(Pageable pageable) {
        return QuerydslUtils.convertToQSortPageable(pageable, this.entityClass, this.getEntityAlias());
    }
}
