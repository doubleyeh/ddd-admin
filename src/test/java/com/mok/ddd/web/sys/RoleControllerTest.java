package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.role.*;
import com.mok.ddd.application.sys.service.RoleService;
import com.mok.ddd.infrastructure.security.JwtAuthenticationFilter;
import com.mok.ddd.infrastructure.security.JwtTokenProvider;
import com.mok.ddd.infrastructure.sys.security.CustomUserDetailsService;
import com.mok.ddd.web.common.GlobalExceptionHandler;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(value = {RoleController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("角色控制器测试")
class RoleControllerTest {

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
    private RoleService roleService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(authorities = "role:list")
    @DisplayName("分页查询角色")
    void findPage_ShouldReturnPage() throws Exception {
        RoleDTO dto = new RoleDTO();
        dto.setId(1L);
        dto.setName("Test Role");
        Page<RoleDTO> page = new PageImpl<>(Collections.singletonList(dto));

        given(roleService.findPage(any(), any(Pageable.class))).willReturn(page);

        mockMvc.perform(get("/api/roles")
                        .param("name", "Test")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(authorities = "role:list")
    @DisplayName("根据ID查询角色")
    void getById_ShouldReturnDto() throws Exception {
        RoleDTO dto = new RoleDTO();
        dto.setId(1L);
        given(roleService.getById(1L)).willReturn(dto);

        mockMvc.perform(get("/api/roles/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(authorities = "role:create")
    @DisplayName("创建角色")
    void save_ShouldReturnSavedDto() throws Exception {
        RoleSaveDTO saveDTO = new RoleSaveDTO();
        saveDTO.setName("New Role");
        saveDTO.setCode("new_role");

        RoleDTO resultDTO = new RoleDTO();
        resultDTO.setId(2L);
        given(roleService.createRole(any(RoleSaveDTO.class))).willReturn(resultDTO);

        mockMvc.perform(post("/api/roles")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(2L));
    }

    @Test
    @WithMockUser(authorities = "role:update")
    @DisplayName("更新角色")
    void update_ShouldReturnUpdatedDto() throws Exception {
        RoleSaveDTO saveDTO = new RoleSaveDTO();
        saveDTO.setName("Updated Role");
        saveDTO.setCode("updated_code");

        RoleDTO resultDTO = new RoleDTO();
        resultDTO.setId(1L);
        given(roleService.updateRole(any(RoleSaveDTO.class))).willReturn(resultDTO);

        mockMvc.perform(put("/api/roles/1")
                        .content(jsonMapper.writeValueAsString(saveDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @WithMockUser(authorities = "role:update")
    @DisplayName("更新角色状态")
    void updateState_ShouldReturnUpdatedDto() throws Exception {
        RoleDTO resultDTO = new RoleDTO();
        resultDTO.setId(1L);
        resultDTO.setState(0);
        given(roleService.updateState(1L, 0)).willReturn(resultDTO);

        mockMvc.perform(put("/api/roles/1/state")
                        .param("state", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value(0));
    }

    @Test
    @WithMockUser(authorities = "role:delete")
    @DisplayName("删除角色")
    void deleteById_ShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/roles/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(roleService).deleteRoleBeforeValidation(eq(1L));
    }

    @Test
    @WithMockUser(authorities = "role:update")
    @DisplayName("为角色授权")
    void grant_ShouldReturnSuccess() throws Exception {
        RoleGrantDTO grantDTO = new RoleGrantDTO();
        grantDTO.setMenuIds(Collections.singleton(1L));

        mockMvc.perform(post("/api/roles/1/grant")
                        .content(jsonMapper.writeValueAsString(grantDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(roleService).grant(eq(1L), any(RoleGrantDTO.class));
    }

    @Test
    @WithMockUser
    @DisplayName("获取角色选项列表")
    void getRoleOptions_ShouldReturnList() throws Exception {
        RoleOptionDTO optionDTO = new RoleOptionDTO();
        optionDTO.setId(1L);
        optionDTO.setName("Role Option");
        List<RoleOptionDTO> options = Collections.singletonList(optionDTO);

        given(roleService.getRoleOptions(any(RoleQuery.class))).willReturn(options);

        mockMvc.perform(get("/api/roles/options")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1L));
    }
}
