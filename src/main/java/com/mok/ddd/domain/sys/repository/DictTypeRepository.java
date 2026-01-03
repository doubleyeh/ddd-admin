package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.DictType;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DictTypeRepository extends CustomRepository<DictType, Long> {
    Optional<DictType> findByCode(String code);
    boolean existsByCode(String code);
}
