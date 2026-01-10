package com.mok.ddd.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SnowFlakeIdGeneratorTest {

    @BeforeEach
    void setUp() {
        new SnowFlakeIdGenerator();
    }

    @Test
    void nextId_isUnique() {
        Long id1 = SnowFlakeIdGenerator.nextId();
        Long id2 = SnowFlakeIdGenerator.nextId();
        assertNotEquals(id1, id2);
    }

    @Test
    void nextId_generatesManyUniqueIds() {
        int count = 10000;
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < count; i++) {
            ids.add(SnowFlakeIdGenerator.nextId());
        }
        assertEquals(count, ids.size());
    }

    @Test
    void nextId_isThreadSafe() throws InterruptedException {
        int numThreads = 10;
        int idsPerThread = 1000;
        int totalIds = numThreads * idsPerThread;
        Set<Long> ids = Collections.synchronizedSet(new HashSet<>());
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < idsPerThread; j++) {
                    ids.add(SnowFlakeIdGenerator.nextId());
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(totalIds, ids.size());
    }
}