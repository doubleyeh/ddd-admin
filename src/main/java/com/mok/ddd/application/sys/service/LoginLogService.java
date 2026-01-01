package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.log.LoginLogDTO;
import com.mok.ddd.domain.sys.model.LoginLog;
import com.mok.ddd.domain.sys.model.QLoginLog;
import com.mok.ddd.domain.sys.model.QTenant;
import com.mok.ddd.domain.sys.repository.LoginLogRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Transactional
    public void createLoginLog(LoginLog loginLog) {
        loginLogRepository.save(loginLog);
    }

    @Transactional(readOnly = true)
    public Page<LoginLogDTO> findPage(Predicate predicate, Pageable pageable) {
        QLoginLog loginLog = QLoginLog.loginLog;
        QTenant tenant = QTenant.tenant;

        JPAQuery<LoginLogDTO> query = loginLogRepository.getJPAQueryFactory()
                .select(Projections.bean(LoginLogDTO.class,
                        loginLog.id,
                        loginLog.username,
                        loginLog.ipAddress,
                        loginLog.status,
                        loginLog.message,
                        loginLog.tenantId,
                        tenant.name.as("tenantName"),
                        loginLog.createTime
                ))
                .from(loginLog)
                .leftJoin(tenant).on(loginLog.tenantId.eq(tenant.tenantId))
                .where(predicate);

        loginLogRepository.applyTenantFilter(query, loginLog);
        JPQLQuery<LoginLogDTO> paginatedQuery = loginLogRepository.getQuerydsl().applyPagination(pageable, query);

        return PageableExecutionUtils.getPage(paginatedQuery.fetch(), pageable, () -> {
            JPAQuery<Long> countQuery = loginLogRepository.getJPAQueryFactory()
                    .select(loginLog.count())
                    .from(loginLog)
                    .leftJoin(tenant).on(loginLog.tenantId.eq(tenant.tenantId))
                    .where(predicate);
            return Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        });
    }
}
