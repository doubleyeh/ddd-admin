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
}
