package com.mok.ddd.infrastructure.repository;

import com.mok.ddd.infrastructure.security.TenantContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.orm.jpa.JpaTransactionManager;

/**
 * 不要加@Component 会自动成为一个Bean导致 TransactionManager Bean重复
 */
@Slf4j
public class CustomJpaTransactionManager extends JpaTransactionManager {

    public CustomJpaTransactionManager(EntityManagerFactory emf) {
        super(emf);
    }

    @Override
    @Nonnull
    protected EntityManager createEntityManagerForTransaction() {
        EntityManager em = super.createEntityManagerForTransaction();

        String tenantId = TenantContextHolder.getTenantId();
        log.debug("注入tenantIdc查询条件 {}", tenantId);
        em.unwrap(Session.class).enableFilter("tenantFilter")
                .setParameter("tenantId", tenantId);
        return em;
    }

}
