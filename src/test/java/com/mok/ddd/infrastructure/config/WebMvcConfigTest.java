package com.mok.ddd.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebMvcConfigTest {

    @Mock
    private TraceIdInterceptor traceIdInterceptor;

    @Mock
    private InterceptorRegistry registry;

    @InjectMocks
    private WebMvcConfig webMvcConfig;

    @Test
    void addInterceptors() {
        webMvcConfig.addInterceptors(registry);
        ArgumentCaptor<TraceIdInterceptor> captor = ArgumentCaptor.forClass(TraceIdInterceptor.class);
        verify(registry).addInterceptor(captor.capture());
        assertEquals(traceIdInterceptor, captor.getValue());
    }
}