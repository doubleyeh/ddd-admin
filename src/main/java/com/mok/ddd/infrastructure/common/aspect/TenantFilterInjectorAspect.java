package com.mok.ddd.infrastructure.common.aspect;

import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.infrastructure.tenant.TenantFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class TenantFilterInjectorAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* com.mok.ddd.domain..repository.*.*(..))")
    @Transactional(readOnly = true)
    public Object setTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();

        TenantFilter.TenantFilterPolicy policy =
                resolvePolicy(method, declaringClass);

        boolean superAdmin = TenantContextHolder.isSuperAdmin();
        Session session = entityManager.unwrap(Session.class);
        session.disableFilter("tenantFilter");

        try {
            boolean shouldEnable = switch (policy) {
                case FORCE -> true;
                case SKIP  -> false;
                default -> !superAdmin;
            };

            if (shouldEnable) {
                String tenantId = TenantContextHolder.getTenantId();
                if (tenantId != null) {
                    session.enableFilter("tenantFilter")
                            .setParameter("tenantId", tenantId);
                }
            }

            return joinPoint.proceed();

        } finally {
            session.disableFilter("tenantFilter");
        }
    }

    private TenantFilter.TenantFilterPolicy resolvePolicy(Method method, Class<?> clazz) {
        TenantFilter m = method.getAnnotation(TenantFilter.class);
        if (m != null) return m.value();

        TenantFilter c = clazz.getAnnotation(TenantFilter.class);
        if (c != null) return c.value();

        return TenantFilter.TenantFilterPolicy.DEFAULT;
    }
}