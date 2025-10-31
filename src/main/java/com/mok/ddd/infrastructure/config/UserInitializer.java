package com.mok.ddd.infrastructure.config;

import com.mok.ddd.domain.entity.User;
import com.mok.ddd.domain.repository.UserRepository;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserInitializer {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserInitializer(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {

            String defaultTenantId = "000000";
            String rootUsername = "root";
            String rawPassword = "123456";

            TenantContextHolder.setTenantId(defaultTenantId);

            if (userRepository.findByUsername(rootUsername).isEmpty()) {
                User rootUser = new User();
                rootUser.setUsername(rootUsername);
                rootUser.setPassword(passwordEncoder.encode(rawPassword));
                rootUser.setTenantId(defaultTenantId);
                rootUser.setNickname(rootUsername);
                rootUser.setState(1);

                userRepository.save(rootUser);
            }

            TenantContextHolder.clear();
        };
    }
}