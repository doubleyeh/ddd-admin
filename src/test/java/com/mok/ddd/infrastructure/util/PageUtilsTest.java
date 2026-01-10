package com.mok.ddd.infrastructure.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageUtilsTest {

    @Test
    void parseSavePage_withUnsortedPageable() {
        Pageable unsortedPageable = PageRequest.of(0, 10);
        Pageable result = PageUtils.parseSavePage(unsortedPageable);
        assertEquals(Sort.by("createTime").descending(), result.getSort());
    }

    @Test
    void parseSavePage_withSortedPageable() {
        Sort sort = Sort.by("name").ascending();
        Pageable sortedPageable = PageRequest.of(0, 10, sort);
        Pageable result = PageUtils.parseSavePage(sortedPageable);
        assertEquals(sort, result.getSort());
    }

    @Test
    void parseSavePage_withInvalidSortProperty() throws Exception {
        Sort.Order order = Sort.Order.by("validProperty");
        Field propertyField = Sort.Order.class.getDeclaredField("property");
        propertyField.setAccessible(true);
        propertyField.set(order, "");

        Sort invalidSort = Sort.by(Collections.singletonList(order));
        Pageable invalidPageable = PageRequest.of(0, 10, invalidSort);
        Pageable result = PageUtils.parseSavePage(invalidPageable);
        assertEquals(Sort.by("createTime").descending(), result.getSort());
    }
}