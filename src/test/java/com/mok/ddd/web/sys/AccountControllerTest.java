package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.auth.AccountInfoDTO;
import com.mok.ddd.application.sys.dto.auth.SelfPasswordUpdateDTO;
import com.mok.ddd.application.sys.dto.user.UserDTO;
import com.mok.ddd.application.sys.dto.user.UserPasswordDTO;
import com.mok.ddd.application.sys.service.UserService;
import com.mok.ddd.infrastructure.config.RateLimitProperties;
import com.mok.ddd.infrastructure.limiter.RateLimiterAspect;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.web.common.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
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

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RateLimitProperties rateLimitProperties;

    private MockedStatic<TenantContextHolder> mockedTenantContext;

    @BeforeEach
    void setUp() {
        mockedTenantContext = mockStatic(TenantContextHolder.class);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any())).thenReturn(1L);
    }

    @AfterEach
    void tearDown() {
        mockedTenantContext.close();
    }

    @Test
    @DisplayName("getMyInfo - 场景一：默认情况，不启用限流")
    void getMyInfo_whenRateLimitIsDisabled_shouldSucceed() throws Exception {
        rateLimitProperties.setEnabled(false);
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        when(userService.findAccountInfoByUsername("admin")).thenReturn(accountInfoDTO);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn("admin");

        mockMvc.perform(get("/api/account/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getMyInfo - 场景二：启用限流，且未超过限制")
    void getMyInfo_whenRateLimitedAndWithinLimit_shouldSucceed() throws Exception {
        rateLimitProperties.setEnabled(true);
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        when(userService.findAccountInfoByUsername("admin")).thenReturn(accountInfoDTO);
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any())).thenReturn(15L);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn("admin");

        mockMvc.perform(get("/api/account/info"))
                .andExpect(status().isOk());
    }

    @RepeatedTest(value = 31, name = "第 {currentRepetition}/{totalRepetitions} 次调用")
    @DisplayName("getMyInfo - 场景三：启用限流，并在最后一次调用时超过限制")
    void getMyInfo_whenRateLimitedAndExceedsLimit_shouldFail(RepetitionInfo repetitionInfo) throws Exception {
        rateLimitProperties.setEnabled(true);
        AccountInfoDTO accountInfoDTO = new AccountInfoDTO();
        when(userService.findAccountInfoByUsername("admin")).thenReturn(accountInfoDTO);
        long currentRepetition = repetitionInfo.getCurrentRepetition();
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), any(), any())).thenReturn(currentRepetition);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn("admin");

        if (currentRepetition <= 30) {
            mockMvc.perform(get("/api/account/info"))
                    .andExpect(status().isOk());
        } else {
            mockMvc.perform(get("/api/account/info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("系统繁忙，请稍后重试"));
        }
    }

    @Test
    @DisplayName("changeMyPassword - 成功")
    void changeMyPassword_Success() throws Exception {
        SelfPasswordUpdateDTO dto = new SelfPasswordUpdateDTO();
        dto.setOldPassword("oldPass123");
        dto.setNewPassword("newPass123");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        when(userService.findByUsername("admin")).thenReturn(userDTO);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn("admin");

        mockMvc.perform(put("/api/account/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).updatePassword(any(UserPasswordDTO.class));
    }

    @Test
    @DisplayName("updateNickname - 成功")
    void updateNickname_Success() throws Exception {
        AccountController.NicknameUpdateDTO dto = new AccountController.NicknameUpdateDTO();
        dto.setNickname("New Nick");
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        when(userService.findByUsername("admin")).thenReturn(userDTO);
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn("admin");

        mockMvc.perform(put("/api/account/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(userService).updateNickname(1L, "New Nick");
    }

    @Test
    @DisplayName("updateNickname - 校验失败")
    void updateNickname_ValidationFail() throws Exception {
        AccountController.NicknameUpdateDTO dto = new AccountController.NicknameUpdateDTO();
        dto.setNickname("");
        mockedTenantContext.when(TenantContextHolder::getUsername).thenReturn("admin");

        mockMvc.perform(put("/api/account/nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));
    }
}