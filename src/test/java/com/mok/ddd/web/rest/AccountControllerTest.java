package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.service.UserService;
import com.mok.ddd.infrastructure.config.RateLimitProperties;
import com.mok.ddd.infrastructure.limiter.RateLimiterAspect;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.mok.ddd.infrastructure.tenant.TenantContextHolder.TENANT_ID;
import static com.mok.ddd.infrastructure.tenant.TenantContextHolder.USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@Import({AccountController.class, RateLimiterAspect.class, RateLimitProperties.class, GlobalExceptionHandler.class})
@DisplayName("AccountController 测试")
@EnableAspectJAutoProxy(proxyTargetClass = true)
class AccountControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        EntityManagerFactory entityManagerFactory() {
            return mock(EntityManagerFactory.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    @Test
    @DisplayName("getMyInfo - 场景一：默认情况，不启用限流")
    void getMyInfo_whenRateLimitIsDisabled_shouldSucceed() {
        rateLimitProperties.setEnabled(false); // 禁用限流
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        when(userService.findAccountInfoByUsername("admin")).thenReturn(accountInfoDTO);

        ScopedValue.where(TENANT_ID, "000000")
                .where(USERNAME, "admin")
                .run(() -> {
                    try {
                        mockMvc.perform(get("/api/account/info"))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Test
    @DisplayName("getMyInfo - 场景二：启用限流，且未超过限制")
    void getMyInfo_whenRateLimitedAndWithinLimit_shouldSucceed() {
        rateLimitProperties.setEnabled(true); // 启用限流
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        when(userService.findAccountInfoByUsername("admin")).thenReturn(accountInfoDTO);
        // 模拟在限制次数内（例如，第15次访问）
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any())).thenReturn(15L);

        ScopedValue.where(TENANT_ID, "000000")
                .where(USERNAME, "admin")
                .run(() -> {
                    try {
                        mockMvc.perform(get("/api/account/info"))
                                .andExpect(status().isOk());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @RepeatedTest(value = 31, name = "第 {currentRepetition}/{totalRepetitions} 次调用")
    @DisplayName("getMyInfo - 场景三：启用限流，并在最后一次调用时超过限制")
    void getMyInfo_whenRateLimitedAndExceedsLimit_shouldFail(RepetitionInfo repetitionInfo) {
        rateLimitProperties.setEnabled(true); // 启用限流
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        when(userService.findAccountInfoByUsername("admin")).thenReturn(accountInfoDTO);

        long currentRepetition = repetitionInfo.getCurrentRepetition();
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any())).thenReturn(currentRepetition);

        ScopedValue.where(TENANT_ID, "000000")
                .where(USERNAME, "admin")
                .run(() -> {
                    try {
                        if (currentRepetition <= 30) {
                            // 在限制次数内，应该成功
                            mockMvc.perform(get("/api/account/info"))
                                    .andExpect(status().isOk());
                        } else {
                            // 第31次，超过限制
                            mockMvc.perform(get("/api/account/info"))
                                    .andExpect(status().isOk())
                                    .andExpect(jsonPath("$.code").value(500))
                                    .andExpect(jsonPath("$.message").value("系统繁忙，请稍后重试"));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
