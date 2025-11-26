package com.mok.ddd.infrastructure.config;

import com.mok.ddd.infrastructure.repository.CustomJpaTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class JpaConfig {

    @Bean(name = "transactionManager")
    public PlatformTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new CustomJpaTransactionManager(entityManagerFactory);
    }
}