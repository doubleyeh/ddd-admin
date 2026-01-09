package com.mok.ddd.common;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SysUtil 工具类测试")
class SysUtilTest {

    @Test
    @DisplayName("isSuperAdmin - 是超级管理员")
    void isSuperAdmin_True() {
        assertTrue(SysUtil.isSuperAdmin(Const.DEFAULT_TENANT_ID, Const.SUPER_ADMIN_USERNAME));
    }

    @Test
    @DisplayName("isSuperAdmin - 不是超级管理员")
    void isSuperAdmin_False() {
        assertFalse(SysUtil.isSuperAdmin("other", Const.SUPER_ADMIN_USERNAME));
        assertFalse(SysUtil.isSuperAdmin(Const.DEFAULT_TENANT_ID, "other"));
        assertFalse(SysUtil.isSuperAdmin("other", "other"));
    }

    @Test
    @DisplayName("isSuperTenant - 是超级租户")
    void isSuperTenant_True() {
        assertTrue(SysUtil.isSuperTenant(Const.DEFAULT_TENANT_ID));
    }

    @Test
    @DisplayName("isSuperTenant - 不是超级租户")
    void isSuperTenant_False() {
        assertFalse(SysUtil.isSuperTenant("other"));
    }

    @Test
    @DisplayName("getIpAddress - X-Forwarded-For")
    void getIpAddress_XForwardedFor() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("1.1.1.1, 2.2.2.2");
        assertEquals("1.1.1.1", SysUtil.getIpAddress(request));
    }

    @Test
    @DisplayName("getIpAddress - Proxy-Client-IP")
    void getIpAddress_ProxyClientIP() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn("2.2.2.2");
        assertEquals("2.2.2.2", SysUtil.getIpAddress(request));
    }

    @Test
    @DisplayName("getIpAddress - WL-Proxy-Client-IP")
    void getIpAddress_WLProxyClientIP() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("3.3.3.3");
        assertEquals("3.3.3.3", SysUtil.getIpAddress(request));
    }

    @Test
    @DisplayName("getIpAddress - RemoteAddr")
    void getIpAddress_RemoteAddr() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("");
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("4.4.4.4");
        assertEquals("4.4.4.4", SysUtil.getIpAddress(request));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "'' | Unknown",
            "' ' | Unknown",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 | Windows - Chrome",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 | macOS - Chrome",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 | Linux - Chrome",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1 | iOS - Safari",
            "Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36 | Android - Chrome",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0 | Windows - Firefox",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59 | Windows - Edge",
            "PostmanRuntime/7.26.8 | Unknown OS (Postman)",
            "curl/7.64.1 | Unknown OS (cURL)",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36 MicroMessenger/6.5.2.501 NetType/WIFI WindowsWechat QBCore/3.43.27.400 QQBrowser/9.0.2524.400 | Windows - WeChat"
    })
    @DisplayName("getBrowser - 解析UserAgent")
    void getBrowser(String userAgent, String expected) {
        assertEquals(expected, SysUtil.getBrowser(userAgent));
    }
}
