package com.mok.ddd.infrastructure.util;

import com.querydsl.core.types.Order;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QSort;

import static org.junit.jupiter.api.Assertions.*;

class QuerydslUtilsTest {

    private static class TestEntity {}

    @Test
    void convertToQSortPageable_withUnsortedPageable() {
        Pageable unsortedPageable = PageRequest.of(0, 10);
        Pageable result = QuerydslUtils.convertToQSortPageable(unsortedPageable, TestEntity.class, "testEntity");
        assertSame(unsortedPageable, result);
    }

    @Test
    void convertToQSortPageable_withSortedPageable() {
        Sort sort = Sort.by(Sort.Direction.DESC, "name");
        Pageable sortedPageable = PageRequest.of(0, 10, sort);
        Pageable result = QuerydslUtils.convertToQSortPageable(sortedPageable, TestEntity.class, "testEntity");

        assertNotSame(sortedPageable, result);
        Sort resultSort = result.getSort();
        assertInstanceOf(QSort.class, resultSort);

        Sort.Order order = resultSort.getOrderFor("name");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void convertToQSortPageable_withNullEntityClass() {
        Sort sort = Sort.by("name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Pageable result = QuerydslUtils.convertToQSortPageable(pageable, null, "alias");
        assertSame(pageable, result);
    }

    @Test
    void convertToQSortPageable_withEmptyAlias() {
        Sort sort = Sort.by("name");
        Pageable pageable = PageRequest.of(0, 10, sort);
        Pageable result = QuerydslUtils.convertToQSortPageable(pageable, TestEntity.class, "");
        assertSame(pageable, result);
    }
}