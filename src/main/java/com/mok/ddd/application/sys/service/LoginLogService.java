package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.log.LoginLogDTO;
import com.mok.ddd.domain.sys.model.LoginLog;
import com.mok.ddd.domain.sys.model.QLoginLog;
import com.mok.ddd.domain.sys.model.QTenant;
import com.mok.ddd.domain.sys.repository.LoginLogRepository;
import com.mok.ddd.infrastructure.util.QuerydslUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        Pageable qSortPageable = QuerydslUtils.convertToQSortPageable(pageable, LoginLog.class, loginLog.getMetadata().getName());

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
        JPQLQuery<LoginLogDTO> paginatedQuery = loginLogRepository.getQuerydsl().applyPagination(qSortPageable, query);

        List<LoginLogDTO> content = paginatedQuery.fetch();

        long total = Optional.ofNullable(loginLogRepository.getJPAQueryFactory()
                .select(tenant.count())
                .from(tenant)
                .where(predicate)
                .fetchOne()).orElse(0L);

        Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return new PageImpl<>(content, cleanPageable, total);
    }
}
