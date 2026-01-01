package com.mok.ddd.domain.sys.model;

import com.mok.ddd.domain.common.model.TenantBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_login_log")
public class LoginLog extends TenantBaseEntity {

    private String username;

    private String ipAddress;

    private String status;

    private String message;

}
