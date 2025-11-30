package com.mok.ddd.common;

import static com.mok.ddd.common.Const.DEFAULT_TENANT_ID;
import static com.mok.ddd.common.Const.SUPER_ADMIN_USERNAME;

public final class SysUtil {

    public static boolean isSuperAdmin(String tenantId, String username) {
        return DEFAULT_TENANT_ID.equals(tenantId) && SUPER_ADMIN_USERNAME.equals(username);
    }

    public static boolean isSuperTenant(String tenantId) {
        return DEFAULT_TENANT_ID.equals(tenantId);
    }
}
