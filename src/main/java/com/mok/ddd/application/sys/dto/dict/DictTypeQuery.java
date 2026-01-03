package com.mok.ddd.application.sys.dto.dict;

import com.mok.ddd.domain.sys.model.QDictType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class DictTypeQuery {
    private String name;
    private String code;

    public Predicate toPredicate() {
        QDictType dictType = QDictType.dictType;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(name)) {
            builder.and(dictType.name.containsIgnoreCase(name));
        }
        if (StringUtils.hasText(code)) {
            builder.and(dictType.code.containsIgnoreCase(code));
        }
        return builder;
    }
}
