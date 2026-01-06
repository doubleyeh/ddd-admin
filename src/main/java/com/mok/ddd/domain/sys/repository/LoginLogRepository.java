package com.mok.ddd.domain.sys.repository;

import com.mok.ddd.domain.sys.model.LoginLog;
import com.mok.ddd.infrastructure.repository.CustomRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginLogRepository extends CustomRepository<LoginLog, Long> {
}
