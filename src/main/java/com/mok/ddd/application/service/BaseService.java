package com.mok.ddd.application.service;

import com.mok.ddd.domain.entity.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BaseService<E extends BaseEntity, ID, DTO, Query> {

    DTO getById(ID id);

    List<DTO> findAll();

    Page<DTO> findPage(Query query, Pageable pageable);

    DTO save(DTO dto);

    DTO update(DTO dto);

    void deleteById(ID id);
}