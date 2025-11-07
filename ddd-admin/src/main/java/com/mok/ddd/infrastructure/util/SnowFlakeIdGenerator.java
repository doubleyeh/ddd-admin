package com.mok.ddd.infrastructure.util;

import org.springframework.stereotype.Component;

@Component
public class SnowFlakeIdGenerator {

    private static final long START_STAMP = 1480166465631L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    private static SnowFlakeIdGenerator INSTANCE;

    public SnowFlakeIdGenerator() {
        this.workerId = 1;
        this.datacenterId = 1;
        INSTANCE = this;
    }

    public static SnowFlakeIdGenerator getInstance() {
        return INSTANCE;
    }

    public static synchronized Long nextId() {
        long timestamp = timeGen();
        SnowFlakeIdGenerator generator = getInstance();

        if (timestamp < generator.lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", generator.lastTimestamp - timestamp));
        }

        if (generator.lastTimestamp == timestamp) {
            generator.sequence = (generator.sequence + 1) & ((~(-1L << SEQUENCE_BITS)));
            if (generator.sequence == 0) {
                timestamp = tilNextMillis(generator.lastTimestamp);
            }
        } else {
            generator.sequence = 0L;
        }

        generator.lastTimestamp = timestamp;

        return ((timestamp - START_STAMP) << TIMESTAMP_LEFT_SHIFT) | (generator.datacenterId << DATACENTER_ID_SHIFT)
                | (generator.workerId << WORKER_ID_SHIFT) | generator.sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }
}