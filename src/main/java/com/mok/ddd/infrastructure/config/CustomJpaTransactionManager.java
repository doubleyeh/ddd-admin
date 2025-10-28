package com.mok.ddd.infrastructure.config;

import com.mok.ddd.infrastructure.security.TenantContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.Session;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;

@Component
public class CustomJpaTransactionManager extends JpaTransactionManager {

    public CustomJpaTransactionManager(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    @Nonnull
    protected EntityManager createEntityManagerForTransaction() {
        EntityManager em = super.createEntityManagerForTransaction();
        em.unwrap(Session.class).enableFilter("tenantFilter")
                .setParameter("tenantId", TenantContextHolder.getTenantId());
        return em;
    }

}
