package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.permission.PermissionDTO;
import com.mok.ddd.application.service.PermissionService;
import com.mok.ddd.infrastructure.security.CustomUserDetailsService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(value = {PermissionController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

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
    private PermissionService permissionService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void findByMenuId_ReturnPermissionList() throws Exception {
        PermissionDTO p1 = new PermissionDTO();
        p1.setId(1L);
        p1.setName("查询用户");
        p1.setCode("user:list");

        PermissionDTO p2 = new PermissionDTO();
        p2.setId(2L);
        p2.setName("新增用户");
        p2.setCode("user:create");

        given(permissionService.findAll(any())).willReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/permissions/menu/100")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].code").value("user:list"))
                .andExpect(jsonPath("$.data[1].code").value("user:create"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void save_ReturnSavedPermission() throws Exception {
        PermissionDTO dto = new PermissionDTO();
        dto.setName("测试权限");
        dto.setCode("test:perm");
        dto.setMenuId(100L);
        dto.setUrl("");
        dto.setMethod("*");

        given(permissionService.save(any(PermissionDTO.class))).willReturn(dto);

        mockMvc.perform(post("/api/permissions")
                        .content(jsonMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("test:perm"))
                .andExpect(jsonPath("$.data.method").value("*"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_ReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/permissions/1"))
                .andExpect(status().isOk());

        verify(permissionService).deleteById(1L);
    }
}