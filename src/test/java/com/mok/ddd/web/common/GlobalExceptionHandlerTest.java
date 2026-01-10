package com.mok.ddd.web.common;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test-exceptions")
    public static class TestController {
        @GetMapping("/username-not-found")
        public void usernameNotFound() {
            throw new UsernameNotFoundException("User not found");
        }

        @GetMapping("/bad-credentials")
        public void badCredentials() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/internal-auth")
        public void internalAuth() {
            throw new InternalAuthenticationServiceException("Internal auth error", new BadCredentialsException("Bad credentials"));
        }

        @GetMapping("/authorization-denied")
        public void authorizationDenied() {
            throw new AuthorizationDeniedException("Forbidden", new AuthorizationDecision(false));
        }

        @PostMapping("/validation")
        public void validation(@Valid @RequestBody ValidatedDto dto) {
        }

        @GetMapping("/access-denied")
        public void accessDenied() {
            throw new AccessDeniedException("Access denied");
        }

        @GetMapping("/biz-exception")
        public void bizException() {
            throw new BizException("Business error");
        }

        @GetMapping("/not-found")
        public void notFound() {
            throw new NotFoundException("Not found");
        }

        @GetMapping("/general-exception")
        public void generalException() {
            throw new RuntimeException("Something went wrong");
        }
    }

    @Data
    private static class ValidatedDto {
        @NotEmpty(message = "name cannot be empty")
        private String name;
    }

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void handleAuthenticationException_UsernameNotFound() throws Exception {
        mockMvc.perform(get("/test-exceptions/username-not-found"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void handleAuthenticationException_BadCredentials() throws Exception {
        mockMvc.perform(get("/test-exceptions/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    void handleInternalAuthException() throws Exception {
        mockMvc.perform(get("/test-exceptions/internal-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    void handleAuthorizationDeniedException() throws Exception {
        mockMvc.perform(get("/test-exceptions/authorization-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("您没有权限执行该操作"));
    }

    @Test
    void handleValidationExceptions() throws Exception {
        mockMvc.perform(post("/test-exceptions/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("name cannot be empty"));
    }

    @Test
    void handleAccessDenied() throws Exception {
        mockMvc.perform(get("/test-exceptions/access-denied"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("权限不足，拒绝访问"));
    }

    @Test
    void handleBizException() throws Exception {
        mockMvc.perform(get("/test-exceptions/biz-exception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Business error"));
    }

    @Test
    void handleNotFoundException() throws Exception {
        mockMvc.perform(get("/test-exceptions/not-found"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Not found"));
    }

    @Test
    void handleGeneralException() throws Exception {
        mockMvc.perform(get("/test-exceptions/general-exception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("系统繁忙，请稍后重试"));
    }
}