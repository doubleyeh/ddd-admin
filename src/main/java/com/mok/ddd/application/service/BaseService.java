package com.mok.ddd.application.service;

import com.mok.ddd.domain.entity.BaseEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BaseService<E extends BaseEntity, ID, DTO, Query> {

    DTO getById(ID id);

    List<DTO> findAll();

    List<DTO> findAll(Predicate predicate);

    Page<DTO> findPage(Predicate predicate, Pageable pageable);

    DTO save(DTO dto);

    DTO update(DTO dto);

    void deleteById(ID id);
}