package com.mok.ddd.common;

import jakarta.servlet.http.HttpServletRequest;

import static com.mok.ddd.common.Const.DEFAULT_TENANT_ID;
import static com.mok.ddd.common.Const.SUPER_ADMIN_USERNAME;

public final class SysUtil {

    public static boolean isSuperAdmin(String tenantId, String username) {
        return DEFAULT_TENANT_ID.equals(tenantId) && SUPER_ADMIN_USERNAME.equals(username);
    }

    public static boolean isSuperTenant(String tenantId) {
        return DEFAULT_TENANT_ID.equals(tenantId);
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null && ip.contains(",") ? ip.split(",")[0] : ip;
    }

    public static String getBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) return "Unknown";
        String ua = userAgent.toUpperCase();

        String os = "Unknown OS";
        if (ua.contains("WINDOWS")) os = "Windows";
        else if (ua.contains("ANDROID")) os = "Android";
        else if (ua.contains("IPHONE") || ua.contains("IPAD")) os = "iOS";
        else if (ua.contains("MACINTOSH") || ua.contains("MAC OS X")) os = "macOS";
        else if (ua.contains("LINUX")) os = "Linux";

        String browser = "Other";
        if (ua.contains("POSTMAN")) return os + " (Postman)";
        if (ua.contains("CURL")) return os + " (cURL)";
        if (ua.contains("MICROMESSENGER")) browser = "WeChat";
        else if (ua.contains("EDG/")) browser = "Edge";
        else if (ua.contains("CHROME/")) browser = "Chrome";
        else if (ua.contains("FIREFOX/")) browser = "Firefox";
        else if (ua.contains("SAFARI/") && !ua.contains("CHROME")) browser = "Safari";

        return os + " - " + browser;
    }
}
