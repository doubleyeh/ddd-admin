package com.mok.ddd.web.sys;

import com.mok.ddd.application.sys.dto.menu.MenuDTO;
import com.mok.ddd.application.sys.service.MenuService;
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
@Import(value = {MenuController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class MenuControllerTest {

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
    private MenuService menuService;

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
    void getTree_ReturnComplexMenuTree() throws Exception {
        MenuDTO parent = new MenuDTO();
        parent.setId(1L);
        parent.setName("系统管理");

        MenuDTO child1 = new MenuDTO();
        child1.setId(11L);
        child1.setParentId(1L);
        child1.setName("用户管理");

        MenuDTO child2 = new MenuDTO();
        child2.setId(12L);
        child2.setParentId(1L);
        child2.setName("角色管理");

        parent.setChildren(List.of(child1, child2));
        List<MenuDTO> tree = List.of(parent);

        given(menuService.findAll()).willReturn(List.of(parent, child1, child2));
        given(menuService.buildMenuTree(any())).willReturn(tree);

        mockMvc.perform(get("/api/menus/tree").accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].children.length()").value(2))
                .andExpect(jsonPath("$.data[0].children[0].name").value("用户管理"))
                .andExpect(jsonPath("$.data[0].children[1].name").value("角色管理"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void save_ReturnSavedMenu() throws Exception {
        MenuDTO dto = new MenuDTO();
        dto.setName("新菜单");
        given(menuService.save(any(MenuDTO.class))).willReturn(dto);

        mockMvc.perform(post("/api/menus")
                        .content(jsonMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("新菜单"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void update_ReturnUpdatedMenu() throws Exception {
        Long id = 1L;
        MenuDTO dto = new MenuDTO();
        dto.setId(id);
        dto.setName("更新菜单");
        given(menuService.save(any(MenuDTO.class))).willReturn(dto);

        mockMvc.perform(put("/api/menus/{id}", id)
                        .content(jsonMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("更新菜单"));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void delete_ReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/menus/1"))
                .andExpect(status().isOk());
        verify(menuService).deleteById(1L);
    }
}