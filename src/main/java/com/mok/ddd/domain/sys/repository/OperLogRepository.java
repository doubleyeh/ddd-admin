package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.OperLog;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperLogRepository extends CustomRepository<OperLog, Long> {
}
