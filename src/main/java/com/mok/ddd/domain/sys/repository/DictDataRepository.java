package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.DictData;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataRepository extends CustomRepository<DictData, Long> {
    List<DictData> findByTypeCodeOrderBySortAsc(String typeCode);
    void deleteByTypeCode(String typeCode);
}
