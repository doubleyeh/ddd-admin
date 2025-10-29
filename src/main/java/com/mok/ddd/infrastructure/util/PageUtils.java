package com.mok.ddd.infrastructure.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public final class PageUtils {

    private PageUtils() {}

    private static final Sort DEFAULT_SORT = Sort.by("createTime").descending();

    public static Pageable parseSavePage(Pageable pageable) {
        Sort currentSort = pageable.getSort();

        if (currentSort.isUnsorted()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }

        boolean hasInvalidProperty = false;
        for (Sort.Order order : currentSort) {
            if (!StringUtils.hasText(order.getProperty())) {
                hasInvalidProperty = true;
                break;
            }
        }

        if (hasInvalidProperty) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        }

        return pageable;
    }
}