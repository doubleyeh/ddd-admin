package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.log.OperLogDTO;
import com.mok.ddd.application.sys.dto.log.OperLogQuery;
import com.mok.ddd.domain.sys.repository.OperLogRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperLogService 单元测试")
class OperLogServiceTest {

    @InjectMocks
    private OperLogService operLogService;

    @Mock
    private OperLogRepository operLogRepository;

    @Nested
    @DisplayName("findPage 分页查询测试")
    class FindPageTests {
        @Test
        @DisplayName("分页查询操作日志成功")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void findPage_Success() {
            OperLogQuery query = new OperLogQuery();
            query.setOperName("root");
            com.querydsl.core.types.Predicate predicate = query.toPredicate();
            Pageable pageable = mock(Pageable.class);
            when(pageable.getPageNumber()).thenReturn(0);
            when(pageable.getPageSize()).thenReturn(10);
            when(pageable.getSort()).thenReturn(org.springframework.data.domain.Sort.unsorted());

            com.querydsl.jpa.impl.JPAQueryFactory queryFactory = mock(com.querydsl.jpa.impl.JPAQueryFactory.class);
            com.querydsl.jpa.impl.JPAQuery jpaQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            com.querydsl.jpa.impl.JPAQuery countQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            com.querydsl.jpa.JPQLQuery jpqlQuery = mock(com.querydsl.jpa.JPQLQuery.class);
            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);

            when(operLogRepository.getJPAQueryFactory()).thenReturn(queryFactory);
            when(operLogRepository.getQuerydsl()).thenReturn(querydsl);

            when(queryFactory.select(any(com.querydsl.core.types.Expression.class)))
                    .thenReturn(jpaQuery)
                    .thenReturn(countQuery);

            when(jpaQuery.from(any(com.querydsl.core.types.EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(jpaQuery);

            when(countQuery.from(any(com.querydsl.core.types.EntityPath.class))).thenReturn(countQuery);
            when(countQuery.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(1L);

            when(querydsl.applyPagination(any(), any())).thenReturn(jpqlQuery);
            when(jpqlQuery.fetch()).thenReturn(List.of(new OperLogDTO()));

            org.springframework.data.domain.Page<@NonNull OperLogDTO> result = operLogService.findPage(predicate, pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }
}
