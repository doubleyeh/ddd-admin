package com.mok.ddd.web.rest;

import com.mok.ddd.application.dto.tenant.TenantCreateResultDTO;
import com.mok.ddd.application.dto.tenant.TenantDTO;
import com.mok.ddd.application.dto.tenant.TenantOptionDTO;
import com.mok.ddd.application.dto.tenant.TenantSaveDTO;
import com.mok.ddd.application.service.TenantService;
import com.mok.ddd.infrastructure.security.CustomUserDetailsService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import jakarta.persistence.EntityManagerFactory;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Order;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(value = {TenantController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class TenantControllerTest {

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
    private TenantService tenantService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @Order(1)
    @WithMockUser(authorities = "tenant:list")
    void getById_ReturnTenantDTO_WhenFound() throws Exception {
        TenantDTO tenant = new TenantDTO();
        tenant.setId(1L);
        tenant.setName("测试租户");
        given(tenantService.getById(1L)).willReturn(tenant);

        mockMvc.perform(get("/api/tenants/1")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("测试租户"));
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = "tenant:list")
    void findPage_ReturnTenantDTOPage() throws Exception {
        TenantDTO tenant = new TenantDTO();
        tenant.setId(1L);
        tenant.setName("分页测试");
        Page<@NonNull TenantDTO> page = new PageImpl<>(Collections.singletonList(tenant));

        given(tenantService.findPage(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/tenants")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("分页测试"));
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "tenant:create")
    void save_ReturnCreateResult_WhenSuccessful() throws Exception {
        TenantSaveDTO saveDTO = new TenantSaveDTO();
        saveDTO.setName("新租户");
        saveDTO.setTenantId("T100");

        TenantCreateResultDTO result = new TenantCreateResultDTO();
        result.setId(2L);
        result.setInitialAdminPassword("pwd123");

        given(tenantService.createTenant(any(TenantSaveDTO.class))).willReturn(result);

        mockMvc.perform(post("/api/tenants")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.initialAdminPassword").value("pwd123"));
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "tenant:update")
    void update_ReturnUpdatedDTO() throws Exception {
        Long id = 1L;
        TenantSaveDTO saveDTO = new TenantSaveDTO();
        saveDTO.setName("更新名");

        TenantDTO updated = new TenantDTO();
        updated.setId(id);
        updated.setName("更新名");

        given(tenantService.updateTenant(eq(id), any(TenantSaveDTO.class))).willReturn(updated);

        mockMvc.perform(put("/api/tenants/{id}", id)
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("更新名"));
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "tenant:update")
    void updateState_ReturnUpdatedDTO() throws Exception {
        Long id = 1L;
        TenantDTO updated = new TenantDTO();
        updated.setId(id);
        updated.setEnabled(false);

        given(tenantService.updateTenantState(id, false)).willReturn(updated);

        mockMvc.perform(put("/api/tenants/{id}/state", id)
                        .param("state", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "tenant:delete")
    void deleteById_ReturnSuccess() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete("/api/tenants/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tenantService).deleteByVerify(eq(id));
    }

    @Test
    @Order(7)
    @WithMockUser
    void getOptions_ReturnList() throws Exception {
        TenantOptionDTO option = new TenantOptionDTO();
        option.setName("选项租户");

        given(tenantService.findOptions("test")).willReturn(List.of(option));

        mockMvc.perform(get("/api/tenants/options")
                        .param("name", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("选项租户"));
    }
}