package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.log.LoginLogDTO;
import com.mok.ddd.application.sys.service.LoginLogService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import com.mok.ddd.web.common.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(value = {LoginLogController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class LoginLogControllerTest {

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
    private LoginLogService loginLogService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "root", authorities = "log:login:list")
    void findPage_ShouldReturnLogPage_WhenAuthorized() throws Exception {
        LoginLogDTO logDTO = new LoginLogDTO();
        logDTO.setId(1L);
        logDTO.setUsername("testuser");
        logDTO.setStatus("SUCCESS");
        logDTO.setIpAddress("127.0.0.1");
        logDTO.setCreatedTime(LocalDateTime.now());

        Page<LoginLogDTO> logPage = new PageImpl<>(Collections.singletonList(logDTO));

        given(loginLogService.findPage(any(), any(Pageable.class))).willReturn(logPage);

        mockMvc.perform(get("/api/login-logs")
                        .param("username", "test")
                        .param("status", "SUCCESS")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
