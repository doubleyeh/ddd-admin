package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class TraceIdInterceptorTest {

    private final TraceIdInterceptor interceptor = new TraceIdInterceptor();
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void preHandle_withTraceIdHeader() {
        String traceId = "test-trace-id";
        request.addHeader("X-Trace-ID", traceId);
        interceptor.preHandle(request, response, new Object());
        assertEquals(traceId, MDC.get("traceId"));
    }

    @Test
    void preHandle_withoutTraceIdHeader() {
        interceptor.preHandle(request, response, new Object());
        assertNotNull(MDC.get("traceId"));
        assertFalse(MDC.get("traceId").isEmpty());
    }

    @Test
    void preHandle_withEmptyTraceIdHeader() {
        request.addHeader("X-Trace-ID", "");
        interceptor.preHandle(request, response, new Object());
        assertNotNull(MDC.get("traceId"));
        assertFalse(MDC.get("traceId").isEmpty());
    }

    @Test
    void afterCompletion() {
        MDC.put("traceId", "test-trace-id");
        interceptor.afterCompletion(request, response, new Object(), null);
        assertNull(MDC.get("traceId"));
    }
}