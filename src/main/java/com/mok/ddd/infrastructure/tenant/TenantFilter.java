package com.mok.ddd.infrastructure.tenant;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantFilter {
    TenantFilterPolicy value() default TenantFilterPolicy.DEFAULT;

    static enum TenantFilterPolicy {
        DEFAULT, // 使用系统默认策略
        FORCE,   // 强制启用
        SKIP     // 强制跳过
    }
}