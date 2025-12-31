package com.mok.ddd.infrastructure.limiter;

public enum LimitType {
    /**
     * 默认
     */
    DEFAULT,
    /**
     * 根据IP地址限流
     */
    IP;
}
