package com.mok.ddd.infrastructure.config;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfig {
    @Bean
    public AuditorAware<@NonNull String> auditorAware() {
        return () -> Optional.ofNullable(TenantContextHolder.getUsername());
    }
}