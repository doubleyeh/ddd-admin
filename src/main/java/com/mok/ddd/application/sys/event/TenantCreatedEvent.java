package com.mok.ddd.application.sys.event;

import com.mok.ddd.domain.sys.model.Tenant;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TenantCreatedEvent extends ApplicationEvent {

    private final Tenant tenant;
    private final String rawPassword;

    public TenantCreatedEvent(Object source, Tenant tenant, String rawPassword) {
        super(source);
        this.tenant = tenant;
        this.rawPassword = rawPassword;
    }
}
