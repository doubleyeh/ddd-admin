package com.mok.ddd.infrastructure.config;

import com.mok.ddd.common.Const;
import com.mok.ddd.domain.sys.model.User;
import com.mok.ddd.domain.sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            String encodedPassword = passwordEncoder.encode("123456");
            User rootUser = User.create(Const.SUPER_ADMIN_USERNAME, encodedPassword, "超级管理员", true);
            rootUser.assignTenant(Const.DEFAULT_TENANT_ID);
            userRepository.save(rootUser);
        }
    }
}
