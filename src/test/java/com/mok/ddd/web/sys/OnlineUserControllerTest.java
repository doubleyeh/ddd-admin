package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.sys.service.TenantService;
import com.mok.ddd.infrastructure.common.aspect.OperLogAspect;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.security.OnlineUserDTO;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import com.mok.ddd.infrastructure.tenant.TenantContextHolder;
import com.mok.ddd.web.common.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OnlineUserController.class)
@Import({GlobalExceptionHandler.class, OnlineUserController.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OnlineUserController 测试")
class OnlineUserControllerTest {

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
    private JwtTokenProvider tokenProvider;

    @MockitoBean
    private TenantService tenantService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private OperLogAspect operLogAspect;

    private MockedStatic<TenantContextHolder> mockedTenantContext;

    @BeforeEach
    void setUp() {
        mockedTenantContext = mockStatic(TenantContextHolder.class);
    }

    @AfterEach
    void tearDown() {
        mockedTenantContext.close();
    }

    @Test
    @WithMockUser(authorities = "admin:online-user")
    @DisplayName("list - 成功获取在线用户列表")
    void list_Success() throws Exception {
        TenantOptionDTO option = new TenantOptionDTO();
        option.setTenantId("T1");
        option.setName("Tenant 1");

        OnlineUserDTO userDTO = new OnlineUserDTO(
                1L,
                "user1",
                "T1",
                "Tenant 1",
                Collections.emptyList()
        );

        given(tenantService.findOptions(null)).willReturn(List.of(option));
        mockedTenantContext.when(TenantContextHolder::getTenantId).thenReturn("T1");
        mockedTenantContext.when(TenantContextHolder::isSuperTenant).thenReturn(false);
        given(tokenProvider.getAllOnlineUsers(anyMap(), eq("T1"), eq(false))).willReturn(List.of(userDTO));

        mockMvc.perform(get("/api/online-user/list")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("user1"));
    }

    @Test
    @WithMockUser(authorities = "admin:online-user:kickout")
    @DisplayName("kickout - 成功强退用户")
    void kickout_Success() throws Exception {
        String token = "some-token";
        Map<String, String> body = Map.of("token", token);
        String content = "{\"token\": \"some-token\"}";

        mockMvc.perform(post("/api/online-user/kickout")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tokenProvider).removeToken(token);
    }

    @Test
    @WithMockUser(authorities = "admin:online-user:kickout")
    @DisplayName("kickout - 无Token时请求成功但不执行删除")
    void kickout_NoToken_Success() throws Exception {
        String content = "{}";

        mockMvc.perform(post("/api/online-user/kickout")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tokenProvider, never()).removeToken(anyString());
    }
}