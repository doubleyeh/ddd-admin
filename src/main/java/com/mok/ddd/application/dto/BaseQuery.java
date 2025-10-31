package com.mok.ddd.application.dto;

import com.querydsl.core.types.Predicate;
import lombok.Data;

import java.io.Serializable;

@Data
public abstract class BaseQuery implements Serializable {

    public abstract Predicate toPredicate();
}
