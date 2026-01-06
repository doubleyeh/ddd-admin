package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.TenantBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "sys_login_log")
public class LoginLog extends TenantBaseEntity {

    private String username;

    private String ipAddress;

    private String status;

    private String message;

    public static LoginLog create(String username, String ipAddress, String status, String message) {
        LoginLog log = new LoginLog();
        log.username = username;
        log.ipAddress = ipAddress;
        log.status = status;
        log.message = message;
        return log;
    }

    public void assignTenant(String tenantId) {
        if (this.getTenantId() == null) {
            this.setTenantId(tenantId);
        }
    }
}
