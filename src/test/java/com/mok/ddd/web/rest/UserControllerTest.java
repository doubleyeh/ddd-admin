package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.user.UserDTO;
import com.mok.ddd.application.service.UserService;
import com.mok.ddd.infrastructure.config.JacksonConfig;
import com.mok.ddd.infrastructure.security.CustomUserDetailsService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@WebMvcTest(controllers = UserController.class)
@Import(value = {UserControllerTest.TestConfig.class, UserController.class, JacksonConfig.class})
class UserControllerTest {

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
    private MockMvcTester mvc;

    @MockitoBean
    private JsonMapper jsonMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getById_ReturnUserDTO_WhenFound() throws Exception {
        UserDTO user = new UserDTO();
        user.setId(1L);
        user.setUsername("root");
        given(userService.getById(1L)).willReturn(user);


        assertThat(this.mvc.get().uri("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .hasStatusOk()
                .hasBodyTextEqualTo(jsonMapper.writeValueAsString(user));
    }
}