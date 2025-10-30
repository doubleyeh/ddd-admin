package com.mok.ddd.application.service;

import com.mok.ddd.domain.entity.BaseEntity;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public abstract class BaseServiceImpl<E extends BaseEntity, ID, DTO, Query> implements BaseService<E, ID, DTO, Query> {

    protected abstract CustomRepository<E, ID> getRepository();

    protected abstract E toEntity(DTO dto);
    protected abstract DTO toDto(E entity);

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
    public Page<DTO> findPage(Predicate predicate, Pageable pageable) {
        Page<E> entityPage = getRepository().findAll(predicate, pageable);

        List<DTO> dtoList = entityPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, entityPage.getPageable(), entityPage.getTotalElements());
    }

    @Override
    @Transactional
    public DTO save(DTO dto) {
        E entity = toEntity(dto);
        return toDto(getRepository().save(entity));
    }

    @Override
    @Transactional
    public DTO update(DTO dto) {
        E entity = toEntity(dto);
        return toDto(getRepository().save(entity));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }
}