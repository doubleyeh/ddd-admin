package com.mok.ddd.application.sys.service;

import com.mok.ddd.application.sys.dto.log.LoginLogDTO;
import com.mok.ddd.application.sys.dto.log.LoginLogQuery;
import com.mok.ddd.domain.sys.repository.LoginLogRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginLogService 单元测试")
class LoginLogServiceTest {

    @InjectMocks
    private LoginLogService loginLogService;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Nested
    @DisplayName("findPage 分页查询测试")
    class FindPageTests {
        @Test
        @DisplayName("分页查询登录日志成功")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void findPage_Success() {
            // Arrange
            LoginLogQuery query = new LoginLogQuery();
            query.setUsername("root");
            com.querydsl.core.types.Predicate predicate = query.toPredicate();
            org.springframework.data.domain.Pageable pageable = mock(org.springframework.data.domain.Pageable.class);

            com.querydsl.jpa.impl.JPAQueryFactory queryFactory = mock(com.querydsl.jpa.impl.JPAQueryFactory.class);
            com.querydsl.jpa.impl.JPAQuery jpaQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            com.querydsl.jpa.impl.JPAQuery countQuery = mock(com.querydsl.jpa.impl.JPAQuery.class);
            com.querydsl.jpa.JPQLQuery jpqlQuery = mock(com.querydsl.jpa.JPQLQuery.class);
            org.springframework.data.jpa.repository.support.Querydsl querydsl = mock(org.springframework.data.jpa.repository.support.Querydsl.class);

            when(loginLogRepository.getJPAQueryFactory()).thenReturn(queryFactory);
            when(loginLogRepository.getQuerydsl()).thenReturn(querydsl);

            when(queryFactory.select(any(com.querydsl.core.types.Expression.class)))
                    .thenReturn(jpaQuery) // For the main query
                    .thenReturn(countQuery); // For the count query

            when(jpaQuery.from(any(com.querydsl.core.types.EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.leftJoin(any(com.querydsl.core.types.EntityPath.class))).thenReturn(jpaQuery);
            when(jpaQuery.on(any(com.querydsl.core.types.Predicate.class))).thenReturn(jpaQuery);
            when(jpaQuery.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(jpaQuery);

            when(querydsl.applyPagination(eq(pageable), any())).thenReturn(jpqlQuery);
            when(jpqlQuery.fetch()).thenReturn(List.of(new LoginLogDTO()));

            when(countQuery.from(any(com.querydsl.core.types.EntityPath.class))).thenReturn(countQuery);
            when(countQuery.leftJoin(any(com.querydsl.core.types.EntityPath.class))).thenReturn(countQuery);
            when(countQuery.on(any(com.querydsl.core.types.Predicate.class))).thenReturn(countQuery);
            when(countQuery.where(any(com.querydsl.core.types.Predicate.class))).thenReturn(countQuery);
            when(countQuery.fetchOne()).thenReturn(1L);

            // Act
            org.springframework.data.domain.Page<@NonNull LoginLogDTO> result = loginLogService.findPage(predicate, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }
    }
}
