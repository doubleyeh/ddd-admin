package com.mok.ddd.infrastructure.config;

import com.mok.ddd.domain.sys.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserInitializer userInitializer;

    @Test
    void run_whenRepositoryIsEmpty_createsUser() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");

        userInitializer.run();

        verify(userRepository).save(any());
    }

    @Test
    void run_whenRepositoryIsNotEmpty_doesNotCreateUser() throws Exception {
        when(userRepository.count()).thenReturn(1L);

        userInitializer.run();

        verify(userRepository, never()).save(any());
    }
}