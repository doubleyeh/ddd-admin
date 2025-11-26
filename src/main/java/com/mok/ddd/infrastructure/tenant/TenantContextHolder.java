package com.mok.ddd.infrastructure.tenant;

import com.mok.ddd.common.SysUtil;

public class TenantContextHolder {


    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static String getTenantId() {
        return TENANT_ID.get();
    }

    public static void setUsername(String username) {
        USERNAME.set(username);
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        USERNAME.remove();
    }

    public static boolean isSuperAdmin() {
        String tenantId = getTenantId();
        String username = getUsername();
        return SysUtil.isSuperAdmin(tenantId, username);
    }
}