package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.auth.LoginRequest;
import com.mok.ddd.infrastructure.security.CustomUserDetail;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest2 {

    private MockMvc mockMvc;

    private final JsonMapper jsonMapper = new JsonMapper();

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("john");
        req.setPassword("password");
        req.setTenantId("tenantA");

        CustomUserDetail userDetail = new CustomUserDetail(
                1L, "john", "password", "tenantA", Collections.emptySet(), false
        );
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetail, "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        given(jwtTokenProvider.createToken(
                eq("john"),
                eq("tenantA"),
                any(CustomUserDetail.class),
                anyString(),
                anyString()
        )).willReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .header("User-Agent", "Mozilla/5.0...")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"))
                .andExpect(jsonPath("$.data.tenantId").value("tenantA"))
                .andExpect(jsonPath("$.data.token").value("fake-jwt-token"));
    }
}