package com.mok.ddd.web.sys;

import com.mok.ddd.application.exception.BizException;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageGrantDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageOptionDTO;
import com.mok.ddd.application.sys.dto.tenantPackage.TenantPackageSaveDTO;
import com.mok.ddd.application.sys.service.TenantPackageService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import com.mok.ddd.web.common.GlobalExceptionHandler;
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
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(value = {TenantPackageController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class TenantPackageControllerTest {

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
    private TenantPackageService packageService;

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
    @WithMockUser(authorities = "tenantPackage:list")
    void getById_ReturnDTO_WhenFound() throws Exception {
        Long id = 1L;
        TenantPackageDTO dto = new TenantPackageDTO();
        dto.setId(id);
        dto.setName("基础版");
        given(packageService.getById(id)).willReturn(dto);

        mockMvc.perform(get("/api/tenant-packages/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("基础版"));
    }

    @Test
    @Order(2)
    @WithMockUser(authorities = "tenantPackage:list")
    void findPage_ReturnPage() throws Exception {
        Page<@NonNull TenantPackageDTO> page = new PageImpl<>(Collections.emptyList());
        given(packageService.findPage(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/tenant-packages")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @WithMockUser(authorities = "tenantPackage:create")
    void create_ReturnSuccess() throws Exception {
        TenantPackageSaveDTO saveDTO = new TenantPackageSaveDTO();
        saveDTO.setName("标准版");

        mockMvc.perform(post("/api/tenant-packages")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @WithMockUser(authorities = "tenantPackage:update")
    void update_ReturnSuccess() throws Exception {
        Long id = 1L;
        TenantPackageSaveDTO saveDTO = new TenantPackageSaveDTO();
        saveDTO.setName("修改版");

        mockMvc.perform(put("/api/tenant-packages/{id}", id)
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @WithMockUser(authorities = "tenantPackage:update")
    void update_ReturnError_WhenNotFound() throws Exception {
        Long id = 99L;
        doThrow(new BizException("套餐不存在")).when(packageService).updatePackage(eq(id), any());

        mockMvc.perform(put("/api/tenant-packages/{id}", id)
                        .content(jsonMapper.writeValueAsString(new TenantPackageSaveDTO()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("套餐不存在"));
    }

    @Test
    @Order(6)
    @WithMockUser(authorities = "tenantPackage:update")
    void updateState_ReturnUpdatedDTO() throws Exception {
        Long id = 1L;
        TenantPackageDTO updated = new TenantPackageDTO();
        updated.setState(1);
        given(packageService.updateTenantState(id, 1)).willReturn(updated);

        mockMvc.perform(put("/api/tenant-packages/{id}/state", id).param("state", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value(1));
    }

    @Test
    @Order(7)
    @WithMockUser(authorities = "tenantPackage:delete")
    void deleteById_ReturnSuccess() throws Exception {
        Long id = 1L;
        mockMvc.perform(delete("/api/tenant-packages/{id}", id))
                .andExpect(status().isOk());
        verify(packageService).deleteByVerify(eq(id));
    }

    @Test
    @Order(8)
    @WithMockUser(authorities = "tenantPackage:delete")
    void deleteById_ReturnError_WhenInUse() throws Exception {
        Long id = 1L;
        doThrow(new BizException("套餐正在使用中，不允许删除")).when(packageService).deleteByVerify(id);

        mockMvc.perform(delete("/api/tenant-packages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("套餐正在使用中，不允许删除"));
    }

    @Test
    @Order(9)
    @WithMockUser(authorities = "tenantPackage:update")
    void grant_ReturnSuccess() throws Exception {
        Long id = 1L;
        TenantPackageGrantDTO grantDTO = new TenantPackageGrantDTO();
        grantDTO.setMenuIds(Set.of(1L));

        mockMvc.perform(put("/api/tenant-packages/{id}/grant", id)
                        .content(jsonMapper.writeValueAsString(grantDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(packageService).grant(eq(id), any(TenantPackageGrantDTO.class));
    }

    @Test
    @Order(10)
    @WithMockUser
    void getOptions_ReturnList() throws Exception {
        TenantPackageOptionDTO option = new TenantPackageOptionDTO();
        option.setName("Option 1");
        given(packageService.findOptions("test")).willReturn(List.of(option));

        mockMvc.perform(get("/api/tenant-packages/options")
                        .param("name", "test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Option 1"));
    }
}
