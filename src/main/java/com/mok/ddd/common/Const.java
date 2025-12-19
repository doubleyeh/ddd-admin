package com.mok.ddd.common;

public interface Const {
    String DEFAULT_TENANT_ID = "000000";
    String SUPER_ADMIN_USERNAME = "root";

    String NOT_FOUND_MESSAGE = "数据不存在";

    String DEFAULT_ADMIN_USERNAME = "admin";

    String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    class CacheKey {
        public static final String MENU_TREE = "sys:menu:tree";
        public static final String ROLE_PERMS = "sys:role:perms";

        public static final String AUTH_TOKEN = "auth:token:";
        public static final String USER_TOKENS = "user:tokens:";
    }
}
