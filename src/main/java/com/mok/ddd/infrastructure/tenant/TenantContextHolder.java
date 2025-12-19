package com.mok.ddd.infrastructure.tenant;

import com.mok.ddd.common.SysUtil;

public class TenantContextHolder {


    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();
    public static final ScopedValue<String> USERNAME = ScopedValue.newInstance();
    public static final ScopedValue<Long> USER_ID = ScopedValue.newInstance();

    public static String getTenantId() {
        return TENANT_ID.isBound() ? TENANT_ID.get() : "";
    }

    public static String getUsername() {
        return USERNAME.isBound() ? USERNAME.get() : "";
    }

    public static Long getUserId() {
        return USER_ID.isBound() ? USER_ID.get() : null;
    }

    public static boolean isSuperAdmin() {
        String tenantId = getTenantId();
        String username = getUsername();
        return SysUtil.isSuperAdmin(tenantId, username);
    }

    public static boolean isSuperTenant() {
        String tenantId = getTenantId();
        return SysUtil.isSuperTenant(tenantId);
    }
}