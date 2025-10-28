package com.mok.ddd.infrastructure.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.mok.ddd.infrastructure.security.TenantContextHolder;

@Aspect
@Component
public class TenantFilterInjectorAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.mok.ddd.domain.repository.*.*(..))")
    @Transactional(readOnly = true)
    public Object setTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {

        if (!TenantContextHolder.isSuperAdmin()) {
            Session session = entityManager.unwrap(Session.class);
            String tenantId = TenantContextHolder.getTenantId();

            if (tenantId != null) {
                Filter filter = session.enableFilter("tenantFilter");
                filter.setParameter("tenantId", tenantId);
                try {
                    return joinPoint.proceed();
                } finally {
                    session.disableFilter("tenantFilter");
                }
            }
        }

        return joinPoint.proceed();
    }
}