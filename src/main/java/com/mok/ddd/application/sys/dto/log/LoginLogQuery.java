package com.mok.ddd.application.sys.dto.log;

import com.mok.ddd.domain.sys.model.QLoginLog;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Data
public class LoginLogQuery {

    private String username;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Predicate toPredicate() {
        QLoginLog loginLog = QLoginLog.loginLog;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(username)) {
            builder.and(loginLog.username.containsIgnoreCase(username));
        }

        if (StringUtils.hasText(status)) {
            builder.and(loginLog.status.eq(status));
        }

        if (startTime != null) {
            builder.and(loginLog.createTime.goe(startTime));
        }

        if (endTime != null) {
            builder.and(loginLog.createTime.loe(endTime));
        }

        return builder;
    }
}
