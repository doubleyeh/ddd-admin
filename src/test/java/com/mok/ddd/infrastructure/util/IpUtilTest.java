package com.mok.ddd.infrastructure.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpUtilTest {

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getIpAddr_fromXForwardedFor() {
        request.addHeader("x-forwarded-for", "1.1.1.1");
        assertEquals("1.1.1.1", IpUtil.getIpAddr());
    }

    @Test
    void getIpAddr_fromProxyClientIP() {
        request.addHeader("Proxy-Client-IP", "2.2.2.2");
        assertEquals("2.2.2.2", IpUtil.getIpAddr());
    }

    @Test
    void getIpAddr_fromWLProxyClientIP() {
        request.addHeader("WL-Proxy-Client-IP", "3.3.3.3");
        assertEquals("3.3.3.3", IpUtil.getIpAddr());
    }

    @Test
    void getIpAddr_fromRemoteAddr() {
        request.setRemoteAddr("4.4.4.4");
        assertEquals("4.4.4.4", IpUtil.getIpAddr());
    }

    @Test
    void getIpAddr_withMultipleHeaders() {
        request.addHeader("x-forwarded-for", "1.1.1.1");
        request.addHeader("Proxy-Client-IP", "2.2.2.2");
        assertEquals("1.1.1.1", IpUtil.getIpAddr());
    }

    @Test
    void getIpAddr_withUnknownHeader() {
        request.addHeader("x-forwarded-for", "unknown");
        request.setRemoteAddr("5.5.5.5");
        assertEquals("5.5.5.5", IpUtil.getIpAddr());
    }
}