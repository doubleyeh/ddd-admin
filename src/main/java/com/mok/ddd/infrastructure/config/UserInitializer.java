package com.mok.ddd.infrastructure.config;

import com.mok.ddd.domain.sys.model.User;
import com.mok.ddd.domain.sys.repository.UserRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("debug")
public class UserInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ResourceLoader resourceLoader;
    private final JdbcTemplate jdbcTemplate;

    public UserInitializer(PasswordEncoder passwordEncoder, UserRepository userRepository,
                           ResourceLoader resourceLoader, JdbcTemplate jdbcTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.resourceLoader = resourceLoader;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {

            executeSqlScript("classpath:init.sql");

            String defaultTenantId = "000000";
            String rootUsername = "root";
            String rawPassword = "123456";

            ScopedValue.where(TenantContextHolder.TENANT_ID, defaultTenantId)
                    .run(() -> {
                        if (userRepository.findByUsername(rootUsername).isEmpty()) {
                            User rootUser = new User();
                            rootUser.setUsername(rootUsername);
                            rootUser.setPassword(passwordEncoder.encode(rawPassword));
                            rootUser.setTenantId(defaultTenantId);
                            rootUser.setNickname(rootUsername);
                            rootUser.setState(1);

                            userRepository.save(rootUser);
                        }
                    });
        };
    }

    private void executeSqlScript(String resourcePath) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(resourceLoader.getResource(resourcePath));
        populator.setSeparator(";");
        assert jdbcTemplate.getDataSource() != null;
        populator.execute(jdbcTemplate.getDataSource());
    }
}