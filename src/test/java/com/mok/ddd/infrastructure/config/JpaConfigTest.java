package com.mok.ddd.infrastructure.config;

import com.mok.ddd.infrastructure.repository.CustomJpaTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class JpaConfigTest {

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @InjectMocks
    private JpaConfig jpaConfig;

    @Test
    void jpaTransactionManager() {
        PlatformTransactionManager transactionManager = jpaConfig.jpaTransactionManager(entityManagerFactory);
        assertNotNull(transactionManager);
        assertInstanceOf(CustomJpaTransactionManager.class, transactionManager);
    }
}