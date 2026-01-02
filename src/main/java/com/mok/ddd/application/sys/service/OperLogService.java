package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.log.OperLogDTO;
import com.mok.ddd.domain.sys.model.OperLog;
import com.mok.ddd.domain.sys.model.QOperLog;
import com.mok.ddd.domain.sys.repository.OperLogRepository;
import com.mok.ddd.infrastructure.util.QuerydslUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
public class OperLogService {

    private final OperLogRepository operLogRepository;

    @Transactional(readOnly = true)
    public Page<@NonNull OperLogDTO> findPage(Predicate predicate, Pageable pageable) {
        QOperLog operLog = QOperLog.operLog;

        // 1. 转换排序，用于 QueryDSL 查询
        Pageable qSortPageable = QuerydslUtils.convertToQSortPageable(pageable, OperLog.class, operLog.getMetadata().getName());

        JPAQuery<OperLogDTO> query = operLogRepository.getJPAQueryFactory()
                .select(Projections.bean(OperLogDTO.class,
                        operLog.id,
                        operLog.title,
                        operLog.businessType,
                        operLog.method,
                        operLog.requestMethod,
                        operLog.operName,
                        operLog.operUrl,
                        operLog.operIp,
                        operLog.operParam,
                        operLog.jsonResult,
                        operLog.status,
                        operLog.errorMsg,
                        operLog.costTime,
                        operLog.createTime,
                        operLog.createBy
                ))
                .from(operLog)
                .where(predicate);

        // 2. 对数据查询应用租户过滤
        operLogRepository.applyTenantFilter(query, operLog);
        JPQLQuery<OperLogDTO> paginatedQuery = operLogRepository.getQuerydsl().applyPagination(qSortPageable, query);

        List<OperLogDTO> content = paginatedQuery.fetch();

        // 3. 对总数查询也应用租户过滤
        JPAQuery<Long> countQuery = operLogRepository.getJPAQueryFactory()
                .select(operLog.count())
                .from(operLog)
                .where(predicate);
        operLogRepository.applyTenantFilter(countQuery, operLog);
        long total = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);

        // 4. 参照 LoginLogService，创建干净的 Pageable 对象用于返回
        Pageable cleanPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        return new PageImpl<>(content, cleanPageable, total);
    }
}
