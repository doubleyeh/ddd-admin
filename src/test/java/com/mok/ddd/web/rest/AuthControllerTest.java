package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.auth.LoginRequest;
import com.mok.ddd.infrastructure.security.CustomUserDetailsService;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@WebMvcTest(controllers = AuthController.class)
@Import(AuthControllerTest.TestConfig.class)
@AutoConfigureMockMvc
class AuthControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        EntityManagerFactory entityManagerFactory() {
            return mock(EntityManagerFactory.class);
        }
    }

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("john");
        req.setPassword("password");
        req.setTenantId("tenantA");

        Authentication auth = new UsernamePasswordAuthenticationToken("john", "password");

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(auth);

        given(jwtTokenProvider.createToken("john", "tenantA"))
                .willReturn("fake-jwt-token");

        assertThat(this.mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(req)))
                .hasStatusOk()
                .hasBodyTextEqualTo("john");

        assertThat(this.mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(req)))
                .hasStatusOk()
                .hasBodyTextEqualTo("tenantA");

        assertThat(this.mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(req)))
                .hasStatusOk()
                .hasBodyTextEqualTo("fake-jwt-token");
    }
}