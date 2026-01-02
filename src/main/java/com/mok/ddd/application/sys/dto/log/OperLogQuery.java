package com.mok.ddd.application.sys.dto.log;

import com.mok.ddd.domain.sys.model.QOperLog;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class OperLogQuery {

    private String title;
    private String operName;
    private Integer status;

    public Predicate toPredicate() {
        QOperLog operLog = QOperLog.operLog;
        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(title)) {
            builder.and(operLog.title.containsIgnoreCase(title));
        }
        if (StringUtils.hasText(operName)) {
            builder.and(operLog.operName.containsIgnoreCase(operName));
        }
        if (status != null) {
            builder.and(operLog.status.eq(status));
        }
        return builder;
    }
}
